package kgorlen.games;

import java.util.HashMap;

import kgorlen.games.TTEntry;

/**
 * Support for tree searches
 * 
 * @author Keith gorlen@comcast.net
 *
 */
public abstract class TreeSearch {
	final static public int SCORE_INFINITY = 999999999;
	
	protected boolean debug = false;
	protected long positionsSearched = 0;
	protected long ttHits = 0;
	private long elapsedTime = 0;
	private long startTime = System.nanoTime();
	private Position root;
	private HashMap<Position, TTEntry> transTable;
	
	TreeSearch(int ttCapacity) {
		transTable =  new HashMap<Position, TTEntry>(ttCapacity);
	}
	
	TreeSearch() {
		transTable =  new HashMap<Position, TTEntry>(4096);
	}
	
	/**
	 * @param root		Position to be searched
	 * @param maxDepth	maximum search depth
	 * @return
	 */
	public abstract TreeSearch search(Position root, int maxDepth);
	
	/**
	 * @return the debug switch setting
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @param debug set the debug switch
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Reset the positions searched and transposition table
	 */
	public void setRoot(Position root) {
		this.root = root;
		transTable.clear();
		positionsSearched = 0;
		ttHits = 0;
	}
	
	/**
	 * @param p	Position to save
	 * @param	transposition table entry for specified Position
	 */
	public void putTTEntry(Position p, TTEntry ttEntry) {
		transTable.put(p, ttEntry);
	}
	
	/**
	 * Override to handle position symmetries
	 * 
	 * @param p	Position to find
	 * @return	transposition table entry for specified Position, or null
	 */
	public TTEntry getTTEntry(Position p) {
		return transTable.get(p);
	}
	
	/**
	 * @return	last root Position searched
	 */
	public Position getRoot() {
		return root;
	}
	
	/**
	 * @param start	starting Position of Variation
	 * @return		principal variation from start Position
	 */
	public Variation getPrincipalVariation(Position start) {
		TTEntry ttEntry = getTTEntry(start);
		if (ttEntry == null) return null;
		
		Variation pvar = start.newVariation();
		pvar.setScore(ttEntry.getScore());
		Position nextPosition = start.copy();
		Move nextMove;
		do {
			assert ttEntry.getScoreType() == ScoreType.EXACT : "Principal variation score type not EXACT";
			assert pvar.getScore() == (this instanceof MiniMax ? ttEntry.getScore()
					: start.scoreSign() * nextPosition.scoreSign() * ttEntry.getScore())  // NegaMax
					: "Principal variation scores not equal";
			nextMove = ttEntry.getMove();
			pvar.addMove(nextMove);
			nextPosition.makeMove(nextMove);
		} while ((ttEntry = getTTEntry(nextPosition)) != null);
		return pvar;
	}
	
	/**
	 * @return	principal variation from root Position
	 */
	public Variation getPrincipalVariation() {
		return getPrincipalVariation(getRoot());
	}

	/**
	 * @param start	position searched
	 * @return	score of specified Position
	 */
	public int getScore(Position start) {
		TTEntry ttEntry = getTTEntry(start);
		return ttEntry.getScore();
	}
	
	/**
	 * @return	score of root Position
	 */
	public int getScore() {
		return getScore(getRoot());
	}

	/**
	 * @param start	position searched
	 * @return		best move found from specified Position
	 */
	public Move getMove(Position start) {
		TTEntry ttEntry = getTTEntry(start);
		return ttEntry.getMove();
	}
	
	/**
	 * @return		best move found from root Position
	 */
	public Move getMove() {
		return getMove(getRoot());		
	}

	/**
	 * @return	nanoseconds elapsed since last call or since
	 * 			instance created.
	 */
	public long elapsedTime() {
		long currentTime = System.nanoTime();
		elapsedTime = currentTime - startTime;
		startTime = currentTime;
		return elapsedTime;
	}
		
	/**
	 * Print search statistics
	 */
	public void printStatistics() {
		System.out.printf("%d positions searched in %.2fus (%.2f positions/us)%n%d TT entries, %d TT hits\n",
							positionsSearched, elapsedTime/1000.0, 1000.0*positionsSearched/elapsedTime,
							transTable.size(), ttHits);
	}
}
