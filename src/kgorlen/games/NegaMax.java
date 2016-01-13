package kgorlen.games;

import kgorlen.games.MoveGenerator;
import kgorlen.games.GamePosition;
import kgorlen.games.Variation;

public class NegaMax extends TreeSearch {

	public NegaMax(Variation pvar, boolean debug) {
		this.principalVariation = pvar;
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
	 * @param pvar		updated with principal Variation
	 * @param indent	String prepended to debug output lines
	 * @return			maximum/minimum score
	 */
	public int search(GamePosition parent, int depth,
			Variation pvar, String indent) {
	
		int score;				// score for *parent* GamePosition
		int color = parent.scoreSign();
		
		if (debug) {
			System.out.format("%s{ negaMaxSearch(%s) color=%d, position:%n",
					indent, parent.sideToMove(), color);
			parent.print(indent);
		}

		if (parent.isWin()) {
			score = color * parent.scoreWin();
			pvar.reset();
			if (debug) System.out.format("%s} negaMaxSearch(%s) returning win score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
		
		if (parent.isDraw()) {
			score = color * parent.scoreDraw();
			pvar.reset();
			if (debug) System.out.format("%s} negaMaxSearch(%s) returning draw score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
			
		if (depth == 0) {
			score = color * parent.evaluate();
			pvar.reset();
			if (debug) System.out.format("%s} negaMaxSearch(%s) returning evaluation score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
		
		int bestValue = -999999999;
		MoveGenerator gen = parent.moveGenerator(debug);		
		while (gen.hasNext()) {
			Move move = gen.next();
			positionsSearched++;
			GamePosition child = parent.copy();
			child.makeMove(move);
			Variation var = parent.variation();
			score = -search(child, depth-1, var, indent + "    ");
			if (score > bestValue) {
				bestValue = score;
				pvar.addMoves(move, var, score);
				if (debug) {
					pvar.print(parent, indent);
				}
			}
		}
		
		if (debug) System.out.format("%s} negaMaxSearch(%s) returning search score=%d%n",
				indent, parent.sideToMove(), bestValue);
		return bestValue;
	}

	/**
	 * @param root		root GamePosition to be searched
	 * @param maxDepth	maximum depth to search
	 * @return			MiniMax search results
	 */
	public NegaMax search(GamePosition root, int maxDepth) {
		elapsedTime();
		search(root, maxDepth, principalVariation, "");
		elapsedTime();
		return this;		
	}
}
