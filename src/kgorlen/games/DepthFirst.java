/**
 * 
 */
package kgorlen.games;
import kgorlen.games.Position;

/**
 * @author Keith gorlen@comcast.net
 *
 */
public class DepthFirst extends TreeSearch {
	
	public DepthFirst(boolean debug) {
		this.debug =debug;
	}
	
	/**
	 * @param parent	Position to be searched
	 * @param depth		maximum depth to search
	 * @param indent	String prepended to debug output lines
	 * @return			maximum score
	 */
	public int search(Position parent, int depth, String indent) {
		int score;				// score for *parent* Position
		
		if (debug) {
			System.out.format("%s{ DepthFirst(%d) position:%n", indent, depth);
			parent.print(indent);
		}

		TTEntry ttEntry = getTTEntry(parent);
		if (ttEntry != null) {
			score = ttEntry.getScore();
			ttHits++;
			if (debug) System.out.format("%s} DepthFirst(%d) returning transposition score=%d%n",
					indent, depth, score);
			return score;
		}
		
		score = parent.evaluate();

		MoveGenerator gen = parent.moveGenerator(debug);		
		if (depth == 0 || !gen.hasNext()) {
			if (debug) System.out.format("%s} DepthFirst(%d) returning evaluation score=%d%n",
					indent, depth, score);
			return score;			
		}

		int bestScore = score;		// Deeper search may not improve score
		Move bestMove = null;
		while (gen.hasNext()) {
			Move move = gen.next();
			positionsSearched++;
			Position child = parent.copy();
			child.makeMove(move);
			if (debug) {
				System.out.format("%sSearch move %s ...%n", indent, move.toString());
			}
			score = search(child, depth-1, indent + "    ");
			if (debug) {
				System.out.format("%s... Move %s score=%d%n", indent, move.toString(), score);
			}
			if (score > bestScore) {
				bestScore = score;
				bestMove = move;
				ttEntry = parent.newTTentry(depth, bestScore, bestMove);
				putTTEntry(parent, ttEntry);
				if (debug) {
					System.out.format("%sPrincipal variation:%n", indent);
					Variation pvar = getPrincipalVariation(parent);
					pvar.print(parent, indent);
				}
			}
		}

		if (debug) System.out.format("%s} DepthFirst.search(%d) returning search score=%d%n",
				indent, depth, bestScore);
		return bestScore;
	}
	
	/**
	 * @param parent	Position to be searched
	 * @param depth		maximum depth to search
	 * @return			DepthFirst instance
	 */
	public DepthFirst search(Position root, int depth) {
		setRoot(root);
		elapsedTime();
		search(root, depth, "");
		elapsedTime();
		return this;		
	}

}
