/**
 * 
 */
package kgorlen.games.mcts;

/**
 * @author Keith gorlen@comcast.net
 *
 */
	import java.util.ArrayList;
	import java.util.LinkedList;
	import java.util.List;
	import java.util.Random;
import java.util.logging.Logger;

import kgorlen.games.GamePosition;
import kgorlen.games.Log;
import kgorlen.games.Move;
import kgorlen.games.MoveGenerator;
import kgorlen.games.ScoreType;
import kgorlen.games.TTEntry;
import kgorlen.games.TreeSearch;
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
	public abstract class MCTSPosition implements GamePosition {
		private final static Logger LOGGER = Log.LOGGER;
	    static final int MAX_DEPTH = 6*7;		// @TODO Specific to Connect4--generalize
		
		protected static Random randNum = new Random(424242424242424247L);  // Fixed seed for debugging
	    protected static int rootPly;	// ply at root of search tree
	    protected static int AISide;	// 1 = X/black to play (even ply), -1 = O/red to play (odd ply)

	    protected Move move;			// Move from parent to this child
	    List<MCTSPosition> children;
	    int score;
	    int visits;

	    static public void setSeed() {
	    	randNum = new Random(424242424242424247L);  // Fixed seed for debugging
	    }

	    static public void setAISide(int rootPly) {
	    	MCTSPosition.rootPly = rootPly;
	    	AISide = 1 - (rootPly<<1 & 2);
	    }
	    
	    /**
	     * Constructor
	     */
	    public MCTSPosition() {
	    	reset();
	    }
	    
	    /**
	     * Copy constructor
	     * 
	     * @param n node to copy.  Note: children are shallow copies.
	     */
	    public MCTSPosition(MCTSPosition n) {
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
	    
	    public void addChild(MCTSPosition child) {
	    	if (children == null) children = new LinkedList<MCTSPosition>();
	    	children.add(child);
	    	assert (children.size() <=7): String.format("0x%s.addChild(0x%s): Too many children", this, child);
	    }
	    
	    public boolean isLeaf() {
	        return children == null || children.size() == 0;
	    }

	    /**
	     * Select child node to expand
	     * 
	     * EPSILON small random number 0-EPSILON to break ties in unexpanded nodes,
	     * i.e. when visits = 0.  1/EPSILON > SCORE_INFINITY
	     * 
	     * @param visited list of nodes visited from root to this node
	     * @param debug switch
	     * @return node to expand, or null if at least one child a win or all children draws
	     */
	    private static final double C = Math.sqrt(2);	// Upper confidence bound for Trees (UCT) coefficient
	    private static final double EPSILON = 1.0/(10.0*TreeSearch.SCORE_INFINITY);

	    public MCTSPosition select() throws MCTSSearchException {
	        MCTSPosition selected = null;
	        double bestValue = Double.MIN_VALUE;
	        for (MCTSPosition c : children) {
	        	if (c.isWin()) {
	        		return c;
	        	}
	        	if (c.isDraw()) {
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
	        return selected;
	    }

		/* (non-Javadoc)
		 * @see kgorlen.games.Position#evaluate()
		 */
		@Override
		public int evaluate() {
			MCTSPosition parent = this;
			while (true) {
		        List<Move> moves = new ArrayList<Move>();
				MoveGenerator gen = parent.moveGenerator();		
				while (gen.hasNext()) {
					Move move = gen.next();
					MCTSPosition child = (MCTSPosition) parent.copy();
					child.makeMove(move);

					final MCTSPosition p = parent;
					LOGGER.finer(() -> String.format(
							"Checking move %s for win at ply %d on 0x%h (parent 0x%h):%n%s",
							move.toString(), child.getPly(), System.identityHashCode(child), System.identityHashCode(p),
							child.toString() ));

					if (child.isWin()) return child.scoreWin();
					
					moves.add(move);
				}

				MCTSPosition child = (MCTSPosition) parent.copy();
				Move randMove = moves.get(randNum.nextInt(moves.size()));
				
				final MCTSPosition p = parent;
				LOGGER.finer(() -> String.format(
						"Playing move %s at ply %d on 0x%h (parent 0x%h):%n%s",
						randMove.toString(), child.getPly(), System.identityHashCode(child), System.identityHashCode(p),
						p.toString() ));

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
	    static public void updateStats(List<MCTSPosition> visited, int score) {
	        for (MCTSPosition node : visited) {
	        	assert node != null : "Null node visited";
	        	assert node.getMove() != null || node.getPly() == rootPly : "Null move";
	        	assert score >= -1 && score <= 1 :
	        		String.format("Score %d out of range at ply %d", score, node.getPly());

	        	node.visits++;
		        int nodeScore = score * node.scoreSign();	// win/loss * winner/loser at this node
		        node.score += nodeScore;		// See reference [1] above
//		        if (nodeScore > 0) {			// See reference [2] above
//			        node.score += nodeScore;
//			    }
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
				Variation pvar = newVariation();
		        MCTSPosition parent = this;
		        while (!parent.isLeaf()) {
		        	System.out.printf("Finding best move at ply %d ...%n", parent.getPly());
		        	MCTSPosition bestChild = null;
			        float bestAvg = Float.NEGATIVE_INFINITY;
			        for (MCTSPosition child : parent.children) {
				        assert child.move != null : "Null move at ply %d";
			        	System.out.printf("    %s total/visits: %d/%d",
			        			child.getMove().toString(), child.score, child.visits);
			        	if (child.visits == 0) {
			        		System.out.println("");
			        		continue;	        	
			        	}
			            float avgValue =((float) child.score)/child.visits;
			            System.out.printf(" (%%%.0f)%n", avgValue*100);
			            if (avgValue > bestAvg) {
			                bestChild = child;
			                bestAvg = avgValue;
			            }
			        }	        
		
			        if (bestChild == null) {
			        	assert pvar.size() > 0 : "getPrincipalVariation failed";
			        	return pvar;
			        }
		
			        System.out.printf("... Best move at ply %d is %s (%%%.0f)%n",
			        		parent.getPly(), bestChild.getMove().toString(), bestAvg*100);
			        
			        if (pvar.size() == 0) pvar.setScore((int) Math.round(bestAvg*100));
		    	    pvar.add(bestChild.move);
			        parent = bestChild;
			        assert (pvar.size() <= 6*7) : "Variation size exceeded";
		        }
				return pvar;
			}
		
		/* (non-Javadoc)
		 * @see kgorlen.games.Position#newTTentry(int, int, kgorlen.games.Move)
		 */
		@Override
		public abstract TTEntry newTTentry(int depth, int score, Move bestMove);

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
		 * @see kgorlen.games.Position#moveGenerator(boolean)
		 */
		@Override
		public abstract MoveGenerator moveGenerator();
		
		/* (non-Javadoc)
		 * @see kgorlen.games.Position#moveGenerator(kgorlen.games.Move[], boolean)
		 */
		@Override
		public MoveGenerator moveGenerator(Move[] killers) {
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
		 * @see kgorlen.games.GamePosition#newTTentry(int, kgorlen.games.ScoreType, int, kgorlen.games.Move)
		 */
		@Override
		public abstract TTEntry newTTentry(int depth, ScoreType scoreType, int score, Move bestMove);
		
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
