package operations;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rrd4j.data.LinearInterpolator;

import pelarsServer.BaseData;
import pelarsServer.Data;
import pelarsServer.Face;
import pelarsServer.OpDetail;
import pelarsServer.PelarsSession;
import servlets.Util;

public class PlotFaces extends Operation{

	Double start_time;
	Double end_time;
	String table;
	double step = 1000;
	boolean step_too_small = false;

	JSONArray json_result;

	public PlotFaces(JSONObject content) throws JSONException{

		super(content);

		table = "Face";
		try{
			step = content.getDouble("step");
		}catch(JSONException e){}
	}

	public List<? extends BaseData> extract() throws Exception{

		//get the session, get the session time and get all the samples within a threshold
		PelarsSession s = Util.doQueryUnique(my_session,"SELECT S FROM PelarsSession AS S WHERE S.id = :s_id" , "s_id", cur_session);
		start_time = Double.parseDouble(s.getStart());
		end_time = Double.parseDouble(s.getEnd());
		String query = "SELECT F FROM Face AS F WHERE F.session = :m_session";

		List<Face> faces = Util.doQuery(my_session, query, "m_session", s);

		return faces;
	}

	public void run(List<? extends Data> objs) throws Exception {

		json_result = new JSONArray();

		if (step < 1000){
			step_too_small = true;
			return;
		}

		if (objs == null || objs.size() == 0){
			return;
		}

		double at_time = start_time;

		int j = 0;
		int count = 0;
		while(at_time < end_time)
		{

			long[] times = new long[6];
			double[] values = new double[6];

			long time_step = 950;
			double start = at_time - 2850;
			double end = start + time_step;
			

			//iterate over intervals
			for (int i = 0; i < 6; i++)
			{

				values[i] = 0;

				if(objs.get(j).time >= start && objs.get(j).time <= end){

					while(j < objs.size() -1 && objs.get(j).time == objs.get(j+1).time){
						j++;
					}


					values[i] = ++j - count;
					count = j;
				}

				times[i] = (long)(start + end) / 2;
				start = end;
				end += time_step;
			}

			LinearInterpolator interpolator = new LinearInterpolator(times,values);

			json_result.put(interpolator.getValue((long)(at_time)));

			at_time += step;
		}

	}

	@Override
	public void storeResult() throws Exception {

		OpDetail p = Util.doQueryUnique(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id);
		//result directly stored in the OpDetail object
		if(!step_too_small)
			p.result = json_result.toString();
		else
			p.result = "Step too small. Minimum value is 1000";

		Util.update(my_session,p);
	}

	@Override
	public List<? extends BaseData> getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStream() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getStringResult() {
		// TODO Auto-generated method stub
		return null;
	}

}
