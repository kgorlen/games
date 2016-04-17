/**
 * 
 */
package kgorlen.games.tictactoe;

import kgorlen.games.Move;

/**
 * Instances of Moves are created by MoveGenerators, applied to
 * Positions, and stored in Variations.
 * 
 * @author Keith gorlen@comcast.net
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + move;
		return result;
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
		if (getClass() != obj.getClass())
			return false;
		TicTacToeMove other = (TicTacToeMove) obj;
		if (move != other.move)
			return false;
		return true;
	}

}
