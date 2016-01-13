package kgorlen.games.tictactoe;

import kgorlen.games.Variation;
import kgorlen.games.GamePosition;
import kgorlen.games.Move;

/**
 * Representation of a sequence of Tic-Tac-Toe moves
 * 
 * @author Keith gorlen@comcast.net
 *
 */
class TicTacToeVariation implements Variation {
	private int nmoves;			// Number of moves in the variation
	private int score;			// Variation score
	private short[] move;		// Moves in the variation
	 
	/**
	 * Construct an empty Variation
	 */
	public TicTacToeVariation() {
		nmoves = 0;
		score = 0;
		move = new short[9];
	}
	 
	/* (non-Javadoc)
	 * @see kgorlen.games.Variation#numMoves()
	 */
	public int numMoves() {
		return nmoves;
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Variation#score()
	 */
	public int score() {
		return score;
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Variation#getMove(int)
	 */
	public Move getMove(int i) {
		return new TicTacToeMove(move[i]);
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Variation#getMove()
	 */
	public Move getMove() {
		return getMove(0);
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Variation#addMoves(kgorlen.games.Move, kgorlen.games.Variation, int)
	 */
	public int addMoves(Move firstMove, Variation v, int vscore) {
		score = vscore;
		move[0] = ((TicTacToeMove)firstMove).move;
		int i;
		for (i=0; i<v.numMoves(); i++) {
			move[i+1] = ((TicTacToeVariation)v).move[i];
		}
		return (nmoves = ((TicTacToeVariation)v).nmoves+1);
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Variation#reset()
	 */
	public void reset() {	// Reset variation at leaf position
		nmoves = 0;
		score = 0;
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Variation#print(kgorlen.games.GamePosition, java.lang.String)
	 */
	public void print(GamePosition start, String indent) {
		System.out.print(indent);
		System.out.println("Variation score: " + Integer.toString(score)
								+ " move #" + Integer.toString(start.numMoves()));
		int i;
		for (i=0; i<3; i++) {
			int j;
			StringBuilder line = new StringBuilder();
			line.append(((TicTacToePosition) start).rowToString(i) + " ");
			TicTacToePosition p = new TicTacToePosition((TicTacToePosition) start);		
			for (j=0; j<nmoves; j++) {
				((TicTacToePosition) p).makeMove(move[j]);
				line.append(p.rowToString(i) + " ");
			}
			System.out.print(indent);
			System.out.println(line.toString());
		}
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Variation#print(kgorlen.games.GamePosition)
	 */
	public void print(GamePosition start) {
		print(start, "");
		return;
	}
 }
