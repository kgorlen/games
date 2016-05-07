package kgorlen.games.connect4;

import java.util.logging.Logger;

import kgorlen.games.GamePosition;
import kgorlen.games.Log;
import kgorlen.games.Move;
import kgorlen.games.MoveGenerator;
import kgorlen.games.TTEntry;
import kgorlen.games.Variation;
import kgorlen.games.mcts.MCTS;
import kgorlen.games.mcts.MCTSClassic;
import kgorlen.games.mcts.MCTSPosition;
import kgorlen.games.mcts.MCTSSolver;

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

public class Connect4Position extends MCTSPosition {
	public static final int ROWS = 6;	// max 7
	public static final int COLS = 7;	// max 8

	private static final Logger LOGGER = Log.LOGGER;
	private static final String CLASS_NAME = MCTS.class.getName();
    private static final long colMask =       0x3f3f3f3f3f3f3fL; 	// Mask for column bits
    private static final long bottomRowMask = 0x01010101010101L;	// Mask for bits in first row

    private long[] board;	// Mask for cells occupied by black (X, board[0]) and red (O, board[1])
    private int ply;		// Number of moves; black (X) moves first
    
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

    /* (non-Javadoc)
     * @see kgorlen.games.GamePosition#sideLastMoved()
     */
    @Override
    public final String sideLastMoved() { 	// Return 0 = X (black), 1 = O (red)
		return ((ply & 1) != 0) ? "X" : "O";
    }

	/**
	 * @return mask of occupied squares
	 */
	public final long occupied() {
		return (board[0] | board[1]);
    }
    
	/**
	 * @return mask of empty squares
	 */
	public final long empty() {
		return (~(board[0] | board[1]) & colMask);
    }
	
    public final long moves() {		// Return mask of legal moves
    	return ((board[0] | board[1]) + bottomRowMask) & colMask;
    }
    
	/**
	 * Formats the specified row as a string:
	 * 'X|', 'O|', or ' |' if the square is empty.
	 * 
	 * @param row	index of row to format, range 1-ROWS
	 * @return		seven-character string
	 */
    @Override
	public String rowToString(int row) {
		StringBuilder s = new StringBuilder("|");
	    for (long m = 1L<<((COLS-1)*8+(row-1)); m > 0; m >>= 8) {
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
		for (int i=ROWS; i>0; i--) {
		    s.append(indent + i + rowToString(i) + "\n");
	    }
		s.append(indent + "  a b c d e f g\n");
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

    static Connect4Move winmove = new Connect4Move("a1");	// TODO: remove after debugging
    
	/* (non-Javadoc)
	 * @see kgorlen.games.mcts.MCTSPosition#isWin()
	 */
	@Override
    public boolean isWin() {
//		if (ply == 1) {
//			 return true;	// TODO: test Next move from root position is win
//		}
		
//		if (ply == 2) {
//			return true;	// TODO: test PlayerToMove loses
//		}

//		if (ply == 3) {
//			 return true;	// TODO: test deep win by root
//		}

//		if (ply == 4) {
//			 return true;	// TODO: test deep loss by root
//		}

//		if (ply == 3 && winmove.equals(MCTSSolver.visited.get(1).getMove())) {
//			return true;	// TODO: test find X winmove
//		}

//		if (ply == 4 && winmove.equals(MCTSSolver.visited.get(1).getMove())) {
//			return true;	// TODO: test find O winmove
//		}
		
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
		    	LOGGER.finest(() -> String.format("%s.isWin by %s at ply %d%n",
		    			CLASS_NAME, sideLastMoved(), getPly() ));
		    	return true;
		    }
		}
		return false;
    }

	/* (non-Javadoc)
	 * @see kgorlen.games.mcts.MCTSPosition#isDraw()
	 */
	@Override
	public boolean isDraw() {
//		if (ply == 1) {
//			return true;	// TODO: test All moves from root position draw
//		}

		if (ply < ROWS*COLS) return false;
    	LOGGER.finest(() -> String.format("Draw at ply %d%n", ply));
		return true;
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.mcts.MCTSPosition#newTTentry(int, int, kgorlen.games.Move)
	 */
	@Override
	public TTEntry newTTentry(int depth, int score, Move bestMove) {
		return new Connect4TTEntry(depth, score, (Connect4Move) bestMove);
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.mcts.MCTSPosition#getPly()
	 */
	@Override
	public final int getPly() {
		return ply;
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.mcts.MCTSPosition#isValidMove(kgorlen.games.Move)
	 */
	@Override
	public boolean isValidMove(Move m) {
		return isValidMove(((Connect4Move) m).toLong());
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.mcts.MCTSPosition#moveGenerator()
	 */
	@Override
	public MoveGenerator moveGenerator() {
		return new Connect4MoveGenerator(this);
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.mcts.MCTSPosition#newVariation()
	 */
	@Override
	public Variation newVariation() {
		return new Connect4Variation(this);
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#newVariation(int)
	 */
	@Override
	public Variation newVariation(int score) {
		return new Connect4Variation(this, score);
	}

	/* (non-Javadoc)
	 * 
	 * @return 1 = X, else -1
	 */
	@Override
	public final int scoreSign() {
		return (ply & 1) == 1 ? +1 : -1;
//		return 1 - (~ply<<1 & 2);	TODO: is this faster?
	}

	/**
	 * @return	score of a won GamePosition from X's point of view
	 * 			(X win = 1, O win = -1); i.e., if isWin()
	 * 			is true and X has moved last, return 1.
	 */
	@Override
	public int scoreWin() {
		return (ply & 1) == 1 ? +1 : -1;
//		return 1 - (~ply<<1 & 2);	TODO: is this faster?
	}

	@Override
	public int scoreDraw() {
		return 0;
	}

	/**
	 * Convert column letter to a move bitmask
	 * 
	 * @param col column letter a-g
	 * @return move bitmask, or 0 if illegal move
	 */
	public long columnMove(char letter) {
		return (letter < 'a' || letter > 'g') ? 0
				: ((1L<<ROWS)-1)<<(COLS-(letter-'a')-1<<3) & moves();
	}

	/**
	 * Convert column letter to a Connect4Move
	 * 
	 * @param col column letter a-g
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
