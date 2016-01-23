package kgorlen.games;

/**
 * @author Keith gorlen@comcast.net
 *
 * Extend TreeSearch with support for alpha-beta cutoffs
 */
public abstract class AlphaBetaTreeSearch extends TreeSearch {
	private long alphaCutoffs;
	private long betaCutoffs;	
	
	protected long incCutoffs(boolean maximize) {
		return maximize ? ++betaCutoffs : ++alphaCutoffs;
	}
	
	protected long incCutoffs(int color) {
		return (color > 0) ? ++betaCutoffs : ++alphaCutoffs;
	}
	
	/**
	 * Reset the positions searched counter
	 */
	public void reset() {
		super.reset();
		alphaCutoffs = 0;
		betaCutoffs = 0;
	}
	
	/**
	 * Print search statistics
	 */
	public void printStatistics() {
		System.out.printf("%d positions searched, %d alpha cutoffs, %d beta cutoffs, %.2f positions/us\n",
							positionsSearched, alphaCutoffs, betaCutoffs,
							((float) 1000 * positionsSearched)/elapsedTime);
	}
}
