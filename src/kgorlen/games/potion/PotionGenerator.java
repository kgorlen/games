package kgorlen.games.potion;

import kgorlen.games.MoveGenerator;

public class PotionGenerator implements MoveGenerator {
	private boolean debug;
	private PotionStock currentStock;
	private int lastReaction;
	
	private final static Reaction[] reaction = {
			new Reaction(	// 4 tof + 7 tow + 2 af = 1 health potion
				new Reactant(Ingredient.TOF, -4),
				new Reactant(Ingredient.TOW, -7),
				new Reactant(Ingredient.AF,  -2),
				new Reactant(Ingredient.POTION, +1)
				),
			new Reaction(	// 4 eon + 2 wob = 3 af + 4 tow
				new Reactant(Ingredient.EON, -4),
				new Reactant(Ingredient.WOB, -2),
				new Reactant(Ingredient.AF,  +3),
				new Reactant(Ingredient.TOW, +4)
				),
			new Reaction(	// 3 tow + 1 tof = 2 eon
				new Reactant(Ingredient.TOW, -3),
				new Reactant(Ingredient.TOF, -1),
				new Reactant(Ingredient.EON, +2)					
				),
			new Reaction(	// 1 wob + 2 af = 1 tof
				new Reactant(Ingredient.WOB, -1),
				new Reactant(Ingredient.AF,  -2),
				new Reactant(Ingredient.TOF, +1)					
				),
	};
	
	public PotionGenerator(PotionStock p, boolean debug) {
		this.debug = debug;
		currentStock = p;
		lastReaction = 0;
	}

	public boolean hasNext() {
		while (lastReaction < reaction.length) {
			Reaction r = reaction[lastReaction];
			if (r.canBrew(currentStock)) return true;
			lastReaction++;
		}
		return false;
	}

	public Reaction next() {
		while (lastReaction < reaction.length) {
			Reaction r = reaction[lastReaction++];
			if (r.canBrew(currentStock)) {
				return r;
			}
		}
		return null;
	}
}
