package pelarsServer;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Lorenzo Landolfi, Giacomo Dabisias
 *	class representing a single audio sample. 
 */
public class Audio extends BaseData{
	
	//value is an intensity power value
	public float value;
	
	public float getValue(){
		return value;
	}
	
	public void setValue(float f){
		value = f;
	}
	
	public JSONObject toJson(){
		
		JSONObject o = super.toJson();
		
		try{
			o.put("value", value);
			o.put("type", "audio");
		}catch (JSONException e) {}
		
		return o;
	}
	
	public boolean equals(Audio a){
		return super.equals(a) && value == a.value;
	}
}
