package kgorlen.games.potion;

import java.util.ArrayList;
import java.util.ListIterator;

import kgorlen.games.Move;
import kgorlen.games.Position;
import kgorlen.games.Variation;

/**
 * Manages a sequence of Reactions as a stack, implemented with an ArrayList.
 * 
 * @author Keith gorlen@comcast.net
 *
 */
public class PotionVariation extends ArrayList<Reaction> implements Variation {
	private static final long serialVersionUID = 1L;
	private int score = 0;				// Score of this sequence of Reactions

	@Override
	public int numMoves() {
		return size();
	}

	@Override
	public int getScore() {
		return score;
	}

	@Override
	public Move getMove(int i) {
		return get(size()-i);
	}

	@Override
	public Move getMove() {
		return get(size()-1);
	}

	@Override
	public void addMove(int score, Move firstReaction) {
		this.score = score;
		add((Reaction) firstReaction);
	}

	@Override
	public int setMoves(Variation v) {
		clear();
		addAll(0, (PotionVariation) v);
		return score = ((PotionVariation) v).score;
	}
	
	@Override
	public void addMoves(int score, Move firstMove, Variation v) {
		this.score = score;
		clear();
		addAll(0, (PotionVariation) v);
		add((Reaction) firstMove);
	}

	@Override
	public void reset() {
		clear();
	}

	@Override
	public void print(Position start, String indent) {
		ListIterator<Reaction> i = listIterator(size());
		while (i.hasPrevious()) {
			System.out.println(indent + i.previous().toString());
		}
		System.out.format("%sscore=%d%n", indent, score);
	}

	@Override
	public void print(Position start) {
		print(start, "");
	}

}
