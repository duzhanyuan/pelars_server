package operations;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.*;
import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi
 * Approximate the frequency movement of hands according to a space threshold.
 */
public class HandFrequency extends Frequency{

	/**time thresholds**/
	Double from;
	Double to;
	
	JSONArray jresult;
	
	List<Double> speeds;
	ArrayList<Integer> markers_ids;


	public HandFrequency(JSONObject content) throws JSONException {
		super(content);
		table = "Hand";

		//optionally specify the time interval
		try{
			from = content.getDouble("from") * 1000;
			to = content.getDouble("to") * 1000;
		} catch(JSONException e){}
	}

	/**Filters the result if specific time interval requested**/
	public List<? extends Data> extract() throws Exception{

		List<? extends Data> to_filter = super.extract();

		if(from != null && to != null){

			List<Data> filtered = new ArrayList<Data>();

			PelarsSession s = this.getSession();

			long start = Long.parseLong(s.getStart());

			for(int i = 0; i < to_filter.size(); i++){
				if(to_filter.get(i).getTime() - start <= to && to_filter.get(i).getTime() -start >= from){
					filtered.add(to_filter.get(i));
				}
			}	

			return filtered;
		}
		else{

			return to_filter;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run(List<? extends Data> objs) throws Exception {

		markers_ids = new ArrayList<Integer>();

		//get the identifiers of all the tracked hands
		for (Hand h : (List<Hand>)objs){
			if (!markers_ids.contains(h.getnum()) && h.getnum() != 0){
				markers_ids.add(h.getnum());
			}	
		}

		/**Now computes the velocity of each hand or the overall speed**/
		ArrayList<ArrayList<Hand>> sets = new ArrayList<ArrayList<Hand>>(markers_ids.size());
		//populate an array for each different id
		for (int i = 0; i < markers_ids.size(); i++){

			ArrayList<Hand> hands = new ArrayList<Hand>();

			for(Hand h : (List<Hand>)objs){
				if (h.getnum() == markers_ids.get(i)){
					hands.add(h);
				}
			}
			sets.add(hands);
		}

		/**compute velocity for each hand and keep track of all the hands for overall speed**/

		speeds = new ArrayList<Double>();
		double total_speed = 0;
		long total_iterations = 0;

		for (int i = 0; i < markers_ids.size(); i++){

			ArrayList<Hand> current = sets.get(i);
			double current_speed = 0;
			total_iterations = total_iterations + current.size() - 1;

			for (int j = 0; j < current.size()-1; j++){
				Hand h1 = current.get(j);
				Hand h2 = current.get(j+1);

				//current_speed = current_speed + h1.distance(h2);
				if(h2.num != h1.num){
					throw new Exception("Error same num");
				}
				if(h2.time != h1.time){
					current_speed = current_speed + (h1.distance(h2)/(Math.abs(h2.time - h1.time)/1000));
				}
			}

			if(current_speed != 0){
				//sum of all the speeds of the hands with current marker id
				total_speed = total_speed + current_speed;
				//average speed of the hands with current id
				current_speed = current_speed / (current.size()-1);
				speeds.add(current_speed);
			}
			else{
				markers_ids.remove(i);
			}
			current_speed = 0;	
		}
		
		//weighted total speed 
		total_speed = total_speed / total_iterations;

		this.result = total_speed;
	}

	public void storeResult() throws Exception{

		//set the Json Object
		jresult = new JSONArray();

		for (int i = 0; i < speeds.size(); i++){
			jresult.put(new JSONObject().put("num", markers_ids.get(i)).put("speed", speeds.get(i)));
		}

		jresult.put(new JSONObject().put("overall", this.result));

		OpDetail p = Util.doQueryUnique(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id); 
		//result directly stored in the OpDetail object
		p.result = jresult.toString(1);
		Util.update(my_session,p);
	}

}
