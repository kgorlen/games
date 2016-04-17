package kgorlen.games;

/**
 * Instances of Moves are created by MoveGenerators, applied to
 * Positions, and stored in Variations.
 * 
 * @author Keith gorlen@comcast.net
 *
 */
public interface Move {
	/**
	 * @return	String representing this Move
	 */
	public String toString();
	
	/**
	 * @param obj Move to test for equality
	 * @return true if Moves are equal
	 */
	@Override
	public boolean equals(Object obj);
}
