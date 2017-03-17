package servlets;

import hibernateMapping.HibernateSessionManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;

import pelarsServer.Error;
import pelarsServer.PelarsSession;
import authorization.Permissible;

@WebServlet("/goodsession/*")
public class GoodSessionsManager extends HttpServlet {
	
	private static final long serialVersionUID = 1010L;
	
	Session session;
	
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
			result = SessionManager.getSession(session_id, session);
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
				
				results = Util.doQuery(session, "SELECT S FROM PelarsSession AS S WHERE S.is_valid = :val", "val", true);
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

}
