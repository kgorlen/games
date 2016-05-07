package kgorlen.games.tictactoe;

import kgorlen.games.GamePosition;
import kgorlen.games.Move;
import kgorlen.games.MoveGenerator;
import kgorlen.games.ScoreType;
import kgorlen.games.TTEntry;
import kgorlen.games.tictactoe.TicTacToeMove;
import kgorlen.games.tictactoe.TicTacToeTTEntry;

/**
 * Represents a Tic-Tac-Toe GamePosition using a 2-element array of
 * short bitmaps.
 * 
 * Board square numbering:
 * 
 * 		123
 * 		456
 * 		789
 * 
 * short[] board bit-to-board square mapping:
 * 
 * 		Bit position:	1
 * 						09876543210
 *  	Board square:	123_456_789
 *  
 * where - = 0 "guard bit"; e.g. 1-5-9 diagonal bitmap = 0x421.
 * 
 * This layout enables efficient checking for wins (see isWin()),
 * move generation (see TicTacToeMoveGenerator.getNextMove()),
 * and coding of board patterns as three hex digits, since each
 * row corresponds to a hex digit in the range 0x0 - 0x7.
 * 
 * @see	isWin()
 * 
 * @author Keith gorlen@comcast.net
 *
 */
public class TicTacToePosition implements GamePosition {
	private short[] board;		// board[0] = Xs, board[1] = Os
    private short ply;		// Count of occupied squares

	/**
	 * Construct initial (empty) board position.
	 */
	public TicTacToePosition() {
		board = new short[2];
		ply = 0;
	}

	/**
	 * Construct a copy of specified GamePosition.
	 * 
	 * @param p	board position to copy
	 */
	public TicTacToePosition(TicTacToePosition p) {
		board = new short[2];
		board[0] = p.board[0];
		board[1] = p.board[1];
		ply = p.ply;
	}

	public TicTacToePosition copy() {
		return new TicTacToePosition(this);
	}
	
/*
 * Utility methods.
 * 
 * "final" is hint to javac to expand calls to these simple member functions inline.
 * 
 */
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#getPly()
	 */
	@Override
	public final int getPly() { 		// Return number moves made
		return ply;
    }

	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#sideToMove()
	 */
	@Override
	public final String sideToMove() { 	// Return 0 = X, 1 = O
		return ((ply & 1) == 0) ? "X" : "O";
    }

	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#sideLastMoved()
	 */
	@Override
	public final String sideLastMoved() { 	// Return 0 = X, 1 = O
		return ((ply & 1) != 0) ? "X" : "O";
    }

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#scoreSign()
	 */
	@Override
	public final int scoreSign() { 	// Return 1 = X, -1 = O
		return ((ply & 1) == 0) ? 1 : -1;
    }
	
	/**
	 * @return mask of squares occupied by side on move
	 */
	public final short occupiedOnMove() {
		return (short) board[ply & 1];
    }
    
	/**
	 * @return mask of squares occupied by side off move
	 */
	public final short occupiedOffMove() {
		return (short) board[~ply & 1];
    }
    
	/**
	 * @return mask of empty squares
	 */
	public final short empty()	{
		return (short) (~(board[0] | board[1]) & 0x777);
    }

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#isValidMove(kgorlen.games.Move)
	 */
	@Override
	public boolean isValidMove(Move mv) {
		return isValidMove(((TicTacToeMove)mv).toShort());
	}

	/**
	 * Check specified move for validity.
	 * 
	 * @param mv	bitmask of move
	 * @return		true if move is valid; otherwise false
	 */
	public boolean isValidMove(short mv) {
		// one bit set for an empty square
		return (mv & (mv-1)) == 0 && (empty() & mv) != 0;
    }	

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#makeMove(kgorlen.games.Move)
	 */
	@Override
	public void makeMove(Move mv) {
		makeMove(((TicTacToeMove)mv).toShort());
	}

	/**
	 * Make specified move.
	 * 
	 * @param mv	bitmask of move
	 */
	public void makeMove(short mv) {
		assert isValidMove(mv): "Invalid move: 0x" + Integer.toHexString(mv);
		
		board[ply & 1] |= mv;	// What would board[ply++ & 1] |= mv do?
		ply++;
		return;
    }

	/**
     * @return	true if last move made 3-in-a-row
     */
	@Override
    public boolean isWin() {
		if (ply < 5) return false;	// Win requires at least 5 moves
	
		int m = board[~ply & 1];	// 1 - ply%2 (i.e. check X's if O's turn)
		if ((m & 0x421) == 0x421) return true;		// 1-5-9 diagonal
		if ((m & 0x124) == 0x124) return true;		// 3-5-7 diagonal
		if (((m &= m<<1) != 0)
				&& ((m &= m<<1) != 0)) return true;	// 3 in a row
		m = board[~ply & 1];
		if ((m & m<<4 & m<<8) != 0) return true;	// 3 in a column
		
		return false;
    }

    /**
	 * @return	<code>true</code> if this GamePosition is a draw
	 */
	@Override
	public boolean isDraw() {	// Return true if draw
		if (ply == 9) return true;	// All squares filled
/*
 * These additional checks reduce the number of positions searched by
 * MiniMax of an empty board from 549,945 to 526,905.  Note that all
 * possible drawn positions are *not* detected.
*/				
		TicTacToePosition winsq = new TicTacToePosition(this);
		winsq.board[0] |= empty();		// Fill empty squares with X's
		winsq.ply = 5;				// to check for possible win by X
		if (winsq.isWin()) return false;	// X can still win
		
		winsq.board[1] |= empty();		// Fill empty squares with O's
		winsq.ply = 6;				// to check for possible win by O
		if (winsq.isWin()) return false;	// O can still win
				
		return true;
	}
	
	/**
	 * References:
	 * 	https://chessprogramming.wikispaces.com/Evaluation
	 * 
	 * Wins in fewer moves score better.
	 * 
	 * @return	score of a won GamePosition from X's point of view
	 * 			(X win positive, O win negative); i.e., if isWin()
	 * 			is true and X has moved last, return a positive
	 * 			score.
	 */
	@Override
	public int scoreWin() {
		return ((ply&1) != 0) ? (10-ply) : -(10-ply);
	}

	/**
	 * Score a draw.
	 *
	 * @return	score of drawn game, currently always 0
	 */
	@Override
	public int scoreDraw() {
		return 0;
	}
	
	/**
	 * Evaluate a quiescent position.  Not implemented because
	 * Tic Tac Toe game tree is small (<9! = 362,880 positions)
	 * thus can be exhaustively searched.
	 * 
	 * @return	throws unchecked RuntimeException
	 */
	@Override
	public int evaluate() {		// Evaluate a quiescent position
		throw new RuntimeException("Not implemented");
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#moveGenerator(kgorlen.games.Move[])
	 */
	@Override
	public MoveGenerator moveGenerator(Move[] killers) {
		return new TicTacToeMoveGenerator(this, killers);
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#moveGenerator()
	 */
	@Override
	public TicTacToeMoveGenerator moveGenerator() {
		return new TicTacToeMoveGenerator(this, new Move[0]);
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#newVariation()
	 */
	@Override
	public TicTacToeVariation newVariation() {
		return new TicTacToeVariation(this);
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#newVariation(int)
	 */
	@Override
	public TicTacToeVariation newVariation(int score) {
		return new TicTacToeVariation(this, score);
	}
	
	/**
	 * Formats the specified row as a string of three characters:
	 * 'X', 'O', or the square number 1-9 if empty.
	 * 
	 * @param row	index of row to format, range 0-2
	 * @return		three-character string
	 */
	@Override
	public String rowToString(int row) {
		short sq = (short)(4 << 4*(2-row));		// Current square mask		
		String s = "";
		for (int j = 0; j < 3; j++) {
			if ((board[0] & sq) != 0) {
				s += 'X';
			} else if ((board[1] & sq) != 0) {
				s += 'O';
			} else {
				s += Integer.toString(3*row + j + 1);	/// Print index+1 of empty square
			}
			sq = (short)(sq >>1);	// Advance to next square
		}
		return s;
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#toString(java.lang.String)
	 */
	@Override
	public String toString(String indent) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			s.append(indent + rowToString(i) + "\n");
		}
		return s.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString("");
	}
	
	/**
	 * Print board position with indentation.  Useful for formatting tree
	 * search debug printout to indicate depth.
	 * 
	 * @param indent	string of spaces prepended to each row of board
	 */
	public void print(String indent) {
		System.out.print(toString(indent));
	}
	
	/**
	 * Print board position without indentation.
	 */
	public void print() {
		print("");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return board[0] | board[1]<<12;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof TicTacToePosition)) return false;
		if (board[0] == ((TicTacToePosition) o).board[0]
				&& board[1] == ((TicTacToePosition) o).board[1]) return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#newTTentry(int, byte, int, kgorlen.games.Move)
	 */
	@Override
	public TTEntry newTTentry(int depth, ScoreType scoreType, int score, Move bestMove) {
		return new TicTacToeTTEntry(depth, scoreType, score, (TicTacToeMove) bestMove);
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#newTTentry(int, int, kgorlen.games.Move)
	 */
	@Override
	public TTEntry newTTentry(int depth, int score, Move bestMove) {
		return new TicTacToeTTEntry(depth, ScoreType.EXACT, score, (TicTacToeMove) bestMove);
	}

}
