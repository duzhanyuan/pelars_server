package operations;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;

import servlets.Util;

//TODO operation executing a generic SQL query
public class OperationCustom extends OperationStream{
	
	String query;
	
	public OperationCustom(JSONObject content) throws JSONException{
		super(content);
		query = content.getString("query");
	}
	
	public  void run() throws Exception{
		this.result = Util.doQuery(my_session, query);	
	}

	@Override
	public List<BaseData> extract() throws Exception{
		return null;
	}
}
