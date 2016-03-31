/**
 * 
 */
package kgorlen.games;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class MonteCarloTreeNode implements GamePosition {
    static final double C = Math.sqrt(2);	// Upper confidence bound for Trees (UCT) coefficient
    static final double EPSILON = 1e-6;
	
	protected static Random randNum = new Random(424242424242424247L);  // Fixed seed for debugging
    protected static int rootPly;	// ply at root of search tree
    protected static int AISide;	// 1 = X/black to play (even ply), -1 = O/red to play (odd ply)

    protected Move move;			// Move from parent to this child
    private List<MonteCarloTreeNode> children;
    private int totValue;
    private int nVisits;

    static public void setSeed() {
    	randNum = new Random(424242424242424247L);  // Fixed seed for debugging
    }

    static public void setAISide(int rootPly) {
    	MonteCarloTreeNode.rootPly = rootPly;
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
    	nVisits = n.nVisits;
    	totValue = n.totValue;
    }
    
    public void reset() {
    	move = null;
    	children = null;
    	nVisits = 0;
    	totValue = 0;    	
    }
    
    public Move getMove() {
    	return move;
    }
    
    public int getWins() {
    	return totValue;
    }
    
    public int getVisits() {
    	return nVisits;
    }
 
    /**
     * @return	standard deviation of win percentage
     * 			as per Rule of Sample Proportions.
     * 			Note: 95% confidence interval is +- 2*stdDev()
     */
    public double stdDev() {
    	double prop = ((double) totValue)/nVisits;
    	double stddev = Math.sqrt((prop * (1-prop))/nVisits);
    	return 100*stddev/nVisits;
    }

    /**
     * @return 95% confidence interval of win percentage (+- 2 * standard deviation)
     */
    public double confInt95() {
    	return 2*stdDev();
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
     * @param debug switch
     * @return node to expand, or null if at least one child a win or all children are draws
     */
    public MonteCarloTreeNode select(List<MonteCarloTreeNode> visited, boolean debug) {
        MonteCarloTreeNode selected = null;
        double bestValue = Double.MIN_VALUE;
        for (MonteCarloTreeNode c : children) {
        	if (c.isWin()) {
        		visited.add(c);
        		updateStats(visited, c.scoreWin(), debug);
        		return null;
        	}
        	if (c.isDraw()) continue;
            double uctValue =
                    c.totValue / (c.nVisits + EPSILON) +
                            C * Math.sqrt(Math.log(nVisits+1) / (c.nVisits + EPSILON)) +
                            randNum.nextDouble() * EPSILON;
            // small random number to break ties randomly in unexpanded nodes
            // System.out.println("UCT value = " + uctValue);
            if (uctValue > bestValue) {
                selected = c;
                bestValue = uctValue;
            }
        }
        // System.out.println("Returning: " + selected);
        return selected;
    }

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#evaluate()
	 */
	@Override
	public int evaluate(boolean debug) {
		MonteCarloTreeNode parent = this;
		while (true) {
	        List<Move> moves = new ArrayList<Move>();
			MoveGenerator gen = parent.moveGenerator();		
			while (gen.hasNext()) {
				Move move = gen.next();
				MonteCarloTreeNode child = (MonteCarloTreeNode) parent.copy();
				child.makeMove(move);

				if (debug) {
					System.out.printf("Checking move %s for win at ply %d on 0x%h (parent 0x%h):%n",
							move.toString(), child.getPly(), System.identityHashCode(child), System.identityHashCode(parent));
					child.print();
				}
				if (child.isWin()) return child.scoreWin();
				
				moves.add(move);
			}

			MonteCarloTreeNode child = (MonteCarloTreeNode) parent.copy();
			Move randMove = moves.get(randNum.nextInt(moves.size()));
			if (debug) {
				System.out.printf("Playing move %s at ply %d on 0x%h (parent 0x%h):%n",
						randMove.toString(), child.getPly(), System.identityHashCode(child), System.identityHashCode(parent));
				parent.print();
			}
			child.makeMove(randMove);
			if (child.isDraw()) return child.scoreSign() * child.scoreDraw();
			parent = child;
		}
	}
	
    static public void updateStats(List<MonteCarloTreeNode> visited, int score, boolean debug) {
        for (MonteCarloTreeNode node : visited) {
        	assert node != null : "Null node visited";
        	assert node.getMove() != null || node.getPly() == rootPly : "Null move";

        	node.nVisits++;
	        int nodeScore = score * node.scoreSign();		// win/loss * winner/loser at this node
	        if (nodeScore > 0) node.totValue += nodeScore;	// Increase value if win for player at this ply
	        if (debug) {
	        	if (node.getPly() == rootPly)
		        	System.out.printf("Updating stats for root at ply %d: visits=%d, score=%d, nodeScore=%d, total=%d%n",
			        		node.getPly(), node.nVisits, score, nodeScore, node.totValue);
	        	else
	        		System.out.printf("Updating stats for move %s at ply %d: visits=%d, score=%d, nodeScore=%d, total=%d%n",
	        				node.getMove().toString(), node.getPly(), node.nVisits, score, nodeScore, node.totValue);
	        }
        }
    }

	public Variation getPrincipalVariation() {
		Variation pvar = newVariation();
        MonteCarloTreeNode parent = this;
        while (!parent.isLeaf()) {
//        	System.out.printf("Finding best move at ply %d ...%n", parent.getPly());
        	MonteCarloTreeNode bestChild = null;
	        float bestAvg = Float.NEGATIVE_INFINITY;
	        for (MonteCarloTreeNode child : parent.children) {
		        assert child.move != null : "Null move at ply %d";
//	        	System.out.printf("    %s total/visits: %d/%d",
//	        			child.getMove().toString(), child.totValue, child.nVisits);
	        	if (child.nVisits == 0) {
//	        		System.out.println("");
	        		continue;	        	
	        	}
	            float avgValue =((float) child.totValue)/child.nVisits;
//	            System.out.printf(" (%%%.0f)%n", avgValue*100);
	            if (avgValue > bestAvg) {
	                bestChild = child;
	                bestAvg = avgValue;
	            }
	        }	        

	        if (bestChild == null) {
	        	assert pvar.size() > 0 : "getPrincipalVariation failed";
	        	return pvar;
	        }

//	        System.out.printf("... Best move at ply %d is %s (%%%.0f)%n",
//	        		parent.getPly(), bestChild.getMove().toString(), bestAvg*100);
	        
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
	public abstract MoveGenerator moveGenerator(boolean debug);
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#moveGenerator(kgorlen.games.Move[], boolean)
	 */
	@Override
	public MoveGenerator moveGenerator(Move[] killers, boolean debug) {
		return moveGenerator(debug);
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#moveGenerator()
	 */
	@Override
	public MoveGenerator moveGenerator() {
		return moveGenerator(false);
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#newVariation()
	 */
	@Override
	public abstract Variation newVariation();
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#print(java.lang.String)
	 */
	@Override
	public abstract void print(String indent);
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#print()
	 */
	@Override
	public abstract void print();
	
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
