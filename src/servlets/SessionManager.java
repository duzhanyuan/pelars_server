package servlets;

import hibernateMapping.HibernateSessionManager;

import java.util.Date;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;

import operations.*;

import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import authorization.Permissible;

import pelarsServer.Error;
import pelarsServer.OpDetail;
import pelarsServer.PelarsSession;
import pelarsServer.PhaseEntity;
import pelarsServer.User;

/**
servlet to manage PELARS sessions, supports GET,PUT,DELETE,POST on /session/{session_id}
 */
@WebServlet("/session/*")
public class SessionManager extends HttpServlet {

	private static final long serialVersionUID = 4L;
	Session session;

	public static PelarsSession getSession(long id, Session session) throws Exception {

		PelarsSession ret = null;

		//If session id is not present in db the method will return a null object

		ret = Util.doQueryUnique(session, "SELECT S FROM PelarsSession AS S WHERE S.id = :session_id", "session_id", id);

		return ret;
	}

	/**
	correct endpoint: /session/
	 */
	public void doPut(HttpServletRequest request, 
			HttpServletResponse response) throws IOException{

		PrintWriter out = response.getWriter();

		response.addHeader("Content-Type", "application/json");

		StringBuffer jb = new StringBuffer();
		String line = null;
		JSONObject content = null;

		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
			content = new JSONObject(jb.toString());
		} catch (Exception e) { 
			out.println(new Error(114).toJson());
		}
		//accept /session and /session/
		if(request.getPathInfo() == null || request.getPathInfo().split("/").length < 2)
			newSession(request, content, response);
		else{
			out.println(new Error(120).toJson());
		}
	}

	public void newSession(HttpServletRequest servlet_req, JSONObject request, 
			HttpServletResponse response) throws IOException {

		//can perform check immediately!
		session = HibernateSessionManager.getSession();
		PrintWriter out = response.getWriter();
		if (!ACL_RuleManager.Check(Util.getUser(servlet_req), "PUTSESSION", session)){

			response.setStatus(403);
			out.println(new Error(135).toJson());

			if(session.isOpen())
				session.close();

			return;
		}

		long user_id = 0;
		String institution_name = null;
		String institution_address = null;
		PelarsSession s = new PelarsSession(); 

		try{
			user_id = request.getLong("user_id");
			institution_name = request.getString("institution_name");
			institution_address = request.getString("institution_address");

		}catch (JSONException e){
			out.println(new Error(102).toJson());
			return;
		}


		//each PELARS session must be related to a User
		List<User> m_user = null;
		try {
			m_user = Util.doQuery(session, "SELECT U FROM User AS U WHERE U.id = :id", "id", user_id);
		}catch (Exception e1){
			if(session.isOpen())
				session.close();
			out.println(new Error(106).toJson());
			return;
		}

		if (m_user.size() == 0){
			if (session.isOpen())
				session.close();
			out.println(new Error(109).toJson());
			return;
		}
		//set the correct reference to User in the PELARS session instance
		else {
			s.user = m_user.get(0);
		}

		String start = null;
		try {
			start = request.getString("start");
		} catch (JSONException e) {
		}

		s.institution_address = institution_address;
		s.institution_name = institution_name;

		if(start == null){
			Date date = new Date();
			s.start = new Long(date.getTime()).toString();
		} 
		else{
			s.start = start;
		}

		//get optional description
		try{
			s.description = request.getString("description");
		}catch(Exception e){}


		Util.save(session, s);

		PhaseEntity setup = new PhaseEntity();
		setup.setPhase("setup");
		setup.setSession(s);

		//ensure session start and end are in epoch
		setup.setStart(Long.parseLong(s.getStart()));

		Util.save(session, setup);

		//return the identifier of the PELARS session
		out.println(new Status(s.getId(), "open").toJson());	

		if(session.isOpen())
			session.close();
	}
	/**
	 * correct endpoint: /session/session_id. Only used to close PELARS sessions
	 */
	public void doPost(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		String [] parameters = null;
		StringBuffer jb = new StringBuffer();
		String line = null;
		JSONObject m = null;

		response.addHeader("Content-Type", "application/json");

		try{
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
			m = new JSONObject(jb.toString());
		}catch(Exception e){ 
			new Error(114).toJson();
		}

		if(request.getPathInfo() == null)
			out.println(new Error(113).toJson());
		else{
			parameters = request.getPathInfo().split("/");
			if(parameters.length >= 2)
				if(Util.isInteger(parameters[1]))
					updateSession(Long.parseLong(parameters[1]), request,response, m);
				else
					out.println(new Error(116).toJson());
			else 
				out.println(new Error(120).toJson());
		}
	}

	public void triggerCloseRoutine(long id) throws Exception{

		OperationManager.newJob(operations.Group.defaultInput(id));
	}

	public void updateSession( long request_id, HttpServletRequest req,
			HttpServletResponse response, JSONObject content) throws IOException{

		PrintWriter out = response.getWriter();
		String op_type = null;
		String close_date = null;
		boolean not_present = false;

		//perform the checks on the session we want to modify
		session = HibernateSessionManager.getSessionFactory().openSession();

		try {
			op_type = content.getString("op_code");
		} catch (JSONException e) {
			op_type = "";
		}

		Date date = null;
		PelarsSession mod_session = null;
		try {
			mod_session = getSession(request_id, session);
		} catch (Exception e1) {
			out.println(e1.getMessage());
			if(session.isOpen()){
				session.close();
			}
			return;
		}

		switch(op_type){
		case "close":

			try{
				close_date = content.getString("time");
			} catch(JSONException e){}

			if (close_date == null){
				date = new Date();
				close_date = new Long(date.getTime()).toString();
			}

			if (mod_session == null){
				out.print(new Error(112).toJson());
				not_present = true;
			}
			else{
				try {
					if (ACL_RuleManager.Check(Util.getUser(req), "POSTSESSION", mod_session, session)){
						//get the last phase interval associated to the closed session
						PhaseEntity pe = null;
						try {
							pe = Util.doQueryUnique(session, "SELECT P FROM PhaseEntity AS P "
									+ " WHERE P.start = (SELECT max(PP.start) from PhaseEntity AS PP WHERE PP.session.id = :session)",
									"session", request_id);
						} catch (Exception e) {}

						if(mod_session.end == null){
							mod_session.end = close_date;
						}


						pe.setEnd(Long.parseLong(mod_session.end));

						Util.update(session, mod_session);
						Util.update(session, pe);

						//start routine to get average and meaningful data
						//this.triggerCloseRoutine(request_id);

						//	out.println(new Status(request_id, "Closed").toJson());	
					}
					else{
						response.setStatus(403);
						out.println(new Error(135).toJson());
					}
				}  catch (Exception e) {
					out.println(e.getMessage());
				}
			}
			break;
		default:
			break;
			//out.println(new Error(103).toJson());
		}

		//Update description if requested
		String desc = null;
		Integer score = null;
		Boolean validity = null;

		if(!session.isOpen()){
			session = HibernateSessionManager.getSessionFactory().openSession();
		}

		try {
			desc = content.getString("description");
		} catch (JSONException e) {}

		try {
			score = content.getInt("score");
		} catch (JSONException e) {}

		try {
			validity = content.getBoolean("is_valid");
		} catch (JSONException e) {}

		if (desc != null){
			String query = "UPDATE PelarsSession S set S.description = :desc WHERE S.id = :s_id";
			Util.doUpdate(session, query, "desc", desc, "s_id", request_id);
		}
		if (score != null){
			String query = "UPDATE PelarsSession S set S.score = :score WHERE S.id = :s_id";
			Util.doUpdate(session, query, "score", score, "s_id", request_id);
		}
		if (validity != null){
			String query = "UPDATE PelarsSession S set S.is_valid = :val WHERE S.id = :s_id";
			Util.doUpdate(session, query, "val", validity, "s_id", request_id);
			session.flush();

			if(validity == true){
				//Check weather the default set of operations has been already computed
				OpDetail example = Util.doQueryUnique(session, "SELECT O FROM OpDetail AS O WHERE O.name = 'aftersession_presence' AND O.session.id = :ses","ses", request_id);
				if(example == null){
					try{
						new Group(Group.defaultInput(request_id));
					}catch(JSONException e){}
				}
			}
		}

		if(session.isOpen())
			session.close();

		if (!not_present){
			out.println(new Status(request_id, "Updated").toJson());
		}

	}

	/**
	correct endpoint: /session/[session_id]
	 */
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {

		String [] parameters = null;

		response.addHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null)
			getSession(response, request);
		else{
			parameters = request.getPathInfo().split("/");
			if(parameters.length <= 1)
				getSession(response, request);
			else{
				if(Util.isInteger(parameters[1])){
					getSession(Long.parseLong(parameters[1]), response, request);}
				else{
					PrintWriter out = response.getWriter();
					out.println(new Error(116).toJson());
				}	
			}
		}
	}

	/**
	 * 
	prints a single PELARS session representation 
	 */
	public void getSession(long session_id, 
			HttpServletResponse response, HttpServletRequest request) throws IOException {

		PelarsSession result = null;
		PrintWriter out = response.getWriter();

		session = HibernateSessionManager.getSession();


		try {
			result = getSession(session_id, session);
		} catch (Exception e1) {
			out.println(e1.getMessage());
		}
		//notify there is not such session in the database
		if(result == null){
			response.setStatus(404);
			out.println(new Error(112).toJson());
		}
		else{
			//user has right to read such a session, return it
			try {
				if (ACL_RuleManager.Check(Util.getUser(request), "GETSESSION", result, session)){
					out.println(result.toJson());
				}
				//notify he has no right to perform this
				else{
					response.setStatus(403);
					out.println(new Error(135).toJson());
				}
			} catch (Exception e) {
				out.println(e.getMessage());
			}
		}

		if(session.isOpen())
			session.close();
	}

	/**
	prints all the PELARS session representation 
	 */
	public void getSession(HttpServletResponse response, HttpServletRequest request) throws IOException {

		String namespace = null;
		List<? extends Permissible> results = null;
		PrintWriter out = response.getWriter();

		session = HibernateSessionManager.getSession();

		namespace = request.getParameter("namespace");
		//filters result according to namespace attribute
		if(namespace != null){
			try {
				results = Util.doQuery(session,  "SELECT S FROM PelarsSession AS S WHERE S.namespace = :namespace" , "namespace", namespace);
			} catch (Exception e) {
				if(session.isOpen())
					session.close();
				out.println(new Error(106).toJson());
				return;
			}
		}
		else{

			try {

				results = Util.doQuery(session, "SELECT S FROM PelarsSession AS S");
				results = ACL_RuleManager.Check(Util.getUser(request), "GETSESSION", results, session);

			} catch (Exception e) {
				if(session.isOpen())
					session.close();
				out.println(new Error(106).toJson());
				return;
			}
		}

		if(results.size() != 0){
			JSONArray oj = new JSONArray();
			for(Permissible ps : results){

				PelarsSession pss = (PelarsSession)ps;
				oj.put(pss.toJson());
			}
			try{
				out.println(oj.toString(4));
			}catch(JSONException e){
				if(session.isOpen())
					session.close();
				out.println(new Error(119).toJson());
			}
		}
		else{
			out.println(new Status("Empty").toJson());
		}
		if(session.isOpen())
			session.close();
	}


	/**
	correct endpoint: /session/{session_id}, deletes a single session
	 */
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();

		response.addHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null){
			out.println(new Error(113).toJson());
			return;
		}

		String [] parameters = request.getPathInfo().split("/");

		if(parameters.length < 1)
			out.println(new Error(113).toJson());
		else{
			if(Util.isInteger(parameters[1]))
				deleteSession(Long.parseLong(parameters[1]), response, request);
			else{
				out.println(new Error(116).toJson());
			}
		}
	}

	/**
	 Deletes the PEALRS session identified by "request" parameter
	 */
	public void deleteSession(long request, HttpServletResponse response, HttpServletRequest req) throws IOException{

		PrintWriter out = response.getWriter();
		PelarsSession result = null;

		session = HibernateSessionManager.getSession();

		try{
			result = getSession(request, session);
		} catch(Exception e){
			out.println(e.getMessage());
		}

		if(result == null){
			out.println(new Error(112).toJson());
		}
		else{
			try {
				if (ACL_RuleManager.Check(Util.getUser(req), "DELETESESSION", result, session)){
					//first of all delete all the data associated to this session
					//result.deleteData(session);
					//delete the session
					Util.delete(session, result);
					//delete upload folder of the session
					Util.executeCommand("rm -r " + System.getProperty("upload.location") + result.getId());
					out.println(new Status("Success").toJson());
				}
				else{
					response.setStatus(403);
					out.println(new Error(135).toJson());
				}
			} catch (Exception e) {
				out.println(e.getMessage());
			}
		}

		if(session.isOpen())
			session.close();
	}

}