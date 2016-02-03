package kgorlen.games;

/**
 * Interface to search tree positions (nodes)
 * 
 * @author Keith gorlen@comcast.net
 *
 */
public interface Position {

	/**
	 * @param depth		search depth of score
	 * @param score		position score
	 * @param bestMove	best Move
	 * @return
	 */
	public TTEntry newTTentry(int depth, int score, Move bestMove);
	
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
	public Variation newVariation();

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

	/**
	 * @return	hashCode for this Position
	 */
	public int hashCode();

	/**
	 * @param v	Position to compare
	 * @return	true if equals
	 */
	public boolean equals(Object p);

	/**
	 * @return +1 if Max to move, -1 if Min to move
	 */
	public int scoreSign();

}
