package pelarsServer;

import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;

import authorization.Permissible;

/**
 * 
 * @author Lorenzo Landolfi
 * abstract class representing a generic sampled data
 *
 */
public abstract class Data implements Permissible{

	public PelarsSession session;
	public double time;
	public long id;

	public PelarsSession getSession(){
		return session;
	}

	public void setSession(PelarsSession session){
		this.session = session;
	}

	public double getTime(){
		return time;
	}

	public void setTime(double time){
		this.time = time;
	}

	public long getId(){
		return id;
	}

	public void setId(long id){
		this.id = id;
	}

	public boolean belongs(User u, Session session) {

		return this.session.user.equals(u);
	}

	public boolean belongsToGroup(User u, Session session){
		return this.session.getUser().getNamespace().equals(u.namespace);
	}

	public JSONObject toJson(){

		JSONObject jo = new JSONObject();
		try{
			jo.put("data_id" , id);
			jo.put("session", this.getSession().getId());
			jo.put("time", time);
		}catch (JSONException e){
			e.printStackTrace();
		}
		return jo;
	}

}
