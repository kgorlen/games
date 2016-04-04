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
	 * Play Tic Tac Toe games until quit (q) command entered
	 * 
	 * @param args none
	 */
	public static void main(String []args){
	
		while (true) {
			TreeSearch miniMax = new MiniMax(Debug);
			TreeSearch negaMax = new NegaMax(Debug);
			TreeSearch negaMaxPruned = new NegaMaxAlphaBeta(Debug);;
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
						negaMax.search(root, 10);
						TicTacToeMoveGenerator.resetStatistics();
						negaMaxPruned.search(root, 10);
						if (((TicTacToeMove) negaMaxPruned.getMove(root)).toShort() !=
								((TicTacToeMove) negaMax.getMove(root)).toShort()) {
							System.out.format("NegaMax move %s (score=%d):%n",
									negaMax.getMove(root).toString(),
									negaMax.getScore(root));
							
						}
						System.out.print("NegaMax search statistics:\n");
						negaMax.printStatistics();
						System.out.print("NegaMaxAlphaBeta search statistics:\n");
						negaMaxPruned.printStatistics();
						TicTacToeMoveGenerator.printStatistics();
						System.out.format("Machine's move %s (score=%d):%n",
								negaMaxPruned.getMove(root).toString(),
								negaMaxPruned.getScore(root));
						root.makeMove(negaMaxPruned.getMove(root));
						root.print();
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
