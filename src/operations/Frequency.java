package operations;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Data;
import pelarsServer.OpDetail;
import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi
 * This operation is intended to compute the frequency of samples of a given type. Unit is Hz
 *
 */
public class Frequency extends OperationSingleValue {

	boolean detail = false;
	double duration = 0.0;
	double count = 0.0;
	double frequency;

	public Frequency(JSONObject content) throws JSONException{
		super(content);

		try{
			detail = content.getBoolean("detail");
		}catch(JSONException e){}
	}

	/**
	 * 
	 */
	@Override
	public void run(List<? extends Data> objs) throws Exception {

		if(phase == null){
			try {
				from = Double.parseDouble(this.getSession().getStart());
				to = Double.parseDouble(this.getSession().getEnd());
			} catch (Exception e) {}
		}

		//samples are sorted by time so take first and last time
		duration = (to - from) / 1000;
		frequency = objs.size()/duration;
		count = frequency * duration;
	}

	public void storeResult() throws Exception{

		JSONObject jresult = new JSONObject();
		jresult.put("number", (int)count);
		jresult.put("frequency", frequency);
		jresult.put("unit", "#/s");

		List<OpDetail> results = Util.doQuery(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id); 
		OpDetail p = results.get(0);
		//result directly stored in the OpDetail object
		p.result = jresult.toString(1);
		Util.update(my_session,p);
	}

	@Override
	public String getStringResult() {
		JSONObject jresult = new JSONObject();
		try{
			jresult.put("number", (int)count);
			jresult.put("frequency", frequency);
			jresult.put("unit", "#/s");
			return jresult.toString(4);
		}catch(JSONException e){
			return "";
		}
	}



}
