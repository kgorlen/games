package kgorlen.games;

import java.util.HashMap;
import java.util.logging.Logger;

import kgorlen.games.TTEntry;

/**
 * Support for tree searches
 * 
 * @author Keith gorlen@comcast.net
 *
 */
public abstract class TreeSearch {
	private final static Logger LOGGER = Log.LOGGER;
	private static final String CLASS_NAME = TreeSearch.class.getName();

	public static final int SCORE_INFINITY = 999999999;
	
	protected long positionsSearched = 0;
	protected long ttHits = 0;
	protected long elapsedTime = 0;
	private long startTime = System.nanoTime();
	private Position root;
	private HashMap<Position, TTEntry> transTable;
	
	protected TreeSearch(int ttCapacity) {
		transTable =  new HashMap<Position, TTEntry>(ttCapacity);
	}
	
	protected TreeSearch() {
		transTable =  new HashMap<Position, TTEntry>(4096);
	}
	
	/**
	 * @param root	Position to be searched
	 * @param limit	search limit (depth, iterations, time, etc.)
	 * @return principal Variation found by search
	 */
	public abstract Variation search(Position root, int limit);
	
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
	
	public int getTTSize() {
		return transTable.size();
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
	 * @return		principal variation from start Position (may be null)
	 */
	public Variation getPrincipalVariation(Position start) {
		LOGGER.finest(() -> String.format("{ Entering %s.getPrincipalVariation%n", CLASS_NAME));
		TTEntry ttEntry = getTTEntry(start);
		if (ttEntry == null) return null;
		assert ttEntry.getScoreType() == ScoreType.EXACT :
			"Principal variation score type not EXACT";
		
		Variation pvar = start.newVariation(ttEntry.getScore());
		Position nextPosition = start.copy();
		Move nextMove;
		do {
			final TTEntry logTTEntry = ttEntry;
			LOGGER.finest(() -> String.format("  Position:%n%s  TTEntry: %s%n",
					nextPosition.toString("  "), logTTEntry.toString() ));
			assert pvar.getScore() == (this instanceof MiniMax ? ttEntry.getScore()
					: start.scoreSign() * nextPosition.scoreSign() * ttEntry.getScore())  // NegaMax
					: "Principal variation scores not equal";
			nextMove = ttEntry.getMove();
			pvar.addMove(nextMove);
			nextPosition.makeMove(nextMove);
		} while ((ttEntry = getTTEntry(nextPosition)) != null);
		
		LOGGER.finest(() -> String.format("} Exiting %s.getPrincipalVariation%n", CLASS_NAME));
		return pvar;
	}
	
	/**
	 * @return	principal variation from root Position  (may be null)
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
	 * Log search statistics	 * 
	 */
	public void logStatistics() {
		String className = getClass().getSimpleName();
		LOGGER.info(() -> String.format(
				"%s.search statistics:%n", className));
		LOGGER.info(() -> String.format(
				"  %d positions searched in %fs (%,d positions/s)%n",
				positionsSearched, elapsedTime/1E9, 1000000000*positionsSearched/elapsedTime));
		LOGGER.info(() -> String.format(
				"  %d TT entries, %d TT hits%n",
				getTTSize(), ttHits ));		
	}
	
	/**
	 * Print search statistics
	 */
	public void printStatistics() {
		System.out.format("%d positions searched in %fs (%,d positions/s)%n%d TT entries, %d TT hits%n",
							positionsSearched, elapsedTime/1E9, 1000000000*positionsSearched/elapsedTime,
							transTable.size(), ttHits);
	}
}
