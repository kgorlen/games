package kgorlen.games.potion;

public class Reactant {
	private Ingredient name;
	private int amount;
	
	Reactant(Ingredient name, int amount){
		this.name = name;
		this.amount = amount;
		assert amount != 0: "Reactant amount must not be zero";
	}
	
	public Ingredient getName() {
		return name;
	}
	
	/**
	 * @return the amount
	 */
	public int getAmount() {
		return amount;
	}
}
