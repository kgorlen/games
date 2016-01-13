package kgorlen.games;

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
	protected long elapsedTime = 0;
	long startTime = System.currentTimeMillis();
	
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
	}
	
	/**
	 * @return	milliseconds elapsed since last call or since
	 * 			instance created.
	 */
	public long elapsedTime() {
		long currentTime = System.currentTimeMillis();
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
		return principalVariation.score();
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
		System.out.printf("%d positions searched, %.0f positions/ms\n",
							positionsSearched, ((float) positionsSearched)/elapsedTime);
	}
}
