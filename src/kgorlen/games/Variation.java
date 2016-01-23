package kgorlen.games;

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
	public int getScore();

	/**
	 * @param i index of Move to get 0 - numMoves()-1
	 * @return	specified Move
	 */
	public Move getMove(int i);
	
	/**
	 * @param v	Variation from the current GamePosition
	 * @return	score of Variation
	 */
	public int setMoves(Variation v);
	
	/**
	 * @return	first Move in this Variation
	 */
	public Move getMove();

	/**
	 * @param vscore	score of this Variation
	 * @param firstMove	Move to add to the specified Variation
	 */
	public void addMove(int vscore, Move firstMove);
	
	/**
	 * @param vscore	score of this Variation
	 * @param firstMove	Move to add to the specified Variation
	 * @param v			Variation from the current GamePosition
	 */
	public void addMoves(int vscore, Move firstMove, Variation v);

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
	public void print(Position start, String indent);

	/**
	 * Print Variation without indentation
	 * 
	 * @param start		starting GamePosition of this Variation
	 */
	public void print(Position start);

}
