package pelarsServer;

import org.json.JSONException;
import org.json.JSONObject;

public class KeyLog extends BaseData {
	
	public String activity;
	public double timestamp;
	
	public String getActivity(){
		return activity;
	}
	
	public void setActivity(String s){
		activity = s;
	}
	
	public double getTimestamp(){
		return timestamp;
	}
	
	public void setTimestamp(double d){
		timestamp = d;
	}
	
	public JSONObject toJson(){
		JSONObject jo = new JSONObject();
		try{
			jo.put("activity_type", activity);
			jo.put("time", time);
			jo.put("id", id);
			jo.put("timestamp", timestamp);
			jo.put("type", "keylog");
		}catch (JSONException e){
			e.printStackTrace();
		}
		return jo;
	}
}
