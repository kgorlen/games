/**
 * 
 */
package kgorlen.games.connect4;

import kgorlen.games.Move;
import kgorlen.games.ScoreType;
import kgorlen.games.TTEntry;
import kgorlen.games.connect4.Connect4Move;

/**
 * @author Keith gorlen@comcast.net
 *
 */
public class Connect4TTEntry implements TTEntry {
	private int score;
	private short depth;
	private long move;

	public Connect4TTEntry(int depth, int score, Connect4Move move) {
		this.depth = (short) depth;
		this.score = score;
		this.move = move.toLong();
	}

	@Override
	public int getDepth() {
		return depth;
}

	@Override
	public ScoreType getScoreType() {
		return ScoreType.EXACT;
	}

	@Override
	public int getScore() {
		return score;
	}

	@Override
	public Move getMove() {
		return new Connect4Move(move);
	}

	@Override
	public boolean isPrincipalVariation() {
		return true;
	}

	@Override
	public String toString() {
		return String.format("depth=%d, score=%d, move %s",
				depth, score, getMove().toString());
	}
}
