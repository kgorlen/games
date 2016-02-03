package kgorlen.games;

public interface TTEntry {
	
	public int getDepth();
	
	public ScoreType getScoreType();
	
	public int getScore();
	
	public Move getMove();
	
	public boolean isPrincipalVariation();
}
