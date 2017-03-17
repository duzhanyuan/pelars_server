package operations;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Data;
import pelarsServer.OpDetail;
import pelarsServer.PelarsSession;
import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi
 * Tries to transform discrete samples data in continuous ones. For example can approximate time 
 * looking at the screen or the movement of the hands
 */
public class Times extends OperationSingleValue{

	double active_time;
	double inactive_time;

	double threshold;

	public Times(JSONObject content) throws JSONException {

		super(content);

		table = "Face";
		active_time = 0.0;
		inactive_time = 0.0;
		threshold = 1200;
		
		
	}
	

	//TODO if sample time in the client is not fixed to one sec, add it as a field of the class
	public void addActive(Data a, Data b){

		double diff = Math.abs(b.time - a.time);
		//if the difference between the two samples is approximately 1 sec add 1 sec to the active time
		if (diff < threshold && diff != 0){
			active_time += diff;
		}
		else{
			inactive_time += diff;
		}	
	}

	//meaningful only for faces
	public void run(List<? extends Data> objs) throws Exception{

		if(objs != null && objs.size() == 0){
			throw new Exception("Not enough data to estimate");
		}

		for(int i = 0; i < objs.size()-1; i++){
			addActive(objs.get(i), objs.get(i + 1));
		}
	}

	public void storeResult() throws Exception{

		List<OpDetail> results = Util.doQuery(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id); 
		OpDetail p = results.get(0);
		//result directly stored in the OpDetail object
		JSONObject js = new JSONObject();

		PelarsSession ps = this.getSession();

		long duration = ps.getDuration();

		js.put("active_time", (active_time/(double)duration) * 100);
		js.put("inactive_time", (100 - (active_time/(double)duration) * 100));
		js.put("unit", "%");

		p.setResult(js.toString(1));

		Util.update(my_session,p);
	}	

	public String getStringResult() {

		PelarsSession ps = null;
		try {
			ps = this.getSession();
		} catch (Exception e1) {}

		long duration = ps.getDuration();

		JSONObject js = new JSONObject();
		try {
			js.put("active_time", (active_time/(double)duration) * 100);
			js.put("inactive_time", (100 - (active_time/(double)duration) * 100));
		} catch (JSONException e) {
		}
		return js.toString();
	}

}
