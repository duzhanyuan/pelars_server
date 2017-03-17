package operations;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Data;
import pelarsServer.PelarsSession;
import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi
 * This operation merges two sessions. Checks the session with the smaller starting time. it computes the time gap
 * between the end of the fist session (possibly considered the last data received) and the starting of the second one.
 * The time field of all the data belonging to the second session is shifted left by the computed gap. 
 */
public class Merge extends OperationSingleValue{

	long [] otherids;
	boolean smart = false;

	public Merge(JSONObject content) throws JSONException {
		super(content);

		//TODO: implement smart merge without copy
		/*try{
			smart = content.getBoolean("smart");
		} catch(JSONException e){}*/

		try{
			JSONArray sessions = content.getJSONArray("with");
			otherids = new long[sessions.length()];
			for(int i=0; i<sessions.length(); i++){
				otherids[i] = sessions.getLong(i);
			}
		}catch(JSONException e){
			otherids = new long[1];
			otherids[0] = content.getLong("with");
		}
	}


	@Override
	public List<? extends Data> extract() throws Exception {
		return null;
	}



	@Override
	public void run(List<? extends Data> objs) throws Exception {

		PelarsSession thissession = this.getSession();
		PelarsSession nsession = new PelarsSession();

		for(int i=0; i<otherids.length; i++){
			PelarsSession othersession = Util.doQueryUnique(my_session, "SELECT S FROM PelarsSession AS S WHERE S.id = :sid", "sid", otherids[i]);
			if(!smart){
				nsession = thissession.merge(othersession,this.my_session);
			}
			/*else{
				nsession = thissession.smartMerge(othersession, this.my_session);
			}*/

			thissession = nsession;
		}
		this.result = (double)thissession.getId();
	}

}
