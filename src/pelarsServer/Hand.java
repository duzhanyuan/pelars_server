package pelarsServer;

import javafx.geometry.Point2D;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Lorenzo Landolfi, Giacomo Dabisias
 * class containing the positions of ARUCO markers in 3D space 
 *
 */
public class Hand extends BaseData {

	//for now the marker position is static but it must be captured by the client during an handshake phase
	public static final float threshold_x = (float) 0.70;
	public static final float threshold_y = (float) 0.70;

	//identifier of the ARUCO marker
	public int num;
	public Float tx;
	public Float ty;
	public Float tz;
	public Float rx;
	public Float ry;
	public Float rz;
	public Float rw;


	public Hand(){}

	public Hand(Hand other){
		super(other);
		this.num = other.num;
		this.tx = other.tx;
		this.ty = other.ty;
		this.tz = other.tz;
		this.rx = other.rx;
		this.ry = other.ry;
		this.rz = other.rz;
		this.rw = other.rw;
		//	this.open = other.open;
	}

	public boolean tDiff(Hand other, float thresh){
		if (Math.abs(this.tx - other.tx) > thresh){
			return true;
		}
		if (Math.abs(this.ty - other.ty) > thresh){
			return true;
		}
		if (Math.abs(this.tz - other.tz) > thresh){
			return true;
		}
		return false;
	}

	public int getNum(){
		return this.getnum();
	}

	public int getnum(){
		return num;
	}

	public void setnum(int num){
		this.num = num;
	}

	public Float getTx(){
		return tx;
	}

	public void setTx(Float pos_x){
		this.tx = pos_x;
	}

	public Float getTy(){
		return ty;
	}

	public void setTy(Float pos_y){
		this.ty = pos_y;
	}

	public Float getTz(){
		return tz;
	}

	public void setTz(Float pos_z){
		this.tz = pos_z;
	}

	/*	public boolean isOpen(){
		return open;
	}

	public void setOpen(boolean open){
		this.open = open;
	}

	public boolean getOpen(){
		return open;
	}
	 */	
	public Float getRx(){
		return rx;
	}

	public void setRx(Float f){
		rx = f;
	}

	public Float getRy(){
		return ry;
	}

	public void setRy(Float f){
		ry = f;
	}

	public Float getRz(){
		return rz;
	}

	public void setRz(Float f){
		rz = f;
	}

	public Float getRw(){
		return rw;
	}

	public void setRw(Float f){
		rw = f;
	}

	public String to_string(){
		return "id: " + this.num + ", position:  " + this.tx + "; " + this.ty +
				"; " + this.tz + ", time:  "  + this.time;
	}

	public boolean equals(Hand h){
		boolean b = super.equals(h);

		return (b && rw == h.rw && rz == h.rz && ry == h.ry && rx == h.rx && num == h.num && tx == h.tx && ty == h.ty && tz == h.tz);
	}

	/**
	 * must return true if the hands are enough close to the marker delimiting the desk
	 */
	public boolean presence(){
		//get if the module is greater than a threshold
		Float distance = (float) Math.sqrt((Math.pow((double)tx, 2) + Math.pow((double)ty, 2) + Math.pow((double)tz, 2)));
		return distance < 1.3;
	}

	public double distance(Hand other){

		return Math.sqrt(Math.pow(this.tx - other.tx,2) + Math.pow(this.ty - other.ty, 2) + Math.pow(this.tz - other.tz, 2));
	}

	public double getDistance(BaseData other){
		Hand h = (Hand)other;
		return this.distance(h);
	}

	public double getAngle(BaseData other){

		Point2D p1 = new Point2D(this.tx, this.ty);
		Hand hother = (Hand)other;
		Point2D p2 = new Point2D(hother.tx, hother.ty);

		return p1.angle(p2);
	}

	public JSONObject toJson(){
		JSONObject jo = super.toJson();
		try{
			jo.put("tx", tx);
			jo.put("ty", ty);
			jo.put("tz", tz);
			jo.put("rx", rx);
			jo.put("ry", ry);
			jo.put("rz", rz);
			jo.put("rw", rw);
			jo.put("num", num);
			//			jo.put("open", open);
			jo.put("type", "hand");
		}catch (JSONException e){
			e.printStackTrace();
		}
		return jo;
	}
}

