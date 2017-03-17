package operations.utilities;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;
import pelarsServer.Data;
import pelarsServer.PelarsSession;
import servlets.Util;

public class ConvertSessionTime extends operations.Operation {

	public ConvertSessionTime(JSONObject content) throws JSONException {
		super(content);
	}

	@Override
	/**
	 * converts all the session starting time and ending time in epoch milliseconds
	 */
	public List<? extends Data> extract() throws Exception {
		List<PelarsSession> all_sessions = Util.doQuery(my_session, "SELECT S From PelarsSession AS S");

		for (int i = 0; i < all_sessions.size(); i++){
			all_sessions.get(i).convertTime();
		}

		Util.update(my_session, all_sessions.toArray(new PelarsSession[all_sessions.size()]));

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

	@Override
	public void run(List<? extends Data> objs) throws Exception {
		// TODO Auto-generated method stub
		
	}

}


