package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.context.internal.ManagedSessionContext;

/**
dummy web servlet used just to keep the connection to database alive.
 */
@WebServlet("/update/*")
public class Refresher extends HttpServlet {

	private static final long serialVersionUID = 198L;

	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {

		response.addHeader("Content-Type", "application/json");

		PrintWriter out = response.getWriter();
		Session session = hibernateMapping.HibernateSessionManager.getSession();

		try{
			ManagedSessionContext.bind(session);
			session.getTransaction().setTimeout(10);
			session.beginTransaction();
			session.getTransaction().commit();
			
		}catch (Exception e) {
			out.println(e.getMessage());
			Util.rollback(session);
			return;
		}

		out.println(new Status("success").toJson());
		if(session.isOpen())
			session.close();
	}
}
