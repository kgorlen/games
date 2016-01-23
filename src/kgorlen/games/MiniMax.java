package kgorlen.games;

import kgorlen.games.MoveGenerator;
import kgorlen.games.GamePosition;
import kgorlen.games.Variation;

public class MiniMax extends TreeSearch {
	public MiniMax(Variation pvar, boolean debug) {
		this.principalVariation = pvar;
		this.debug =debug;
	}
	
	/**
	 * Performs a full minimax search of a GamePosition
	 * References:
	 * 		https://en.wikipedia.org/wiki/Minimax
	 * 		https://chessprogramming.wikispaces.com/Principal+variation
	 * 
	 * @param parent	GamePosition to be searched
	 * @param depth		maximum depth to search
	 * @param maximize	true to maximize score, false to minimize
	 * @param pvar		updated with principal Variation
	 * @param indent	String prepended to debug output lines
	 * @return			maximum/minimum score
	 */
	public int search(GamePosition parent, int depth, boolean maximize,
			Variation pvar, String indent) {
	
		int score;				// score for *parent* GamePosition
		
		if (debug) {
			System.out.format("%s{ MiniMax.search(%s) maximize=%b, position:%n",
					indent, parent.sideToMove(), maximize);
			parent.print(indent);
		}

		if (parent.isWin()) {
			score = parent.scoreWin();
			pvar.reset();
			if (debug) System.out.format("%s} MiniMax.search(%s) returning win score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
		
		if (parent.isDraw()) {
			score = parent.scoreDraw();
			pvar.reset();
			if (debug) System.out.format("%s} MiniMax.search(%s) returning draw score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
			
		if (depth == 0) {
			score = parent.evaluate();
			pvar.reset();
			if (debug) System.out.format("%s} MiniMax.search(%s) returning evaluation score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
		
		MoveGenerator gen = parent.moveGenerator(debug);		
		int bestValue;		// "best" = highest if maximizing, lowest if minimizing

		if (maximize) {
			bestValue = -999999999;	// -"infinity"
			while (gen.hasNext()) {
				Move move = gen.next();
				positionsSearched++;
				GamePosition child = parent.copy();
				child.makeMove(move);
				Variation var = parent.variation();
				score = search(child, depth-1, false, var, indent + "    ");
				if (score > bestValue) {
					bestValue = score;
					pvar.addMoves(score, move, var);
					if (debug) {
						pvar.print(parent, indent);
					}
				}
			}
		} else {	// minimize
			bestValue = 999999999;	// +"infinity"
			while (gen.hasNext()) {
				Move move = gen.next();
				positionsSearched++;
				GamePosition child = parent.copy();
				child.makeMove(move);
				Variation var = parent.variation();
				score = search(child, depth-1, true, var, indent + "    ");
				if (score < bestValue) {
					bestValue = score;
					pvar.addMoves(score, move, var);
					if (debug) {
						pvar.print(parent, indent);
					}
				}
			}
		}
		
		if (debug) System.out.format("%s} MiniMax.search(%s) returning search score=%d%n",
				indent, parent.sideToMove(), bestValue);
		return bestValue;
	}

	/**
	 * @param root		root GamePosition to be searched
	 * @param maxDepth	maximum depth to search
	 * @return			MiniMax search results
	 */
	public MiniMax search(GamePosition root, int maxDepth) {
		elapsedTime();
		search(root, maxDepth, root.scoreSign() > 0, principalVariation, "");
		elapsedTime();
		return this;		
	}
}
