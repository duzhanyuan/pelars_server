package pelarsServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import servlets.Util;

public class CrossSessions {

	private LiveStatistics mean_hand_dist;
	private LiveStatistics mean_face_dist;
	private LiveStatistics mean_presence;
	private LiveStatistics mean_hand_speed;
	private LiveStatistics mean_time_looking;
	private LiveStatistics mean_post_number;
	private HashMap<LiveStatistics,List<PelarsSession>> session_map;

	private static CrossSessions crosssessions;

	private CrossSessions() throws JSONException{

		mean_hand_dist = new SimpleLiveStatistics("m");
		mean_face_dist = new SimpleLiveStatistics("m");
		mean_presence = new SimpleLiveStatistics("%");
		mean_hand_speed  = new SimpleLiveStatistics("m/s");
		mean_time_looking  = new SimpleLiveStatistics("%");
		mean_post_number  = new SimpleLiveStatistics();
		session_map = new HashMap<LiveStatistics,List<PelarsSession>>();

		Session ses = hibernateMapping.HibernateSessionManager.getSession();

		List<PelarsSession> goodsessions = Util.doQuery(ses, "SELECT S FROM PelarsSession AS S WHERE S.is_valid = true");

		String query = "SELECT O FROM OpDetail AS O WHERE O.name = :cname";

		for(int i=0; i<operations.Group.op_mapping.size(); i++){

			List<OpDetail> ops = Util.doQuery(ses, query, "cname", "aftersession_"+operations.Group.op_mapping.get(i));

			LiveStatistics ls = new LiveStatistics();

			switch(i){
			//0: hand_speed
			case 0:
				ls = mean_hand_speed;
				break;
			case 1:
				ls = mean_time_looking;
				break;
			case 2:
				ls = mean_post_number;
				break;
			case 3:
				ls = mean_hand_dist;
				break;
			case 4: 
				ls = mean_face_dist;
				break;
			case 5:
				ls = mean_presence;
				break;
			default:
				break;
			}

			ArrayList<PelarsSession> as_sessions = new ArrayList<PelarsSession>();

			for(OpDetail o : ops){

				JSONObject js = new JSONObject();
				JSONArray jsa = new JSONArray();

				if(o.result != null && goodsessions.contains(o.getSession())){

					switch(i){
					//0: hand_speed
					case 0:
						jsa = new JSONArray(o.result);
						ls.add(jsa.getJSONObject(jsa.length()-1).getDouble("overall"));
						break;
					case 1:
						js = new JSONObject(o.result);
						ls.add(js.getDouble("active_time"));
						break;
					case 2:
						jsa = new JSONArray(o.result);
						ls.add(jsa.getJSONObject(jsa.length()-1).getDouble("total_posts"));
						break;
					case 3:
						js = new JSONObject(o.result);
						ls.add(js.getDouble("mean"));
						break;
					case 4: 
						js = new JSONObject(o.result);
						ls.add(js.getDouble("mean"));
						break;
					case 5:
						js = new JSONObject(o.result);
						ls.add(js.getDouble("total_presence"));
						break;
					default:
						break;
					}
					as_sessions.add(o.getSession());
				}
			}
			session_map.put(ls, as_sessions);
		}
		ses.close();
	}	


	public static CrossSessions getInstance() throws JSONException{

		crosssessions = null;
		crosssessions = new CrossSessions();

		return crosssessions;
	}

	public JSONArray getJSONArray(LiveStatistics l){

		JSONArray jsa = new JSONArray();
		List<PelarsSession> cur_sessions = this.session_map.get(l);
		for(PelarsSession s : cur_sessions){
			jsa.put(s.getId());
		}
		return jsa;
	}

	public JSONObject toJSON() throws JSONException{
		JSONObject js = new JSONObject();
		js.put("hand_distance", this.mean_hand_dist.toJson());
		js.put("face_distance", this.mean_face_dist.toJson());
		js.put("time_looking", this.mean_time_looking.toJson());
		js.put("presence", this.mean_presence.toJson());
		js.put("hand_speed", this.mean_hand_speed.toJson());
		js.put("number of posts", this.mean_post_number.toJson());
		return js;
	}

	public JSONObject toJSONComplete() throws JSONException{
		JSONObject js = new JSONObject();
		js.put("hand_distance", this.mean_hand_dist.toJson().put("sessions", getJSONArray(this.mean_hand_dist)));
		js.put("face_distance", this.mean_face_dist.toJson().put("sessions", getJSONArray(this.mean_face_dist)));
		js.put("time_looking", this.mean_time_looking.toJson().put("sessions", getJSONArray(this.mean_time_looking)));
		js.put("presence", this.mean_presence.toJson().put("sessions", getJSONArray(this.mean_presence)));
		js.put("hand_speed", this.mean_hand_speed.toJson().put("sessions", getJSONArray(this.mean_hand_speed)));
		js.put("number of posts", this.mean_post_number.toJson().put("sessions", getJSONArray(this.mean_post_number)));
		return js;
	}

	//to be called on session close
	public void update(long session_id){
		Session ses = hibernateMapping.HibernateSessionManager.getSession();
		String query = "SELECT O FROM OpDetail AS O WHERE O.name = :cname AND O.session.id = :id";

		for(int i=0; i<operations.Group.op_mapping.size(); i++){
			List<OpDetail> ops = Util.doQuery(ses, query, "cname", operations.Group.op_mapping.get(i), "id", session_id);

			LiveStatistics ls = new LiveStatistics();

			switch(i){
			//0: hand_speed
			case 0:
				ls = mean_hand_speed;
				break;
			case 1:
				ls = mean_time_looking;
				break;
			case 2:
				ls = mean_post_number;
				break;
			case 3:
				ls = mean_hand_dist;
				break;
			case 4: 
				ls = mean_face_dist;
				break;
			case 5:
				ls = mean_presence;
				break;
			default:
				break;
			}

			for(OpDetail o : ops){

				try{
					JSONObject js;
					JSONArray jsa;

					switch(i){
					//0: hand_speed
					case 0:
						jsa = new JSONArray(o.result);
						ls.add(jsa.getJSONObject(jsa.length()-1).getDouble("overall"));
						break;
					case 1:
						js = new JSONObject(o.result);
						ls.add(js.getDouble("active_time"));
						break;
					case 2:
						jsa = new JSONArray(o.result);
						ls.add(jsa.getJSONObject(jsa.length()-1).getDouble("total_posts"));
						break;
					case 3:
						js = new JSONObject(o.result);
						ls.add(js.getDouble("mean"));
						break;
					case 4: 
						js = new JSONObject(o.result);
						ls.add(js.getDouble("mean"));
						break;
					case 5:
						js = new JSONObject(o.result);
						ls.add(js.getDouble("total_presence"));
						break;
					default:
						break;
					}
				}catch(JSONException e){}
			}
		}
		ses.close();
	}
}
