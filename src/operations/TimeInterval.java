package operations;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Data;

/**
Filters on time without script engine
 */
public class TimeInterval extends OperationStream {

	Double from;
	Double to;

	public TimeInterval(JSONObject content) throws JSONException{
		super(content);

		from = content.getDouble("from");
		to = content.getDouble("to");
	}


	public void run(List<? extends Data> objs) throws Exception{

		result = new ArrayList<Data>();

		for(Data b : objs){
			if (b.time >= from && b.time <= to)
				result.add(b);
		}
	}
}