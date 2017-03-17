package operations;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Data;
import pelarsServer.OpDetail;
import pelarsServer.BaseData;
import servlets.DataManager;
import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi
 *	Outlines the operations that return a single value result
 */
public abstract class OperationSingleValue extends Operation{

	Double result;
	
	//maybe should be an array of Strings
	public String table;

	public OperationSingleValue(JSONObject content) throws JSONException{
		super(content);

		try{
			table = content.getString("table");
		} catch(JSONException ej){}
	}
	

	@Override
	public void storeResult() throws Exception{

		List<OpDetail> results = Util.doQuery(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id); 
		OpDetail p = results.get(0);
		//result directly stored in the OpDetail object
		p.setResult(result.toString());

		Util.update(my_session,p);
	}

	@Override
	public List<? extends Data> extract() throws Exception{
		
		return DataManager.getDataByPhase(phase, my_session, cur_session, table);
	}

	@Override
	public void run(List<? extends Data> objs) throws Exception{}

	@Override
	public List<BaseData> getResult(){
		return null;
	}

	public String getStringResult(){
		return result.toString();
	}

	public boolean isStream(){
		return false;
	}

}
