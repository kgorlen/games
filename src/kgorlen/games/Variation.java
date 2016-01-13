package kgorlen.games;

import kgorlen.games.GamePosition;
import kgorlen.games.Move;

/**
 * A Variation stores the sequence of Moves played to get to a GamePosition
 * 
 * @author Keith gorlen@comcast.net
 *
 */
public interface Variation {
	/**
	 * @return number of Moves in this Variation
	 */
	public int numMoves();
	
	/**
	 * @return score of this Variation
	 */
	public int score();

	/**
	 * @param i index of Move to get 0 - numMoves()-1
	 * @return	specified Move
	 */
	public Move getMove(int i);
	
	/**
	 * @return	first Move in this Variation
	 */
	public Move getMove();

	/**
	 * @param firstMove	Move to add to the specified Variation
	 * @param v			Variation from the current GamePosition
	 * @param vscore	score of this Variation
	 * @return			number of moves in updated Variation
	 */
	public int addMoves(Move firstMove, Variation v, int vscore);

	/**
	 * Removes all Moves from this Variation
	 */
	public void reset();
	
	/**
	 * Print Variation, indented
	 * 
	 * @param start		starting GamePosition of this Variation
	 * @param indent	String to prepend to printed lines (to
	 * 					indicate current search depth)
	 */
	public void print(GamePosition start, String indent);

	/**
	 * Print Variation without indentation
	 * 
	 * @param start		starting GamePosition of this Variation
	 */
	public void print(GamePosition start);

}
