package operations;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import servlets.Util;

import pelarsServer.BaseData;
import pelarsServer.Data;

/**
operation that maps all the ones available in Hibernate QL
*/
public class StandardOperation extends OperationSingleValue{

	String field;
	String operation;

	public StandardOperation(JSONObject content) throws JSONException{
		super(content);

		try{
			field = content.getString("field");
		}
		catch(JSONException e){
			field = "result";
		}
		operation = content.getString("type");
	}

	public void run (List<? extends Data> objs)throws Exception{
		
		if(objs.size() > 0){
			List<Number> to_save = Util.doQuery(my_session, "SELECT " + operation +"(M." + field + ") FROM " + table +" AS M");
			result = to_save.get(0).doubleValue();
		}
		else{
			result = 0.0;
		}
	}
}
