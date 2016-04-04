package kgorlen.games;

import kgorlen.games.MoveGenerator;
import kgorlen.games.GamePosition;
import kgorlen.games.TTEntry;

public class NegaMaxAlphaBeta extends AlphaBetaTreeSearch {
	
	public NegaMaxAlphaBeta(boolean debug) {
		this.debug =debug;
	}
	
	/**
	 * References:
	 * 	https://en.wikipedia.org/wiki/Negamax
	 * 	https://chessprogramming.wikispaces.com/Principal+variation
	 * 	https://chessprogramming.wikispaces.com/Alpha-Beta
	 * 	http://web.cs.ucla.edu/~rosen/161/notes/alphabeta.html
	 *  http://web.archive.org/web/20070809015843/http://www.seanet.com/~brucemo/topics/hashing.htm
	 *  http://people.csail.mit.edu/plaat/mtdf.html#abmem
	 * 
	 * Alpha-beta reduces the number of positions searched from an 
	 * empty Tic Tac Toe board from 549,945 to 210,911.
	 * 
	 * @param parent	GamePosition to be searched
	 * @param depth		maximum depth to search
	 * @param alpha		lower bound for child position score
	 * @param beta		upper bound for child position score
	 * @param indent	String prepended to debug output lines
	 * @return			maximum (color = +1) or minimum (color = -1) score
	 */
	protected int search(GamePosition parent, int depth, int alpha, int beta, String indent) {
		int alphaOrig = alpha;
		int score;				// score for *parent* GamePosition
		int color = parent.scoreSign();

		if (debug) {
			System.out.format("%s{ negaMaxAlphaBeta.search(%s) alpha=%s, beta=%d, color=%d, position:%n",
					indent, parent.sideToMove(), alpha, beta, color);
			parent.print(indent);
		}

		TTEntry ttEntry = getTTEntry(parent);
		if (ttEntry != null && ttEntry.getDepth() >= depth) {
			ttHits++;
			score = ttEntry.getScore();
			switch (ttEntry.getScoreType()) {
			case INVALID:
				throw new RuntimeException("Invalid score type");
			case EXACT:
				if (debug) System.out.format("%s} negaMaxAlphaBeta.search(%s) returning transposition %s score=%d%n",
						indent, parent.sideToMove(), ttEntry.getScoreType(), score);
				return score;
			case LOWERBOUND:
				if (score > alpha) alpha = score;
				break;
			case UPPERBOUND:
				if (beta < score) beta = score;
				break;
			}
			if (alpha >= beta) {
				if (debug) System.out.format("%s} negaMaxAlphaBeta.search(%s) returning transposition %s score=%d%n",
						indent, parent.sideToMove(), ttEntry.getScoreType(), score);
				return score;
			}
		}		
		
		if (parent.isDraw()) {
			score = color * parent.scoreDraw();
			if (debug) System.out.format("%s} negaMaxAlphaBeta.search(%s) returning draw score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
			
		if (parent.isWin()) {
			score = color * parent.scoreWin();
			if (debug) System.out.format("%s} negaMaxAlphaBeta.search(%s) returning win score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
		
		if (depth == 0) {
			score = color * parent.evaluate(debug);
			if (debug) System.out.format("%s} negaMaxAlphaBeta.search(%s) returning evaluation score=%d%n",
					indent, parent.sideToMove(), score);
			return score;
		}
		
		int bestScore = -TreeSearch.SCORE_INFINITY;
		ScoreType scoreType = ScoreType.INVALID;
		Move bestMove = null;
		MoveGenerator gen = parent.moveGenerator(getKillers(parent.getPly()), debug);		
		assert gen.hasNext() : "Unexpected terminal position";
		while (gen.hasNext()) {
			positionsSearched++;
			Move move = gen.next();
			GamePosition child = parent.copy();
			child.makeMove(move);
			if (debug) {
				System.out.format("%sSearch move %s ...%n", indent, move.toString());
			}
			score = -search(child, depth-1, -beta, -alpha, indent + "    ");
			if (debug) {
				System.out.format("%s... Move %s score=%d%n", indent, move.toString(), score);
			}
			if (score > bestScore) {
				bestScore = score;
				bestMove = move;
			}
			if (score > alpha) alpha = score;	// possible PV-node
			if (alpha >= beta) {				// searched a Cut-Node
				addKiller(parent.getPly(), bestMove);
				break;
			}
		}

		if (bestScore <= alphaOrig) {			// searched an All-Node
			scoreType = ScoreType.UPPERBOUND;
		} else if (bestScore >= beta) {			// searched a Cut-Node
			scoreType = ScoreType.LOWERBOUND;
			incCutoffs(color);			
		} else scoreType = ScoreType.EXACT;		// searched a PV-Node
		
		ttEntry = parent.newTTentry(depth, scoreType, bestScore, bestMove);
		putTTEntry(parent, ttEntry);
		if (debug) {
			if (scoreType == ScoreType.EXACT) {
				Variation pvar = getPrincipalVariation(parent);
				pvar.print(parent, indent);
			}
			System.out.format("%s} negaMaxAlphaBeta.search(%s) returning %s score=%d%n",
				indent, parent.sideToMove(), scoreType, bestScore);
		}
		return bestScore;
	}

	/**
	 * @param root		root GamePosition to be searched
	 * @param maxDepth	maximum depth to search
	 * @return			NegaMaxAlphaBeta search results
	 */
	public NegaMaxAlphaBeta search(Position root, int maxDepth) {
		setRoot(root);
		elapsedTime();
		search((GamePosition) root, maxDepth, -TreeSearch.SCORE_INFINITY, +TreeSearch.SCORE_INFINITY, "");
		elapsedTime();
		return this;
	}
}
