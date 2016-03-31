package kgorlen.games.connect4;

import kgorlen.games.Position;
import kgorlen.games.Variation;
import kgorlen.games.connect4.Connect4Position;

/**
 * Representation of a sequence of Tic-Tac-Toe moves
 * 
 * @author Keith gorlen@comcast.net
 *
 */
public class Connect4Variation extends Variation {
	private static final long serialVersionUID = 3865530167190401014L;

	/* (non-Javadoc)
	 * @see kgorlen.games.Variation#print(kgorlen.games.GamePosition, java.lang.String)
	 */
	public void print(Position start, String indent) {
		System.out.print(indent);
		System.out.println("Variation score: " + Integer.toString(getScore())
								+ " move #" + Integer.toString(start.getPly()));
		int i;
		for (i=0; i<3; i++) {
			StringBuilder line = new StringBuilder();
			line.append(((Connect4Position) start).rowToString(i) + " ");
			Connect4Position p = new Connect4Position((Connect4Position) start);		
			for (int j=0; j<size(); j++) {
				((Connect4Position) p).makeMove(getMove(j));
				line.append(p.rowToString(i) + " ");
			}
			System.out.print(indent);
			System.out.println(line.toString());
		}
	}

}
