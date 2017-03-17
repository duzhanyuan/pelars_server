package pelarsServer;

import javafx.geometry.Point2D;

/**
 * 
 * @author Lorenzo Landolfi
 *	utility class to represent a rectangle in 2D space 
 */
public class Box {
	
	public int x;
	public int y;
	public int width;
	public int height;
	
	public Box(){
		
	}
	
	public Box(int x, int y, int width, int height){
		
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public Point2D[] getDiagonalPoints(){
		
		Point2D uppercorner = new Point2D(this.x, this.y);
		Point2D lowercorner = new Point2D(this.x + width, this.y - height);
		
		Point2D[] ret = new Point2D[2];
		ret[0] = uppercorner;
		ret[1] = lowercorner;
		
		return ret;
	}

}
