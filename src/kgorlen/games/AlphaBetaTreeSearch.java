package kgorlen.games;

/**
 * @author Keith gorlen@comcast.net
 *
 * Extend TreeSearch with support for alpha-beta cutoffs
 */
public abstract class AlphaBetaTreeSearch extends TreeSearch {
	long alphaCutoffs;
	long betaCutoffs;	
	
	long incCutoffs(boolean maximize) {
		return maximize ? ++betaCutoffs : ++alphaCutoffs;
	}
	
	long incCutoffs(int color) {
		return (color > 0) ? ++betaCutoffs : ++alphaCutoffs;
	}
	
	/**
	 * Print search statistics
	 */
	public void printStatistics() {
		System.out.printf("%d positions searched, %d alpha cutoffs, %d beta cutoffs, %.0f positions/ms\n",
							positionsSearched, alphaCutoffs, betaCutoffs,
							((float) positionsSearched)/elapsedTime);
	}
}
