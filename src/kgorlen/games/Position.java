package kgorlen.games;

/**
 * Interface to search tree positions (nodes)
 * 
 * @author Keith gorlen@comcast.net
 *
 */
public interface Position {

	/**
	 * @return a deep copy of this Position
	 */
	public Position copy();

	/**
	 * @return number of moves made before this Position
	 */
	public int numMoves();

	/**
	 * Check specified move for validity.
	 * 
	 * @param m		Move to be played
	 * @return		true if move is valid; otherwise false
	 */
	public boolean isValidMove(Move m);

	/**
	 * Make specified move.
	 * 
	 * @param m		Move to be played
	 */
	public void makeMove(Move m);

	/**
	 * Evaluate a quiescent position.
	 * 
	 * @return	throws unchecked RuntimeException
	 */
	public int evaluate();

	/**
	 * Create a MoveGenerator for this Position
	 * 
	 * @param debug enable/disable debug; default: false
	 * @return	instance of a MoveGenerator for this Position
	 */
	public MoveGenerator moveGenerator(boolean debug);

	public MoveGenerator moveGenerator();

	/**
	 * Create a Variation for specified Position
	 * 
	 * @return	instance of a Variation for this Position
	 */
	public Variation variation();

	/**
	 * Print board position with indentation.  Useful for formatting tree
	 * search debug printout to indicate depth.
	 * 
	 * @param indent	string of spaces prepended to each row of board
	 */
	public void print(String indent);

	/**
	 * Print board position without indentation.
	 */
	public void print();

}
