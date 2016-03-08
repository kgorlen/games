package kgorlen.games.snake;

public enum Direction {
	LEFT 	(-PlaySnake.CELL_SIZE, 0),
	RIGHT 	(+PlaySnake.CELL_SIZE, 0),
	UP 		(0, -PlaySnake.CELL_SIZE),
	DOWN 	(0, +PlaySnake.CELL_SIZE);
	
	private final int x,y;
	Direction(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public final int x() { return x; }
	public final int y() { return y; }
}
