package pelarsServer;

import javafx.geometry.Point2D;

public class Feature2D {
	
	public Point2D point;
	public String id_s;
	public long id;
	public Point2D getPoint() {
		return point;
	}
	public void setPoint(Point2D point) {
		this.point = point;
	}
	public String getIds() {
		return id_s;
	}
	public void setIds(String ids) {
		this.id_s = ids;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

}
