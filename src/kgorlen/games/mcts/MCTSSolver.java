/**
 * 
 */
package kgorlen.games.mcts;

import java.util.Random;
import java.util.logging.Logger;

import kgorlen.games.Log;
import kgorlen.games.Move;
import kgorlen.games.MoveGenerator;
import kgorlen.games.Position;
import kgorlen.games.TreeSearch;
import kgorlen.games.Variation;

/**
 * @author Keith gorlen@comcast.net
 *
 */
public class MCTSSolver extends TreeSearch {
	private static final Logger LOGGER = Log.LOGGER;
	private static final String CLASS_NAME = TreeSearch.class.getName();
	
	private static Random randNum;

	/**
	 * @param r instance of Random number generator
	 * @param ttCapacity Transposition HashMap initial capacity
	 */
	public MCTSSolver(Random r, int ttCapacity) {
		super(ttCapacity);
		randNum = r;
	}

	/**
	 * @param r instance of Random number generator
	 */
	public MCTSSolver(Random r) {
		super();
		randNum = r;
	}

	/**
	 * 
	 */
	public MCTSSolver() {
		super();
		randNum = new Random();
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.TreeSearch#search(kgorlen.games.Position, int)
	 */
	@Override
	public Variation search(Position root, int limit) {
		assert !((MCTSPosition) root).isWin() && !((MCTSPosition)root).isDraw() :
			"root is terminal position";
		setRoot(root);
		elapsedTime();
		
		try {
			for (int i=0; i < limit; i++) {		// TODO: Change limit to elapsed time
			search((MCTSPosition) root);
			}
		}catch(MCTSSearchException e) {
			LOGGER.fine(() -> e.toString());
		}

		elapsedTime();
		logStatistics(CLASS_NAME);
		Variation pvar = getPrincipalVariation();
		Variation.logPrincipalVariation(pvar, CLASS_NAME);
		return pvar;
	}
	
/*	Reference:
 * 	Mark H.M. Winands, Yngvi Bjornsson, and Jahn-Takeshi Saito,
 *  "Monte-Carlo Tree Search Solver"

	Integer MCTSSolver(Node N){
// Assumes N is not terminal position?

// But need to generate child positions to determine if winner,
//   and may as well add them to tree.
		if (playerToMoveWins(N))
			return INFINITY
		else (playerToMoveLoses(N))
			return -INFINITY

		bestChild = select(N)
// If bestChild is win for playerToMove, does bestChild.value == -INFINITY?
// What if one child is winner, and parent is root?
// What if all children are draws?
		N.visitCount++
		
		if (bestChild.value != -INFINITY AND bestChild.value != INFINITY) {
			if(bestChild.visitCount == 0){
				R = -playOut(bestChild)
// See above comment re: adding all children to tree
				addToTree(bestChild)
				goto DONE
			} else
				R = -MCTSSolver(bestChild)  // I.e. R = -INFINITY if playerToMove loses
		} else
			R = bestChild.value	// I.e. +-INFINITY

		if (R == INFINITY){
			N.value = -INFINITY
			return R
		} else
			if (R == -INFINITY){
			
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
	
	public int search (MCTSPosition parent) throws MCTSSearchException {
		int result;

//  if (playerToMoveLoses(N)) return -INFINITY
		if (parent.isWin()) return -TreeSearch.SCORE_INFINITY;

		if (parent.isDraw()) return parent.scoreSign() * parent.scoreDraw();
		
		if (parent.children == null) {
    		MoveGenerator gen = parent.moveGenerator();		
    		while (gen.hasNext()) {
    			Move move = gen.next();
    			MCTSPosition child = (MCTSPosition) parent.copy();
    			child.makeMove(move);
    			parent.addChild(child);
    		}
    		assert parent.children != null :
    			"Failed to generate children of non-terminal position";
		}

		MCTSPosition bestChild = select(parent);
		if (bestChild == null) {
			return parent.scoreSign() * parent.scoreDraw();
		}

// if (playerToMoveWins(N)) return INFINITY
		if (bestChild.isWin()) return TreeSearch.SCORE_INFINITY;

		parent.visits++;
		if (bestChild.score != TreeSearch.SCORE_INFINITY
				&& bestChild.score != -TreeSearch.SCORE_INFINITY) {
			if (bestChild.visits == 0) {
				result = -bestChild.evaluate();
				parent.score += result;	// parent.computeAverage(score);
				return result;

			} else {
				result = search(bestChild);
			}
		} else {
			result = bestChild.score;
		}
		
		if (result == TreeSearch.SCORE_INFINITY) {
			parent.score = -TreeSearch.SCORE_INFINITY;
			return result;
		} if (result == -TreeSearch.SCORE_INFINITY) {
			for (MCTSPosition child : parent.children) {
				if (child.score != -TreeSearch.SCORE_INFINITY) {
					result = -1;
					break;
				}
			}
			
			parent.score = TreeSearch.SCORE_INFINITY;
			return result;
		}

		assert result > -TreeSearch.SCORE_INFINITY && result < TreeSearch.SCORE_INFINITY :
			"Score out of range: " + result;
		parent.score += result;	// parent.computeAverage(score);
		return result;
	}

	/**
     * Select child node to expand
     * 
     * References:
     *   http://mcts.ai/code/java.html
     *   http://scalab.uc3m.es/~seminarios/seminar11/slides/lucas2.pdf
     * 
     * EPSILON small random number 0-EPSILON to break ties in unexpanded nodes,
     * i.e. when visits = 0.  1/EPSILON > SCORE_INFINITY
     * 
     * @param visited list of nodes visited from root to this node
     * @param debug switch
     * @return node to expand, or null if all children draws
     */
    private static final double C = Math.sqrt(2);	// Upper confidence bound for Trees (UCT) coefficient
    private static final double EPSILON = 1.0/(10.0*TreeSearch.SCORE_INFINITY);

    public MCTSPosition select(MCTSPosition parent) {
        MCTSPosition selected = null;
        double bestValue = Double.MIN_VALUE;
        for (MCTSPosition c : parent.children) {
        	if (c.isWin()) {
        		c.score = -TreeSearch.SCORE_INFINITY;
        		return c;
        	}
        	if (c.isDraw()) {
        		c.score = c.scoreSign() * c.scoreDraw();
        		continue;
        	}
            double uctValue =
                    c.score / (c.visits + EPSILON) +
                            C * Math.sqrt(Math.log(parent.visits+1) / (c.visits + EPSILON)) +
                            randNum.nextDouble() * EPSILON;
            if (uctValue > bestValue) {
                selected = c;
                bestValue = uctValue;
            }
        }
        return selected;
    }
	
}
