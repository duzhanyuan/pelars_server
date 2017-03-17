package operations;

import java.util.List;

import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Data;
import pelarsServer.PelarsSession;
import pelarsServer.User;
import servlets.Util;

/**
Abstract class defining an operation
 */
public abstract class Operation implements Cloneable{

	public Long id;
	public Session my_session;	
	public String name;
	public long cur_session;
	public Double from;
	public Double to;
	public User user;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String phase = null;

	public Operation(){

	}

	public Operation(JSONObject content) throws JSONException{
		
		cur_session = 0;
		try{
			cur_session = content.getLong("session");
		} catch(JSONException ej){

		}

		try{
			name = content.getString("name");
		} catch(JSONException e){}

		try{
			phase = content.getString("phase");
		} catch(JSONException ej){}
	}

	public void routine() throws Exception{
		this.run(this.extract());
		this.storeResult();
	}

	/**
	Extracts the input of the operation from the database to an array of BaseData
	 */
	public abstract List<? extends Data> extract() throws Exception;

	/**
	Store the result in the DB
	 */
	public abstract void storeResult() throws Exception;

	public abstract List<? extends Data> getResult();

	/**
	Manipulates data
	 */
	public abstract void run(List<? extends Data> objs) throws Exception;

	public abstract boolean isStream();

	public abstract String getStringResult();

	public PelarsSession getSession() throws Exception{
		return getSession(cur_session);
	}

	/**
	returns the PELARS session object corresponding to session_id
	 */
	public PelarsSession getSession(long session_id) throws Exception{

		List<PelarsSession> for_query = Util.doQuery(my_session, "Select S from PelarsSession AS S where" +
				" S.id = :id", "id", session_id);

		if(for_query.size() == 0){
			throw new RuntimeException("Requested session not present");
		}
		else {
			return for_query.get(0);
		}
	}

	public Object clone(){
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
