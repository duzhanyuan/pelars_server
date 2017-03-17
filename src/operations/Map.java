package operations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;
import pelarsServer.Data;
import pelarsServer.OpDetail;
import pelarsServer.StreamElement;
import servlets.OpException;
import servlets.OperationManager;
import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi
 * Data parallel operation splitting operation work among time windows that can overlap
 *
 */

public class Map extends Operation{

	Operation my_op;
	int split;

	//number between 0 and 1 representing the overlapping percentage of the windows 
	float overlap;

	//parallelism degree
	int parallel_degree = 1;
	private Thread[] p_threads;
	private MapWorker[] map_workers;

	List<List<? extends Data>> my_inputs;
	List<List<? extends Data>> my_results;
	Operation[] my_ops;
	List<JSONObject> my_single_values;

	//can actually avoid to store such list
	List<Double> intervals;

	JSONObject in_par;


	public Map(JSONObject content) throws JSONException, OpException {
		super(content);
		in_par = content;
		my_inputs = new ArrayList<List<? extends Data>>();
		my_results = new ArrayList<List<? extends Data>>();
		my_single_values = new ArrayList<JSONObject>();
		intervals = new ArrayList<Double>();

		//if an exception is thrown here, it is captured by the doPut method of OperationManager and outputs the correct JSON message
		my_op = OperationManager.parseOp(content.getJSONObject("operation"));

		split = content.getInt("window_size");

		short percentage = (short)content.getLong("overlap");

		if(percentage < 0){
			throw new OpException("percentage must be at least 0");
		}

		if(percentage >= 100){
			throw new OpException("percentage must be strictly smaller than 100");
		}

		overlap = 1 - ((float)percentage / 100f);

		try{
			parallel_degree = content.getInt("parallelism");
		} catch(JSONException e){}

		p_threads = new Thread[parallel_degree];
		map_workers = new MapWorker[parallel_degree];

	}

	@Override
	public List<Data> extract() throws Exception {

		my_op.id = id;
		my_op.my_session = my_session;
		my_op.cur_session = cur_session;

		List<? extends Data> initial_input = my_op.extract();

		//TODO: starting time must be the beginning of the session or of the phase
		double current_time = initial_input.get(0).time;
		//	intervals.add(current_time);

		//overlapping_time =  overlap * (next_time - last_time)
		double last_time = initial_input.get(initial_input.size()-1).time;
		double next_time = current_time + split;
		//	intervals.add(next_time);

		int i = 0;
		boolean last = false;

		while(!last){
			if (next_time >= last_time){
				last = true;
			}
			List<Data> ret = new ArrayList<Data>();

			while(i < initial_input.size() && initial_input.get(i).time <= next_time){
				ret.add(initial_input.get(i));
				i++;
			}

			//since the beginning time does not match must record also them
			intervals.add(current_time);
			intervals.add(next_time);

			//E.g. if specified overlap of 99% in the JSON input, advance only by 1% of the window_size
			current_time = current_time + (split * overlap);
			next_time = current_time + split;

			my_inputs.add(ret);
		}

		my_ops = new Operation[my_inputs.size()];
		for (int k = 0; k < my_inputs.size(); k++){
			//TODO: better to deep copy here
			my_ops[k] =  OperationManager.parseOp(in_par.getJSONObject("operation"));
			my_ops[k].id = id;
			my_ops[k].my_session = my_session;
			my_ops[k].cur_session = cur_session;
		}
		return null;
	}

	@Override
	public void run(List<? extends Data> objs) throws Exception {

		int wpt = my_inputs.size() / parallel_degree;
		int ipt = 2 * wpt;

		int rest = 0;

		//split the input among workers and start them. Scatter.
		for (int j = 0; j < parallel_degree; j++){

			if (j == parallel_degree - 1){
				rest = my_inputs.size() % parallel_degree;
			}

			map_workers[j] = new MapWorker( Arrays.copyOfRange(my_ops, j*wpt, ((j+1)*wpt) + rest), my_inputs.subList(j*wpt, ((j+1)*wpt) + rest), intervals.subList(j*ipt, (j+1)*ipt + 2*rest));
			p_threads[j] = new Thread(map_workers[j]);
		}

		//run the threads
		for (int j = 0; j < parallel_degree; j++){
			p_threads[j].start();
		}

		//join the threads and collect the outputs
		for (int j = 0; j < parallel_degree; j++){

			p_threads[j].join();
			my_single_values.addAll(map_workers[j].my_single_values);

			if (map_workers[j].exception_msg != null){
				throw new Exception(map_workers[j].exception_msg);
			}
		}
	}

	@Override
	public void storeResult() throws Exception {
		OpDetail p = Util.doQueryUnique(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id);
		//result directly stored in the OpDetail object
		if(!my_op.isStream()) {

			JSONArray allMap = new JSONArray(my_single_values);
			p.result = allMap.toString();

			Util.update(my_session,p);
		}
		else {

			List <Data> ret = new ArrayList<Data>();

			for (List<? extends Data> l : my_results){
				ret.addAll(l);
			}

			StreamElement[] elements = new StreamElement[ret.size()];

			for (int i = 0; i< ret.size(); i++){
				elements[i] = new StreamElement((BaseData)ret.get(i),this.id);
			}

			Util.save(my_session, elements);
		}

	}

	@Override
	public List<? extends Data> getResult() {
		// TODO return the concatenation of the results
		List <Data> ret = new ArrayList<Data>();

		for (List<? extends Data> l : my_results){
			ret.addAll(l);
		}

		return ret;
	}

	public String getStringResult(){
		return my_single_values.toString();
	}

	@Override
	public boolean isStream() {
		// TODO Auto-generated method stub
		return my_op.isStream();
	}

}
