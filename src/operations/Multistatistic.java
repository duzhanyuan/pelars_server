package operations;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Data;
import pelarsServer.LiveStatistics;
import pelarsServer.OpDetail;

import servlets.Util;
/**
 * 
 * @author Lorenzo Landolfi
 *
 */
public class Multistatistic extends StatisticOperation{

	LiveStatistics result;

	public Multistatistic(JSONObject content) throws JSONException{
		super(content);
		result = new LiveStatistics();
	}

	@Override
	public void storeResult() throws Exception{

		List<OpDetail> results = Util.doQuery(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id); 
		OpDetail p = results.get(0);

		p.result = result.toJson().toString();

		Util.update(my_session,p);
	}


	@Override
	public void run(List<? extends Data> objs) throws Exception{

		Statistics statistics = fillData(objs);

		for (int i = 0; i < objs.size(); i++){
			result.add(statistics.data[i]);
		}
	}

	@Override
	public String getStringResult(){
		return result.toString();
	}
}