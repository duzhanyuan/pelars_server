package operations;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.rrd4j.data.LinearInterpolator;

import pelarsServer.BaseData;
import pelarsServer.Data;
import pelarsServer.Face;
import pelarsServer.PelarsSession;
import servlets.Util;

public class NumFaces extends OperationSingleValue{

	Double instant;
	Double start_time;
	
	int range;
	double start;

	public NumFaces(JSONObject content) throws JSONException{

		super(content);

		table = "Face";

		//consider this instant relative to the session
		instant = content.getDouble("time");

		//It is supposed that instant is given in seconds
		instant = instant * 1000;
		
		range = 2850;
	}

	public List<? extends Data> extract() throws Exception{

		//get the session, get the session time and get all the samples within a threshold
		PelarsSession s = Util.doQueryUnique(my_session,"SELECT S FROM PelarsSession AS S WHERE S.id = :s_id" , "s_id", cur_session);
		start_time = Double.parseDouble(s.getStart());
		start  = start_time + instant;
		String query = "SELECT F FROM Face AS F WHERE F.session = :m_session AND F.time >= :r1 AND F.time <= :r2";

		List<Face> faces = Util.doQuery(my_session, query, "m_session", s, "r1", start - range, "r2", start + range);

		return faces;
	}

	public void run(List<? extends Data> objs) throws Exception {
		
		if (objs == null || objs.size() == 0){
			result = -1.0;
			return;
		}


		long[] times = new long[6];
		double[] values = new double[6];

		long time_step = 950;
		double start_t = start - range;
		double end = start_t + time_step;
		int j = 0;
		
		//iterate over intervals
		int count = 0;
		for (int i = 0; i < 6; i++){

			values[i] = 0;
			
			if(objs.get(j).time >= start_t && objs.get(j).time <= end){

				while(j < objs.size() -1 && objs.get(j).time == objs.get(j+1).time){
					j++;
				}
				
				
				values[i] = ++j - count;
				count = j;
			}
		
			times[i] = (long)(start_t + end) / 2;
			start_t = end;
			end += time_step;
		}

		LinearInterpolator interpolator = new LinearInterpolator(times,values);

		result = interpolator.getValue((long)(start));
	}

}
