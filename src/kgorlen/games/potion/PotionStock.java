/**
 * 
 */
package kgorlen.games.potion;

import kgorlen.games.Move;
import kgorlen.games.Position;
import kgorlen.games.Variation;
import kgorlen.games.potion.PotionVariation;
import kgorlen.games.potion.PotionGenerator;

/**
 * @author Keith gorlen@comcast.net
 *
 */
public class PotionStock implements Position {
	private int[] amount = new int[Ingredient.values().length];
	private int numReactions = 0;

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#copy()
	 */
	@Override
	public PotionStock copy() {
		PotionStock p = new PotionStock();	// must make a deep copy
		p.numReactions = numReactions;
		for (int i = 0; i < amount.length; i++) {
			p.amount[i] = amount[i];
		}
		return p;
	}

	public int getAmount(Ingredient i) {
		return amount[i.ordinal()];
	}
	
	public int setAmount(Ingredient i, int amount) {
		if (amount < 0) throw new RuntimeException("Ingredient amount <0");
		
		return this.amount[i.ordinal()] = amount;
	}
	
	public int addAmount(Ingredient i, int amount) {
		return this.amount[i.ordinal()] += amount;
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#numMoves()
	 */
	@Override
	public int numMoves() {
		return numReactions;
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#isValidMove(kgorlen.games.Move)
	 */
	@Override
	public boolean isValidMove(Move m) {
		if (m == null) return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#makeMove(kgorlen.games.Move)
	 */
	@Override
	public void makeMove(Move m) {
		((Reaction) m).brew(this);
		numReactions++;
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#evaluate()
	 */
	@Override
	public int evaluate() {
		return 1000*amount[Ingredient.POTION.ordinal()] 
				- numReactions;	// penalize if more reactions
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#moveGenerator(boolean)
	 */
	@Override
	public PotionGenerator moveGenerator(boolean debug) {
		return new PotionGenerator(this, debug);
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#moveGenerator()
	 */
	@Override
	public PotionGenerator moveGenerator() {
		return new PotionGenerator(this, false);
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#variation()
	 */
	@Override
	public Variation variation() {
		return new PotionVariation();
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#print(java.lang.String)
	 */
	@Override
	public void print(String indent) {
		for (Ingredient i: Ingredient.values()) {
			System.out.format("%s%s = %d%n", indent, i, amount[i.ordinal()]);
		}
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#print()
	 */
	@Override
	public void print() {
		print("");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hashcode = 0;
		for (Ingredient i: Ingredient.values()) {
			hashcode = hashcode*13 ^ amount[i.ordinal()];
		}
		return hashcode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof PotionStock)) return false;
		for (Ingredient i: Ingredient.values()) {
			if (amount[i.ordinal()] != ((PotionStock) o).amount[i.ordinal()])
				return false;
		}
		return true;
	}

}
