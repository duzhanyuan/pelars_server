package operations;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Data;

public class Variance extends StatisticOperation {

	public Variance(JSONObject content)throws JSONException{
		super(content);
	}

	public void run(List<? extends Data> objs) throws Exception{

		Statistics statistic = fillData(objs);
		result = statistic.getVariance();
	}
}
