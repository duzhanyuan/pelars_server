package servlets;

import hibernateMapping.HibernateSessionManager;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import pelarsServer.Error;
import pelarsServer.PelarsSession;


@WebServlet("/live/*")
public class AliverManager extends HttpServlet{

	private static final long serialVersionUID = 9856L;

	/**
	 * must answer with the status of session session_id
	 */
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

			if(Util.isInteger(parameters[1])){

				isAlive(request, response, Long.parseLong(parameters[1]), out);
			}
			else{
				out.println(new Error(116).toJson());
			}
		}
	}


	private void isAlive(HttpServletRequest request, HttpServletResponse response, long session_id, PrintWriter out){

		Session session = HibernateSessionManager.getSession();

		PelarsSession s = Util.doQueryUnique(session, "SELECT S FROM PelarsSession AS S Where S.id = :id", "id", session_id);

		//check authorization
		if(ACL_RuleManager.Check(Util.getUser(request), "GETDATA", s, session)){

			response.setHeader("Content-Type", "application/json");

			if(websockets.Aliver.online_sessions.contains(session_id)){
				out.println(new Status("online").toJson());
			}
			else{
				out.println(new Status("offline").toJson());
			}
		}
		else{
			response.setStatus(401);
		}

		if(session.isOpen()){
			session.close();
		}
	}


}
