package kgorlen.games.tictactoe;

import kgorlen.games.Variation;
import kgorlen.games.Move;
import kgorlen.games.Position;

/**
 * Representation of a sequence of Tic-Tac-Toe moves
 * 
 * @author Keith gorlen@comcast.net
 *
 */
class TicTacToeVariation extends Variation {
	private static final long serialVersionUID = 1L;

	TicTacToeVariation(TicTacToePosition start) {
		super(start);
	}
	
	TicTacToeVariation(TicTacToePosition start, int score) {
		super(start, score);
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Variation#toString(java.lang.String)
	 */
	@Override
	public String toString(String indent) {
		Position start = getStart();
		StringBuilder s = new StringBuilder();
		for (int i=0; i<3; i++) {
			s.append(indent);
			TicTacToePosition p = new TicTacToePosition((TicTacToePosition) start);	
			for (Move move : this) {
				p.makeMove(move);
				s.append(p.rowToString(i));
				s.append(" ");
			}
			s.append("\n");
		}
		s.append(String.format("%sscore=%d%n", indent, getScore()));
		return s.toString();
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Variation#print(kgorlen.games.GamePosition, java.lang.String)
	 */
	public void print(String indent) {
		System.out.print(toString(indent));
	}

 }
