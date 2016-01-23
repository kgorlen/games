/**
 * 
 */
package kgorlen.games;

/**
 * @author Keith gorlen@comcast.net
 *
 */
public class DepthFirst extends TreeSearch {
	
	public DepthFirst(Variation pvar, boolean debug) {
		this.principalVariation = pvar;
		this.debug =debug;
	}
	
	public int search(Position parent, int depth, Variation pvar, String indent) {
		int score;				// score for *parent* Position
		
		if (debug) {
			System.out.format("%s{ DepthFirst(%d) position:%n", indent, depth);
			parent.print(indent);
		}

		Variation var = transposition.get(parent);
		if (var != null) {
			score = pvar.setMoves(var);
			ttHits++;
			if (debug) System.out.format("%s} DepthFirst(%d) returning transposition score=%d%n",
					indent, depth, score);
			return score;
		}
		
		MoveGenerator gen = parent.moveGenerator(debug);		
		int bestValue = parent.evaluate();
		if (depth == 0 || !gen.hasNext()) {
			pvar.reset();
			if (debug) System.out.format("%s} DepthFirst(%d) returning evaluation score=%d%n",
					indent, depth, bestValue);
			return bestValue;			
		}

		while (gen.hasNext()) {
			Move move = gen.next();
			positionsSearched++;
			Position child = parent.copy();
			child.makeMove(move);
			var = parent.variation();
			score = search(child, depth-1, var, indent + "    ");
			var.addMove(score, move);
			transposition.put(parent, var);
			if (score > bestValue) {
				bestValue = score;
				pvar.setMoves(var);
				if (debug) {
					System.out.format("%sReaction %s score = %d%n", indent, move.toString(), score);
					pvar.print(parent, indent);
				}
			}
		}

		if (debug) System.out.format("%s} DepthFirst.search(%d) returning search score=%d%n",
				indent, depth, bestValue);
		return bestValue;
	}
}
