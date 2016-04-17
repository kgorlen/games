/**
 * 
 */
package kgorlen.games.mcts;

/**
 * @author Keith gorlen@comcast.net
 * 
 * Exceptions thrown by MCTS selection method
 */
@SuppressWarnings("serial")
public class MCTSSearchException extends Exception {
	public MCTSSearchException(String message) {
		super(message);
	}
}
