package kgorlen.games;

import kgorlen.games.MoveGenerator;

import java.util.logging.Logger;

import kgorlen.games.GamePosition;
import kgorlen.games.TTEntry;

public class NegaMaxAlphaBeta extends AlphaBetaTreeSearch {
	private final static Logger LOGGER = Log.LOGGER;
	private static final String CLASS_NAME = NegaMaxAlphaBeta.class.getName();
	
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
		final int alphaOrig = alpha;
		final int betaOrig = alpha;
		int color = parent.scoreSign();

		LOGGER.fine(() -> String.format(
				"%s{ Entering %s.search(%s) depth=%d, alpha=%s, beta=%d, color=%d, position:%n%s",
					indent, CLASS_NAME, parent.sideToMove(), depth, alphaOrig, betaOrig, color, parent.toString(indent) ));

		final TTEntry ttEntry = getTTEntry(parent);
		if (ttEntry != null && ttEntry.getDepth() >= depth) {
			ttHits++;
			final int ttScore = ttEntry.getScore();
			switch (ttEntry.getScoreType()) {
			case INVALID:
				throw new RuntimeException("Invalid score type");
			case EXACT:
				LOGGER.fine(() -> String.format(
						"%s} %s.search(%s) returning transposition %s score=%d%n",
						indent, CLASS_NAME, parent.sideToMove(), ttEntry.getScoreType(), ttScore));
				return ttScore;
			case LOWERBOUND:
				if (ttScore > alpha) alpha = ttScore;
				break;
			case UPPERBOUND:
				if (beta < ttScore) beta = ttScore;
				break;
			}
			if (alpha >= beta) {
				LOGGER.fine(() -> String.format(
						"%s} %s.search(%s) returning transposition %s score=%d%n",
						indent, CLASS_NAME, parent.sideToMove(), ttEntry.getScoreType(), ttScore));
				return ttScore;
			}
		}		
		
		if (parent.isWin()) {
			final int winScore = color * parent.scoreWin();
			LOGGER.fine(() -> String.format(
					"%s} %s.search(%s) returning win score=%d%n",
					indent, CLASS_NAME, parent.sideToMove(), winScore));
			return winScore;
		}
		
		if (parent.isDraw()) {
			final int drawScore = color * parent.scoreDraw();
			LOGGER.fine(() -> String.format(
					"%s} %s.search(%s) returning draw score=%d%n",
					indent, CLASS_NAME, parent.sideToMove(), drawScore));
			return drawScore;
		}
			
		if (depth == 0) {
			final int evalScore = color * parent.evaluate();
			LOGGER.fine(() -> String.format("%s} %s.search(%s) returning evaluation score=%d%n",
					indent, CLASS_NAME, parent.sideToMove(), evalScore));
			return evalScore;
		}
		
		
		int score;				// score for *parent* GamePosition
		int bestScore = -TreeSearch.SCORE_INFINITY;
		ScoreType scoreType = ScoreType.INVALID;
		Move bestMove = null;
		MoveGenerator gen = parent.moveGenerator(getKillers(parent.getPly()));		
		assert gen.hasNext() : "Unexpected terminal position";
		while (gen.hasNext()) {
			positionsSearched++;
			Move move = gen.next();
			GamePosition child = parent.copy();
			child.makeMove(move);
			LOGGER.fine(() -> String.format(
					"%sSearch move %s ...%n", indent, move.toString() ));
			final int evalScore = score = -search(child, depth-1, -beta, -alpha, indent + "    ");
			LOGGER.fine(() -> String.format(
					"%s... Move %s score=%d%n", indent, move.toString(), evalScore ));
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
//			bestScore = beta;					// for fail-hard pruning?
		} else scoreType = ScoreType.EXACT;		// searched a PV-Node
		
		final TTEntry newTTentry = parent.newTTentry(depth, scoreType, bestScore, bestMove);
		putTTEntry(parent, newTTentry);

		LOGGER.fine(() -> newTTentry.getScoreType() == ScoreType.EXACT ?
				getPrincipalVariation(parent).toString(indent) :
					String.format("%s} %s.search(%s) returning %s score=%d%n",
							indent, CLASS_NAME, parent.sideToMove(), newTTentry.getScoreType(), newTTentry.getScore() ));

		return bestScore;
	}

	/**
	 * @param root		root GamePosition to be searched
	 * @param maxDepth	maximum depth to search
	 * @return			principal Variation found by search
	 */
	@Override
	public Variation search(Position root, int maxDepth) {
		setRoot(root);
		elapsedTime();
		search((GamePosition) root, maxDepth, -TreeSearch.SCORE_INFINITY, +TreeSearch.SCORE_INFINITY, "");
		elapsedTime();
		logStatistics(CLASS_NAME);
		Variation pvar = getPrincipalVariation();
		Variation.logPrincipalVariation(pvar, CLASS_NAME);
		return pvar;
	}
}
