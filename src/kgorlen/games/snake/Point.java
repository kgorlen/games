/**
 * 
 */
package kgorlen.games.snake;

public class Point
{
	private int x;
	private int y;

	public Point(int xValue, int yValue){
		x = xValue;
		y = yValue;
	}
	
	public Point(Point p){
		this(p.x, p.y);
	}

	public Point(){
		this(0, 0);
	}

	public void setX(int xValue){
		x = xValue;
	}

	public int getX(){
		return x;
	}

	public void setY(int xValue){
		y = xValue;
	}

	public int getY(){
		return y;
	}

	public void translate(int dx, int dy) {
		x += dx;
		y += dy;
	}
	
	public boolean inBounds(Point upperLeft, Point lowerRight) {
		return x >= upperLeft.x && y >= upperLeft.y
				&& x <= lowerRight.x && y <= lowerRight.y;
	}
	
	public String toString(){
		return "(" + x + ", " + y + ")";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Point))
				return false;
			Point other = (Point) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}

}