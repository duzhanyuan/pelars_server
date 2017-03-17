package pelarsServer;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;

import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi, Giacomo Dabisias
 * this class stores information about submitted operation jobs
 *
 */
public class OpDetail implements Cloneable{
	
	public enum Status{
		TERMINATED, FAILED, EXECUTING, QUEUED
	}

	public long id;
	public Status status;
	public String submission_time;
	public double execution_time;
	public String result;
	public Blob failure_description;
	public boolean is_stream = false;
	public PelarsSession session;
	public String name;
	
	
	public String getName() {
		return name;
	}

	public void setName(String custom_name) {
		this.name = custom_name;
	}

	public PelarsSession getSession(){
		return session;
	}

	public void setSession(PelarsSession s){
		session = s;
	}

	public boolean getIs_stream(){
		return is_stream;
	}

	public void setIs_stream(boolean b){
		is_stream = b;
	}

	public Blob getFailure_description(){
		return failure_description;
	}

	public void setFailure_description(Blob s){
		failure_description = s;
	}

	public long getId(){
		return id;
	}

	public Status getStatus(){
		return status;
	}

	public String getSubmission_time(){
		return submission_time;
	}

	public double getExecution_time(){
		return execution_time;
	}

	public String getResult(){
		return result;
	}

	public void setId(long f){
		id=f;
	}

	public void setStatus(Status s){
		status = s;
	}

	public void setSubmission_time(String s){
		submission_time = s;
	}

	public void setExecution_time(double s){
		execution_time = s;
	}

	public void setResult(String d){
		result = d;
	}
	
	public String getErrorDescription(){
		
		if (failure_description == null){
			return null;
		}
		
		byte[] bytes = null;
		try{
			bytes = this.failure_description.getBytes(1, (int)failure_description.length());
		}catch (SQLException e){
		}
		return new String(bytes);
	}
	
	public void deleteStreams(Session session){
		
		List<StreamElement> my_streams = null;
		
		try {
			my_streams = Util.doQuery(session, "SELECT S FROM StreamElement AS S WHERE S.task = :id",
					"id", this.id);
		} catch (Exception e) {}
		
		Util.delete(session, my_streams.toArray(new StreamElement[my_streams.size()]));
	/*	for(StreamElement st : my_streams){
			Util.delete(session, st);
		}*/
		
	}
	
	public Object clone(){  
	    try{  
	        return super.clone();  
	    }catch(Exception e){ 
	        return null; 
	    }
	}

	public JSONObject toJson(){
		JSONObject jo = new JSONObject();
		try{
			jo.put("id", id);
			jo.put("status", status);
			jo.put("submission_time", submission_time);
			jo.putOpt("execution_time", execution_time);
			jo.putOpt("result", result);
			jo.put("complex", is_stream);
			jo.putOpt("name", name);
			jo.put("session", session.getId());
			jo.putOpt("error", getErrorDescription());
		}catch (JSONException e){
			e.printStackTrace();
		}
		return jo;
	}
}
