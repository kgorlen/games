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
	 * @return name of side to move, e.g. 'X' or 'O'
	 */
	public String sideToMove();

	/**
	 * @return +1 if Max to move, -1 if Min to move
	 */
	public int scoreSign();
	
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
