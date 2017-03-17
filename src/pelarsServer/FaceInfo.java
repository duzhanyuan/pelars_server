package pelarsServer;

import javafx.geometry.Point3D;

public class FaceInfo {
	
	public Feature2D[] features;
	public boolean eyeclosure_left;
	public boolean eyeclosure_right;
	public int facescale;
	public Point3D gaze_direction;
	public Point3D translation;
	public MultimediaContent image;
	
	public MultimediaContent getImage() {
		return image;
	} 
	public void setImage(MultimediaContent image) {
		this.image = image;
	}
	public Feature2D[] getFeatures() {
		return features;
	}
	public void setFeatures(Feature2D[] features) {
		this.features = features;
	}
	public boolean isEyeclosure_left() {
		return eyeclosure_left;
	}
	public void setEyeclosure_left(boolean eyeclosure_left) {
		this.eyeclosure_left = eyeclosure_left;
	}
	public boolean isEyeclosure_right() {
		return eyeclosure_right;
	}
	public void setEyeclosure_right(boolean eyeclosure_right) {
		this.eyeclosure_right = eyeclosure_right;
	}
	public int getFacescale() {
		return facescale;
	}
	public void setFacescale(int facescale) {
		this.facescale = facescale;
	}
	public Point3D getGaze_direction() {
		return gaze_direction;
	}
	public void setGaze_direction(Point3D gaze_direction) {
		this.gaze_direction = gaze_direction;
	}
	public Point3D getTranslation() {
		return translation;
	}
	public void setTranslation(Point3D translation) {
		this.translation = translation;
	}

}
