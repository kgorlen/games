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

	public Connect4Variation(Connect4Position start) {
		super(start);
	}
	
	public Connect4Variation(Connect4Position start, int score) {
		super(start, score);
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Variation#print(kgorlen.games.GamePosition, java.lang.String)
	 */
	@Override
	public String toString(String indent) {
		Position start = getStart();
		StringBuilder s = new StringBuilder();
		s.append(String.format("%sVariation score: %d move #%d%n",
				indent, getScore(), start.getPly()+1 ));
		for (int i=1; i<=Connect4Position.ROWS; i++) {
			s.append(indent + i + ((Connect4Position) start).rowToString(i));
			Connect4Position p = new Connect4Position((Connect4Position) start);		
			for (int j=0; j<size(); j++) {
				((Connect4Position) p).makeMove(getMove(j));
				s.append("  " + p.rowToString(i));
			}
			s.append("\n");
		}
		s.append(indent + "  A B C D E F G");
		for (int j=0; j<size(); j++) s.append("    A B C D E F G");
		s.append("\n");
		return s.toString();
	}

	public void print(String indent) {
		System.out.print(toString(indent));
	}
	
	public void print() {
		System.out.print(toString());
	}
	
}
