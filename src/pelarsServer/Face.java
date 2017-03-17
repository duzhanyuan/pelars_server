package pelarsServer;


import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import org.json.JSONException;
import org.json.JSONObject;

import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi
 * class representing a sampled the location of a face in 3D space
 *
 */

public class Face extends BaseData{

	//Just to test with a predefined value (Maybe this value should be captured by the client during session creation)
	public static double std_width = 200.0; //mm

	public Float pos_x0, pos_x1, pos_x2;
	public Float pos_y0, pos_y1, pos_y2;
	public Float pos_z0, pos_z1, pos_z2;

	//identifies a face in a single frame 
	public int num;
	public Double distance;

	public Face(){}

	public Face(Face f){
		super(f);
		this.pos_x0 = f.pos_x0;
		this.pos_x1 = f.pos_x1;
		this.pos_y0 = f.pos_y0;
		this.pos_y1 = f.pos_y1;
		this.pos_x2 = f.pos_x2;
		this.pos_y2 = f.pos_y2;
		this.pos_z0 = f.pos_z0;
		this.pos_z1 = f.pos_z1;
		this.pos_z2 = f.pos_z2;
		this.distance = f.distance;
		this.num = f.num;
	}

	public Float getX2() {
		return pos_x2;
	}

	public void setX2(Float pox_x2) {
		this.pos_x2 = pox_x2;
	}

	public Float getY2() {
		return pos_y2;
	}

	public void setY2(Float pos_y2) {
		this.pos_y2 = pos_y2;
	}

	public Float getX(){
		return pos_x0;
	}

	public void setX(Float x){
		this.pos_x0 = x;
	}

	public Float getX1(){
		return pos_x1;
	}

	public void setX1(Float x1){
		this.pos_x1 = x1;
	}

	public Float getY(){
		return pos_y0;
	}

	public void setY(Float y){
		this.pos_y0 = y;
	}

	public Float getY1(){
		return pos_y1;
	}

	public void setY1(Float y1){
		this.pos_y1 = y1;
	}

	public Float getPos_z0() {
		return pos_z0;
	}

	public void setPos_z0(Float pos_z0) {
		this.pos_z0 = pos_z0;
	}

	public Float getPos_z1() {
		return pos_z1;
	}

	public void setPos_z1(Float pos_z1) {
		this.pos_z1 = pos_z1;
	}

	public Float getPos_z2() {
		return pos_z2;
	}

	public void setPos_z2(Float pos_z2) {
		this.pos_z2 = pos_z2;
	}

	public int getNum(){
		return num;
	}

	public void setNum(int num){
		this.num = num;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public boolean equals(Face f){
		boolean base = super.equals(f);

		return (base && num == f.num && pos_x0 == f.pos_x0 && pos_x1 == f.pos_x1 && pos_y0 == f.pos_y0 && pos_y1 == f.pos_y1 && pos_z0 == f.pos_z0);
	}

	public boolean samePosition(Face f){

		Point2D f1 = this.getMedian();
		Point2D f2 = f.getMedian();

		return f1.equals(f2);
	}

	//returns the distance from camera. Unit of measure depends on the measure of width
	//N.B. focal for C920 for image at 640*480 is 489.3
	//     width is the width of the face e.g. 20cm 
	// in general it is focal length in pixels = (image with in pixels) * (focal length in mm) / (CCD width in mm)
	// C920 focal length = 3.67mm; CCD width = 4.80mm
	public double getDistanceFromCamera(double focal, double width){
		return (focal * width)/Math.abs(pos_x0 - pos_x1);
	}

	public double getDistanceFromC920(double width){
		return (width * Util.focal_length_pixel) / Math.abs(pos_x1 - pos_x0);
	}

	/**
	 * 
	 * @return
	 * N.B. the distance is in millimeters
	 */
	public double getDistanceFromC920(){
		return getDistanceFromC920(std_width);
	}

	public Point2D getPoint2D(int i){

		switch(i){

		case 0:
			return new Point2D(this.pos_x0, this.pos_y0);
		case 1: 
			return new Point2D(this.pos_x1, this.pos_y1);
		case 2: 
			return new Point2D(this.pos_x2, this.pos_y2);
		default:
			return null;
		}
	}

	public Point2D getMedian(){

		return new Point2D((this.pos_x1 + this.pos_x0)/2.0,(this.pos_y1 + this.pos_y0)/2.0);
	}

	public Point3D getMedian3D(){

		return new Point3D((this.pos_x1 + this.pos_x0)/2.0, (this.pos_y0 + this.pos_y1)/2.0, (this.pos_z0 + this.pos_z1)/2.0);
	}

	/**
	 * 
	 * @param 
	 * @return angle in degrees between this face and the parameter, ignoring the z-axis
	 */
	public double getAngle(BaseData other){
		Face f = (Face)other;
		return this.getMedian().angle(f.getMedian());
	}

	/**
	 * 
	 * @param 
	 * @return distance in meters between this face and the parameter
	 */
	public double getDistance(BaseData other){
		Face f = (Face)other;

		return this.getMedian3D().distance(f.getMedian3D());
	}

	public double getDistance2D(Face other){

		return this.getMedian().distance(other.getMedian());
	}

	/**returns true if the distance from camera is less than a threshold, for now the threshold is static. In the next future it must be 
	set for each session (maybe during a session client-server handshake).*/
	public boolean presence(){
		//distance should be retrieved from a marker
		if (this.distance < 1.7){
			return true;
		}
		else {
			return false;
		}
	}

	public JSONObject toJson(){
		JSONObject jo = super.toJson();
		try{
			jo.put("pos_x0", pos_x0);
			jo.put("pos_y0", pos_y0);
			jo.put("pos_z0", pos_z0);
			jo.put("pos_x1", pos_x1);
			jo.put("pos_y1", pos_y1);
			jo.put("pos_z1", pos_z1);
			jo.put("pos_x2", pos_x2);
			jo.put("pos_y2", pos_y2);
			jo.put("pos_z2", pos_z2);
			jo.put("num", num);
			jo.put("type", "face");
			//	jo.put("distance", getDistanceFromC920() / 1000);
			jo.put("distance", distance);
		}catch (JSONException e){
			e.printStackTrace();
		}
		return jo;
	}

	public String toString(){
		return this.toJson().toString();
	}

}
