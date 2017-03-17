package operations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pelarsServer.BaseData;
import pelarsServer.Data;
import pelarsServer.OpDetail;
import servlets.DataManager;
import servlets.Util;


/**
 * 
 * @author Lorenzo Landolfi
 * this operation estimates the presence at the screen. If at least one face is pointing at the 
 * screen in a given interval of time, that interval is tagged as a "presence" interval 
 *
 */

public class Presence extends OperationSingleValue{

	//TODO: make step an input parameter
	public double step = 1100;

	double[] intervals;
	boolean[] presences;

	double start;
	double end;

	List<BaseData> data;

	public double total_presence = 0.0;
	public double total_absence = 0.0;

	public boolean detail = false;

	double duration;

	String[] tables;

	//must get the position of the markers limiting the desk
	public Presence(JSONObject content) throws JSONException {
		super(content);

		tables = new String[2];
		tables[0] = "Face";
		tables[1] = "Hand";

		try{
			detail = content.getBoolean("details");
		}catch(JSONException e){}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BaseData> extract() throws Exception{

		data = (List<BaseData>)DataManager.getDataByPhase(phase, my_session, cur_session, tables);

		Collections.sort(data, new Comparator<BaseData>(){
			public int compare(BaseData b1, BaseData b2){
				return (int)(b1.time - b2.time);
			}
		});

		start =  ((this.phase == null) ? data.get(0).getTime() : this.getSession().getPhaseBounds(phase, my_session)[0]);

		if(phase != null){
			end = this.getSession().getPhaseBounds(phase, my_session)[1];
		}
		else{
			end = data.get(data.size()-1).time;
		}

		init();

		return null;
	}

	public int init(){
		int all_intervals = (int) Math.ceil((end-start)/step);
		intervals = new double[all_intervals];
		presences = new boolean[all_intervals];
		Arrays.fill(presences, false);
		duration = end - start;
		return all_intervals;
	}

	//function to decide the presence according to the presences around index
	private boolean decidePresence(int index){
		return false;
	}

	public void compute(List<BaseData> current_data, int i){
		for (BaseData b : current_data){
			if(b.presence()){
				presences[i] = true;
				break;
			}
		}
	}

	public void refine(){
		for (int i = intervals.length-1; i >= 0; i--){
			if(presences[i] == false){
				//decide to risk false positive according to neighbor intervals
				presences[i] = decidePresence(i);
			}
		}

		for(int i=0; i<presences.length; i++){
			if(presences[i]){
				total_presence += step;
			}
			else{
				total_absence += step;
			}
		}
	}

	public void run(List<? extends Data> objs) throws Exception{

		double temp_start = start;
		double temp_end = start + step;

		int j_hand = 0;

		//first view: just set presence to true if we are sure of the presence
		//check for each time-step whether we can state the presence of someone at the desk
		for(int i = 0; i< intervals.length; i++){

			List<BaseData> current_data = new ArrayList<BaseData>();

			//get all the samples in that interval
			while (j_hand < data.size() && data.get(j_hand).time >= temp_start && data.get(j_hand).time < temp_end){
				current_data.add(data.get(j_hand));
				j_hand ++;
			}

			compute(current_data, i);

			temp_start = temp_end;
			temp_end = temp_end + step;
		}

		//now we can venture to affirm the presence according to the certain presence intervals
		//can devise an heuristic to approximate the presence. For example iterate until convergence
		refine();
	}

	public void storeResult() throws Exception{

		OpDetail p = Util.doQueryUnique(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id); 

		p.setResult(getStringResult());

		Util.update(my_session,p);
	}

	public String getStringResult() {

		JSONObject complex = new JSONObject();
		JSONArray j_presences;

		try {
			complex.put("total_presence", (total_presence/(double)duration)*100);
			complex.put("total_absence", (100 - (total_presence/(double)duration)*100));
		} catch (JSONException e1) {}

		if(detail == true){

			j_presences = new JSONArray();

			for (int i=0; i < intervals.length; i++){

				JSONObject jobj = new JSONObject();
				try {
					jobj.put("presence", presences[i]);
					jobj.put("start",start + i*step);
					jobj.put("end", start + ((i+1) * step));
					j_presences.put(jobj);
				} catch (JSONException e) {
				}

			}

			try {
				complex.put("presences", j_presences);
			} catch (JSONException e) {}
		}

		return complex.toString();
	}
}
