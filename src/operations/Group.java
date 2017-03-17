package operations;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;
import pelarsServer.Data;
import pelarsServer.OpDetail;

import servlets.OpException;
import servlets.OperationManager;
import servlets.Util;


/**
 * 
 * @author Lorenzo Landolfi
 * complex operation intended to run several operations over the same input
 *
 */
public class Group extends Operation {

	public boolean def = false;

	/**TAKES track of the ids of the sub-operations*/
	ArrayList<Long> ids = new ArrayList<Long>();

	List<? extends BaseData> input;

	/**The JSON input of the operation to be started toghether**/
	JSONArray opinputs;

	public static HashMap<Integer, String> op_mapping;

	//0: hand_speed
	//1: time_looking
	//2: media_info
	//3: hand_proximity
	//4: face_proximity
	//5: presence

	/**standard launched operations**/
	static{
		op_mapping = new HashMap<Integer,String>();
		op_mapping.put(0, "hand_speed");
		op_mapping.put(1, "time_looking");
		op_mapping.put(2, "media_info");
		op_mapping.put(3, "hand_proximity");
		op_mapping.put(4, "face_proximity");
		op_mapping.put(5, "presence");
	}

	public Group(JSONObject obj) throws JSONException {

		super(obj);

		/**
		 * get the input from JSON or generate a default one
		 */
		try{
			opinputs = obj.getJSONArray("operations");
		}catch(Exception e){
			def = true;
			opinputs = defaultInput(cur_session).getJSONArray("operations");
		}

		for(int i=0; i < opinputs.length(); i++){

			//if session is not specified, give the default one
			try{
				opinputs.getJSONObject(i).getLong("session");
			}catch(JSONException e){
				opinputs.getJSONObject(i).put("session", cur_session);
			}

			try {
				ids.add(OperationManager.newJob(opinputs.getJSONObject(i)));
			} catch (OpException e) {}
		}
		
		/**Add the conventional names**/
		if(def){
			for(int i=0; i < opinputs.length(); i++){
				
				opinputs.getJSONObject(i).put("name", "aftersession_" + op_mapping.get(i));
			}
		}
	}

	public static JSONObject defaultInput(long id){


		JSONObject rinput = new JSONObject();
		JSONArray subinput = new JSONArray();

		try{
			rinput.put("session", id);
			rinput.put("type", "group");
			rinput.put("name", "after session routine");

			JSONObject hand_speed = new JSONObject();
			hand_speed.put("type", "hand_speed");
			hand_speed.put("session", id);
			subinput.put(hand_speed);

			JSONObject time_looking = new JSONObject();
			time_looking.put("type", "time_looking");
			time_looking.put("session", id);
			subinput.put(time_looking);

			JSONObject media_info = new JSONObject();
			media_info.put("type", "media_info");
			media_info.put("session", id);
			subinput.put(media_info);

			JSONObject proximity = new JSONObject();
			proximity.put("type", "proximity");
			proximity.put("session", id);
			proximity.put("table", "Hand");
			proximity.put("statistics", true);
			subinput.put(proximity);

			JSONObject proximityf = new JSONObject();
			proximityf.put("type", "proximity");
			proximityf.put("table", "Face");
			proximityf.put("session", id);
			proximityf.put("statistics", true);
			subinput.put(proximityf); 

			JSONObject presence = new JSONObject();
			presence.put("type", "presence");
			presence.put("session", id);
			subinput.put(presence);

			rinput.put("operations", subinput);
		}catch(Exception e){}
		return rinput;
	}

	public boolean isTerminated(Session s){

		//check the OpDetails that correspond to the launched operations 
		for(Long i : this.ids){
			OpDetail op = Util.doQueryUnique(s, "SELECT O FROM OpDetail AS O WHERE O.id = :idi", "idi", i);

			if(op.getStatus() == OpDetail.Status.EXECUTING || op.getStatus() == OpDetail.Status.QUEUED){
				return false;
			}
		}
		return true;
	}

	public List<? extends BaseData> extract() throws Exception{
		return null;
	}

	public String getStringResult(){
		return null;
	}

	@Override
	public void run(List<? extends Data> objs) throws Exception{

	}

	@Override
	public void storeResult() throws Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


		/**check that there are no operations with the same name: in case of tie
		keep the one with the highest submission time**/
		for(int i=0; i<op_mapping.size(); i++){
			String query = "SELECT U FROM OpDetail AS U WHERE U.name = :na AND U.session.id = :ses";
			List<OpDetail> ops =  Util.doQuery(my_session, query, "na", "aftersession_" + op_mapping.get(i), "ses", cur_session);

			if(ops.size() > 1){

				OpDetail cur_max = ops.get(0);
				ArrayList<OpDetail> todelete = new ArrayList<OpDetail>();
				for(int j=1; j<ops.size(); j++){
					long date1 = dateFormat.parse(ops.get(j).submission_time).getTime();
					long date2 = dateFormat.parse(cur_max.submission_time).getTime();
					if(date1 > date2){
						todelete.add(cur_max);
						cur_max = ops.get(j);
					}
				}

				if(todelete.size() > 0){
					Util.delete(my_session, todelete.toArray(new OpDetail[todelete.size()]));
				}
			}
		}
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

}
