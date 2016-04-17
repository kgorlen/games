/**
 * 
 */
package kgorlen.games;
import java.util.logging.Logger;

import kgorlen.games.Position;

/**
 * @author Keith gorlen@comcast.net
 *
 */
public class DepthFirst extends TreeSearch {
	private static final Logger LOGGER = Log.LOGGER;
	private static final String CLASS_NAME = DepthFirst.class.getName();
	
	/**
	 * @param parent	Position to be searched
	 * @param depth		maximum depth to search
	 * @param indent	String prepended to debug output lines
	 * @return			maximum score
	 */
	public int search(Position parent, int depth, String indent) {
		
		LOGGER.fine(() -> String.format("%s{ DepthFirst(%d) position:%n%s", 
				indent, depth, parent.toString(indent) ));

		TTEntry ttEntry = getTTEntry(parent);
		if (ttEntry != null) {
			final int ttScore = ttEntry.getScore();
			ttHits++;
			LOGGER.fine(() -> String.format(
					"%s} DepthFirst(%d) returning transposition score=%d%n",
					indent, depth, ttScore ));
			return ttScore;
		}
		
		int score = parent.evaluate();

		MoveGenerator gen = parent.moveGenerator();		
		if (depth == 0 || !gen.hasNext()) {
			final int leafScore = score;
			LOGGER.fine(() -> String.format(
					"%s} DepthFirst(%d) returning evaluation score=%d%n",
					indent, depth, leafScore ));
			return leafScore;			
		}

		int bestScore = score;		// Deeper search may not improve score
		Move bestMove = null;
		while (gen.hasNext()) {
			Move move = gen.next();
			positionsSearched++;
			Position child = parent.copy();
			child.makeMove(move);

			LOGGER.fine(() -> String.format("%sSearch move %s ...%n", indent, move.toString() ));
			score = search(child, depth-1, indent + "    ");

			final int logScore = score;
			LOGGER.fine(() -> String.format("%s... Move %s score=%d%n",
					indent, move.toString(), logScore ));

			if (score > bestScore) {
				bestScore = score;
				bestMove = move;
				ttEntry = parent.newTTentry(depth, bestScore, bestMove);
				putTTEntry(parent, ttEntry);
				LOGGER.fine(() -> String.format("%sPrincipal variation:%n%s",
						indent, this.getPrincipalVariation(parent).toString(indent) ));
			}
		}

		final int logScore = bestScore;
		LOGGER.fine(() -> String.format("%s} DepthFirst.search(%d) returning search score=%d%n",
				indent, depth, logScore ));

		return bestScore;
	}
	
	/**
	 * @param parent	Position to be searched
	 * @param depth		maximum depth to search
	 * @return			principal Variation from search (may be null)
	 */
	@Override
	public Variation search(Position root, int depth) {
		setRoot(root);
		elapsedTime();
		search(root, depth, "");
		elapsedTime();
		logStatistics(CLASS_NAME);
		Variation pvar = getPrincipalVariation();
		Variation.logPrincipalVariation(pvar, CLASS_NAME);
		return pvar;
	}

}
