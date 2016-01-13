package kgorlen.games;

/**
 * A MoveGenerator returns Move instances, or null when no move possible.
 * 
 * @author Keith gorlen@comcast.net
 *
 */

public interface MoveGenerator {
	public boolean hasNext();
	public Move next();
}
