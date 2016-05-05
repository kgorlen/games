package kgorlen.games.mcts;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import kgorlen.games.Log;
import kgorlen.games.Move;
import kgorlen.games.MoveGenerator;

/**
 * @author Keith gorlen@comcast.net
 *
 * References:
 * https://jeffbradberry.com/posts/2015/09/intro-to-monte-carlo-tree-search/
 * http://www.cameronius.com/research/mcts/about/index.html
 * http://scalab.uc3m.es/~seminarios/seminar11/slides/lucas2.pdf
 * https://spin.atomicobject.com/2015/12/12/monte-carlo-tree-search-algorithm-game-ai/
 * https://github.com/PetterS/monte-carlo-tree-search
 */
public class MCTSClassic extends MCTS {
	private static final Logger LOGGER = Log.LOGGER;
	private static final String CLASS_NAME = MCTSClassic.class.getName();

	/**
	 * @param r instance of Random number generator
	 * @param ttCapacity Transposition HashMap initial capacity
	 */
	public MCTSClassic(Random r, int ttCapacity) {
		super(r,ttCapacity);
	}

	/**
	 * @param r instance of Random number generator
	 */
	public MCTSClassic(Random r) {
		super(r);
	}

	public MCTSClassic() {
		super();
	}

	@Override
	public int mcts(MCTSPosition root, int depth, String indent) throws MCTSSearchException {
		LOGGER.finer(() -> String.format(
				"{Entering %s.mcts at ply %d with score=%+d, visits=%d:%n%s",
				CLASS_NAME, root.getPly(), root.getScore(), root.visits, root.toString() ));

//      List<MCTSPosition> visited = new LinkedList<MCTSPosition>(); // nodes visited for updateStats()
        MCTSPosition parent = root;
        visited.add(parent);
        MCTSPosition child = null;
        
// Select leaf position to expand
        while (parent.children != null) {
        	child = select(parent);

        	final MCTSPosition p = parent;
        	final MCTSPosition c = child;
        	
        	if (child == null) {	// all children draws
            	LOGGER.finer(() -> String.format("  All moves from ply %d draw, cannot expand%n",
            			p.getPly(), p.toString("  ") ));
        		break;    		
        	}
        	if (child.isWin()) {	// one child is win
            	LOGGER.finer(() -> String.format("  Move %s to ply %d is win by %s, cannot expand%n",
            			c.getMove().toString(), c.getPly(), p.sideToMove() ));
        		break;			
        	}

        	LOGGER.finest(() -> String.format("  Visiting ply %d position:%n%s",
        			c.getPly(), c.toString() ));

        	parent = child;
        	visited.add(parent);
        	depth++;
        	indent += "  ";
        }

        final MCTSPosition bestParent = parent;
        
        if (bestParent.children == null) {	// parent is non-terminal leaf
        	bestParent.expand();
        	child = select(bestParent);
        }

        final MCTSPosition bestChild = child;
        
        if (bestChild == null) { 		// all children draws
    		for (MCTSPosition draw : bestParent.children) {
    			visited.add(draw);
    		}
    		updateStats(visited, 0);
    		if (depth == 0)
				throw new MCTSSearchException("Draw from root position");
			
			LOGGER.finer(() -> String.format(
					"}Exiting %s.mcts, all moves from ply %d draw, result=0%n",
					CLASS_NAME, bestParent.getPly() ));			
			return 0;
        }

    	if (bestChild.isWin()) {
    		visited.add(bestChild);
    		updateStats(visited, bestChild.scoreWin());
    		if (depth == 0)
    			throw new MCTSSearchException("Win from root position");

    		LOGGER.finer(() -> String.format(
					"}Exiting %s.mcts, move %s to ply %d is win, result=%d%n",
					CLASS_NAME, bestChild.getMove().toString(), bestChild.getPly(),
					root.scoreSign() * bestChild.scoreWin() ));			
    		return root.scoreSign() * bestChild.scoreWin();
    	}
        
// Simulate: Play out (random) moves until win/loss/draw       
        visited.add(bestChild);
        int score = bestChild.evaluate();
        positionsSearched++;
        
// Update: Update statistics for visited nodes with playout results      
        updateStats(visited, score);
		LOGGER.finer(() -> String.format(
				"}Exiting %s.mcts, playout result=%d%n",
				CLASS_NAME, root.scoreSign() * bestChild.scoreWin() ));			
        return root.scoreSign() * score;
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
	 * @param score from playout: -1 = X loss, 0 = draw, +1 = X win
	 */
	static public void updateStats(List<MCTSPosition> visited, int score) {
		assert score >= -1 && score <= 1 :
			String.format("Score %d out of range", score);

		for (MCTSPosition node : visited) {
			node.visits++;
			int nodeScore = score * node.scoreSign();
			node.updateScore(nodeScore);		// See reference [1] above
			//	        if (nodeScore > 0) {			// See reference [2] above
			//		        node.score += nodeScore;
			//		    }
			LOGGER.finer(() -> node.getMove() == null ?
					String.format("Updating stats at root ply %d: visits=%d, score=%d, nodeScore=%d, total=%d%n",
							node.getPly(), node.visits, score, nodeScore, node.getScore() ) :
					String.format("Updating stats for move %s to ply %d: visits=%d, score=%d, nodeScore=%d, total=%d%n",
							node.getMove().toString(), node.getPly(), node.visits, score, nodeScore, node.getScore() ));
		}
	}
	
	/* 
	 * Compute average score for principal variation move selection
	 * 
	 * (non-Javadoc)
	 * @see kgorlen.games.mcts.MCTS#pvScore(kgorlen.games.mcts.MCTSPosition)
	 */
	@Override
	double pvScore(MCTSPosition child) {
		return ((double) child.getScore())/child.visits;
	}

}
