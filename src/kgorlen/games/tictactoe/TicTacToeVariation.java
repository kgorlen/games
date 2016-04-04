package kgorlen.games.tictactoe;

import kgorlen.games.Variation;
import kgorlen.games.Position;

/**
 * Representation of a sequence of Tic-Tac-Toe moves
 * 
 * @author Keith gorlen@comcast.net
 *
 */
class TicTacToeVariation extends Variation {
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see kgorlen.games.Variation#print(kgorlen.games.GamePosition, java.lang.String)
	 */
	public void print(Position start, String indent) {
		System.out.println(indent + "Variation score: " + Integer.toString(getScore())
								+ " move #" + Integer.toString(start.getPly()));
		for (int i=0; i<3; i++) {
			System.out.println(indent + lineToString(start, i));
		}
	}

 }
