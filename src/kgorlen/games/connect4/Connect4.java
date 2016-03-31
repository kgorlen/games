package kgorlen.games.connect4;

import java.util.Scanner;

import kgorlen.games.MonteCarloTreeSearch;
import kgorlen.games.Move;
import kgorlen.games.TreeSearch;


/**
 * @author		Keith gorlen@comcast.net
 * @version     %I%, %G%
 *
 */
public class Connect4 {
	static int SEARCH_LIMIT = 1000;		// search limit (iterations)
	static Scanner Input = new Scanner(System.in);	// Command input stream
	static boolean Debug = false;					// Debug printout enable
	
	/**
	 * Toggle Debug switch
	 * 
	 * @return	new Debug switch setting
	 */
	static boolean toggleDebug() {
		Debug = !Debug;
		if (Debug) System.out.println("Debug ON");
		else System.out.println("Debug OFF");						
		return Debug;
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
				case "v": 		// Toggle debug (verbose) mode
					toggleDebug();
					continue;
				case "Q":		// Quit
				case "q":  
					System.exit(0);
				default:
					Connect4Move move = p.newMove(Character.toUpperCase(cmd.charAt(0)));
					if (p.isValidMove(move)) {
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
			System.out.println((p.sideToMove() == "X") ? "O wins!" : "X wins!");
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
	
		while (true) {
			TreeSearch mcts = new MonteCarloTreeSearch(Debug);
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
						mcts.search(root, SEARCH_LIMIT);
						System.out.print("Monte Carlo search statistics:\n");
						mcts.printStatistics();
						Move move = mcts.getMove(root);
						System.out.printf("%d. Machine's move %s (score=%d):%n",
								root.getPly()+1,
								move.toString(),
								mcts.getScore(root));
						root.makeMove(move);
						root.print();
						if (isGameOver(root)) break;
						opponentsMove(root);
					} ;
					break;
				}

				case "V":
				case "v": 		// Toggle debug (verbose) mode
					toggleDebug();
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
