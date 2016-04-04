package kgorlen.games;

import java.util.LinkedList;
import java.util.List;

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

	public MonteCarloTreeSearch(boolean debug) {
		this.debug = debug;
	}
	
	public MonteCarloTreeSearch() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public TreeSearch search(Position root, int limit) {
		assert !((MonteCarloTreeNode) root).isWin() && !((MonteCarloTreeNode)root).isDraw() :
			"root is terminal position";
		if (debug) MonteCarloTreeNode.setSeed();	// set constant seed for debugging
		setRoot(root);
		MonteCarloTreeNode.setAISide(root.getPly());
		elapsedTime();
		
		try {
			search:
			for (int i=0; i < limit; i++) {		// change limit to elapsed time
		        List<MonteCarloTreeNode> visited = new LinkedList<MonteCarloTreeNode>(); // nodes visited for updateStats()
		        MonteCarloTreeNode node = (MonteCarloTreeNode) root;
		        visited.add(node);
		        
	// Select child position to expand
		        while (!node.isLeaf()) {
		            node = node.select(visited, debug);
		            if (node == null) { // one child is win or all children draws
		            	continue search; 
		            }
		            visited.add(node);
		            if (debug) {
		            	System.out.printf("Visiting 0x%h at ply %d:%n",
		            			System.identityHashCode(node), node.getPly());
		            	node.print();
		            };
		        }
	
	// Expand: Add all children to tree and select one to play out
	    		MoveGenerator gen = node.moveGenerator(debug);		
	    		while (gen.hasNext()) {
	    			Move move = gen.next();
	    			MonteCarloTreeNode child = (MonteCarloTreeNode) node.copy();
	    			child.makeMove(move);
	    			node.addChild(child);
	    		}
		        MonteCarloTreeNode newNode = node.select(visited, debug);
		        if (newNode == null) continue search; // one child is win or all children are draws
		        visited.add(newNode);
	            if (debug) {
	            	System.out.printf("Expanding 0x%h at ply %d:%n",
	            			System.identityHashCode(newNode), newNode.getPly());
	            	newNode.print();
	            };
		        
	// Simulate: Play out (random) moves until win/loss/draw       
		        int score = newNode.evaluate(debug);
		        positionsSearched++;
		        
	// Update: Update statistics for visited nodes with playout results      
		        MonteCarloTreeNode.updateStats(visited, score, debug);
			}
		}catch(MCTSSelectException e) {
			if (debug) {
				System.out.println(e);
			}
		}
		elapsedTime();
		return this;
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
