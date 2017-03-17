package operations.utilities;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import operations.Operation;
import pelarsServer.Data;
import pelarsServer.BaseData;
import pelarsServer.PelarsSession;
import servlets.ACL_RuleManager;
import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi
 * utility operation used to remove sessions shorter than a given duration
 *
 */
public class CleanSessions extends Operation {

	public long max_duration;
	public boolean test = false;

	public CleanSessions(JSONObject content) throws JSONException {
		super(content);
		
		//duration expressed in milliseconds
		max_duration = 10000;

		try{
			max_duration = content.getLong("duration");
		}catch(JSONException e){}

		try{
			test = content.getBoolean("test");
		}catch(JSONException e){}
	}

	@Override
	public List<? extends Data> extract() throws Exception {

			List<PelarsSession> all_sessions = Util.doQuery(my_session, "SELECT S From PelarsSession AS S WHERE S.end - S.start < :duration",
					"duration",String.valueOf(max_duration));

			PelarsSession current;
			//must cycle since need to remove associated data first 
			for (int i = 0; i < all_sessions.size(); i++){

				current = all_sessions.get(i);

				if (ACL_RuleManager.Check(this.getUser(), "DELETESESSION", current, my_session)){
					Util.delete(my_session, all_sessions.toArray(new PelarsSession[all_sessions.size()]));
					//delete upload folder of the session
					try{
					Util.executeCommand("rm -r " + System.getProperty("upload.location") + current.getId());
					}catch(Exception e){}
					//Util.delete(my_session, current);
				}
				else{
					throw new Exception(current.getUser().toJson().toString());
				}
			}

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


