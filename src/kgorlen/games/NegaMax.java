package kgorlen.games;

import kgorlen.games.MoveGenerator;
import kgorlen.games.GamePosition;
import kgorlen.games.Variation;

public class NegaMax extends TreeSearch {

	public NegaMax(boolean debug) {
		this.debug =debug;
	}
	
	/**
	 * Negamax implementation of a full minimax search of a GamePosition
	 * References:
	 * 		https://en.wikipedia.org/wiki/Negamax
	 * 		https://chessprogramming.wikispaces.com/Principal+variation
	 * 
	 * @param parent	GamePosition to be searched
	 * @param depth		maximum depth to search
	 * @param indent	String prepended to debug output lines
	 * @return			maximum/minimum score
	 */
	public int search(GamePosition parent, int depth, String indent) {
	
		int score;				// score for *parent* GamePosition
		int color = parent.scoreSign();
		
		if (debug) {
			System.out.format("%s{ negaMax.search(%s) color=%d, position:%n",
					indent, parent.sideToMove(), color);
			parent.print(indent);
		}

		TTEntry ttEntry = getTTEntry(parent);
		if (ttEntry != null) {
			score = ttEntry.getScore();
			ttHits++;
			if (debug) System.out.format("%s} negaMax.search(%s) returning transposition score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
		
		if (parent.isWin()) {
			score = color * parent.scoreWin();
			if (debug) System.out.format("%s} negaMax.search(%s) returning win score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
		
		if (parent.isDraw()) {
			score = color * parent.scoreDraw();
			if (debug) System.out.format("%s} negaMax.search(%s) returning draw score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
			
		if (depth == 0) {
			score = color * parent.evaluate(debug);
			if (debug) System.out.format("%s} negaMax.search(%s) returning evaluation score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
		
		int bestScore = -TreeSearch.SCORE_INFINITY;
		Move bestMove = null;
		MoveGenerator gen = parent.moveGenerator(debug);		
		while (gen.hasNext()) {
			Move move = gen.next();
			positionsSearched++;
			GamePosition child = parent.copy();
			child.makeMove(move);
			score = -search(child, depth-1, indent + "    ");
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
		
		if (debug) System.out.format("%s} negaMax.search(%s) returning search score=%d%n",
				indent, parent.sideToMove(), bestScore);
		return bestScore;
	}

	/**
	 * @param root		root GamePosition to be searched
	 * @param maxDepth	maximum depth to search
	 * @return			MiniMax search results
	 */
	public NegaMax search(Position root, int maxDepth) {
		setRoot(root);
		elapsedTime();
		search((GamePosition) root, maxDepth, "");
		elapsedTime();
		return this;		
	}
}
