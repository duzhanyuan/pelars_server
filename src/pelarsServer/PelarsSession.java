package pelarsServer;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.io.Files;

import authorization.Permissible;

import servlets.Util;

/**
 * 
 * @author Giacomo Dabisias, Lorenzo Landolfi
 * this class is used to represent information regarding PELARS sessions
 *
 */
public class PelarsSession implements Permissible {

	public long id;
	public User user;
	public String institution_name;
	public String institution_address;
	public String start;
	public String end;
	public String description;
	public Integer score;
	public Boolean is_valid = false;


	public Boolean getIs_valid() {
		return is_valid;
	}

	public void setIs_valid(Boolean is_valid) {
		this.is_valid = is_valid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public long getId(){
		return id;
	}

	public void setId(long i){
		id = i;
	}

	public User getUser(){
		return user;
	}

	public void setUser(User user){
		this.user = user;
	}

	public String getInstitution_name(){
		return institution_name;
	}

	public void setInstitution_name(String institution_name){
		this.institution_name = institution_name;
	}

	public String getInstitution_address(){
		return institution_address;
	}

	public void setInstitution_address(String institution_address){
		this.institution_address = institution_address;
	}

	public String getStart(){
		return start;
	}

	public void setStart(String start){
		this.start = start;
	}

	public String getEnd(){
		return end;
	}
	
	public boolean isAlive(){
		return this.end == null;
	}

	public void setEnd(String end){
		this.end = end;
	}
	/**
	 * 
	 * @param cur_session the hibernate session
	 *deletes all the entities associated with a session 
	 */
	public void deleteData(Session cur_session){
		List<BaseData> associated = null;
		List<MultimediaContent> media_associated = null;
		List<PhaseEntity> phases_associated = null;
		List<OpDetail> op_associated = null;
		List<Calibration> calib_associated = null;

		try{
			associated = Util.doQuery(cur_session, "SELECT S from BaseData AS S where S.session = :id", "id" , this);
			media_associated = Util.doQuery(cur_session,"SELECT M from MultimediaContent AS M where M.session = :id","id",this);
			phases_associated = Util.doQuery(cur_session,"SELECT M from PhaseEntity AS M where M.session = :id","id",this);
			op_associated = Util.doQuery(cur_session,"SELECT M from OpDetail AS M where M.session = :id","id",this);
			calib_associated = Util.doQuery(cur_session,"SELECT M from Calibration AS M where M.session = :id","id",this);
		}
		catch (Exception e){
		}
		Util.delete(cur_session, associated.toArray(new BaseData[associated.size()]));

		Util.delete(cur_session, media_associated.toArray(new MultimediaContent[media_associated.size()]));

		Util.delete(cur_session, phases_associated.toArray(new PhaseEntity[phases_associated.size()]));

		for(OpDetail op : op_associated){
			op.deleteStreams(cur_session);
			//	Util.delete(cur_session, op);
		}

		Util.delete(cur_session, op_associated.toArray(new OpDetail[op_associated.size()]));

		Util.delete(cur_session, calib_associated.toArray(new Calibration[calib_associated.size()]));

		//Delete also files associated to its multimedia contents
		try {
			Util.executeCommand("rm -r " + System.getProperty("upload.location") + this.getId());
		} catch (Exception e) {}
	}


	public void convertTime(){

		//for compatibility duration is reported also if Session start and end time are stored as a date
		DateFormat date_format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date_start,date_end;

		try {
			date_start = date_format.parse(start);
			Long epoch_start = date_start.getTime();
			this.start = epoch_start.toString();
			if (end != null){
				date_end = date_format.parse(end);
				Long epoch_end = date_end.getTime();
				this.end = epoch_end.toString();
			}
		} catch (ParseException e) {

		}
	}

	public long getDuration(){
		return Long.parseLong(end) - Long.parseLong(start);
	}

	public long[] getPhaseBounds(String req_phase, Session session) throws Exception{

		long[] bounds = new long[2];

		List<PhaseEntity> phases = Util.doQuery(session, "SELECT P FROM PhaseEntity AS P WHERE P.session.id = :id AND "
				+ "P.phase = :phase ORDER BY P.start", "id",id,"phase",req_phase);

		if(phases == null || phases.size() == 0){
			throw new Exception("No such phase");
		}

		bounds[0] = phases.get(0).getStart();
		bounds[1] = phases.get(phases.size()-1).getEnd();

		return bounds;
	}

	public JSONObject toJson(){

		long duration = 0;
		String formatted_start,formatted_end;
		//for compatibility duration is reported also if Session start and end time are stored as a date
		DateFormat date_format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date_start,date_end;

		try {
			date_start = date_format.parse(start);
			if (end != null){
				date_end = date_format.parse(end);
				duration = date_end.getTime() - date_start.getTime();
			}
		} catch (ParseException e) {

			if (end != null ){
				duration = Long.parseLong(end) - Long.parseLong(start);
				Date d_end = new Date(Long.parseLong(end));	
				formatted_end = date_format.format(d_end);
			}

			Date d_start = new Date(Long.parseLong(start));
			formatted_start = date_format.format(d_start);
		}

		JSONObject jo = new JSONObject();
		try{
			jo.put("session", id);
			jo.put("user", user.getEmail());
			jo.put("institution_name", institution_name);
			jo.put("institution_address", institution_address);
			jo.put("start", start);
			jo.put("end", end);
			jo.put("description", description);
			jo.put("score", score);
			jo.put("is_valid", is_valid);

			if(duration != 0){
				jo.put("duration", duration);
			}
		}
		catch (JSONException e){
			e.printStackTrace();
		} 
		return jo;
	}

	@Override
	public boolean belongs(User u, Session session) {

		return this.getUser().equals(u);
	}

	@Override
	public boolean belongsToGroup(User u, Session session){

		return u.namespace.equals(this.getUser().getNamespace());
	}

	/**
	 * This should append all data belonging to other to this session, without copying anything. other is destroyed
	 * @param session to be appended to this one
	 * @param session Hibernate session
	 */
	public void append(PelarsSession other, Session session){

	}

	/**
	 * 
	 * @param other
	 * @param session
	 * @return a deep copy of this session, includeing all db associated data
	 */
	/*public PelarsSession copy(Session session){

		PelarsSession ret = new PelarsSession();
		ret.description = "merging between " + this.getId() + " and " + other.getId();
		ret.start = this.start;
		ret.end = this.end;
		ret.institution_address = this.institution_address;
		ret.institution_name = this.institution_name;
		ret.is_valid = this.is_valid;
		ret.user = this.getUser();

		//save session to get its ID
		Util.save(session, ret);

		//merge all data samples
		List<BaseData> firstsession_samples = Util.doQuery(session, "SELECT B FROM BaseData AS B WHERE B.session = :ses", "ses", this);
		List<BaseData> newsession_samples = new ArrayList<BaseData>(firstsession_samples.size());

		newsession_samples.addAll(firstsession_samples);

		BaseData[] finalcopy = new BaseData[newsession_samples.size()];
		for(int j=0; j<finalcopy.length; j++){

			finalcopy[j] = (BaseData)newsession_samples.get(j).clone();
			finalcopy[j].session = ret;
		}

		Util.save(session,finalcopy);

		//merge all multimedia
		List<MultimediaContent> firstsession_multimedia = Util.doQuery(session,"SELECT M from MultimediaContent AS M where M.session = :id","id", this.id);
		List<MultimediaContent> newsession_multimedia = new ArrayList<MultimediaContent>(firstsession_multimedia.size());
		newsession_multimedia.addAll(firstsession_multimedia);

		MultimediaContent[] finalcopy_m = new MultimediaContent[newsession_multimedia.size()];
		for(int j=0; j<finalcopy_m.length; j++){

			finalcopy_m[j] = (MultimediaContent)newsession_multimedia.get(j).clone();
			finalcopy_m[j].session = ret.getId();
		}

		Util.save(session,finalcopy_m);

		//merge files
		String path = System.getProperty("upload.location") + ret.getId();
		Util.executeCommand("mkdir " + path);
		Util.executeCommand("mkdir " + path + "/image");
		Util.executeCommand("mkdir " + path + "/text");
		Util.executeCommand("mkdir " + path + "/video");

		MultimediaContent m,old_m;
		PelarsSession cur_session = this;
		for(int i=0; i<finalcopy_m.length; i++){

			m = finalcopy_m[i];
			old_m = newsession_multimedia.get(i);
			//SAVE in the proper folder
			try{
			File from = new File(System.getProperty("upload.location") + cur_session.getId() + "/" + old_m.getType() + "/" + old_m.getId() + "." + old_m.getMimetype());
			File to = new File(System.getProperty("upload.location") + ret.getId() + "/" + m.getType() + "/" + m.getId() + "." + m.getMimetype());
			Files.copy(from, to);
			}catch(IOException e){}

		}

		//merge associated operations
		List<OpDetail> firstsession_op = Util.doQuery(session,"SELECT M from OpDetail AS M where M.session = :id","id",this);
		List<OpDetail> newsession_op = new ArrayList<OpDetail>(firstsession_op.size());

		newsession_op.addAll(firstsession_op);
		OpDetail[] finalcopy_o = new OpDetail[newsession_op.size()];

		for(int j=0; j<newsession_op.size();j++){
			finalcopy_o[j] = (OpDetail)newsession_op.get(j).clone();
			finalcopy_o[j].session = ret;
		}

		Util.save(session, finalcopy_o);

		//TODO: maybe possibility of choosing which calibration
		List<Calibration> calib_associated = Util.doQuery(session,"SELECT M from Calibration AS M where M.session = :id","id",this);
		for(Calibration c : calib_associated){
			c = new Calibration(c);
			c.session = ret;
			Util.save(session, c);
		}


		List<PhaseEntity> firstsession_phases= Util.doQuery(session,"SELECT M from PhaseEntity AS M where M.session = :id","id",this);
		List<PhaseEntity> newsession_phases = new ArrayList<PhaseEntity>(firstsession_phases.size());

		newsession_phases.addAll(firstsession_phases);

		List<PhaseEntity> finalcopy_p = new ArrayList<PhaseEntity>(newsession_phases.size());

		for(int j=0; j<newsession_phases.size(); j++){
			PhaseEntity e = new PhaseEntity(newsession_phases.get(j));
			e.session = ret;
			finalcopy_p.add(e);
		}


		Util.save(session, finalcopy_p.toArray(new PhaseEntity[finalcopy_p.size()]));

		return ret;
	} */

	/**
	 * TODO:this method merges two sessions without performing any deep copy
	 * @param other: session this one has to be merged with
	 * @param session: hibernate session
	 * @return new session
	 */
	/*public PelarsSession smartMerge(PelarsSession other, Session session) throws Exception{

		if(this.end == null || other.end == null){
			throw new Exception("Session termination cannot be null");
		}

		PelarsSession firstsession = (Long.parseLong(this.start) < Long.parseLong(other.start)) ? this : other;
		PelarsSession lastsession = (Long.parseLong(this.start) < Long.parseLong(other.start)) ? other : this;

		Long tdiff = Long.parseLong(lastsession.start) - Long.parseLong(firstsession.end);

		String desc = this.getDescription();
		if(desc == null){
			desc = other.getDescription();
		}
		if(desc == null){
			desc = "merging between " + this.getId() + " and " + other.getId();
		}

		PelarsSession ret = new PelarsSession();
		ret.description = desc;
		ret.start = firstsession.start;
		long endt = Long.parseLong(firstsession.start) + firstsession.getDuration() + lastsession.getDuration();
		ret.end = "" + endt;
		ret.institution_address = this.institution_address;
		ret.institution_name = this.institution_name;
		ret.is_valid = this.is_valid || other.is_valid;
		ret.user = this.getUser();

		//save session to get its ID
		Util.save(session, ret);

		//update session and time instead of extract and copy
		String query = "UPDATE BaseData AS B SET B.session = :nses WHERE B.session= :ses";
		Util.doUpdate(session, query, "ses", firstsession, "nses",ret);
		query = "UPDATE BaseData AS B SET B.session = :nses , B.time = B.time - :ntime WHERE B.session = :ses";
		Util.doUpdate(session, query, "nses", ret, "ses", lastsession, "ntime", tdiff);

		List<MultimediaContent> oldms = Util.doQuery(session, "SELECT MultimediaContent AS M WHERE M.session = :ses", "ses", firstsession.getId());

		query = "UPDATE MultimediaContent AS M SET M.session = :nses WHERE M.session = :ses";
		Util.doUpdate(session, query, "nses", ret.getId(), "ses", firstsession.getId());
		query = "UPDATE MultimediaContent AS M SET M.session = :nses , M.time = M.time - :ntime WHERE M.session = :ses";
		Util.doQuery(session, query, "nses", ret.getId(), "ntime", tdiff, "ses", lastsession.getId());

		//merge files
		String path = System.getProperty("upload.location") + ret.getId();
		Util.executeCommand("mkdir " + path);
		Util.executeCommand("mkdir " + path + "/image");
		Util.executeCommand("mkdir " + path + "/text");
		Util.executeCommand("mkdir " + path + "/video");

		List<MultimediaContent> finalcopy_m = Util.doQuery(session, "SELECT MultimediaContent AS M WHERE M.session = :ses", "ses", ret.getId());

		MultimediaContent m;
		PelarsSession cur_session = firstsession;
		for(int i=0; i<finalcopy_m.size(); i++){

			if(i >= oldms.size()){
				cur_session = lastsession;
			}

			m = finalcopy_m.get(i);
			//SAVE in the proper folder
			try{
				File from = new File(System.getProperty("upload.location") + cur_session.getId() + "/" + m.getType() + "/" + m.getId() + "." + m.getMimetype());
				File to = new File(System.getProperty("upload.location") + ret.getId() + "/" + m.getType() + "/" + m.getId() + "." + m.getMimetype());
				Files.copy(from, to);
			}catch(IOException e){}

		}

		query = "UPDATE OpDetail AS M SET M.session = :nses WHERE M.session = :ses";
		Util.doUpdate(session, query, "nses", ret.getId(), "ses", firstsession.getId());
		query = "UPDATE OpDetail AS M SET M.session = :nses WHERE M.session = :ses";
		Util.doQuery(session, query, "nses", ret.getId(), "ses", lastsession.getId());

		query = "UPDATE Calibration AS M SET M.session = :nses WHERE M.session = :ses";
		Util.doUpdate(session, query, "nses", ret.getId(), "ses", firstsession.getId());
		query = "UPDATE Calibration AS M SET M.session = :nses WHERE M.session = :ses";
		Util.doQuery(session, query, "nses", ret.getId(), "ses", lastsession.getId());

		List<PhaseEntity> firstsession_phases= Util.doQuery(session,"SELECT M from PhaseEntity AS M where M.session = :id","id",firstsession);
		List<PhaseEntity> lastsession_phases= Util.doQuery(session,"SELECT M from PhaseEntity AS M where M.session = :id","id",lastsession);
		List<PhaseEntity> newsession_phases = new ArrayList<PhaseEntity>(firstsession_phases.size() + lastsession_phases.size());

		newsession_phases.addAll(firstsession_phases);
		newsession_phases.addAll(lastsession_phases);
		List<PhaseEntity> finalcopy_p = new ArrayList<PhaseEntity>(newsession_phases.size());

		for(int j=0; j<newsession_phases.size(); j++){
			PhaseEntity e = new PhaseEntity(newsession_phases.get(j));

			if(j >= firstsession_phases.size()){
				e.start -= tdiff;
				e.end -= tdiff;
			}
			e.session = ret;
			finalcopy_p.add(e);
		}

		//TODO: if first phase after setup of the second session is the same of the last of the first session, merge them
		for(int i = 1; i < finalcopy_p.size(); i++){
			if(finalcopy_p.get(i).phase.equals("setup")){
				finalcopy_p.get(i-1).end = finalcopy_p.get(i).end;
				finalcopy_p.remove(i);

				if (finalcopy_p.get(i).getPhase().equals(finalcopy_p.get(i-1).getPhase())){
					finalcopy_p.get(i-1).end = finalcopy_p.get(i).end;
					finalcopy_p.remove(i);
					break;
				}	
			}
		}

		return ret;
	}


	public PelarsSession merge(PelarsSession other) throws Exception{
		Session session = HibernateSessionManager.getSession();
		PelarsSession nsession = this.merge(other,session);
		if(session.isOpen()){
			session.close();
		}
		return nsession;
	}*/

	/**
	 * 
	 * @param other
	 * @return a session which is the fusion of this session with the parameter.
	 */
	public PelarsSession merge(PelarsSession other, Session session) throws Exception {

		if(this.end == null || other.end == null){
			throw new Exception("Session termination cannot be null");
		}

		PelarsSession firstsession = (Long.parseLong(this.start) < Long.parseLong(other.start)) ? this : other;
		PelarsSession lastsession = (Long.parseLong(this.start) < Long.parseLong(other.start)) ? other : this;

		Long tdiff = Long.parseLong(lastsession.start) - Long.parseLong(firstsession.end);

		PelarsSession ret = new PelarsSession();

		String desc = this.getDescription();
		if(desc == null){
			desc = other.getDescription();
		}
		if(desc == null){
			desc = "merging between " + this.getId() + " and " + other.getId();
		}

		ret.description = desc;
		ret.start = firstsession.start;
		long endt = Long.parseLong(firstsession.start) + firstsession.getDuration() + lastsession.getDuration();
		ret.end = "" + endt;
		ret.institution_address = this.institution_address;
		ret.institution_name = this.institution_name;
		ret.is_valid = this.is_valid || other.is_valid;
		ret.user = this.getUser();

		//save session to get its ID
		Util.save(session, ret);

		//merge all data samples
		List<BaseData> firstsession_samples = Util.doQuery(session, "SELECT B FROM BaseData AS B WHERE B.session = :ses", "ses", firstsession);
		List<BaseData> lastsession_samples = Util.doQuery(session, "SELECT B FROM BaseData AS B WHERE B.session = :ses", "ses", lastsession);	
		List<BaseData> newsession_samples = new ArrayList<BaseData>(firstsession_samples.size() + lastsession_samples.size());

		newsession_samples.addAll(firstsession_samples);
		newsession_samples.addAll(lastsession_samples);

		BaseData[] finalcopy = new BaseData[newsession_samples.size()];
		for(int j=0; j<finalcopy.length; j++){

			finalcopy[j] = (BaseData)newsession_samples.get(j).clone();
			if(j >= firstsession_samples.size()){
				finalcopy[j].time -= tdiff;
			}
			finalcopy[j].session = ret;
		}

		Util.save(session,finalcopy);

		//merge all multimedia
		List<MultimediaContent> firstsession_multimedia = Util.doQuery(session,"SELECT M from MultimediaContent AS M where M.session = :id","id",firstsession);
		List<MultimediaContent> lastsession_multimedia = Util.doQuery(session,"SELECT M from MultimediaContent AS M where M.session = :id","id",lastsession);
		List<MultimediaContent> newsession_multimedia = new ArrayList<MultimediaContent>(firstsession_multimedia.size() + lastsession_multimedia.size());
		newsession_multimedia.addAll(firstsession_multimedia);
		newsession_multimedia.addAll(lastsession_multimedia);

		MultimediaContent[] finalcopy_m = new MultimediaContent[newsession_multimedia.size()];
		for(int j=0; j<finalcopy_m.length; j++){

			finalcopy_m[j] = (MultimediaContent)newsession_multimedia.get(j).clone();
			if(j >= firstsession_multimedia.size()){
				finalcopy_m[j].time -= tdiff;
			}
			finalcopy_m[j].session = ret;
		}

		Util.save(session,finalcopy_m);

		//merge files
		String path = System.getProperty("upload.location") + ret.getId();
		Util.executeCommand("mkdir " + path);
		Util.executeCommand("mkdir " + path + "/image");
		Util.executeCommand("mkdir " + path + "/text");
		Util.executeCommand("mkdir " + path + "/video");

		MultimediaContent m,old_m;
		PelarsSession cur_session = firstsession;
		for(int i=0; i<finalcopy_m.length; i++){

			if(i >=firstsession_multimedia.size()){
				cur_session = lastsession;
			}

			m = finalcopy_m[i];
			old_m = newsession_multimedia.get(i);
			//SAVE in the proper folder
			try{
				File from = new File(System.getProperty("upload.location") + cur_session.getId() + "/" + old_m.getType() + "/" + old_m.getId() + "." + old_m.getMimetype());
				File to = new File(System.getProperty("upload.location") + ret.getId() + "/" + m.getType() + "/" + m.getId() + "." + m.getMimetype());
				Files.copy(from, to);
			}catch(IOException e){}

		}

		//merge associated operations
		List<OpDetail> firstsession_op = Util.doQuery(session,"SELECT M from OpDetail AS M where M.session = :id","id",firstsession);
		List<OpDetail> lastsession_op = Util.doQuery(session,"SELECT M from OpDetail AS M where M.session = :id","id",lastsession);
		List<OpDetail> newsession_op = new ArrayList<OpDetail>(firstsession_op.size() + lastsession_op.size());

		newsession_op.addAll(firstsession_op);
		newsession_op.addAll(lastsession_op);
		OpDetail[] finalcopy_o = new OpDetail[newsession_op.size()];

		for(int j=0; j<newsession_op.size();j++){
			finalcopy_o[j] = (OpDetail)newsession_op.get(j).clone();
			finalcopy_o[j].session = ret;
		}

		Util.save(session, finalcopy_o);

		//TODO: maybe possibility of choosing which calibration
		List<Calibration> calib_associated = Util.doQuery(session,"SELECT M from Calibration AS M where M.session = :id","id",firstsession);
		for(Calibration c : calib_associated){
			c = new Calibration(c);
			c.session = ret;
			Util.save(session, c);
		}


		List<PhaseEntity> firstsession_phases= Util.doQuery(session,"SELECT M from PhaseEntity AS M where M.session = :id","id",firstsession);
		List<PhaseEntity> lastsession_phases= Util.doQuery(session,"SELECT M from PhaseEntity AS M where M.session = :id","id",lastsession);
		List<PhaseEntity> newsession_phases = new ArrayList<PhaseEntity>(firstsession_phases.size() + lastsession_phases.size());

		newsession_phases.addAll(firstsession_phases);
		newsession_phases.addAll(lastsession_phases);
		List<PhaseEntity> finalcopy_p = new ArrayList<PhaseEntity>(newsession_phases.size());

		for(int j=0; j<newsession_phases.size(); j++){
			PhaseEntity e = new PhaseEntity(newsession_phases.get(j));

			if(j >= firstsession_phases.size()){
				e.start -= tdiff;
				e.end -= tdiff;
			}
			e.session = ret;
			finalcopy_p.add(e);
		}

		//TODO: if first phase after setup of the second session is the same of the last of the first session, merge them
		for(int i = 1; i < finalcopy_p.size(); i++){
			if(finalcopy_p.get(i).phase.equals("setup")){
				finalcopy_p.get(i-1).end = finalcopy_p.get(i).end;
				finalcopy_p.remove(i);

				if (finalcopy_p.get(i).getPhase().equals(finalcopy_p.get(i-1).getPhase())){
					finalcopy_p.get(i-1).end = finalcopy_p.get(i).end;
					finalcopy_p.remove(i);
					break;
				}	
			}
		}
		Util.save(session, finalcopy_p.toArray(new PhaseEntity[finalcopy_p.size()]));

		return ret;
	}
}
