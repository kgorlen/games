package kgorlen.games.tictactoe;

import java.util.Scanner;
import kgorlen.games.tictactoe.TicTacToePosition;
import kgorlen.games.TreeSearch;
import kgorlen.games.NegaMaxAlphaBeta;
import kgorlen.games.NegaMax;
import kgorlen.games.MiniMax;

/**
 * @author		Keith gorlen@comcast.net
 * @version     %I%, %G%
 *
 */
public class TicTacToe {
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
	 * Call tree search method for current GamePosition, maximizing for
	 * the side/color the machine is playing.
	 * 
	 * @param root		GamePosition to be searched
	 * @return			TreeSearch search instance
	 */
	static TreeSearch searchRoot(TicTacToePosition root) {
		TreeSearch ts = null;
		switch(2) {
			case 0:
				ts = new MiniMax(Debug);
				((MiniMax) ts).search(root, 10);
				break;
			case 1:
				ts = new NegaMax(Debug);
				((NegaMax) ts).search(root, 10);
				break;
			case 2:
				ts = new NegaMaxAlphaBeta(Debug);			
				((NegaMaxAlphaBeta) ts).search(root, 10);
				break;
				default:
					throw new RuntimeException("Invalid search method");
		}
		return ts;
	}
	
	/**
	 * Read and play opponent's move or command.
	 * 
	 * @param p	current board position
	 */
	static void opponentsMove(TicTacToePosition p) {
		while (true) {
			System.out.print("Enter 'd', 'q', or move 1-9:");

			if (Input.hasNextInt()) {
				TicTacToeMove move = new TicTacToeMove(Input.nextInt());
				if (p.isValidMove(move)) {
					p.makeMove(move);
					p.print();
					return;
				} else {
					System.out.println("Illegal move");
					continue;
				}
			}
			
			String cmd = Input.next();
			switch (cmd) {
				case "D":
				case "d": 		// Toggle debug mode
					toggleDebug();
					continue;
				case "Q":		// Quit
				case "q":  
					System.exit(0);
				default:
					System.out.println("Invalid command");
			}
		}
	}

	/**
	 * Check for draw or win and print outcome
	 * 
	 * @param p
	 * @return	true if game drawn or won
	 */
	static boolean isGameOver(TicTacToePosition p) {	// Test and announce game over
		if (p.isDraw()) {
			System.out.println("Draw");
			return true;
		}
		if (p.isWin()) {
			System.out.println((p.sideToMove() == "X") ? "O wins!" : "X wins!");
			return true;
		}
		return false;
	}
	
	/**
	 * Play Tic Tac Toe games until quit (q) command entered
	 * 
	 * @param args none
	 */
	public static void main(String []args){
	
		while (true) {
			TicTacToePosition root = new TicTacToePosition();	// Initialize game
			System.out.print("Enter 'x', 'o', 'd', or 'q':");
			String cmd = Input.next();
			switch (cmd) {
				case "X":
				case "x":		// User plays X
					root.print();
					opponentsMove(root);
				case "O":
				case "o": {		// Machine plays X
					while (!isGameOver(root)) {
						TreeSearch searchResult = searchRoot(root);
						System.out.format("Machine's move %s (score=%d):%n",
								(searchResult.getMove(root)).toString(),
								searchResult.getScore(root));
						root.makeMove(searchResult.getMove(root));
						root.print();
						searchResult.printStatistics();
						if (isGameOver(root)) break;
						opponentsMove(root);
					} ;
					break;
				}

				case "D":
				case "d": 		// Toggle debug mode
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
