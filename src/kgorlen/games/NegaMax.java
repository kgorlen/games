package kgorlen.games;

import kgorlen.games.MoveGenerator;

import java.util.logging.Logger;

import kgorlen.games.GamePosition;

public class NegaMax extends TreeSearch {
	private final static Logger LOGGER = Log.LOGGER;
	private static final String CLASS_NAME = NegaMax.class.getName();

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
	
		int color = parent.scoreSign();
		
		LOGGER.fine(() -> String.format(
				"%s{ %s.search(%s) color=%d, position:%n%s",
					indent, CLASS_NAME, parent.sideToMove(), color, parent.toString(indent) ));

		TTEntry ttEntry = getTTEntry(parent);
		if (ttEntry != null) {
			final int ttScore = ttEntry.getScore();
			ttHits++;
			LOGGER.fine(() -> String.format(
					"%s} %s.search(%s) returning transposition score=%d%n",
					indent, CLASS_NAME, parent.sideToMove(), ttScore));
			return ttScore;
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
		Move bestMove = null;
		MoveGenerator gen = parent.moveGenerator();		
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
				LOGGER.fine(() -> String.format("%sPrincipal variation:%n%s",
						indent, this.getPrincipalVariation(parent).toString(indent) ));
			}
		}
		
		final int logScore = bestScore;
		LOGGER.fine(() -> String.format(
				"%s} %s.search(%s) returning search score=%d%n",
				indent, CLASS_NAME, parent.sideToMove(), logScore));
		return bestScore;
	}

	/**
	 * @param root		root GamePosition to be searched
	 * @param maxDepth	maximum depth to search
	 * @return			MiniMax search results
	 */
	@Override
	public Variation search(Position root, int maxDepth) {
		setRoot(root);
		elapsedTime();
		search((GamePosition) root, maxDepth, "");
		elapsedTime();
		logStatistics(CLASS_NAME);
		Variation pvar = getPrincipalVariation();
		Variation.logPrincipalVariation(pvar, CLASS_NAME);
		return pvar;
	}
}
