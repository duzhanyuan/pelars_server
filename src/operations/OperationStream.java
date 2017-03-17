package operations;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;
import pelarsServer.Data;
import pelarsServer.OpDetail;
import pelarsServer.StreamElement;
import servlets.DataManager;
import servlets.Util;

/**
Outlines the operations that return a stream result
 */
public abstract class OperationStream extends Operation{

	List<Data> result;
	public String table;

	public String phase = null;

	public OperationStream(JSONObject content) throws JSONException {

		super(content);
		try{
			table = content.getString("table");
		}catch(JSONException e){}

		try{
			phase = content.getString("phase");
		}catch(JSONException e){}
	}

	@Override
	public List<? extends Data> extract() throws Exception{

		return DataManager.getDataByPhase(phase, my_session, cur_session, table);
	}


	@Override
	public void storeResult() throws Exception{

		StreamElement[] stream = new StreamElement[result.size()];

		if (result.size() > 0){
			//creates as many streamElements as many are the BaseData instances
			for(int i = 0; i< result.size(); i++){
				stream[i] = new StreamElement((BaseData)result.get(i), id);
			}
		}

		Util.save(my_session, stream);

		List<OpDetail> results = Util.doQuery(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id); 
		OpDetail p = results.get(0);

		//also updates OpDetail entity to state that the result in the db is a stream
		p.is_stream = true;
		Util.update(my_session, p);	
	}

	public List<Data> getResult(){
		return result;
	}

	public String getStringResult(){
		return null;
	}

	@Override
	public void run(List<? extends Data> objs) throws Exception{}

	public boolean isStream(){
		return true;
	}

}
