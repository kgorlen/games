package kgorlen.games.tictactoe;

import kgorlen.games.Move;
import kgorlen.games.ScoreType;
import kgorlen.games.TTEntry;

public class TicTacToeTTEntry implements TTEntry {
	private int score;
	private short depth;
	private short move;
	private byte scoreType;

	public TicTacToeTTEntry(int depth, ScoreType scoreType, int score,
			TicTacToeMove move) {
		this.depth = (short) depth;
		this.scoreType = (byte) scoreType.ordinal();
		this.score = score;
		this.move = move.toShort();
	}

	@Override
	public int getDepth() {
		return depth;
}

	@Override
	public ScoreType getScoreType() {
		return ScoreType.values[scoreType];
	}

	@Override
	public int getScore() {
		return score;
	}

	@Override
	public Move getMove() {
		return new TicTacToeMove(move);
	}

	@Override
	public boolean isPrincipalVariation() {
		if (scoreType == ScoreType.EXACT.ordinal()) return true;
		return false;
	}

	@Override
	public String toString() {
		return String.format("depth=%d, scoreType %s, score=%d, move %s",
				depth, getScoreType(), score, getMove().toString());
	}
}
