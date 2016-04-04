/**
 * 
 */
package kgorlen.games.tictactoe;

import kgorlen.games.Move;

/**
 * Instances of Moves are created by MoveGenerators, applied to
 * Positions, and stored in Variations.
 * 
 * @author Keith
 *
 */
public class TicTacToeMove implements Move {
	/**
	 * Moves are represented as a bitmask.  One of the bits 0-11 is
	 * set to indicate the square to be occupied by the side
	 * playing the move.
	 */
	short move = 0;
	
	/**
	 * @param m	move bitmask
	 */
	public TicTacToeMove(short m) {
		move = m;
	}
	
	/**
	 * @param square	integer square number 1-9
	 */
	public TicTacToeMove(int square) {
		int m = 1 <<(8-(square-1));
		m = (m & 0x7<<0) | (m & 0x7<<3) <<1 | (m & 0x7<<6) <<2;
		move = (short) m;
	}
	
	/**
	 * @param square	String square number 1-9
	 */
	public TicTacToeMove(String square) {
		this(Integer.parseInt(square));	// Calls TicTacToeMove(int square)
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param mv	move to be copied
	 */
	public TicTacToeMove(TicTacToeMove mv) {
		move = mv.move;
	}
	
	public short toShort() {
		return move;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		int m = (move & 0x7) | (move & 0x7<<4) >>1 | (move & 0x7<<8) >>2;
		return Integer.toString(9-Integer.numberOfTrailingZeros(m));
	}

}
