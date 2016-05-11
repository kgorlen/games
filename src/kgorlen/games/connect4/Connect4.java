package kgorlen.games.connect4;

import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import kgorlen.games.Log;
import kgorlen.games.Move;
import kgorlen.games.TreeSearch;
import kgorlen.games.Variation;
import kgorlen.games.mcts.MCTSClassic;
import kgorlen.games.mcts.MCTSSolver;


/**
 * @author		Keith gorlen@comcast.net
 * @version     %I%, %G%
 *
 */
public class Connect4 {
	static int SEARCH_LIMIT = 1000;		// search limit (iterations)
	static Scanner Input = new Scanner(System.in);	// Command input stream
	private static final Logger LOGGER = Log.LOGGER;

	/**
	 * Step debug log level
	 */
	static void stepDebug() {
		String levels[] = {/*"OFF", "SEVERE",*/ "WARNING", /*"INFO",*/ "CONFIG", "FINE", "FINER", "FINEST"/*, "ALL"*/};
		String current = LOGGER.getLevel().toString();
		for (int i=0; i<levels.length; i++) {
			if (current == levels[i]) {
				LOGGER.setLevel(Level.parse(levels[(i+1)%levels.length]));
				break;
			}
		}
		System.out.println("Log level " + LOGGER.getLevel().toString());
	}
	
	/**
	 * Read and play opponent's move or command.
	 * 
	 * @param p	current board position
	 */
	static void opponentsMove(Connect4Position p) {
		while (true) {
			System.out.printf("%d. Enter 'v', 'q', or move a-g:", p.getPly()+1);
			String cmd = Input.next();
			switch (cmd) {
				case "V":
				case "v": 		// step logging level (verbosity)
					stepDebug();
					continue;
				case "Q":		// Quit
				case "q":  
					System.exit(0);
				default:
					Connect4Move move = p.newMove(Character.toLowerCase(cmd.charAt(0)));
					if (p.isValidMove(move)) {
						LOGGER.info(String.format("Opponent's move: %s%n", move.toString()));
						p.makeMove(move);
						p.print();
						return;
					} else {
						System.out.printf("Move: %s (0x%x), valid: 0x%x%n", 
								move.toString(), move.toLong(), p.moves());
					}
					System.out.println("Illegal move or invalid command");
			}
		}
	}

	/**
	 * Check for draw or win and print outcome
	 * 
	 * @param p
	 * @return	true if game drawn or won
	 */
	static boolean isGameOver(Connect4Position p) {	// Test and announce game over
		if (p.isWin()) {
			System.out.println(p.sideLastMoved() + " wins!");
			return true;
		}
		if (p.isDraw()) {
			System.out.println("Draw");
			return true;
		}
		return false;
	}
	
	/**
	 * Play Connect Four games until quit (q) command entered
	 * 
	 * @param args none
	 */
	public static void main(String []args){
		LOGGER.setLevel(Level.CONFIG);
		long DEBUG_SEED = 424242424242424247L;  // Fixed seed for debugging  TODO: use random seed
		
		while (true) {
//			TreeSearch mcts = new MCTSClassic(new Random(DEBUG_SEED));
			TreeSearch mcts = new MCTSSolver(new Random(DEBUG_SEED));
			Connect4Position root = new Connect4Position();	// Initialize game

			System.out.print("Enter 'x', 'o', 'v', or 'q':");
			String cmd = Input.next();
			switch (cmd) {
				case "X":
				case "x":		// User plays X
					root.print();
					opponentsMove(root);
				case "O":
				case "o": {		// Machine plays X
					while (!isGameOver(root)) {
						Variation pv = mcts.search(root, SEARCH_LIMIT);
						Move move = pv.getMove();
						System.out.printf("%d. Machine's move %s (score %+d):%n",
								root.getPly()+1,
								move.toString(),
								pv.getScore());
						root.makeMove(move);
						root.print();
						if (isGameOver(root)) break;
						opponentsMove(root);
					} ;
					break;
				}

				case "V":
				case "v": 		// Toggle debug (verbose) mode
					stepDebug();
					break;

				case "Q":		// Quit
				case "q":  
					return;

				default:
					System.out.println("Invalid command");
					break;
			} 
		}
	}

}
