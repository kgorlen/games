package kgorlen.games;

import java.util.ArrayList;

/**
 * Manages a sequence of Moves, implemented with an ArrayList.
 * 
 * @author Keith gorlen@comcast.net
 *
 */
public class Variation extends ArrayList<Move> {
	private static final long serialVersionUID = 1L;
	private int score = 0;				// Score of this sequence of Moves

	public int numMoves() {
		return size();
	}

	public int setScore(int score) {
		this.score = score;
		return score;
	}

	public int getScore() {
		return score;
	}

	public Move getMove(int i) {
		return get(i);
	}

	public Move getMove() {
		return get(0);
	}

	public void addMove(Move move) {
		add(move);
	}

	public void clear() {
		score = 0;
		super.clear();
	}

	public void print(Position start, String indent) {
		for (Move move : this) {
			System.out.println(indent + move.toString());			
		}
		System.out.format("%sscore=%d%n", indent, score);
	}

	public void print(Position start) {
		print(start, "");
	}

}
