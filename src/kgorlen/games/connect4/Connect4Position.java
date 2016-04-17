package kgorlen.games.connect4;

import java.util.logging.Logger;

import kgorlen.games.GamePosition;
import kgorlen.games.Log;
import kgorlen.games.Move;
import kgorlen.games.MoveGenerator;
import kgorlen.games.TTEntry;
import kgorlen.games.Variation;
import kgorlen.games.mcts.MonteCarloTreeNode;

/**
 * Represents a Connect Four GamePosition using a 2-element array of
 * long bitmaps.
 * 
 * Board square numbering:
 * 
 * 		1
 * 		2
 * 		3
 * 		4
 * 		5
 * 		6
 * 		 A B C D E F G
 * 
 * long[] board bit-to-board square mapping:
 * 
 * 	- one column per byte, row 6 = 0x1 through row 1 = 0x20
 *  - 0x40 and 0x80 bits of each byte are 0 "guard bits" to
 *    stop carries/shifts from changing adjacent bytes
 *  - A column in 7th byte through G column in lowest-order byte.
 * 
 * This layout enables efficient checking for wins (see isWin())
 * and move generation (see Connect4MoveGenerator.getNextMove()).
 * 
 * @see	isWin()
 * @see Connect4MoveGenerator.getNextMove()
 * 
 * @author Keith gorlen@comcast.net
 *
 */

public class Connect4Position extends MonteCarloTreeNode {
	public static final int ROWS = 6;	// max 7
	public static final int COLS = 7;	// max 8

	private static final Logger LOGGER = Log.LOGGER;
    private static final long colMask =       0x3f3f3f3f3f3f3fL; 	// Mask for column bits
    private static final long bottomRowMask = 0x01010101010101L;	// Mask for bits in first row

    private long[] board;	// Mask for cells occupied by black (board[0]) and red (board[1])
    private int ply;		// Number of moves; black moves first
    
	/**
	 * Construct initial (empty) board position.
	 */
    public Connect4Position() {
		board = new long[2];
		ply = 0;    	
    }
    
	/**
	 * Construct a copy of specified Connect4Position.
	 * 
	 * @param p	board position to copy
	 */
	public Connect4Position(Connect4Position p) {
		super(p);		// Superclass fields initialized, not copied
		board = new long[2];
		board[0] = p.board[0];
		board[1] = p.board[1];
		ply = p.ply;
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.MonteCarloTreeNode#copy()
	 */
	@Override
	public GamePosition copy() {
		return new Connect4Position(this);
	}
	
/*
 * Utility methods.
 * 
 * "final" is hint to javac to expand calls to these simple member functions inline.
 */
	
    /* (non-Javadoc)
     * @see kgorlen.games.MonteCarloTreeNode#sideToMove()
     */
    @Override
    public final String sideToMove() { 	// Return 0 = X (black), 1 = O (red)
		return ((ply & 1) == 0) ? "X" : "O";
    }

	public final long occupied() {	// Return mask of occupied squares
		return (board[0] | board[1]);
    }
    
	public final long empty()	{	// Return mask of empty squares
		return (~(board[0] | board[1]) & colMask);
    }
	
    public final long moves() {		// Return mask of legal moves
    	return ((board[0] | board[1]) + bottomRowMask) & colMask;
    }
    
	/**
	 * Formats the specified row as a string of seven characters:
	 * 'X', 'O', or '.' if the square is empty.
	 * 
	 * @param row	index of row to format, range 1-ROWS
	 * @return		seven-character string
	 */
	public String rowToString(int row) {
		StringBuilder s = new StringBuilder("|");
	    for (long m = 1L<<((COLS-1)*8+(ROWS-row)); m > 0; m >>= 8) {
			if ((board[0] & m) != 0) s.append("X|");
			else if ((board[1] & m) != 0) s.append("O|");
			else s.append(" |");
		}
		return s.toString();
	}

	/* (non-Javadoc)
     * @see kgorlen.games.MonteCarloTreeNode#print()
     */
    @Override
    public String toString(String indent) {
    	StringBuilder s = new StringBuilder();
		for (int i=1; i<=ROWS; i++) {
		    s.append(indent + i + rowToString(i) + "\n");
	    }
		s.append(indent + "  A B C D E F G\n");
		return s.toString();
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.MonteCarloTreeNode#print()
	 */
	@Override
	public String toString() {
		return toString("");
	}

	/**
	 * Print to System.out with indentation
	 * 
	 * @param indent String to prepend to each line
	 */
	public void print(String indent) {
		System.out.print(toString(indent));
	}

	/**
	 * Print to System.out
	 */
	public void print() {
		print("");
	}

	/**
	 * Check specified move for validity.
	 * 
	 * @param mv	bitmask of move
	 * @return		true if move is valid; otherwise false
	 */
	public boolean isValidMove(long mv) {
		// one bit set for a valid move
		return (mv & (mv-1L)) == 0 && (mv & moves()) != 0;
    }	

	/* (non-Javadoc)
	 * @see kgorlen.games.MonteCarloTreeNode#makeMove(kgorlen.games.Move)
	 */
	@Override
	public void makeMove(Move mv) {
		makeMove(((Connect4Move) mv).toLong());
		move = mv;
	}

	/**
	 * Make specified move.
	 * 
	 * @param mv	bitmask of move
	 */
	public void makeMove(long mv) {
		assert isValidMove(mv): String.format("Invalid move: 0x%x, valid: 0x%x", mv, moves());
		
		board[ply & 1] |= mv;
		ply++;
		super.reset();
		return;
    }

	/**
     * @return	true if last color moved made 4-in-a-row
     */
    static final int[] dirShift = { 1, 7, 8, 9 };  // shifts for | / - \

	@Override
    public boolean isWin() {
		if (ply < 7) return false;	// Win requires at least 7 moves
   	
		long m;			// mask for cells occupied by player last moved
		for (int direction = 0; direction < dirShift.length; direction++) {
		    m = board[~ply & 1];
		    m &= m << dirShift[direction];
		    if (m == 0) continue;
		    m &= m << dirShift[direction];
		    if (m == 0) continue;
		    m &= m << dirShift[direction];
		    if (m != 0) {
		    	LOGGER.finest(() -> String.format("Win for %s at ply %d%n",
		    			((ply & 1) != 0) ? "X" : "O", getPly() ));
		    	return true;
		    }
		}
		return false;
    }

	@Override
	public TTEntry newTTentry(int depth, int score, Move bestMove) {
		return new Connect4TTEntry(depth, score, (Connect4Move) bestMove);
	}

	@Override
	public int getPly() {
		return ply;
	}

	@Override
	public boolean isValidMove(Move m) {
		return isValidMove(((Connect4Move) m).toLong());
	}

	@Override
	public MoveGenerator moveGenerator() {
		return new Connect4MoveGenerator(this);
	}

	@Override
	public Variation newVariation() {
		return new Connect4Variation(this);
	}

	@Override
	public Variation newVariation(int score) {
		return new Connect4Variation(this, score);
	}

	/* (non-Javadoc)
	 * 
	 * @return 1 = AI, else -1
	 */
	@Override
	public int scoreSign() {
		assert AISide != 0 : "setAISide() not called";
		return AISide * (1 - (~ply<<1 & 2));
	}

	@Override
	public boolean isDraw() {
		return ply == ROWS*COLS;
	}

	/**
	 * @return	score of a won GamePosition from machine's point of view
	 * 			(AI win = +1, AI loss = -1).
	 */
	@Override
	public int scoreWin() {
		return scoreSign();
	}

	@Override
	public int scoreDraw() {
		return 0;
	}

	/**
	 * Convert column letter to a move bitmask
	 * 
	 * @param col column letter A-G
	 * @return move bitmask, or 0 if illegal move
	 */
	public long columnMove(char letter) {
		return (letter < 'A' || letter > 'G') ? 0
				: ((1L<<ROWS)-1)<<(COLS-(letter-'A')-1<<3) & moves();
	}

	/**
	 * Convert column letter to a Connect4Move
	 * 
	 * @param col column letter A-G
	 */
	public Connect4Move newMove(char letter) {
		return new Connect4Move(columnMove(letter));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = (int) (board[0] ^ board[1]);
		return result ^= (int) (board[0] ^ board[1] >> 32);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Connect4Position))
			return false;
		Connect4Position other = (Connect4Position) obj;
		if (board[0] != other.board[0] || board[1] != other.board[1])
			return false;
		return true;
	}

}
