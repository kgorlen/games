/**
 * 
 */
package kgorlen.games.potion;

import kgorlen.games.Move;
import kgorlen.games.MoveGenerator;
import kgorlen.games.Position;
import kgorlen.games.Variation;
import kgorlen.games.potion.Ingredient;

/**
 * @author Keith gorlen@comcast.net
 *
 */
public class PotionStock implements Position {
	Ingredient[] ingredient = {
			new Ingredient("potion"),
			new Ingredient("eon"),
			new Ingredient("tof"),
			new Ingredient("wob"),
			new Ingredient("af"),
			new Ingredient("tow")
	};
	int numReactions = 0;

	@Override
	public Position copy() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#makeMove(kgorlen.games.Move)
	 */
	@Override
	public void makeMove(Move m) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#evaluate()
	 */
	@Override
	public int evaluate() {
		return ingredient[0].amount;
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#moveGenerator(boolean)
	 */
	@Override
	public MoveGenerator moveGenerator(boolean debug) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#moveGenerator()
	 */
	@Override
	public MoveGenerator moveGenerator() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#variation()
	 */
	@Override
	public Variation variation() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#print(java.lang.String)
	 */
	@Override
	public void print(String indent) {
		int i;
		for (i=0; i < ingredient.length; i++) {
			System.out.format("%s%s = %d%n", indent, ingredient[i].name, ingredient[i].amount);
		}
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#print()
	 */
	@Override
	public void print() {
		print("");
	}

}
