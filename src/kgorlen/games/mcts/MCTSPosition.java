/**
 * 
 */
package kgorlen.games.mcts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import kgorlen.games.GamePosition;
import kgorlen.games.Log;
import kgorlen.games.Move;
import kgorlen.games.MoveGenerator;
import kgorlen.games.Position;
import kgorlen.games.ScoreType;
import kgorlen.games.TTEntry;
import kgorlen.games.TreeSearch;
import kgorlen.games.Variation;

/**
 * @author Keith gorlen@comcast.net
 * 
 * References:
 * https://jeffbradberry.com/posts/2015/09/intro-to-monte-carlo-tree-search/
 * http://www.cameronius.com/research/mcts/about/index.html
 * http://scalab.uc3m.es/~seminarios/seminar11/slides/lucas2.pdf
 * http://mcts.ai/code/java.html
 * https://spin.atomicobject.com/2015/12/12/monte-carlo-tree-search-algorithm-game-ai/
 * https://github.com/PetterS/monte-carlo-tree-search
 * https://github.com/memo/ofxMSAmcts
 *
 */
public abstract class MCTSPosition implements GamePosition {
	private final static Logger LOGGER = Log.LOGGER;

	protected Move move;			// Move from parent to this child
	List<MCTSPosition> children;
	private int score;				// TODO: remove private, getScore()/setScore()/updateScore()
	int visits;

	/**
	 * Constructor
	 */
	public MCTSPosition() {
		reset();
	}

	/**
	 * Copy constructor
	 * 
	 * @param n node to copy.  Note: children are shallow copies.
	 */
	public MCTSPosition(MCTSPosition n) {
		children = n.children;
		visits = n.visits;
		score = n.score;
	}

	public void reset() {
		move = null;
		children = null;
		visits = 0;
		score = 0;    	
	}

	public Move getMove() {
		return move;
	}

	public int getScore() {
		return score;
	}

	/**
	 * @param score the score to set
	 */
	void setScore(int score) {
		this.score = score;
	}

	/**
	 * @param score the score to add to the current score
	 * 				range -1 to +1
	 */
	void updateScore(int score) {
		assert score >= -1 && score <= 1 :
			"Invalid score: " + score;
		assert this.score != TreeSearch.SCORE_INFINITY &&
				this.score != -TreeSearch.SCORE_INFINITY :
			"Invalid score update; current score: " + this.score;
		this.score += score;
	}

	int getVisits() {
		return visits;
	}

	public void addChild(MCTSPosition child) {
		if (children == null) children = new LinkedList<MCTSPosition>();
		children.add(child);
	}

	/**
	 * @param move Move from this position
	 * @return Child Position for specified Move
	 */
// TODO: @Override
	public Position getChild(Move move) {
		for (MCTSPosition child : children) {
			if (move.equals(child.move)) return child;
		}
		throw new RuntimeException(String.format("Move %s not found", move.toString()));
	} 
	
	public void expand(String indent) {
		MoveGenerator gen = moveGenerator();
		LOGGER.finer(() -> String.format("%sExpanding at ply %d...%n",
				indent, getPly() ));
		while (gen.hasNext()) {
			Move move = gen.next();
			MCTSPosition child = (MCTSPosition) copy();
			child.makeMove(move);
			addChild(child);
			LOGGER.finest(() -> String.format("%s  Added child of move %s to ply %d%n",
					indent, move.toString(), child.getPly() ));
		}
		assert children != null :
			"Failed to generate children of non-terminal position";
		LOGGER.finer(() -> String.format("%s... %d children added at ply %d%n",
				indent, children.size(), getPly() ));		
	}
	
	public void expand() {
		expand("");
	}
	
	/* (non-Javadoc)
	 * @see kgorlen.games.Position#evaluate()
	 */
	@Override
	public int evaluate() {
		assert this.getScore() == 0 && this.visits == 0 && this.children == null:
			"Evaluation of previously visited position attempted";
		MCTSPosition parent = this;

		while (true) {
			List<MCTSPosition> positions = new ArrayList<MCTSPosition>();
			MoveGenerator gen = parent.moveGenerator();		
			assert gen.hasNext() : "Attempt to evaluate terminal position";

			while (gen.hasNext()) {
				Move move = gen.next();
				MCTSPosition child = (MCTSPosition) parent.copy();
				child.makeMove(move);

				if (child.isWin()) {
					final int winScore = child.scoreWin();
					LOGGER.finer(() -> String.format(
							"Playout move %s to ply %d is win by %s, score %+d:%n%s",
							move.toString(), child.getPly(), child.sideLastMoved(), winScore,
							child.toString() ));
					return winScore;
				}

				positions.add(child);
			}

			MCTSPosition child = positions.get(MCTS.randGen.nextInt(positions.size()));

			LOGGER.finest(() -> String.format(
					"%s playing move %s to ply %d...%n%s",
					child.sideLastMoved(), child.getMove().toString(), child.getPly(), child.toString() ));

			if (child.isDraw()) {
				final int drawScore = this.scoreSign() * child.scoreDraw();
				LOGGER.finer(() -> String.format(
						"Playout move %s to ply %d by %s is draw, score %+d:%n%s",
						child.getMove().toString(), child.getPly(), child.sideLastMoved(), drawScore,
						child.toString() ));
				return drawScore;
			}
			parent = child;
		}
	}

	/**
	 * @return	standard deviation of win percentage
	 * 			as per Rule of Sample Proportions.
	 * 			Note: 95% confidence interval is +- 2*stdDev()
	 */
	public double getStdDev() {
		double prop = ((double) score)/visits;
		double stddev = Math.sqrt((prop * (1-prop))/visits);
		return 100*stddev/visits;
	}

	/**
	 * @return 95% confidence interval of win percentage (+- 2 * standard deviation)
	 */
	public double getcConfInt95() {
		return 2*getStdDev();
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#newTTentry(int, int, kgorlen.games.Move)
	 */
	@Override
	public abstract TTEntry newTTentry(int depth, int score, Move bestMove);

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#getPly()
	 */
	@Override
	public abstract int getPly();

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#isValidMove(kgorlen.games.Move)
	 */
	@Override
	public abstract boolean isValidMove(Move m);

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#makeMove(kgorlen.games.Move)
	 */
	@Override
	public abstract void makeMove(Move m);

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#moveGenerator(boolean)
	 */
	@Override
	public abstract MoveGenerator moveGenerator();

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#moveGenerator(kgorlen.games.Move[], boolean)
	 */
	@Override
	public MoveGenerator moveGenerator(Move[] killers) {
		return moveGenerator();
	}

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#newVariation()
	 */
	@Override
	public abstract Variation newVariation();

	/* (non-Javadoc)
	 * @see kgorlen.games.Position#scoreSign()
	 */
	@Override
	public abstract int scoreSign();

	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#copy()
	 */
	@Override
	public abstract GamePosition copy();

	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#newTTentry(int, kgorlen.games.ScoreType, int, kgorlen.games.Move)
	 */
	@Override
	public TTEntry newTTentry(int depth, ScoreType scoreType, int score, Move bestMove) {
		return newTTentry(depth, score, bestMove);
	};

	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#sideToMove()
	 */
	@Override
	public abstract String sideToMove();

	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#isWin()
	 */
	@Override
	public abstract boolean isWin();

	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#isDraw()
	 */
	@Override
	public abstract boolean isDraw();

	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#scoreWin()
	 */
	@Override
	public abstract int scoreWin();

	/* (non-Javadoc)
	 * @see kgorlen.games.GamePosition#scoreDraw()
	 */
	@Override
	public abstract int scoreDraw();

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public abstract int hashCode();

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public abstract boolean equals(Object obj);

}
