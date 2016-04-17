package kgorlen.games;

import java.util.logging.Logger;

import kgorlen.games.MoveGenerator;

public class MiniMax extends TreeSearch {
	private final static Logger LOGGER = Log.LOGGER;
	private static final String CLASS_NAME = MiniMax.class.getName();
	
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
	
		LOGGER.fine(() -> String.format(
				"%s{ %s.search(%s) maximize=%b, position:%n%s",
					indent, CLASS_NAME, parent.sideToMove(), maximize, parent.toString(indent) ));

		TTEntry ttEntry = getTTEntry(parent);
		if (ttEntry != null) {
			final int ttScore = ttEntry.getScore();
			ttHits++;
			LOGGER.fine(() -> String.format(
					"%s} %s.search(%s) returning transposition score=%d%n",
					indent, CLASS_NAME, parent.sideToMove(), ttScore ));
			return ttScore;
		}
		
		if (parent.isWin()) {
			final int winScore = parent.scoreWin();
			LOGGER.fine(() -> String.format(
					"%s} %s.search(%s) returning win score=%d%n",
					indent, CLASS_NAME, parent.sideToMove(), winScore ));
			return winScore;
		}
		
		if (parent.isDraw()) {
			final int drawScore = parent.scoreDraw();
			LOGGER.fine(() -> String.format(
					"%s} %s.search(%s) returning draw score=%d%n",
					indent, CLASS_NAME, parent.sideToMove(), drawScore ));
			return drawScore;
		}
			
		if (depth == 0) {
			final int evalScore = parent.evaluate();
			LOGGER.fine(() -> String.format(
					"%s} %s.search(%s) returning evaluation score=%d%n",
					indent, CLASS_NAME, parent.sideToMove(), evalScore ));
			return evalScore;
		}
		
		MoveGenerator gen = parent.moveGenerator();		
		int bestScore;		// "best" = highest if maximizing, lowest if minimizing
		Move bestMove = null;

		if (maximize) {
			bestScore = -TreeSearch.SCORE_INFINITY;	// -"infinity"
			while (gen.hasNext()) {
				positionsSearched++;
				Move move = gen.next();
				GamePosition child = parent.copy();
				child.makeMove(move);
				int score = search(child, depth-1, false, indent + "    ");
				if (score > bestScore) {
					bestScore = score;
					bestMove = move;
					ttEntry = parent.newTTentry(depth, ScoreType.EXACT, bestScore, bestMove);
					putTTEntry(parent, ttEntry);
					LOGGER.fine(() -> String.format("%sPrincipal variation:%n%s",
							indent, this.getPrincipalVariation(parent).toString(indent) ));
				}
			}
		} else {	// minimize
			bestScore = TreeSearch.SCORE_INFINITY;	// +"infinity"
			while (gen.hasNext()) {
				positionsSearched++;
				Move move = gen.next();
				GamePosition child = parent.copy();
				child.makeMove(move);
				int score = search(child, depth-1, true, indent + "    ");
				if (score < bestScore) {
					bestScore = score;
					bestMove = move;
					ttEntry = parent.newTTentry(depth, ScoreType.EXACT, bestScore, bestMove);
					putTTEntry(parent, ttEntry);
					LOGGER.fine(() -> String.format("%sPrincipal variation:%n%s",
							indent, this.getPrincipalVariation(parent).toString(indent) ));
				}
			}
		}
		
		final int logScore = bestScore;
		LOGGER.fine(() -> String.format(
				"%s} %s.search(%s) returning search score=%d%n",
				indent, CLASS_NAME, parent.sideToMove(), logScore ));
		return bestScore;
	}

	/**
	 * @param root		root GamePosition to be searched
	 * @param maxDepth	maximum depth to search
	 * @return			principal Variation found by search (may be null)
	 */
	@Override
	public Variation search(Position root, int maxDepth) {
		setRoot(root);
		elapsedTime();
		search((GamePosition) root, maxDepth, root.scoreSign() > 0, "");
		elapsedTime();
		logStatistics(CLASS_NAME);
		Variation pvar = getPrincipalVariation();
		Variation.logPrincipalVariation(pvar, CLASS_NAME);
		return pvar;
	}
}
