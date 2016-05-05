/**
 * 
 */
package kgorlen.games.connect4;

import kgorlen.games.Move;

/**
 * @author Keith gorlen@comcast.net
 *
 */
public class Connect4Move implements Move {
	/**
	 * Moves are represented as a bitmask.  One of the bits 0-53
	 * (see Connect4Position) is set to indicate the square to be
	 * occupied by the side playing the move.
	 */
	private long move = 0;
	
	/**
	 * Construct from bitmask
	 * 
	 * @param m move bitmask
	 */
	public Connect4Move(long m) {
		move = m;
	}
	
	/**
	 * Construct from column/row string: "a1" through "g6"
	 * 
	 * @param mv column a-g + 1-6
	 */
	public Connect4Move(String mv) {
		int col = Connect4Position.COLS - (Character.toLowerCase(mv.charAt(0))-'a') - 1;
		int row = mv.charAt(1) - '1';
		assert col >= 0 && col < Connect4Position.COLS
				&& row >=0 && row < Connect4Position.ROWS :
					"Invalid move: " + mv;
		move = (1L << row) << (col << 3);
	}
	
	/**
	 *  Copy constructor
	 *  
	 * @param mv move to be copied
	 */
	public Connect4Move(Connect4Move mv) {
		move = mv.move;
	}
	
	/**
     * @return move as long bitmask
     */
    public final long toLong() {
    	return move;
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		int m = Long.numberOfTrailingZeros(move);
		return ((char) ('a' + Connect4Position.COLS-1 - (m>>3))) + ""
				+ ((char) ('1' + (m&0x7)));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (move ^ (move >>> 32));
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
		Connect4Move other = (Connect4Move) obj;
		if (move != other.move)
			return false;
		return true;
	} 
	
}
