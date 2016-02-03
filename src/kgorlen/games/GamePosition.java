package kgorlen.games;

/**
 * Extends Position with methods to support 2-player game trees
 * 
 * @author Keith gorlen@comcast.net
 * 
 */
public interface GamePosition extends Position {

	/**
	 * @return a deep copy of this Position
	 */
	public GamePosition copy();

	/**
	 * @param depth		search depth of score
	 * @param scoreType	type of score (EXACT, UPPERBOUND, LOWERBOUND)
	 * @param score		position score
	 * @param bestMove	best Move
	 * @return
	 */
	public TTEntry newTTentry(int depth, ScoreType scoreType, int score, Move bestMove);
	
	/**
	 * @return name of side to move, e.g. 'X' or 'O'
	 */
	public String sideToMove();

	/**
     * @return	<code>true</code> if last move resulted in a win
     */
    public boolean isWin();

	/**
	 * @return	<code>true</code> if this GamePosition is a draw
	 */
	public boolean isDraw();
	
	/**
	 * References:
	 * 	https://chessprogramming.wikispaces.com/Evaluation
	 * 
	 * @return	score of a won GamePosition from Max's point of view
	 * 			(Max win positive, Min win negative); i.e., if isWin()
	 * 			is true and Max has moved last, return a positive
	 * 			score.
	 */
	public int scoreWin();

	/**
	 * Score a draw.
	 */
	public int scoreDraw();

}
