package operations;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


import pelarsServer.Data;
import pelarsServer.LiveStatistics;
import pelarsServer.OpDetail;
import pelarsServer.PelarsSession;
import servlets.Util;

public class ProgrammingTime extends Times{

	public ProgrammingTime(JSONObject content) throws JSONException {
		super(content);
		table = "Ide";
	}


	public void run(List<? extends Data> objs) throws Exception {

		LiveStatistics ls = new LiveStatistics();

		for(int i=0; i < objs.size() -1; i++){
			ls.add(Math.abs(objs.get(i+1).time - objs.get(i).time));
		}
		
		threshold = (ls.getMax() + ls.getMin()) / 2;

		super.run(objs);
	}
	
	public void storeResult() throws Exception{

		List<OpDetail> results = Util.doQuery(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id); 
		OpDetail p = results.get(0);
		//result directly stored in the OpDetail object
		JSONObject js = new JSONObject();

		PelarsSession ps = this.getSession();

		long duration = ps.getDuration();

		js.put("programming_time", (active_time/(double)duration) * 100);
		js.put("unit", "%");

		p.setResult(js.toString(1));

		Util.update(my_session,p);
	}

}
