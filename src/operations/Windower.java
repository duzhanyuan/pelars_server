package operations;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;

//TODO wrong approach must return a portion of data given a certain rule
public class Windower{
	
	double time_interval;
	
	public Windower(JSONObject content) throws JSONException{
		time_interval = content.getDouble("interval");
	}
	
	List<BaseData> windowing(List<BaseData> b){
		
		ArrayList<BaseData> ret = new ArrayList<BaseData>();
		double current_time = b.get(0).time;
		double next_time = current_time + time_interval;
		ret.add(b.get(0));
		
		for(int i = 1; i < b.size(); i++){
			if(b.get(i).time >= next_time){
				ret.add(b.get(i));
				current_time = b.get(i).time;
				next_time = current_time + time_interval;
			}
		}	
		return ret;
	}
}