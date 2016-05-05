package kgorlen.games.connect4;

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
		Connect4Position start = (Connect4Position) getStart();
		StringBuilder s = new StringBuilder();
		s.append(String.format("%s#%-2d %s %-+7d:",
				indent, start.getPly(), getMove(0), getScore()));
		s.append(indent + " ");
		for (int j=1; j<size(); j++) {
			s.append(String.format("#%-2d %s:          ",
					start.getPly()+j, getMove(j).toString()));
		}
		s.append("\n");
		for (int i=Connect4Position.ROWS; i>0; i--) {
			s.append(indent + i);
			Connect4Position p = new Connect4Position((Connect4Position) start);		
			for (int j=0; j<size(); j++) {
				((Connect4Position) p).makeMove(getMove(j));
				s.append(p.rowToString(i) + "  ");
			}
			s.append("\n");
		}
		s.append(indent + " ");
		for (int j=0; j<size(); j++) s.append(" a b c d e f g   ");
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
