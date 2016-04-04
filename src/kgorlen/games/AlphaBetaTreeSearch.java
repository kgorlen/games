package kgorlen.games;

import java.util.ArrayList;

/**
 * @author Keith gorlen@comcast.net
 *
 * Extend TreeSearch with support for alpha-beta cutoffs
 */
public abstract class AlphaBetaTreeSearch extends TreeSearch {
	private long alphaCutoffs;
	private long betaCutoffs;
	private int killerLength;
	private ArrayList<Move[]> killerMoves;
	
	/**
	 * @param ttCapacity	transposition table capacity
	 * @param killerSize	number of killer moves to save per ply
	 */
	AlphaBetaTreeSearch(int ttCapacity, int killerSize) {
		super(ttCapacity);
		this.killerLength = killerSize;
		killerMoves = new ArrayList<Move[]>();
	}
	
	AlphaBetaTreeSearch() {
		super();
		this.killerLength = 2;
		killerMoves = new ArrayList<Move[]>();
	}

	/**
	 * @param ply		Move ply
	 * @param killer	killer Move to save for specified ply
	 */
	protected void addKiller(int ply, Move killer) {
		while (killerMoves.size() <= ply)	// expand ArrayList to ply
			killerMoves.add(new Move[killerLength]);
		Move[] moves = killerMoves.get(ply);
		for (int i=0; i<moves.length-1; i++) moves[i+1] = moves[i];
		moves[0] = killer;
	}
	
	protected Move[] getKillers(int ply) {
		while (killerMoves.size() <= ply)	// expand ArrayList to ply
			killerMoves.add(new Move[killerLength]);
		return killerMoves.get(ply);		
	}
	
	protected long incCutoffs(boolean maximize) {
		return maximize ? ++betaCutoffs : ++alphaCutoffs;
	}
	
	protected long incCutoffs(int color) {
		return (color > 0) ? ++betaCutoffs : ++alphaCutoffs;
	}
	
	/**
	 * Reset the positions searched counter
	 */
	public void setRoot(Position root) {
		super.setRoot(root);
		alphaCutoffs = 0;
		betaCutoffs = 0;
	}
	
	/**
	 * Print search statistics
	 */
	public void printStatistics() {
		super.printStatistics();
		System.out.format("%d alpha cutoffs, %d beta cutoffs%n", alphaCutoffs, betaCutoffs);
	}
}
