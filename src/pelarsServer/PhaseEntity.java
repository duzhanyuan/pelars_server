package pelarsServer;



import org.json.JSONException;
import org.json.JSONObject;


/**
 * 
 * @author Lorenzo Landolfi, Giacomo Dabisias
 *  This class represent a phase during a session. It can be identified by its name, start and termination time
 *
 */
public class PhaseEntity {
	
	//concatenate phase entities having the same phase field to get all the phase in a session

	public long id;
	public String phase;
	public PelarsSession session;
	public long start;
	public long end;
	
	public PhaseEntity(){
		
	}

	public PhaseEntity(PhaseEntity e){
		this.phase = e.phase;
		this.session = e.session;
		this.start = e.start;
		this.end = e.end;
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getPhase() {
		return phase;
	}
	public void setPhase(String phase) {
		this.phase = phase;
	}
	public PelarsSession getSession() {
		return session;
	}
	public void setSession(PelarsSession session) {
		this.session = session;
	}
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public long getEnd() {
		return end;
	}
	public void setEnd(long end) {
		this.end = end;
	}

	public JSONObject toJson(){

		//	DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		JSONObject jo = new JSONObject();
		try{
			jo.put("data_id" , id);
			jo.put("session", session.getId());
			jo.put("phase", phase);
			//	Date date = new Date(start);
			//	String d = df.format(date);
			//jo.put("start", d);
			jo.put("start", start);
			//	date = new Date(end);
			//	d = df.format(date);
			//jo.put("end", d);
			jo.put("end", end);

		}catch (JSONException e){
			e.printStackTrace();
		}
		return jo;
	}


}
