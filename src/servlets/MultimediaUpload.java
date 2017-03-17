package servlets;

import hibernateMapping.HibernateSessionManager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Error;
import pelarsServer.MultimediaContent;
import pelarsServer.PelarsSession;
import authorization.PasswordService;

@WebServlet("/multimediaupload/*")
public class MultimediaUpload  extends HttpServlet{

	private static final long serialVersionUID = 1L;

	/**
	 * valid PUT endoint: /multimedia
	 */
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException{

		PrintWriter out = response.getWriter();
		StringBuffer jb = new StringBuffer();
		String line = null;
		JSONObject content = null;

		response.setHeader("Content-Type", "application/json");

		try{
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
			content = new JSONObject(jb.toString());
		}catch (Exception e){ 
			out.println(new Error(114).toJson());
		}

		if(request.getPathInfo() == null || request.getPathInfo().split("/").length < 2)
			newMultimediaObject(request, response, content);
		else{
			out.println(new Error(113).toJson());
		}
	}

	public void newMultimediaObject(HttpServletRequest request, HttpServletResponse response, JSONObject content) throws IOException {

		PrintWriter out = response.getWriter();

		Session session = HibernateSessionManager.getSession();

		File uploads;

		MultimediaContent m = new MultimediaContent();
		try{
			//for instance type : image
			m.type =  content.getString("type");
			//for instance mimetype : png
			m.mimetype =  content.getString("mimetype");
			//learning session must be specified by in the JSON content body

			m.session = Util.doQueryUnique(session, "SELECT S FROM PelarsSession AS S WHERE S.id = :iid", content.getLong("id"));

		}catch (JSONException e) {
			out.println(new Error(114).toJson());
			if(session.isOpen())
				session.close();
			return;
		}

		Blob m_data = null;
		byte[] bdata = {};

		try {
			bdata = content.getString("data").getBytes();
			m_data = Hibernate.getLobCreator(session).createBlob(bdata);
		} catch (JSONException e1) {}	

		try{
			m.time = content.getDouble("time");
		}catch(JSONException e){}


		try{
			m.view = content.getString("view");
		}catch(JSONException e){}

		try{
			m.creator = content.getString("creator");
		}catch(JSONException e){}

		try{
			m.triggering = content.getString("trigger");
		}catch(JSONException e){}

		//check if the passed session is valid 
		PelarsSession m_session = null;
		try {
			m_session = Util.doQueryUnique(session, "SELECT S from PelarsSession AS S WHERE S.id = :id", "id",m.session.getId());
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

		if (m.time == 0.0d){
			m.time = new Date().getTime();
		}

		//multimedia-content must be saved anyway only if time is consistent with session
		Double end_session = null;
		Double start_session = null;
		boolean terminated = false;
		boolean not_started = true;

		if(m_session.getEnd() != null){
			end_session = Double.parseDouble(m_session.getEnd());
			terminated = true;
		}

		if(m_session.getStart() != null){
			start_session = Double.parseDouble(m_session.getStart());
			not_started = false;
		}

		if(not_started == false && start_session <= m.time){

			//save only if time is consistent with the end of session
			if(!terminated || end_session >= m.time){

				//check that there is not already the same multimedia in the database before saving

				String query = "SELECT M FROM MultimediaContent AS M WHERE M.time = :t AND M.type = :ty AND M.session = :ses";

				if(Util.doQueryUnique(session, query, "t", m.time, "ty", m.type, "ses", m.session.getId()) == null){
					Util.save(session, m);
				}
				else{
					response.setStatus(500);
					out.println(new Error(140).toJson());
					if(session.isOpen())
						session.close();
					return;
				}


				//in this case must create a file and store in the filesystem: edit setenv.sh in $TOMCAT_HOME/bin
				//export CATALINA_OPTS="$CATALINA_OPTS -Dupload.location=/home/poweredge/uploads/{session_id}/{type}/{multimedia_id}.{mimetype}"

				String pref = System.getProperty("upload.location")+m.session+"/"+m.type+"/";
				new File(pref).mkdirs();

				uploads = new File(System.getProperty("upload.location")+m.session+"/"+m.type+"/",String.valueOf(m.getId()) + "." +  m.mimetype);

				String s = "";
				try {
					//files must be less than 2GB
					s = new String(m_data.getBytes(1, (int) m_data.length()));
				} catch (SQLException e1) {
					out.println("SqlException");
				}
				//decode it as an array of bytes
				byte[] b = PasswordService.base64ToByte(s);

				ByteArrayInputStream bis = new ByteArrayInputStream(b);
				Files.copy(bis, uploads.toPath());

			}
		}

		out.println(new Status(m.getId(), "Success").toJson());	
		if(session.isOpen())
			session.close();
	}

}
