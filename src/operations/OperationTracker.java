package operations;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import pelarsServer.PelarsSession;
import java.text.ParseException;
import servlets.Util;

/**
This operation counts the samples of a given class in the last minute
*/
public class OperationTracker extends OperationStream{

	public OperationTracker(JSONObject content) throws JSONException{
		
		super(content);
		try{
			table = content.getString("table");
		}
		catch (JSONException e){}

		cur_session = content.getLong("session");
	}
	
	/**
	get the difference in milliseconds between two dates
	*/
	private static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit){
	    long diffInMillies = date2.getTime() - date1.getTime();
	    return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
	}
		
	public void run() throws Exception{
		
		List<PelarsSession> cur_ses = Util.doQuery(my_session, "SELECT S from PelarsSession AS S WHERE S.id = :id", "id",cur_session);

		if(cur_ses.size() == 0){
			throw new RuntimeException("Requested session not present");
		}
		
		Date start_date = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		try{
			start_date = dateFormat.parse(cur_ses.get(0).start);
		}
		catch(ParseException e){}
		
		//get the current date and computes the difference
		Date cur_date = new Date();
		//time_diff is the current time relative to the start of the session
		long time_diff = getDateDiff(start_date, cur_date, TimeUnit.SECONDS);
		
		result = Util.doQuery(my_session, 
				"SELECT H from " + table + " AS H WHERE (H.time < :ub AND H.time > :lb AND H.session = :s)",
				"ub", new Double(time_diff), "lb", new Double(time_diff-60),"s",cur_ses.get(0));	 
	}
}