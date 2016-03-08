package kgorlen.games.snake;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PlaySnake extends Application {
	public static final int CELL_SIZE = 10;
	public static final int ROWS = 30;
	public static final int COLUMNS = 30;
	public static final int GRID_WIDTH = COLUMNS * CELL_SIZE;
    public static final int GRID_HEIGHT  = ROWS * CELL_SIZE;
    public static final int SCORE_HEIGHT = 30;
    public static final int BORDERPANE_WIDTH = GRID_WIDTH + 2*CELL_SIZE;
    public static final int BORDERPANE_HEIGHT = CELL_SIZE + GRID_HEIGHT + SCORE_HEIGHT;
    public static final double SPEED_MS = 100;
    public static final Point UPPERLEFT = new Point(0,0);
    public static final Point LOWERRIGHT = new Point(GRID_WIDTH-1, GRID_HEIGHT-1);		

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Snake");
// Canvas for drawing apple and snake		
		Canvas canvas = new Canvas(GRID_WIDTH, GRID_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
// Stack Text on top of background rectangle
        StackPane scoreStack = new StackPane();
        Text score = new Text("Score: 0");
        scoreStack.getChildren().addAll(
        		new Rectangle(BORDERPANE_WIDTH, SCORE_HEIGHT, Color.LIGHTBLUE),
        		score);
// Layout canvas with borders
        BorderPane layout = new BorderPane();
		layout.setTop(new Rectangle(BORDERPANE_WIDTH, CELL_SIZE, Color.LIGHTBLUE));
		layout.setLeft(new Rectangle(CELL_SIZE, GRID_HEIGHT, Color.LIGHTBLUE));
		layout.setRight(new Rectangle(CELL_SIZE, GRID_HEIGHT, Color.LIGHTBLUE));
		layout.setBottom(scoreStack);
        layout.setCenter(canvas);
	    
	    Snake snake = new Snake(gc);
	    Apple apple = new Apple(gc);
        apple.move(snake);
	    
	    Scene scene = new Scene(layout, BORDERPANE_WIDTH, BORDERPANE_HEIGHT);
	    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				snake.keyPressed(event);
			}	    	
	    });

	    primaryStage.setScene(scene);
	    primaryStage.show();
	    
	    Timeline timeline = new Timeline();
	    timeline.setCycleCount(Timeline.INDEFINITE);
	    timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(SPEED_MS),
                  new EventHandler<ActionEvent>() {
                    // KeyFrame event handler
                    public void handle(ActionEvent event) {
                    	if (!snake.addHead() || snake.eats(apple)) {
                        	score.setText("Score: " + snake.getScore());
                            apple.move(snake);
                    	}
                    }
                }));
        timeline.playFromStart();
	}
}