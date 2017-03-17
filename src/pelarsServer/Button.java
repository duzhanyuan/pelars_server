package pelarsServer;

import org.json.JSONObject;

/**
 * 
 * @author Lorenzo Landolfi, Giacomo Dabisias
 * class representing a button press 
 *
 */
public class Button extends Particle{
	
	public JSONObject toJson(){
 		JSONObject jo = super.toJson();
		try{
			jo.put("type", "button");
			jo.put("data", new String(data.getBytes(1l, (int) data.length())));	
			jo.put("name", name);
		}catch (Exception e){
			e.printStackTrace();
		}
		return jo;
 	}

}
