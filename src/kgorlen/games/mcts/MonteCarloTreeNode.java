/**
 * 
 */
package kgorlen.games.mcts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import kgorlen.games.DepthFirst;
import kgorlen.games.GamePosition;
import kgorlen.games.Log;
import kgorlen.games.Move;
import kgorlen.games.MoveGenerator;
import kgorlen.games.Position;
import kgorlen.games.ScoreType;
import kgorlen.games.TTEntry;
import kgorlen.games.Variation;

/**
 * @author Keith gorlen@comcast.net
 * 
 * References:
 * https://jeffbradberry.com/posts/2015/09/intro-to-monte-carlo-tree-search/
 * http://www.cameronius.com/research/mcts/about/index.html
 * http://scalab.uc3m.es/~seminarios/seminar11/slides/lucas2.pdf
 * http://mcts.ai/code/java.html
 * https://spin.atomicobject.com/2015/12/12/monte-carlo-tree-search-algorithm-game-ai/
 * https://github.com/PetterS/monte-carlo-tree-search
 * https://github.com/memo/ofxMSAmcts
 *
 */
public abstract class MonteCarloTreeNode implements GamePosition {
	private static final Logger LOGGER = Log.LOGGER;
	private static final String CLASS_NAME = DepthFirst.class.getName();
	
	protected static Random randNum = new Random(424242424242424247L);  // Fixed seed for debugging
    protected static int rootPly;	// ply at root of search tree
    protected static int AISide;	// 1 = X/black to play (even ply), -1 = O/red to play (odd ply)

    protected Move move;			// Move from parent to this child
    private List<MonteCarloTreeNode> children;
    private int score;
    private int visits;

    static public void initialize(Position root) {
    	if (LOGGER.getLevel() != Level.OFF)
    		randNum = new Random(424242424242424247L);  // Fixed seed for debugging

    	MonteCarloTreeNode.rootPly = root.getPly();
    	AISide = 1 - (rootPly<<1 & 2);
    }
    
    /**
     * Constructor
     */
    public MonteCarloTreeNode() {
    	reset();
    }
    
    /**
     * Copy constructor
     * 
     * @param n node to copy.  Note: children are shallow copies.
     */
    public MonteCarloTreeNode(MonteCarloTreeNode n) {
    	children = n.children;
    	visits = n.visits;
    	score = n.score;
    }
    
    public void reset() {
    	move = null;
    	children = null;
    	visits = 0;
    	score = 0;    	
    }
    
    public Move getMove() {
    	return move;
    }
    
    public int getScore() {
    	return score;
    }
    
    public int getVisits() {
    	return visits;
    }
 
    /**
     * @return	standard deviation of win percentage
     * 			as per Rule of Sample Proportions.
     * 			Note: 95% confidence interval is +- 2*stdDev()
     */
    public double getStdDev() {
    	double prop = ((double) score)/visits;
    	double stddev = Math.sqrt((prop * (1-prop))/visits);
    	return 100*stddev/visits;
    }

    /**
     * @return 95% confidence interval of win percentage (+- 2 * standard deviation)
     */
    public double getcConfInt95() {
    	return 2*getStdDev();
    }
    
    public void addChild(MonteCarloTreeNode child) {
    	if (children == null) children = new LinkedList<MonteCarloTreeNode>();
    	children.add(child);
    	assert (children.size() <=7): String.format("0x%s.addChild(0x%s): Too many children", this, child);
    }
    
    public boolean isLeaf() {
        return children == null || children.size() == 0;
    }

    /**
     * Select child node to expand
     * 
     * @param visited list of nodes visited from root to this node
     * @return node to expand, or null if at least one child a win or all children draws
     */
    private static final double C = Math.sqrt(2);	// Upper confidence bound for Trees (UCT) coefficient
    private static final double EPSILON = 1e-6;		// Small random number 0-EPSILON to break ties in unexpanded nodes

    public MonteCarloTreeNode select(List<MonteCarloTreeNode> visited) throws MCTSSearchException {
        MonteCarloTreeNode selected = null;
        double bestValue = Double.MIN_VALUE;
        for (MonteCarloTreeNode c : children) {
        	if (c.isWin()) {
        		visited.add(c);
        		updateStats(visited, c.scoreWin());
        		if (this == visited.get(0))
        			throw new MCTSSearchException("Win from root position");
        		return null;
        	}
        	if (c.isDraw()) {
        		visited.add(c);
        		updateStats(visited, c.scoreDraw());
        		continue;
        	}
            double uctValue =
                    c.score / (c.visits + EPSILON) +
                            C * Math.sqrt(Math.log(visits+1) / (c.visits + EPSILON)) +
                            randNum.nextDouble() * EPSILON;
            if (uctValue > bestValue) {
                selected = c;
                bestValue = uctValue;
            }
        }
        if (selected == null && this == visited.get(0))
        	throw new MCTSSearchException("Draw from root position");
        return selected;
    }

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#evaluate()
	 */
	@Override
	public int evaluate() {
		MonteCarloTreeNode parent = this;
		while (true) {
	        List<Move> moves = new ArrayList<Move>();
			MoveGenerator gen = parent.moveGenerator();		
			while (gen.hasNext()) {
				Move move = gen.next();
				MonteCarloTreeNode child = (MonteCarloTreeNode) parent.copy();
				child.makeMove(move);

								if (child.isWin()) {
					final MonteCarloTreeNode p = parent;
					LOGGER.finest(() -> String.format(
							"Winning move %s at ply %d on 0x%h (parent 0x%h):%n%s",
							move.toString(), child.getPly(), System.identityHashCode(child),
							System.identityHashCode(p), child.toString() ));
					return child.scoreWin();
				}
				
				moves.add(move);
			}

			MonteCarloTreeNode child = (MonteCarloTreeNode) parent.copy();
			Move randMove = moves.get(randNum.nextInt(moves.size()));

			final MonteCarloTreeNode p = parent;
			LOGGER.finest(() -> String.format(
				"Playing move %s at ply %d on 0x%h (parent 0x%h):%n%s",
				randMove.toString(), child.getPly(), System.identityHashCode(child),
				System.identityHashCode(p), child.toString() ));

			child.makeMove(randMove);
			if (child.isDraw()) return child.scoreSign() * child.scoreDraw();
			parent = child;
		}
	}
	
    /**
     * References:
     * [1] http://ccg.doc.gold.ac.uk/teaching/ludic_computing/ludic16.pdf
     * [2] https://jeffbradberry.com/posts/2015/09/intro-to-monte-carlo-tree-search/
     * 
     * [1] Score for node includes both wins and losses; all nodes updated.
     * [2] Score for node includes only wins; winning player nodes updated.
     * 
     * @param visited List of nodes visited during selection and expansion
     * @param score from playout: -1 = root player loss, 0 = draw, +1 = root player win
     */
    static public void updateStats(List<MonteCarloTreeNode> visited, int score) {
        for (MonteCarloTreeNode node : visited) {
        	assert node != null : "Null node visited";
        	assert node.getMove() != null || node.getPly() == rootPly : "Null move";
        	assert score >= -1 && score <= 1 :
        		String.format("Score %d out of range at ply %d", score, node.getPly());

        	node.visits++;
	        int nodeScore = score * node.scoreSign();	// win/loss * winner/loser at this node
	        node.score += nodeScore;		// See reference [1] above
//	        if (nodeScore > 0) {			// See reference [2] above
//		        node.score += nodeScore;
//		    }
	        assert node.score <= node.visits :
	        	String.format("Score (%d) > visits (%d) at ply %d",
	        		node.score, node.visits, node.getPly());
	        LOGGER.finer(() -> node.getPly() == rootPly ?
	        		String.format("Updating stats for root at ply %d: visits=%d, score=%d, nodeScore=%d, total=%d%n",
			        		node.getPly(), node.visits, score, nodeScore, node.score) :
	        		String.format("Updating stats for move %s at ply %d: visits=%d, score=%d, nodeScore=%d, total=%d%n",
	        				node.getMove().toString(), node.getPly(), node.visits, score, nodeScore, node.score) );
        }
    }

		/**
		 * References:
		 * [1] http://ccg.doc.gold.ac.uk/teaching/ludic_computing/ludic16.pdf
		 * [2] https://jeffbradberry.com/posts/2015/09/intro-to-monte-carlo-tree-search/
		 * [3] https://github.com/theKGS/MCTS
		 * 
		 * [1] Best move = highest UCT--but why include regret term?
		 * [2] Best move = highest win percent
		 * [3] Best move = most visited
		 * 
		 * @return principal Variation (moves w/ highest win percent as per [2])
		 */
		public Variation getPrincipalVariation() {
			LOGGER.finer(() -> String.format("Entering %s.getPrincipalVariation%n", CLASS_NAME));

			Variation pvar = newVariation();
	        MonteCarloTreeNode parent = this;
	        pvar.setStart(this);

	        while (!parent.isLeaf()) {
	        	final MonteCarloTreeNode logParent = parent;
	        	LOGGER.finer(() -> String.format(
	        			"  Finding best move at ply %d ...%n", logParent.getPly()));

	        	MonteCarloTreeNode bestChild = null;
		        float bestAvg = Float.NEGATIVE_INFINITY;

		        for (MonteCarloTreeNode child : parent.children) {
			        assert child.move != null : "Null move at ply %d";

			        LOGGER.finer(() -> String.format(
			        		"    %s total/visits: %d/%d",
		        			child.getMove().toString(), child.score, child.visits));

			        if (child.visits == 0) {
		        		LOGGER.finer("\n");
		        		continue;	        	
		        	}
			        float avgValue =((float) child.score)/child.visits;
		            LOGGER.finer(() -> String.format(" (%%%.0f)%n", avgValue*100));

		            if (avgValue > bestAvg) {
		                bestChild = child;
		                bestAvg = avgValue;
		            }
		        }	        
	
		        if (bestChild == null) {
		        	assert pvar.size() > 0 : "getPrincipalVariation failed";
		        	return pvar;
		        }
	
		        final MonteCarloTreeNode logChild = bestChild;
		        final float logAvg = bestAvg;
		        LOGGER.finer(() -> String.format(
		        		"  ... Best move at ply %d is %s (%%%.0f)%n",
		        		logParent.getPly(), logChild.getMove().toString(), logAvg*100));
		        
		        if (pvar.size() == 0) pvar.setScore((int) Math.round(bestAvg*100));
	    	    pvar.add(bestChild.move);
		        parent = bestChild;
		        assert (pvar.size() <= 6*7) : "Variation size exceeded";
	        }

	        LOGGER.finer(() -> String.format(
	        		"Exiting %s.getPrincipalVariation first move %s with score %d at ply %d%n",
	        		CLASS_NAME, pvar.getMove().toString(), pvar.getScore(), pvar.getStart().getPly() ));

	        return pvar;
		}
	
		/**
		 * The principal variation is the sequence of moves to the
		 * most-played positions at each ply (UCT selects the positions
		 * with the most wins to play most often).
		 * 
		 * @return Principal Variation from this node based on most visits
		 */
//		public Variation getPrincipalVariation() {
//			Variation pvar = newVariation();
//	        MonteCarloTreeNode parent = this;
//	        while (!parent.isLeaf()) {
//	        	System.out.printf("Finding best move at ply %d ...%n", parent.getPly());
//	        	MonteCarloTreeNode bestChild = null;
//		        int mostPlays = -1;
//		        for (MonteCarloTreeNode child : parent.children) {
//			        assert child.move != null : "Null move at ply %d";
//
//		        	System.out.printf("    %s total/visits: %d/%d",
//		        			child.getMove().toString(), child.totValue, child.nVisits);
//		        	if (child.nVisits > 0) {
//			            System.out.printf(" (%%%.0f)", 100.0*child.totValue/child.nVisits);
//		        	}
//	        		System.out.println("");
//		        	
//		            if (child.visits > mostPlays) {
//		                bestChild = child;
//		                mostPlays = child.visits;
//		            }
//		        }	        
//		        assert bestChild != null : "getPrincipalVariation() failed";
//		        System.out.printf("... Best move at ply %d is %s (%d)%n",
//		        		parent.getPly(), bestChild.getMove().toString(), mostPlays);
//		        assert pvar.size() > 0 || mostPlays > 0 : "";
//		        
//		        if (pvar.size() == 0) pvar.setScore(mostPlays);
//		        if (mostPlays == 0) return pvar;	// All children at this ply unexpanded
//		        
//	    	    pvar.add(bestChild.move);
//		        parent = bestChild;
//		        assert (pvar.size() <= MAX_DEPTH) : "Variation size exceeded";
//	        }
//			return pvar;
//		}

		/* (non-Javadoc)
	 * @see kgorlen.games.Position#newTTentry(int, int, kgorlen.games.Move)
	 */
	@Override
	public abstract TTEntry newTTentry(int depth, int score, Move bestMove);

	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#newTTentry(int, kgorlen.games.ScoreType, int, kgorlen.games.Move)
	 */
	@Override
	public TTEntry newTTentry(int depth, ScoreType scoreType, int score, Move bestMove) {
		return newTTentry(depth, score, bestMove);
	};
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#getPly()
	 */
	@Override
	public abstract int getPly();
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#isValidMove(kgorlen.games.Move)
	 */
	@Override
	public abstract boolean isValidMove(Move m);
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#makeMove(kgorlen.games.Move)
	 */
	@Override
	public abstract void makeMove(Move m);
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#moveGenerator(kgorlen.games.Move[], boolean)
	 */
	@Override
	public MoveGenerator moveGenerator(Move[] killers) {
		return moveGenerator();
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#moveGenerator()
	 */
	@Override
	public MoveGenerator moveGenerator() {
		return moveGenerator();
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#newVariation()
	 */
	@Override
	public abstract Variation newVariation();
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#scoreSign()
	 */
	@Override
	public abstract int scoreSign();
	
	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#copy()
	 */
	@Override
	public abstract GamePosition copy();
	
	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#sideToMove()
	 */
	@Override
	public abstract String sideToMove();
	
	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#isWin()
	 */
	@Override
	public abstract boolean isWin();
	
	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#isDraw()
	 */
	@Override
	public abstract boolean isDraw();
	
	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#scoreWin()
	 */
	@Override
	public abstract int scoreWin();
	
	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#scoreDraw()
	 */
	@Override
	public abstract int scoreDraw();

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public abstract int hashCode();

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public abstract boolean equals(Object obj);

}
