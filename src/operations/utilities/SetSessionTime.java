package operations.utilities;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;
import pelarsServer.Data;
import servlets.Util;

public class SetSessionTime extends operations.Operation{
	
	public SetSessionTime(JSONObject content) throws JSONException {
		super(content);
	}

	@Override
	public List<? extends BaseData> extract() throws Exception {
		
		List<BaseData> all_data = Util.doQuery(my_session, "SELECT F From BaseData AS F");
		
		for (int i = 0; i < all_data.size(); i++){
			all_data.get(i).setEpochTime();
		}
		
		Util.update(my_session, all_data.toArray(new BaseData[all_data.size()]));
		
		return null;
	}

	@Override
	public void storeResult() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<? extends BaseData> getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	public void run(List<? extends Data> objs) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isStream() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getStringResult() {
		// TODO Auto-generated method stub
		return null;
	}

}
