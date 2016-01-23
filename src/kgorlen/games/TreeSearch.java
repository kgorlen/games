package kgorlen.games;

import java.util.HashMap;

import kgorlen.games.Variation;

/**
 * Support for tree searches
 * 
 * @author Keith gorlen@comcast.net
 *
 */
public abstract class TreeSearch {
	protected boolean debug = false;
	protected long positionsSearched = 0;
	protected Variation principalVariation = null;
	protected HashMap<Position, Variation> transposition = new HashMap<Position, Variation>();
	protected long ttHits = 0;
	protected long elapsedTime = 0;
	private long startTime = System.nanoTime();
	
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
	 * Reset the positions searched counter
	 */
	public void reset() {
		positionsSearched = 0;
		principalVariation = null;
		transposition.clear();
	}
	
	/**
	 * @return	milliseconds elapsed since last call or since
	 * 			instance created.
	 */
	public long elapsedTime() {
		long currentTime = System.nanoTime();
		elapsedTime = currentTime - startTime;
		startTime = currentTime;
		return elapsedTime;
	}
		
	/**
	 * @return	principal Variation found by search
	 */
	public Variation getPrincipalVariation() {
		return principalVariation;
	}
	
	/**
	 * @return	best score found by search
	 */
	public int getScore() {
		return principalVariation.getScore();
	}
	
	/**
	 * @return	best Move found by search
	 */
	public Move getMove() {
		return principalVariation.getMove();
	}
	
	/**
	 * Print search statistics
	 */
	public void printStatistics() {
		System.out.printf("%d positions searched, %d TT entries, %d TT hits, %.2f positions/us\n",
							positionsSearched, transposition.size(), ttHits,
							((float) 1000*positionsSearched)/elapsedTime);
	}
}
