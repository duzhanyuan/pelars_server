package operations;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Data;

public class MapWorker implements Runnable{

	public List<List<? extends Data>> my_inputs;
	public List<Double> intervals;
	public Operation[] my_ops;

	public List<List<? extends Data>> my_results;
	public List<JSONObject> my_single_values;

	String exception_msg;

	public MapWorker(Operation[] ops, List<List<? extends Data>> in, List<Double> intervals){

		my_ops = ops;
		my_inputs = in;
		this.intervals = intervals;

		my_single_values = new ArrayList<JSONObject>();
		my_results = new ArrayList<List<? extends Data>>();
	}


	@Override
	public void run(){

		for (int i = 0; i < my_inputs.size(); i++){

			try {

				my_ops[i].from = intervals.get(2*i);
				my_ops[i].to = intervals.get((2*i) +1);
				my_ops[i].run(my_inputs.get(i));
				//TODO: must propagate the exception

			} catch (Exception e) {
				exception_msg = e.getMessage();
			}

			if (my_ops[0].isStream()){
				my_results.add(my_ops[i].getResult());
			}
			else {



				long thread_id = Thread.currentThread().getId();
				try{
					my_single_values.add(new JSONObject(my_ops[i].getStringResult()).put("start", intervals.get(2*i)).put("end", intervals.get((2*i)+1)).put("thread", thread_id));
				} catch(JSONException ej){ 
					JSONObject jo = new JSONObject();
					try {
						jo.put("result", my_ops[i].getStringResult()).put("start", intervals.get(2*i)).put("end", intervals.get((2*i)+1));
						my_single_values.add(jo);
					} catch (JSONException e) {}
				}

			}
		}
	}



}
