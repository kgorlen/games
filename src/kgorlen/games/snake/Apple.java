package kgorlen.games.snake;

import java.util.Random;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Apple extends Point {
    private Random r = new Random();
    private GraphicsContext gc;
    
    Apple(GraphicsContext gc) {
    	this.gc = gc;
    }
    
    /**
     * @return random cell X coordinate
     */
    private final int randX() {
    	return r.nextInt(PlaySnake.COLUMNS) * PlaySnake.CELL_SIZE;
    }

    /**
     * @return random cell Y coordinate
     */
    private final int randY() {
    	return r.nextInt(PlaySnake.ROWS) * PlaySnake.CELL_SIZE;
    }
    
    /**
     * @param snake	Snake to avoid when moving this Apple
     */
    public void move(Snake snake){
        do {
        	setX(randX());
            setY(randY());
        } while (snake.contains(this));

        gc.setFill(Color.RED);
        gc.fillOval(getX(), getY(), PlaySnake.CELL_SIZE, PlaySnake.CELL_SIZE);
    }

}
