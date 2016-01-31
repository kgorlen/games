/**
 * 
 */
package kgorlen.games.potion;

import kgorlen.games.Move;
import kgorlen.games.ScoreType;
import kgorlen.games.TTEntry;

/**
 * @author Keith gorlen@comcast.net
 *
 */
public class PotionTTEntry implements TTEntry {

	/**
	 * 
	 */
	private int score;
	private int depth;
	private Reaction reaction;

	public PotionTTEntry(int depth, int score, Reaction reaction) {
		this.depth = depth;
		this.score = score;
		this.reaction = reaction;
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
		return reaction;
	}

	@Override
	public boolean isPrincipalVariation() {
		return true;
	}

}
