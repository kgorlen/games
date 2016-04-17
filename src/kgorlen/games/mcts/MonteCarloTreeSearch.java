package kgorlen.games.mcts;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import kgorlen.games.Log;
import kgorlen.games.Move;
import kgorlen.games.MoveGenerator;
import kgorlen.games.Position;
import kgorlen.games.TreeSearch;
import kgorlen.games.Variation;

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
public class MonteCarloTreeSearch extends TreeSearch {
	private static final Logger LOGGER = Log.LOGGER;
	private static final String CLASS_NAME = MonteCarloTreeSearch.class.getName();

	@Override
	public Variation search(Position root, int limit) {
		assert !((MonteCarloTreeNode) root).isWin() && !((MonteCarloTreeNode)root).isDraw() :
			"root is terminal position";
		setRoot(root);
		MonteCarloTreeNode.initialize(root);
		elapsedTime();
		
		try {
			search:
			for (int i=0; i < limit; i++) {		// change limit to elapsed time
		        List<MonteCarloTreeNode> visited = new LinkedList<MonteCarloTreeNode>(); // nodes visited for updateStats()
		        MonteCarloTreeNode node = (MonteCarloTreeNode) root;
		        visited.add(node);
		        
	// Select child position to expand
		        while (!node.isLeaf()) {
		        	final MonteCarloTreeNode logNode = node = node.select(visited);
		            if (node == null) { // one child is win or all children draws
		            	continue search; 
		            }
		            visited.add(node);
		            LOGGER.fine(() -> String.format("Visiting 0x%h at ply %d:%n%s",
		            			System.identityHashCode(logNode), logNode.getPly(), logNode.toString() ));
		        }
	
	// Expand: Add all children to tree and select one to play out
	    		MoveGenerator gen = node.moveGenerator();		
	    		while (gen.hasNext()) {
	    			Move move = gen.next();
	    			MonteCarloTreeNode child = (MonteCarloTreeNode) node.copy();
	    			child.makeMove(move);
	    			node.addChild(child);
	    		}
		        MonteCarloTreeNode newNode = node.select(visited);
		        if (newNode == null) continue search; // one child is win or all children are draws
		        visited.add(newNode);
		        LOGGER.fine(() -> String.format("Expanding 0x%h at ply %d:%n%s",
	            			System.identityHashCode(newNode), newNode.getPly(), newNode.toString() ));
		        
	// Simulate: Play out (random) moves until win/loss/draw       
		        int score = newNode.evaluate();
		        positionsSearched++;
		        
	// Update: Update statistics for visited nodes with playout results      
		        MonteCarloTreeNode.updateStats(visited, score);
			}
		}catch(MCTSSearchException e) {
			LOGGER.fine(() -> e.toString());
		}
		elapsedTime();
		logStatistics(CLASS_NAME);
		Variation pvar = getPrincipalVariation();
		Variation.logPrincipalVariation(pvar, CLASS_NAME);
		return pvar;
	}

	/**
	 * @param start	starting Position of Variation
	 * @return		principal variation from start Position
	 */
	@Override
	public Variation getPrincipalVariation(Position start) {
		return ((MonteCarloTreeNode) start).getPrincipalVariation();
	}
	
	/**
	 * @param start	position searched
	 * @return		best move found from specified Position
	 */
	@Override
	public Move getMove(Position start) {
		return getPrincipalVariation(start).getMove();
	}
	
	/**
	 * @param start	position searched
	 * @return	score of specified Position
	 */
	
	public int getScore(Position start) {
		return getPrincipalVariation(start).getScore();
	}
	
}
