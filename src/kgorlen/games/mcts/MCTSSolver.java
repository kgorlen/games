/**
 * 
 */
package kgorlen.games.mcts;

import java.util.Random;
import java.util.logging.Logger;

import kgorlen.games.Log;
import kgorlen.games.TreeSearch;

/**
 * @author Keith gorlen@comcast.net
 *
 */
public class MCTSSolver extends MCTS {
	private static final Logger LOGGER = Log.LOGGER;
	private static final String CLASS_NAME = TreeSearch.class.getName();
	
	/**
	 * @param c Upper Confidence Bounds for Trees (UCT) coefficient
	 * @param r instance of Random number generator
	 * @param ttCapacity Transposition HashMap initial capacity
	 */
	public MCTSSolver(double c, Random r, int ttCapacity) {
		super(c, r, ttCapacity);
	}

	/**
	 * @param c Upper Confidence Bounds for Trees (UCT) coefficient
	 * @param r instance of Random number generator
	 */
	public MCTSSolver(double c, Random r) {
		super(c, r);
	}

	/**
	 * @param c Upper Confidence Bounds for Trees (UCT) coefficient
	 */
	public MCTSSolver(double c) {
		super(c);
	}

	/**
	 * @param r instance of Random number generator
	 */
	public MCTSSolver(Random r) {
		super(r);
	}

	public MCTSSolver() {
		super();
	}
		
/*
 * 	Reference:
 * 	Mark H.M. Winands, Yngvi Bjornsson, and Jahn-Takeshi Saito,
 *  "Monte-Carlo Tree Search Solver"

	Integer MCTSSolver(Node N){
// Assumes N is not terminal position?

		if (playerToMoveWins(N))	// I.e. at least one child isWin()
			return INFINITY
		else (playerToMoveLoses(N)) // I.e. N.isWin()
			return -INFINITY

		bestChild = select(N)
// If bestChild is win for playerToMove, does bestChild.value == -INFINITY?
// What if one child is winner, and parent is root?
// What if all children are draws?
		N.visitCount++
		
		if (bestChild.value != -INFINITY AND bestChild.value != INFINITY)
			if(bestChild.visitCount == 0){
				R = -playOut(bestChild)
				addToTree(bestChild)	// I.e bestChild.score = R(?); bestChild.visitCount = 1
				goto DONE
			} else
				R = -MCTSSolver(bestChild)  // I.e. R = -INFINITY if playerToMove loses
		else
			R = bestChild.value	// I.e. +-INFINITY

		if (R == INFINITY){			// playerToMove wins
			N.value = -INFINITY
			return R
		} else
			if (R == -INFINITY){	// playerToMove loses
			
				foreach(child in getChildren(N))
					if (child.value != R){	// I.e. != -INFINITY
// At least one child is not loss for playerToMove
					R = -1
					goto DONE
				}
				
				N.value = INFINITY
				return R
			}
			
	DONE:
// -INFINITY < R < +INFINITY
		N.computeAverage(R)		// N.value += R?
		return R
	}
*/	
	
	public int mcts(MCTSPosition parent) throws MCTSSearchException {
		LOGGER.finer(() -> String.format(
				"%s{Entering %s.mcts of parent at ply %d, total/visits=%+d/%d:%n%s",
				indent, CLASS_NAME, parent.getPly(), parent.getScore(), parent.visits, parent.toString(indent) ));

		visited.add(depth, parent);
		
		if (parent.isWin()) {
			// if playerToMoveLoses return -INFINITY
			LOGGER.finer(() -> String.format(
					"%s}Exiting %s.mcts, playerToMove lost at ply %d, parent total/visits=%+d/%d, result=-INFINITY%n",
					indent, CLASS_NAME, parent.getPly(), parent.getScore(), parent.visits ));			
			return -SCORE_INFINITY;
		}

		if (parent.isDraw()) {
			assert parent.getScore() == 0 : "Drawn position with non-zero score: " + parent.getScore();
			LOGGER.finer(() -> String.format("%s}Exiting %s.mcts, draw at ply %d, parent total/visits=%+d/%d, result=0%n",
					indent, CLASS_NAME, parent.getPly(), parent.getScore(), parent.visits ));			
			return 0;
		}
		
// ***** EXPANSION *****
		if (parent.children == null) parent.expand(indent);	// <<< Expand >>>
// *****

// ***** SELECTION *****
		final MCTSPosition bestChild = select(parent);		// <<< Select >>>
// *****

		if (bestChild.isWin()) {	// At least one child is win for playerToMove
			bestChild.visits++;
			bestChild.setScore(SCORE_INFINITY);
			parent.setScore(-SCORE_INFINITY);
			if (depth == 0) {
				throw new MCTSSearchException("Next move from root position is win");
			}
			
			// if playerToMoveWins return +INFINITY
			LOGGER.finer(() -> String.format(
					"%s}Exiting %s.mcts, move %s to ply %d is win by %s, parent total/visits=%+d/%d, result=+INFINITY%n",
					indent, CLASS_NAME, bestChild.getMove().toString(), bestChild.getPly(), bestChild.sideLastMoved(),
					parent.getScore(), parent.visits ));
			return SCORE_INFINITY;
		}

		if (bestChild.isDraw()) {	// selected child is draw
			bestChild.setScore(0);
			bestChild.visits++;
			LOGGER.finer(() -> String.format(
					"%s}Exiting %s.mcts, move %s to ply %d is draw, parent total/visits=%+d/%d, result=0%n",
					indent, CLASS_NAME, bestChild.getMove().toString(), bestChild.getPly(),
					parent.getScore(), parent.visits ));			

			if (depth == 0) {
				for (MCTSPosition child : parent.children) {
					if (!child.isDraw()) return 0;						
				}
				parent.setScore(0);
				throw new MCTSSearchException("All moves from root position draw");
			}
			
			return 0;
		}

		LOGGER.finer(() -> String.format(
				"%sSelected move %s to ply %d with score %+d to position:%n%s",
				indent, bestChild.getMove().toString(), bestChild.getPly(),
				bestChild.getScore(), bestChild.toString(indent) ));
		
		visited.add(depth+1, bestChild);
		parent.visits++;
		int result;

		if (bestChild.getScore() != SCORE_INFINITY
				&& bestChild.getScore() != -SCORE_INFINITY) {
			// Selected child is not proven win or draw
			if (bestChild.visits == 0) {
// ***** SIMULATION *****
				result = bestChild.scoreSign() * bestChild.evaluate();
				bestChild.setScore(result);
				bestChild.visits = 1;
				parent.updateScore(-result);		// parent.computeAverage(score);
				positionsSearched++;
				LOGGER.finer(() -> String.format(
						"%s}Exiting %s.mcts, move %s to ply %d, parent total/visits=%+d/%d, playout result=%+d%n",
						indent, CLASS_NAME, bestChild.getMove().toString(), bestChild.getPly(),
						parent.getScore(), parent.visits, bestChild.getScore() ));
				return result;
// *****


			} else {
// ***** RECURSION *****
				LOGGER.finer(() -> String.format(
						"%sSearching move %s to ply %d with score %+d...%n",
						indent, bestChild.getMove().toString(), bestChild.getPly(), bestChild.getScore() ));
				if (++depth > maxDepth) maxDepth = depth;
				indent += "  ";
				final int searchResult = result = -mcts(bestChild);		// <<< Recurse >>>
				depth--;
				indent = indent.substring(2);
				LOGGER.finer(() -> String.format(
						"%s...Search of move %s to ply %d result=%+d%n",
						indent, bestChild.getMove().toString(), bestChild.getPly(), searchResult ));				
// *****
			}
		} else {
			// Selected child is proven win or (unproven) loss
			LOGGER.finer(() -> String.format(
					"%sSelected move %s to ply %d with score %+d is proven win or (unproven) loss%n",
					indent, bestChild.getMove().toString(), bestChild.getPly(), bestChild.getScore() ));
			result = bestChild.getScore();	// I.e. +-INFINITY
		}
		
// Here after (1) selection of non-terminal child scored +-INFINITY or (2) recursion
		assert bestChild.visits != 0 : "Child scored but not visited";
		if (result == SCORE_INFINITY) {  // playerToMove wins: mcts() returned -INFINITY
			parent.setScore(-SCORE_INFINITY);
			LOGGER.finer(() -> String.format(
					"%s}Exiting %s.mcts, move %s to ply %d, parent total/visits=%+d/%d, result=+INFINITY (at least one move wins)%n",
					indent, CLASS_NAME, bestChild.getMove().toString(), bestChild.getPly(), parent.getScore(), parent.visits ));
			if (depth == 0) throw new MCTSSearchException("Win from root position");
			
			return SCORE_INFINITY;
		}
		
		if (result == -SCORE_INFINITY) {// playerToMove loses--mcts() returned +INFINITY
			LOGGER.finer(() -> String.format(
					"%sChecking moves to ply %d for proven losses...%n",
					indent, bestChild.getPly() ));

			for (MCTSPosition child : parent.children) {
				LOGGER.finest(() -> String.format("%s  move %s to ply %d, total/visits=%+d/%d%n",
						indent, child.getMove().toString(), child.getPly(), child.getScore(), child.visits ));
				assert child.visits != 0 : "mcts() returned +INFINITY for partially evaluated position";
			}

			for (MCTSPosition child : parent.children) {
				if (child.getScore() != -SCORE_INFINITY) {
					parent.updateScore(1);	// parent.computeAverage(score);
					LOGGER.finer(() -> String.format(
							"%s}Exiting %s.mcts, move %s to ply %d, parent total/visits=%+d/%d, result=-1 (at least one move not loss)%n",
							indent, CLASS_NAME, bestChild.getMove().toString(), bestChild.getPly(), parent.getScore(), parent.visits ));
					return -1;			// At least one child is not loss for playerToMove
				}
			}
			
			parent.setScore(SCORE_INFINITY);
			LOGGER.finer(() -> String.format(
					"%s}Exiting %s.mcts, move %s to ply %d, parent total/visits=%+d/%d, result=-INFINITY (all moves lose)%n",
					indent, CLASS_NAME, bestChild.getMove().toString(), bestChild.getPly(), parent.getScore(), parent.visits ));
			if (depth == 0) throw new MCTSSearchException("Loss from root position");
			
			return -SCORE_INFINITY;	// All children are losses for playerToMove
		}

// Here after recursion returning -1, 0, or +1
		assert result > -SCORE_INFINITY && result < SCORE_INFINITY :
			"Score out of range: " + result;
		parent.updateScore(-result);	// parent.computeAverage(score);
		LOGGER.finer(() -> String.format(
				"%s}Exiting %s.mcts, move %s to ply %d, parent total/visits=%+d/%d, result=%+d%n",
				indent, CLASS_NAME, bestChild.getMove().toString(), bestChild.getPly(),
				parent.getScore(), parent.visits, result));
		return result;
	}

	/**
	 * References:
	 * http://www.ru.is/faculty/yngvi/pdf/WinandsBS08.pdf,
	 *   p.32, "Final Move Selection"
	 * 
	 * @param child position
	 * @return "secure child" value of position
	 */
    @Override
    double pvScore(MCTSPosition child) {
		final double A = 1.0;
		return child.getScore() + A/Math.sqrt(child.visits);
	}

}
