package pelarsServer;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Lorenzo Landolfi, Giacomo Dabisias
 * class representing Arduino Ide actions
 */

public class Ide extends BaseData {

	public String action_id;
	public String opt;

	public String getAction_id(){
		return action_id;
	}

	public void setAction_id(String num){
		this.action_id = num;
	}

	public String getOpt(){
		return opt;
	}

	public void setOpt(String s){
		opt = s;
	}

	public boolean equals(Ide i){
		return (super.equals(i) && action_id.equals(i.action_id) && opt.equals(i.opt));
	}

	public boolean presence(){
		return true;
	}

	public JSONObject toJson(){
		JSONObject jo = super.toJson();
		try{
			jo.put("action_id", action_id);
			jo.put("opt", opt);
			jo.put("type", "ide");
		}catch (JSONException e){
			e.printStackTrace();
		}
		return jo;
	}
}

