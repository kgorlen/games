package kgorlen.games.snake;

import java.util.LinkedList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class Snake extends LinkedList<Point> {
	private static final long serialVersionUID = -636672206807628650L;
	private Direction direction = Direction.UP;
    private GraphicsContext gc;

    Snake(GraphicsContext gc) {
    	this.gc = gc;
    	reset();
    }
    
    public void reset(){
    	gc.clearRect(0, 0, PlaySnake.GRID_WIDTH, PlaySnake.GRID_HEIGHT);
    	clear();
	    direction = Direction.UP;
	    addFirst(new Point(PlaySnake.COLUMNS/2 * PlaySnake.CELL_SIZE,
      		  PlaySnake.ROWS/2 * PlaySnake.CELL_SIZE));
	    addHead();
	    addHead();
        return;
    }
    
    public int getScore(){
        return size()-3;
    }

    public boolean addHead(){
    	Point head = new Point(getFirst());
    	head.translate(direction.x(), direction.y());
        if (!head.inBounds(PlaySnake.UPPERLEFT, PlaySnake.LOWERRIGHT)
        		|| contains(head)) {
        	reset();
        	return false;
        }

        addFirst(head);
// Draw the head       
        gc.setFill(Color.GREEN);
        gc.fillOval(head.getX(), head.getY(),
        		PlaySnake.CELL_SIZE, PlaySnake.CELL_SIZE);
        return true;
    }

    public boolean eats(Apple apple){
        if (getFirst().equals(apple)) return true;
        
        Point tail = removeLast();
// Erase the tail
    	
// fillOval() leaves a green outline???
//     	gc.setFill(Color.WHITE);
//     	gc.fillOval(tailX[size()-1], tailY[size()-1],
//       		PlaySnake.CELL_SIZE, PlaySnake.CELL_SIZE);

// clearRect() of entire cell overlaps cell above and cell left???
     	gc.clearRect(tail.getX()+1, tail.getY()+1,
     			PlaySnake.CELL_SIZE-1, PlaySnake.CELL_SIZE-1);
        return false;
    }
    
    public void keyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case D:
            case RIGHT:
                if (direction != Direction.LEFT)
                    direction = Direction.RIGHT;
                break;
            case A:
            case LEFT:
                if (direction != Direction.RIGHT)
                    direction = Direction.LEFT;
                break;
            case W:
            case UP:
                if (direction != Direction.DOWN)
                    direction = Direction.UP;
                break;
            case S:
            case DOWN:
                if (direction != Direction.UP)
                    direction = Direction.DOWN;
                break;
            case ESCAPE:
                System.exit(0);
                break;
            default:
        }
    }

}