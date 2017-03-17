package pelarsServer;
import org.json.JSONException;
import org.json.JSONObject;


public class PelarsObject extends BaseData{

	public int num;
	public float pos_x;
	public float pos_y;
	public float pos_z;

	public PelarsObject(){}

	public PelarsObject(PelarsObject other){
		super(other);
		this.num = other.num;
		this.pos_x = other.pos_x;
		this.pos_y = other.pos_y;
		this.pos_z = other.pos_z;
	}

	public int getnum(){
		return num;
	}

	public void setnum(int num){
		this.num = num;
	}

	public float getPos_x(){
		return pos_x;
	}

	public void setPos_x(float pos_x){
		this.pos_x = pos_x;
	}

	public float getPos_y(){
		return pos_y;
	}

	public void setPos_y(float pos_y){
		this.pos_y = pos_y;
	}

	public float getPos_z(){
		return pos_z;
	}

	public void setPos_z(float pos_z){
		this.pos_z = pos_z;
	}
	
	public boolean equals(PelarsObject o){
		return (super.equals(o) && o.num == num && o.pos_x == pos_x && o.pos_y == pos_y && o.pos_z == pos_z);
	}
	
	public JSONObject toJson(){
		JSONObject jo = super.toJson();
		try{
			jo.put("pos_x", pos_x);
			jo.put("pos_y", pos_y);
			jo.put("pos_z", pos_z);
			jo.put("num", num);
			jo.put("type", "object");
		}catch (JSONException e){
			e.printStackTrace();
		}
		return jo;
	}
}      