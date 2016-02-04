package kgorlen.games;

import kgorlen.games.MoveGenerator;
import kgorlen.games.Variation;

public class MiniMax extends TreeSearch {
	
	public MiniMax(boolean debug) {
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
	 * @param indent	String prepended to debug output lines
	 * @return			maximum/minimum score
	 */
	public int search(GamePosition parent, int depth, boolean maximize, String indent) {
	
		int score;				// score for *parent* GamePosition
		
		if (debug) {
			System.out.format("%s{ MiniMax.search(%s) maximize=%b, position:%n",
					indent, parent.sideToMove(), maximize);
			parent.print(indent);
		}

		TTEntry ttEntry = getTTEntry(parent);
		if (ttEntry != null) {
			score = ttEntry.getScore();
			ttHits++;
			if (debug) System.out.format("%s} MiniMax.search(%s) returning transposition score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
		
		if (parent.isWin()) {
			score = parent.scoreWin();
			if (debug) System.out.format("%s} MiniMax.search(%s) returning win score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
		
		if (parent.isDraw()) {
			score = parent.scoreDraw();
			if (debug) System.out.format("%s} MiniMax.search(%s) returning draw score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
			
		if (depth == 0) {
			score = parent.evaluate();
			if (debug) System.out.format("%s} MiniMax.search(%s) returning evaluation score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
		
		MoveGenerator gen = parent.moveGenerator(debug);		
		int bestScore;		// "best" = highest if maximizing, lowest if minimizing
		Move bestMove = null;

		if (maximize) {
			bestScore = -TreeSearch.SCORE_INFINITY;	// -"infinity"
			while (gen.hasNext()) {
				positionsSearched++;
				Move move = gen.next();
				GamePosition child = parent.copy();
				child.makeMove(move);
				score = search(child, depth-1, false, indent + "    ");
				if (score > bestScore) {
					bestScore = score;
					bestMove = move;
					ttEntry = parent.newTTentry(depth, ScoreType.EXACT, bestScore, bestMove);
					putTTEntry(parent, ttEntry);
					if (debug) {
						Variation pvar = getPrincipalVariation(parent);
						pvar.print(parent, indent);
					}
				}
			}
		} else {	// minimize
			bestScore = TreeSearch.SCORE_INFINITY;	// +"infinity"
			while (gen.hasNext()) {
				positionsSearched++;
				Move move = gen.next();
				GamePosition child = parent.copy();
				child.makeMove(move);
				score = search(child, depth-1, true, indent + "    ");
				if (score < bestScore) {
					bestScore = score;
					bestMove = move;
					ttEntry = parent.newTTentry(depth, ScoreType.EXACT, bestScore, bestMove);
					putTTEntry(parent, ttEntry);
					if (debug) {
						Variation pvar = getPrincipalVariation(parent);
						pvar.print(parent, indent);
					}
				}
			}
		}
		
		if (debug) System.out.format("%s} MiniMax.search(%s) returning search score=%d%n",
				indent, parent.sideToMove(), bestScore);
		return bestScore;
	}

	/**
	 * @param root		root GamePosition to be searched
	 * @param maxDepth	maximum depth to search
	 * @return			MiniMax search results
	 */
	public MiniMax search(Position root, int maxDepth) {
		setRoot(root);
		elapsedTime();
		search((GamePosition) root, maxDepth, root.scoreSign() > 0, "");
		elapsedTime();
		return this;		
	}
}
