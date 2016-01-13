package kgorlen.games.potion;

public class Ingredient {
	String name;
	int amount;
	
	Ingredient(String name, int amount){
		this.name = name;
		this.amount = amount;
	}
	
	Ingredient(String name){
		this.name = name;
		this.amount = 0;
	}

	/**
	 * @return the amount
	 */
	public int getAmount() {
		return amount;
	}
	/**
	 * @param amount the amount to set
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}
}
