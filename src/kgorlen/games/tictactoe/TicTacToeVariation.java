package kgorlen.games.tictactoe;

import kgorlen.games.Variation;
import kgorlen.games.Position;
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
	public int getScore() {
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
	
	@Override
	public int setMoves(Variation v) {
		for (int i=0; i<v.numMoves(); i++) {
			move[i+1] = ((TicTacToeVariation) v).move[i];
		}
		nmoves = ((TicTacToeVariation) v).nmoves;
		return score = ((TicTacToeVariation) v).score;
	}

	@Override
	public void addMove(int vscore, Move firstMove) {
		score = vscore;
		for (int i=0; i<nmoves; i++) {
			move[i+1] = move[i];
		}
		move[0] = ((TicTacToeMove)firstMove).move;
		nmoves++;
		return;
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Variation#addMoves(kgorlen.games.Move, kgorlen.games.Variation, int)
	 */
	public void addMoves(int vscore, Move firstMove, Variation v) {
		score = vscore;
		move[0] = ((TicTacToeMove)firstMove).move;
		for (int i=0; i<v.numMoves(); i++) {
			move[i+1] = ((TicTacToeVariation)v).move[i];
		}
		nmoves = ((TicTacToeVariation)v).nmoves+1;
		return;
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
	public void print(Position start, String indent) {
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
	public void print(Position start) {
		print(start, "");
		return;
	}

 }
