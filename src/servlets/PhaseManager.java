package servlets;

import hibernateMapping.HibernateSessionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Error;
import pelarsServer.PelarsSession;
import pelarsServer.PhaseEntity;

@WebServlet("/phase/*")
public class PhaseManager extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 111113L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();

		response.setHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null){
			out.println(new Error(113).toJson());
			return;
		}

		String [] parameters = request.getPathInfo().split("/");

		if(request.getPathInfo() == null || parameters.length <= 1)
			out.println(new Error(113).toJson());
		else{
			if(Util.isInteger(parameters[1]))
				try {
					getPhase(Long.parseLong(parameters[1]), response, request);
				} catch (Exception e) {
					response.setHeader("Content-Type", "text/plain");
					out.println(e.getMessage());
				}
			else{
				out.println(new Error(116).toJson());
			}
		}
	}

	/**
	 * Prints all the data relative to session "id"as a JSON array
	 */
	public void getPhase(Long id, HttpServletResponse response, HttpServletRequest request) throws Exception{

		Double from = null;
		Double to = null;
		Boolean epoch = false;
		Double start_time = 0.0;

		try{
			from = Double.parseDouble(request.getParameter("from"));
			to = Double.parseDouble(request.getParameter("to"));
			epoch = Boolean.parseBoolean(request.getParameter("epoch"));
		} catch(NullPointerException e){}

		if (from == null){
			from = 0.0;
		}
		if (to == null){
			to = Double.MAX_VALUE;
		}

		List<PhaseEntity> results = null;
		PrintWriter out = response.getWriter();
		Session session = HibernateSessionManager.getSession();


		//TODO: immediately check whether id is the id of a session belonging to user calling
		PelarsSession s = SessionManager.getSession(id,session);

		if (s == null){
			//return that the session does not exist
			out.println(new Error(107).toJson());
			if (session.isOpen()){
				session.close();
			}
			return;
		}
		else{

			if(!epoch){
				start_time = Double.parseDouble(s.getStart());
				from = from * 1000.0;
				to = to * 1000.0;
			}
			if(ACL_RuleManager.Check(Util.getUser(request), "GETDATA", s, session)){
				results = Util.doQuery(session, "SELECT P FROM PhaseEntity AS P WHERE P.session.id = :id ORDER BY P.start", "id", id);

			}
			else{
				response.setStatus(403);
				out.println(new Error(135).toJson());
				if(session.isOpen()){
					session.close();
				}
				return;
			}
		}


		if(session.isOpen())
			session.close();

		JSONArray oj = new JSONArray();

		for (PhaseEntity p: results){
			if (p.getStart() - start_time >= from && p.getStart() - start_time <= to){
				oj.put(p.toJson());
			}
		}

		if(oj.length() > 0)
			out.println(oj.toString(4));
		else
			out.println(new Status("Empty").toJson());	
	}


	public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		JSONObject content = null;
		StringBuffer jb = new StringBuffer();
		String line = null;

		response.setHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null){
			out.println(new Error(113).toJson());
			return;
		}

		try{
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
			content = new JSONObject(jb.toString());
		}catch (Exception e){ 
			out.println(new Error(114).toJson());
		}

		String [] parameters = request.getPathInfo().split("/");

		if(request.getPathInfo() == null || parameters.length <= 1)
			out.println(new Error(113).toJson());
		else{
			if(Util.isInteger(parameters[1])){
				if (ACL_RuleManager.Check(Util.getUser(request), "PUTSESSION")){
					addPhase(Long.parseLong(parameters[1]), response, request, content);
				}
				else{
					response.setStatus(401);
					out.println(new Error(135).toJson());
					return;
				}
			}
			else{
				out.println(new Error(116).toJson());
			}
		}
	}

	private void addPhase(Long session_id, HttpServletResponse response, HttpServletRequest request, JSONObject content) throws IOException{

		PrintWriter out = response.getWriter();
		PhaseEntity phase = new PhaseEntity();
		Long time = null;

		Session session = HibernateSessionManager.getSession();

		try{
			phase.phase = content.getString("phase");
		}catch (JSONException e) {
			out.println(new Error(114).toJson());
			if(session.isOpen())
				session.close();
			return;
		}

		try{
			time = content.getLong("time");
		}catch (JSONException e) {}

		//check if the passed session is valid 

		PelarsSession m_session = null;
		try {
			m_session = Util.doQueryUnique(session, "SELECT S from PelarsSession AS S WHERE S.id = :id", "id",session_id);
		} catch (Exception e) {
			//error in query, send generic error
			out.println(new Error(125).toJson());
			if(session.isOpen())
				session.close();
			return;
		}

		//not valid session, notify client
		if (m_session == null){
			out.println(new Error(107).toJson());
			if(session.isOpen())
				session.close();
			return;
		}

		//check immediately the rights to access
		//TODO: not clear if set specific operation PUTMULTIMEDIA or just leave PUTSESSION, no before must check the session belongs to caller
	/*	if(!ACL_RuleManager.Check(Util.getUser(request), "PUTMULTIMEDIA", m_session ,session)){
			response.setStatus(403);
			out.println(new Error(135).toJson());
			return;
		}*/
		
		long cur_time = 0;

		if (time != null){
			cur_time = time;
		}
		else{
			cur_time = new Date().getTime();
		}
		
		//check that cur_time is not greater than the end time of the session
		boolean to_save = true;
		
		if(m_session.getEnd() != null && cur_time > Long.parseLong(m_session.getEnd())){
			to_save = false;
		}

		if (!phase.phase.equals("collector") && to_save){
			
			phase.setStart(cur_time);
			phase.setSession(m_session);

			//get the last saved phase entity in the same session
			PhaseEntity last_phase = null;
			try {
				last_phase= Util.doQueryUnique(session, "SELECT P FROM PhaseEntity AS P "
						+ " WHERE P.start = (SELECT max(PP.start) from PhaseEntity AS PP WHERE PP.session = :session)",
						"session", m_session);
			} catch (Exception e) {
				response.setHeader("Content-Type", "text/plain");
				out.println(e.getMessage());
				if(session.isOpen())
					session.close();
				return;
			}
			//have to set an end to the last phase only if it is different from the current one
			//must ensure last_phase is always not null
			if(!phase.getPhase().equals(last_phase.getPhase())){
				last_phase.setEnd(cur_time);
				Util.update(session, last_phase);
				Util.save(session, phase);
				out.println(new Status(phase.getId(), "Success").toJson());	
			}
			else{
				out.println(new Status("Neglected").toJson());
			}
		}
		else{
			out.println(new Error(136).toJson());
		}


		if(session.isOpen())
			session.close();



	}


}
