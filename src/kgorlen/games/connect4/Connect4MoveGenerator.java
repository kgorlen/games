/**
 * 
 */
package kgorlen.games.connect4;

import java.util.NoSuchElementException;

import kgorlen.games.Move;
import kgorlen.games.MoveGenerator;

/**
 * @author Keith gorlen@comcast.net
 *
 */
public class Connect4MoveGenerator implements MoveGenerator {
	private long moves;		// bitmask of legal moves

	/**
	 * Initialize move generator
	 * 
	 * @param p	starting/current GamePosition
	 * @param debug enable debug output
	 */
	public Connect4MoveGenerator(Connect4Position p) {
		moves = p.moves();
	}
		
	/* (non-Javadoc)
	 * @see kgorlen.games.MoveGenerator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return moves != 0;
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.MoveGenerator#next()
	 */
	@Override
	public Move next() {
		if (moves == 0) throw new NoSuchElementException();
		
		long nextMove = moves & ~(moves-1);	// select next move
		moves ^= nextMove;					// mark as returned
		return new Connect4Move(nextMove);
	}

}
