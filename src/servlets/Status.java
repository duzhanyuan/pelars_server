package servlets;

import org.json.JSONException;
import org.json.JSONObject;

/**
* Envelopes the responses in JSON format
 */
public class Status {
	
	String message;
	long id = -1;

	Status(String message){
		this.message = message;
	}
	
	Status(long id, String message){
		this.message = message;
		this.id = id;
	}
	
	public JSONObject toJson() {
		JSONObject jo = new JSONObject();
		try{
			jo.put("status", message);
			if(id !=-1)
				jo.put("id", id);		
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jo;
	}
}
