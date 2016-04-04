/**
 * 
 */
package kgorlen.games;

/**
 * @author Keith gorlen@comcast.net
 * 
 * Exceptions thrown by MCTS selection method
 */
@SuppressWarnings("serial")
public class MCTSSelectException extends Exception {
	public MCTSSelectException(String message) {
		super(message);
	}
}
