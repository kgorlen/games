/**
 	The three witches in Hamlet can brew any potion provided they have the right ingredients. 
	Suppose that five ingredients are necessary in making a health potion: eye of newt (eon), 
	toe of frog (tof), wool of bat (wob), adder’s fork (af), and tooth of wolf (tow). 
	Four reactions can >occur between these ingredients: 

	4 eon + 2 wob = 3 af + 4 tow
	3 tow + 1 tof = 2 eon
	1 wob + 2 af = 1 tof
	4 tof + 7 tow + 2 af = 1 health potion 

	Assuming you can control the order of reactions, write a program that can calculate the
	maximum number of health potions one can brew with a given amount of ingredients. Here
	is example output: If I have 34 eon, 59 tof, 20 wob, 5 af, and 20 tow, I can make seven
	health potions:
	
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
	10899 positions searched in 70740.54us (0.15 positions/us)
	2687 TT entries, 5841 TT hits

	Enter amount of EON:4
	Enter amount of TOF:4
	Enter amount of WOB:2
	Enter amount of AF:0
	Enter amount of TOW:3
	Brewed 1 units of potion with the reactions:
	-4EON -2WOB +3AF +4TOW 
	-4TOF -7TOW -2AF +1POTION 
	score=998
	Stock remaining:
	POTION = 1
	EON = 0
	TOF = 0
	WOB = 0
	AF = 1
	TOW = 0
	7 positions searched in 95.55us (0.07 positions/us)
	2 TT entries, 0 TT hits

	Enter amount of EON:0
	Enter amount of TOF:3
	Enter amount of WOB:1
	Enter amount of AF:4
	Enter amount of TOW:7
	Brewed 1 units of potion with the reactions:
	-1WOB -2AF +1TOF 
	-4TOF -7TOW -2AF +1POTION 
	score=998
	Stock remaining:
	POTION = 1
	EON = 0
	TOF = 0
	WOB = 0
	AF = 0
	TOW = 0
	9 positions searched in 20.44us (0.44 positions/us)
	2 TT entries, 0 TT hits

	Enter amount of EON:0
	Enter amount of TOF:6
	Enter amount of WOB:2
	Enter amount of AF:0
	Enter amount of TOW:9
	Brewed 1 units of potion with the reactions:
	-3TOW -1TOF +2EON 
	-3TOW -1TOF +2EON 
	-4EON -2WOB +3AF +4TOW 
	-4TOF -7TOW -2AF +1POTION 
	score=996
	Stock remaining:
	POTION = 1
	EON = 0
	TOF = 0
	WOB = 0
	AF = 1
	TOW = 0
	9 positions searched in 38.32us (0.23 positions/us)
	4 TT entries, 0 TT hits

 */
package kgorlen.games.potion;

import java.util.Scanner;
import kgorlen.games.DepthFirst;
import kgorlen.games.Move;
import kgorlen.games.Variation;

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
			
			DepthFirst searchResults = new DepthFirst(Debug);
			searchResults.search(root, maxDepth);
			Variation pvar = searchResults.getPrincipalVariation();
			if (pvar != null) {
				int brewed = searchResults.getScore();
				for (Move m : pvar) root.makeMove(m);
				System.out.format("Brewed %d units of potion with the reactions:%n", brewed);
				pvar.print(root);
			} else {
				System.out.println("Brewed 0 units of potion");
			}
			System.out.println("Stock remaining:");
			root.print();
			searchResults.printStatistics();
		}
	}
}
