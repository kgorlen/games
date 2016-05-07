package kgorlen.games.tictactoe;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import kgorlen.games.tictactoe.TicTacToePosition;
import kgorlen.games.TreeSearch;
import kgorlen.games.Variation;
import kgorlen.games.NegaMaxAlphaBeta;
import kgorlen.games.NegaMax;
import kgorlen.games.Log;
import kgorlen.games.MiniMax;


/**
 * @author		Keith gorlen@comcast.net
 * @version     %I%, %G%
 *
 */
public class TicTacToe {
	static Scanner Input = new Scanner(System.in);	// Command input stream
	
	private final static Logger LOGGER = Log.LOGGER;

	/**
	 * Step debug log level
	 */
	static void stepDebug() {
		String levels[] = {/*"OFF", "SEVERE",*/ "WARNING", "INFO", /*"CONFIG",*/ "FINE", "FINER", "FINEST"/*, "ALL"*/};
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
			case "d": 		// Step debug mode
				stepDebug();
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
	 * Play Tic Tac Toe games until quit (q) command entered
	 * 
	 * @param args none
	 */
	public static void main(String []args){
		LOGGER.setLevel(Level.INFO);
	
		while (true) {
			TreeSearch miniMax = new MiniMax();
			TreeSearch negaMax = new NegaMax();
			TreeSearch negaMaxPruned = new NegaMaxAlphaBeta();;
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
					Variation miniMaxPvar = miniMax.search(root, 10);
					Variation negaMaxPvar = negaMax.search(root, 10);
					if (!miniMaxPvar.equals(negaMaxPvar)) {
						LOGGER.warning("MiniMax and NegaMax search results differ\n");
						LOGGER.warning(String.format("MiniMax principal variation:%n%s",
								miniMaxPvar.toString() ));
						LOGGER.warning(String.format("NegaMax principal variation:%n%s",
								negaMaxPvar.toString() ));
					}
					TicTacToeMoveGenerator.resetStatistics();
					Variation negaMaxPrunedPvar = negaMaxPruned.search(root, 10);
					if (!negaMaxPrunedPvar.getMove().equals(negaMaxPvar.getMove())
							|| negaMaxPrunedPvar.getScore() != negaMaxPvar.getScore()) {
						LOGGER.warning("NegaMax and NegaMaxAlphaBeta search results differ:\n");
						LOGGER.warning(String.format("  NegaMax move %s (score=%d)%n  NegaMaxAlphaBeta move %s (score=%d)%n",
								negaMaxPvar.getMove().toString(), negaMaxPvar.getScore(),
								negaMaxPrunedPvar.getMove().toString(), negaMaxPrunedPvar.getScore() ));

					}
					TicTacToeMoveGenerator.logStatistics();
					System.out.format("Machine's move %s (score=%d):%n",
							negaMaxPrunedPvar.getMove().toString(),
							negaMaxPrunedPvar.getScore());
					root.makeMove(negaMaxPrunedPvar.getMove());
					root.print();
					if (isGameOver(root)) break;
					opponentsMove(root);
				} ;
				break;
			}

			case "D":
			case "d": 		// Step debug mode
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
