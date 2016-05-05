package kgorlen.games.mcts;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import kgorlen.games.Log;
import kgorlen.games.Position;
import kgorlen.games.TreeSearch;
import kgorlen.games.Variation;

public abstract class MCTS extends TreeSearch {
	private static final Logger LOGGER = Log.LOGGER;
	private static final String CLASS_NAME = MCTS.class.getName();

	static Random randGen;
	public static ArrayList<MCTSPosition> visited;	// TODO: remove after tested

	/**
	 * @param r instance of Random number generator
	 * @param ttCapacity Transposition HashMap initial capacity
	 */
	public MCTS(Random r, int ttCapacity) {
		super(ttCapacity);
		randGen = r;
	}

	/**
	 * @param r instance of Random number generator
	 */
	public MCTS(Random r) {
		super();
		randGen = r;
	}

	public MCTS() {
		super();
		randGen = new Random();
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.TreeSearch#search(kgorlen.games.Position, int)
	 */
	@Override
	public Variation search(Position root, int limit) {
		assert !((MCTSPosition) root).isWin() && !((MCTSPosition)root).isDraw() :
			"root is terminal position";
		LOGGER.config(String.format("%s.search limit=%d%n",
				CLASS_NAME, limit));
		// TODO: log UCT C
		setRoot(root);
		elapsedTime();
		
		try {
			for (int i=1; i <= limit; i++) {		// TODO: Change limit to elapsed time
				final int iteration = i;
				LOGGER.fine(() -> String.format(">>> %s.search start iteration %d%n",
						CLASS_NAME, iteration));
				visited = new ArrayList<MCTSPosition>();
				final int result = -mcts((MCTSPosition) root, 0, "");
				LOGGER.fine(() -> String.format("<<< %s.search iteration %d, result=%d, principal variation:%n%s",
						CLASS_NAME, iteration, result, getPrincipalVariation().toString()));				
			}
		} catch(MCTSSearchException e) {
			LOGGER.fine(() -> String.format("%s.search terminated: %s%n",
					CLASS_NAME, e.toString() ));
		}
	
		elapsedTime();
		logStatistics(CLASS_NAME);
		Variation pvar = getPrincipalVariation();
		Variation.logPrincipalVariation(pvar, CLASS_NAME);
		LOGGER.info(String.format("MCTSSolver's move: %s%n", pvar.getMove().toString()));
		return pvar;
	}

	abstract int mcts(MCTSPosition root, int i, String string) throws MCTSSearchException;

	/**
	 * Select child node to expand
	 * 
	 * References:
	 *   http://mcts.ai/code/java.html
	 *   http://scalab.uc3m.es/~seminarios/seminar11/slides/lucas2.pdf
	 * 
	 * EPSILON small random number 0-EPSILON to break ties in unexpanded nodes,
	 * i.e. when visits = 0.  1/EPSILON > SCORE_INFINITY
	 * 
	 * @param parent position of children to select
	 * @param indent string prepended to log messages
	 * @return selected child or null if all children draws
	 */
	public MCTSPosition select(MCTSPosition parent, String indent) {
		assert parent.children != null && parent.children.size() > 0 :
			"No children";
		LOGGER.finer(() -> String.format("%s%s.select move from ply %d...%n",
				indent, CLASS_NAME, parent.getPly() ));
		
		MCTSPosition selected = null;
		double bestValue = Double.NEGATIVE_INFINITY;

		for (MCTSPosition child : parent.children) {
			assert parent.getPly()+1 == child.getPly() :
				"Invalid child position ply";
			assert child.getMove() != null :
				"Child move is null";

			if (child.isWin()) {
				LOGGER.finer(() -> String.format("%s... %s.select returning win move %s to ply %d%n",
						indent, CLASS_NAME, child.getMove().toString(), child.getPly() ));
				return child;
			}

			if (child.isDraw()) {
				LOGGER.finest(() -> String.format("%s  move %s to ply %d (draw)%n",
						indent, child.getMove().toString(), child.getPly() ));
				continue;
			}

			double uctValue = uct(parent, child);
			LOGGER.finest(() -> String.format("%s  move %s to ply %d UCT=%+f%n",
					indent, child.getMove().toString(), child.getPly(), uctValue ));
			if (uctValue > bestValue) {
				selected = child;
				bestValue = uctValue;
			}
		}
//		assert selected != null : "No move selected";
		
		final MCTSPosition bp = selected;
		final double bv = bestValue;
		LOGGER.finer(() -> (bp == null) ?	// TODO: remove null case?
				String.format("%s... %s.select returning null at ply %d (all moves draw)%n", CLASS_NAME, parent.getPly() ) :
					String.format("%s... %s.select returning move %s to ply %d UCT=%+f%n",
							indent, CLASS_NAME, bp.getMove().toString(), bp.getPly(), bv));    	
		return selected;
	}

	/**
	 * @param parent position of children to select
	 * @return selected child or null if all children draws
	 */
	public MCTSPosition select(MCTSPosition parent) {
		return select(parent, "");
	}
	
	/**
	 * Compute Upper Confidence Bounds for Trees
	 * 
	 * @param parent position
	 * @param child position
	 * @return Upper Confidence Bounds for Trees value
	 */
	private static final double C = Math.sqrt(2);	// Upper confidence bound for Trees (UCT) coefficient
	private static final double EPSILON = 1.0/(10.0*SCORE_INFINITY);

	protected double uct(MCTSPosition parent, MCTSPosition child) {
		return child.getScore() / (child.visits + EPSILON) +
		C * Math.sqrt(Math.log(parent.visits+1) / (child.visits + EPSILON)) +
		randGen.nextDouble() * EPSILON;		
	}
	
	/**
	 * References:
	 * [1] http://ccg.doc.gold.ac.uk/teaching/ludic_computing/ludic16.pdf
	 * [2] https://jeffbradberry.com/posts/2015/09/intro-to-monte-carlo-tree-search/
	 * [3] https://github.com/theKGS/MCTS
	 * [4] http://www.ru.is/faculty/yngvi/pdf/WinandsBS08.pdf
	 * 
	 * [1] Best move = highest UCT--but why include regret term?
	 * [2] Best move = highest win percent
	 * [3] Best move = most visited
	 * [4] Best move = "secure child": highest score + A/sqrt(visits)
	 * 
	 * @return principal Variation (moves w/ highest win percent as per [2])
	 */
	@Override
	public Variation getPrincipalVariation(Position start) {
		LOGGER.finer(() -> String.format("Entering %s.getPrincipalVariation%n", CLASS_NAME));
		Variation pvar = start.newVariation();
		pvar.setStart(start);
		MCTSPosition parent = (MCTSPosition) start;

		while (parent.children != null) {
			final MCTSPosition logParent = parent;
			LOGGER.finer(() -> String.format(
					"  Finding best move at ply %d...%n", logParent.getPly()));

			MCTSPosition bestChild = null;
			double bestValue = Double.NEGATIVE_INFINITY;

			for (MCTSPosition child : parent.children) {
				assert child.move != null : "Null move at ply %d";
				LOGGER.finer(() -> String.format(
						"    %s total/visits: %+d/%d",
						child.getMove().toString(), child.getScore(), child.visits));
				if (child.visits == 0) {
					LOGGER.finer("\n");
					continue;	        	
				}

				double value = pvScore(child);
				LOGGER.finer(() -> String.format(" (%+f)%n", value));
				if (value > bestValue) {
					bestChild = child;
					bestValue = value;
				}
			}	        

			if (bestChild == null) {
				assert pvar.size() > 0 : "getPrincipalVariation failed";
				return pvar;
			}

			final MCTSPosition logChild = bestChild;
			final double logValue = bestValue;
			LOGGER.finer(() -> String.format(
					"  ...Best move at ply %d is %s (%+f)%n",
					logParent.getPly(), logChild.getMove().toString(), logValue));

//			if (pvar.size() == 0) pvar.setScore(Math.abs(bestChild.getScore()) != SCORE_INFINITY ?
//					(int) Math.round(bestValue*100) : bestChild.getScore());
			if (pvar.size() == 0) pvar.setScore(bestChild.getScore());
			pvar.add(bestChild.move);
			parent = bestChild;
		}

		LOGGER.finer(() -> String.format(
				"Exiting %s.getPrincipalVariation first move %s at ply %d with score %+d%n",
				CLASS_NAME, pvar.getMove().toString(), pvar.getStart().getPly(), pvar.getScore() ));

		return pvar;
	}

	/**
	 * Calculate score for principal variation position
	 * 
	 * @param child
	 * @return score for specified position
	 */
	abstract double pvScore(MCTSPosition child);
	
}
