package kgorlen.games;

/**
 * Score types for alpha-beta search
 * 
 * Reference:
 * 	https://chessprogramming.wikispaces.com/Node+Types
 * 
 * @author Keith gorlen@comcast.net
 *
 */
public enum ScoreType {
	INVALID,
	EXACT,			// Exact score, from a principal variation node
	LOWERBOUND,		// Lower bound, from a Cut Node: one move scored >= beta
	UPPERBOUND;		// Upper bound, from an All Node: all moves scored <= alpha
	public static final ScoreType[] values = values();
}
