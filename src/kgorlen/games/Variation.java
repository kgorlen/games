package kgorlen.games;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Manages a sequence of Moves, implemented with an ArrayList.
 * 
 * @author Keith gorlen@comcast.net
 *
 */
public class Variation extends ArrayList<Move> {
	private static final Logger LOGGER = Log.LOGGER;
	private static final long serialVersionUID = 1L;

	private Position start;		// Initial Position of Variation
	private int score;			// Score of this sequence of Moves

	public Variation(Position start, int score) {
		this.start = start;
		this.score = score;
	}
	
	public Variation(Position start) {
		this.start = start;
		this.score = 0;
	}
		
	/**
	 * @return the start Position
	 */
	public Position getStart() {
		return start;
	}

	/**
	 * @param set the start Position
	 */
	public void setStart(Position start) {
		this.start = start;
	}

	/**
	 * @return the score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(int score) {
		this.score = score;
	}

	public int numMoves() {
		return size();
	}

	public Move getMove(int i) {
		return get(i);
	}

	public Move getMove() {
		return get(0);
	}

	public void addMove(Move move) {
		add(move);
	}

	public void clear() {
		start = null;
		score = 0;
		super.clear();
	}

	/**
	 * @param row of Positions to be converted to string
	 * @return string representation of specified row of Variation Positions
	 */
	public String lineToString(int row) {
		StringBuilder line = new StringBuilder();
		line.append(start.rowToString(row) + " ");
		Position p = start.copy();		
		for (int j=0; j<size(); j++) {
			p.makeMove(getMove(j));
			line.append(p.rowToString(row) + " ");
		}
		return line.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + score;
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Variation))
			return false;
		Variation other = (Variation) obj;
		if (score != other.score)
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}

	/**
	 * Printable move variation with indentation.  Useful for formatting tree
	 * search debug printout to indicate depth.
	 * 
	 * @param indent string of spaces prepended to each line 
	 * @return printable, indented String representation of Variation
	 */
	public String toString(String indent) {
		StringBuilder s = new StringBuilder();
		for (Move move : this) {
			s.append(indent + move.toString() + "\n");			
		}
		s.append(String.format("%sscore=%d%n", indent, score));
		return s.toString();
	}

	/**
	 * @return printable String representation of Variation
	 */
	public String toString() {
		return toString("");
	};
	
	public static void logPrincipalVariation(Variation pvar, String className) {
		if (pvar == null) {
			LOGGER.warning(String.format("WARNING: %s.search found no principal variation%n", className));
		} else {
			LOGGER.info(String.format("  Principal variation:%n%s", pvar.toString("  ")));
		}
	}
	
	/**
	 * @param start Position of Variation
	 * @param indent string to prepend to each line
	 */
	public void print(Position start, String indent) {
		System.out.print(toString(indent));
	}

	/**
	 * @param start Position of Variation
	 */
	public void print(Position start) {
		System.out.print(toString());
	}

}
