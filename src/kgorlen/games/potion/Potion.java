/**
 	The three witches in Hamlet can brew any potion provided they have the right ingredients. 
	Suppose that five ingredients are necessary in making a health potion: eye of newt (eon), 
	toe of frog (tof), wool of bat (wob), adder’s fork (af), and tooth of wolf (tow). 
	Four reactions can >occur between these ingredients: 

	4 eon + 2 wob = 3 af + 4 tow
	3 tow + 1 tof = 2 eon
	1 wob + 2 af = 1 tof
	4 tof + 7 tow + 2 af = 1 health potion 

	Assuming you can control the order of reactions, write a program that can calculate the maximum number of 
	health potions one can brew with a given amount of ingredients. Here is example output: If I have 34 eon, 
	59 tof, 20 wob, 5 af, and 20 tow, I can make seven health potions:
	
		Enter amount of EON:34
		Enter amount of TOF:59
		Enter amount of WOB:20
		Enter amount of AF:5
		Enter amount of TOW:20
		Brewed 7 units of potion with the reactions:
		-4TOF -7TOW -2AF +1POTION 
		-4TOF -7TOW -2AF +1POTION 
		-4EON -2WOB +3AF +4TOW 
		-4TOF -7TOW -2AF +1POTION 
		-4EON -2WOB +3AF +4TOW 
		-4TOF -7TOW -2AF +1POTION 
		-4EON -2WOB +3AF +4TOW 
		-4EON -2WOB +3AF +4TOW 
		-4TOF -7TOW -2AF +1POTION 
		-4EON -2WOB +3AF +4TOW 
		-4EON -2WOB +3AF +4TOW 
		-4TOF -7TOW -2AF +1POTION 
		-4EON -2WOB +3AF +4TOW 
		-4EON -2WOB +3AF +4TOW 
		-4TOF -7TOW -2AF +1POTION 
		score=6985
		Stock remaining:
		POTION = 7
		EON = 2
		TOF = 31
		WOB = 4
		AF = 15
		TOW = 3
		9229 positions searched, 2894 TT entries, 6237 TT hits, Infinity positions/us

 */
package kgorlen.games.potion;

import java.util.ListIterator;
import java.util.Scanner;
import kgorlen.games.DepthFirst;

/**
 * @author Keith gorlen@comcast.net
 *
 */
public class Potion {
	static Scanner Input = new Scanner(System.in);	// Command input stream
	static boolean Debug = false;					// Debug printout enable
	
	public static void main(String[] args) {
		while (true) {
			PotionStock root = new PotionStock();
			int maxDepth = 0;
			
			for (Ingredient i: Ingredient.values() ) {
				if (i == Ingredient.POTION) continue;	// skip output ingredient
				
				System.out.format("Enter amount of %s:", i);
				int amount = Input.nextInt();
				root.setAmount(i, amount);
				maxDepth += amount;			// A reaction consumes at least 1 unit of some ingredient
			}
			
			PotionVariation pvar = new PotionVariation();
			DepthFirst searchResults = new DepthFirst(pvar, Debug);
			searchResults.search(root, maxDepth, pvar, "");
			ListIterator<Reaction> i = pvar.listIterator(pvar.size());
			while (i.hasPrevious()) root.makeMove(i.previous());
			System.out.format("Brewed %d units of potion with the reactions:%n",
					root.getAmount(Ingredient.POTION));
			pvar.print(root);
			System.out.println("Stock remaining:");
			root.print();
			searchResults.printStatistics();
		}
	}
}
