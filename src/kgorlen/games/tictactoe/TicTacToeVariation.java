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
		System.out.print(indent);
		System.out.println("Variation score: " + Integer.toString(getScore())
								+ " move #" + Integer.toString(start.numMoves()));
		int i;
		for (i=0; i<3; i++) {
			StringBuilder line = new StringBuilder();
			line.append(((TicTacToePosition) start).rowToString(i) + " ");
			TicTacToePosition p = new TicTacToePosition((TicTacToePosition) start);		
			for (int j=0; j<size(); j++) {
				((TicTacToePosition) p).makeMove(getMove(j));
				line.append(p.rowToString(i) + " ");
			}
			System.out.print(indent);
			System.out.println(line.toString());
		}
	}

 }
