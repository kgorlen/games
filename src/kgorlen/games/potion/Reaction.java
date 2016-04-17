package kgorlen.games.potion;

import java.util.Formatter;

import kgorlen.games.Move;

public class Reaction implements Move {
	private Reactant[] reactant;

	Reaction(Reactant... reactant ) {
		this.reactant = reactant;
	}
	
	public boolean canBrew(PotionStock stock) {
		for (Reactant r: reactant) {
			if (stock.getAmount(r.getName()) + r.getAmount() < 0)
				return false;	// Insufficient amount in stock
		}
		return true;
	}
	
	public boolean brew(PotionStock stock) {
		for (Reactant r: reactant) {
			if (stock.addAmount(r.getName(), r.getAmount()) < 0)
				throw new RuntimeException("Insufficient stock");
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder f = new StringBuilder();
		Formatter formatter = new Formatter(f);
		for (Reactant r: reactant) {
			formatter.format("%+d%s ", r.getAmount(), r.getName());
		}
		formatter.close();
		return f.toString();
	}
}
