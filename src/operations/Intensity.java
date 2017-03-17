package operations;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;

/**
 * @author Lorenzo Landolfi
 * this operation computes the number of samples recorded for each interval of time
 */
public class Intensity extends Presence {

	public int[] counts;
	
	public Intensity(JSONObject content) throws JSONException {
		super(content);

		tables = new String[1];
		//default table is the ARDUINO IDE table
		tables[0] = "Ide";
		//default time interval: 10 seconds
		step = 10000;
		try{
			step = content.getInt("step");
		}catch(JSONException e){}
	}

	@Override
	public void compute(List<BaseData> current_data, int i) {

		for(BaseData b :current_data){
			if(b.presence()){
				counts[i] = counts[i] + 1;
			}
		}
	}

	public int init(){
		int all_intervals = super.init();
		counts = new int[all_intervals];
		Arrays.fill(counts, 0);
		return all_intervals;
	}

	@Override
	public void refine() {
		total_presence = 0.0;
		for(int i = 0; i < counts.length; i++){

			total_presence += counts[i];
		}
	}

	@Override
	public String getStringResult() {

		JSONObject complex = new JSONObject();
		JSONArray j_presences;

		try {
			complex.put("programming_time", total_presence);
		} catch (JSONException e1) {}

		if(detail == true){

			j_presences = new JSONArray();

			for (int i=0; i < intervals.length; i++){

				JSONObject jobj = new JSONObject();
				try {
					jobj.put("intensity", counts[i]);
					jobj.put("start",start + i*step);
					jobj.put("end", start + ((i+1) * step));
					j_presences.put(jobj);
				} catch (JSONException e) {
				}

			}

			try {
				complex.put("intensities", j_presences);
			} catch (JSONException e) {}
		}

		return complex.toString();
	}
}
