package servlets;

import hibernateMapping.HibernateSessionManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.JSONArray;

import pelarsServer.Calibration;
import pelarsServer.Error;
import pelarsServer.PelarsSession;

@WebServlet("/calibration/*")
public class CalibrationManager extends HttpServlet {


	private static final long serialVersionUID = 1117L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();

		response.setHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null){
			out.println(new Error(113).toJson());
			return;
		}

		String [] parameters = request.getPathInfo().split("/");

		if(request.getPathInfo() == null || parameters.length > 4)
			out.println(new Error(113).toJson());
		else{

			if(Util.isInteger(parameters[1])){
				try {
					GetCalibration(Long.parseLong(parameters[1]),parameters, response, request);
				} catch (Exception e) {
					response.setHeader("Content-Type", "text/plain");
					out.println(e.getMessage());
				}
			}
			else{
				out.println(new Error(116).toJson());
			}
		}
	}

	private void GetCalibration(Long id, String[]params ,HttpServletResponse response, HttpServletRequest request) throws Exception{

		PrintWriter out = response.getWriter();

		List<Calibration> results = null;
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
			if(ACL_RuleManager.Check(Util.getUser(request), "GETDATA", s, session)){

				String query = "SELECT C FROM Calibration AS C WHERE C.session = :ses";

				ArrayList<String> aliases = new ArrayList<String>();
				ArrayList<Object> values = new ArrayList<Object>();
	
				aliases.add("ses");
				values.add(s);

				if(params.length > 2){
					query = query + " AND C.type = :t";
					aliases.add("t");
					values.add(params[2]);
				}

				results = Util.doQuery(session, query, aliases, values);
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

		JSONArray oj = new JSONArray();

		for (Calibration b: results){
			oj.put(b.toJson());
		}

		if(session.isOpen())
			session.close();

		if(oj.length() > 0)
			out.println(oj.toString(4));
		else
			out.println(new Status("Empty").toJson());	
	}
}
