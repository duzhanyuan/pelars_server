package servlets;

import hibernateMapping.HibernateSessionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;


import pelarsServer.Error;
import pelarsServer.PelarsSession;

@WebServlet("/visualize/*")
public class WidgetServlet extends HttpServlet{

	private static final long serialVersionUID = 1457L;
	Session session;


	public void readFile(PrintWriter writer)  throws IOException 
	{
		
		//
		// We are going to read a file called configuration.properties. This
		// file is placed under the WEB-INF directory.
		//
		String filename = "widgets.html";
		
		ServletContext context = getServletContext();
		
		//
		// First get the file InputStream using ServletContext.getResourceAsStream()
		// method.
		//
		InputStream is = context.getResourceAsStream(filename);
		if (is != null) {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isr);
		
			String text = "";
			
			//
			// We read the file line by line and later will be displayed on the 
			// browser page.
			//
			while ((text = reader.readLine()) != null) {
				writer.print(text);
			}
		}
	}

	/**
	 * valid GET endoint: /visualise/{session_id}/[phase]
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String [] parameters = null;
		Long session_id = 542l;
		String phase = "default";
		int error = 0;
		PrintWriter out = response.getWriter();

		if(request.getPathInfo() != null){

			parameters = request.getPathInfo().split("/");
			if(parameters.length == 1){
				outputHTML(out,session_id,phase,request,response);
			}
			else {
				if(parameters.length <= 2){
					if(Util.isInteger(parameters[1])){
						session_id = Long.parseLong(parameters[1]);
						outputHTML(out,session_id,phase,request,response);
					}
					else{
						//error = 116;
					}
				}
				else{
					if(parameters.length >= 3){
						if(Util.isInteger(parameters[1])){
							session_id = Long.parseLong(parameters[1]);
							phase = parameters[2];
							outputHTML(out,session_id,phase,request,response);
						}
						else{
							//	error = 116;
						}
					}
					else{
						error = 120;
					}
				}
			}
		}
		else{
			outputHTML(out,session_id,phase,request,response);
		}
		if (error != 0){
			out.println(new Error(error).toJson());
		}
	}

	private void outputHTML(PrintWriter out, long session_id, String phase, HttpServletRequest request, HttpServletResponse response){

		//check that the session exist
		session = HibernateSessionManager.getSession();

		PelarsSession s = Util.doQueryUnique(session, "SELECT S FROM PelarsSession AS S Where S.id = :sid", "sid", session_id);

		if (s == null){
			out.println("session not present");
		}
		else{
			out.println("<!doctype html><html><head>");
			out.println("<title>Demo &raquo; Dynamic grid width &raquo; gridster.js</title><link rel=\"stylesheet\" type=\"text/css\" href=\"gridster/dist/jquery.gridster.css\">");
			out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"gridster.css\"><link href=\"vis.css\" rel=\"stylesheet\" type=\"text/css\" /><link rel=\"stylesheet\" href=\"style.css\">");
			out.println("<link rel=\"icon\" type=\"image/png\" href=\"/pelars/pelarslogo.png\" sizes=\"16x16\"><script data-main=\"config\" src=\"requirejs/require.js\"></script></head>");
			 
			try {
				readFile(out);
			} catch (IOException e) {
				out.println("file not found");
			}
		}

		if(session.isOpen()){
			session.close();
		}
	}

}
