package kgorlen.games;

import kgorlen.games.MoveGenerator;
import kgorlen.games.GamePosition;
import kgorlen.games.Variation;

public class NegaMaxAlphaBeta extends AlphaBetaTreeSearch {
	
	public NegaMaxAlphaBeta(Variation pvar, boolean debug) {
		this.principalVariation = pvar;
		this.debug =debug;
	}
	
	/**
	 * References:
	 * 	https://en.wikipedia.org/wiki/Negamax
	 * 	https://chessprogramming.wikispaces.com/Principal+variation
	 * 	https://chessprogramming.wikispaces.com/Alpha-Beta
	 * 	http://web.cs.ucla.edu/~rosen/161/notes/alphabeta.html
	 * 
	 * Alpha-beta reduces the number of positions searched from an 
	 * empty Tic Tac Toe board from 549,945 to 210,911.
	 * 
	 * @param parent	GamePosition to be searched
	 * @param depth		maximum depth to search
	 * @param alpha		lower bound for child position score
	 * @param beta		upper bound for child position score
	 * @param pvar		updated with principal Variation
	 * @param indent	String prepended to debug output lines
	 * @return			maximum (color = +1) or minimum (color = -1) score
	 */
	protected int search(GamePosition parent, int depth, int alpha, int beta, Variation pvar, String indent) {
	
		int score;				// score for *parent* GamePosition
		int color = parent.scoreSign();
		
		if (debug) {
			System.out.format("%s{ negaMaxAlphaBeta.search(%s) alpha=%d, beta=%d, color=%d, position:%n",
					indent, parent.sideToMove(), alpha, beta, color);
			parent.print(indent);
		}

		if (parent.isWin()) {
			score = color * parent.scoreWin();
			pvar.reset();
			if (debug) System.out.format("%s} negaMaxAlphaBeta.search(%s) returning win score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
		
		if (parent.isDraw()) {
			score = color * parent.scoreDraw();
			pvar.reset();
			if (debug) System.out.format("%s} negaMaxAlphaBeta.search(%s) returning draw score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
			
		if (depth == 0) {
			score = color * parent.evaluate();
			pvar.reset();
			if (debug) System.out.format("%s} negaMaxAlphaBeta.search(%s) returning evaluation score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
		
		MoveGenerator gen = parent.moveGenerator(debug);		
		while (gen.hasNext()) {
			Move move = gen.next();
			positionsSearched++;
			GamePosition child = parent.copy();
			child.makeMove(move);
			Variation var = parent.variation();
			score = -search(child, depth-1, -beta, -alpha, var, indent + "    ");
			if (score > beta) {
				incCutoffs(color);
				return beta;		// fail-hard cutoff
			}
			if (score > alpha) {
				alpha = score;
				pvar.addMoves(score, move, var);
				if (debug) {
					pvar.print(parent, indent);
				}
			}
		}
		
		if (debug) System.out.format("%s} negaMaxAlphaBeta.search(%s) returning search score=%d%n",
				indent, parent.sideToMove(), alpha);
		return alpha;
	}

	/**
	 * @param root		root GamePosition to be searched
	 * @param maxDepth	maximum depth to search
	 * @return			NegaMaxAlphaBeta search results
	 */
	public NegaMaxAlphaBeta search(GamePosition root, int maxDepth) {
		elapsedTime();
		search(root, maxDepth, -999999999, +999999999, principalVariation, "");
		elapsedTime();
		return this;
	}
}
