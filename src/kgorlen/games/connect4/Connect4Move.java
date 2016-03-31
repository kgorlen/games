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
		return ((char) ('A' + Connect4Position.COLS-1 - (m>>3))) + ""
				+ ((char) ('1' + Connect4Position.ROWS-1 - (m&0x7)));
	} 
	
}
