package operations;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Data;

import servlets.OpException;
import servlets.OperationManager;

/**
Executes a list of operations one after the other passing output of the previous one to the following 
 */
public class Pipeline extends Operation{

	List<Operation> operations;
	List<? extends Data> input;
	List<? extends Data> output;


	public Pipeline(JSONObject obj)throws JSONException{

		super(obj);
		operations = new ArrayList<Operation>();

		JSONArray steps = obj.getJSONArray("operations");

		for(int i=0; i < steps.length(); i++){
			try {
				operations.add(OperationManager.parseOp(steps.getJSONObject(i)));
			} catch (OpException e) {
				throw new JSONException("unrecognized operation type");
			}
		}
	}

	/**
	Input is the input of the first operation of the list
	 */
	public List<? extends Data> extract() throws Exception{

		operations.get(0).my_session = my_session;
		operations.get(0).id = id;
		operations.get(0).cur_session = cur_session;
		return operations.get(0).extract();
	}

	/**
	Output is the output of the last operation of the list
	 */
	public void storeResult() throws Exception{
		operations.get(operations.size()-1).storeResult();
	}

	public List<? extends Data> getResult(){
		return output;
	}

	public String getStringResult(){
		return null;
	}

	public boolean isStream(){
		return operations.get(operations.size()-1).isStream();
	}


	@Override
	public void run(List<? extends Data> objs) throws Exception{

		output = objs;

		for(Operation op : operations){

			if(op.isStream() && (output.size() == 0 || output == null)){
				throw new RuntimeException("Incompatible pipeline stages");
			}
			else {
				input  = output;
			}

			//Hibernate session is passed to the list of operations...only for the last one
			op.my_session = my_session;
			op.id = id;

			op.run(input);
			output = op.getResult();
		} 
	}
}