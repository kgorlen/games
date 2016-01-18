package kgorlen.games.potion;

import java.util.ArrayList;
import java.util.Iterator;

import kgorlen.games.Move;
import kgorlen.games.Position;
import kgorlen.games.Variation;

public class PotionVariation extends ArrayList<Reaction> implements Variation {
	private static final long serialVersionUID = -6144206174257223734L;

	private int brewed = 0;				// Amount of potion brewed by this variation

	@Override
	public int numMoves() {
		return size();
	}

	@Override
	public int score() {
		return brewed;
	}

	@Override
	public Move getMove(int i) {
		return get(i);
	}

	@Override
	public Move getMove() {
		return get(0);
	}

	@Override
	public int addMoves(Move firstMove, Variation v, int amount) {
		brewed = amount;
		clear();
		add((Reaction) firstMove);
		addAll(1, (PotionVariation) v);
		return size();
	}

	@Override
	public void reset() {
		clear();
	}

	@Override
	public void print(Position start, String indent) {
		Iterator<Reaction> i = iterator();
		while (i.hasNext()) {
			System.out.println(indent + i.next().toString());
		}
	}

	@Override
	public void print(Position start) {
		print(start, "");
	}

}
