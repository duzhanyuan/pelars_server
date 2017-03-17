package operations;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import pelarsServer.Data;

/**
 * 
 * @author Lorenzo Landolfi
 * returns the average value of a column of data
 *
 */
public class Mean extends StatisticOperation {

	public Mean(JSONObject content) throws JSONException{
		super(content);
	}
	
	public void run(List<? extends Data> objs) throws Exception{

		Statistics statistic = fillData(objs);
		result = statistic.getMean();
	}
}