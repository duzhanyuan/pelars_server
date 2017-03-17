package servlets;


import hibernateMapping.HibernateSessionManager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import authorization.PasswordService;

import pelarsServer.Error;
import pelarsServer.MultimediaContent;
import pelarsServer.PelarsSession;

/**
 * Servlet managing multimedia contents
 */
@WebServlet("/multimedia/*")
public class MultimediaManager extends HttpServlet {

	private static final long serialVersionUID = 2L;

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

			m.session = Util.doQueryUnique(session, "SELECT S FROM PelarsSession AS S WHERE S.id = :id", "id", content.getLong("id"));

			if(m.session == null){
				out.println(new Error(107).toJson());
				if(session.isOpen())
					session.close();
				return;
			}

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
		
		if(m.type.equals("image")){
			try {
				m.generateThumbnail(session, m_data);
			} catch (SQLException e) {}
		}
		
		try{
			m.time = content.getDouble("time");
		}catch(JSONException e){
			m.time = new Date().getTime();
		}


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
		PelarsSession m_session = m.session;
		
		//check immediately the rights to access
		/*	if(!ACL_RuleManager.Check(Util.getUser(request), "PUTMULTIMEDIA", m_session.get(0) ,session)){
			response.setStatus(403);
			out.println(new Error(135).toJson());
			return;
		}*/

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

				Util.save(session, m);


				//in this case must create a file and store in the filesystem: edit setenv.sh in $TOMCAT_HOME/bin
				//export CATALINA_OPTS="$CATALINA_OPTS -Dupload.location=/home/poweredge/uploads/{session_id}/{type}/{multimedia_id}.{mimetype}"

				String pref = System.getProperty("upload.location")+m.session.getId()+"/"+m.type+"/";
				new File(pref).mkdirs();

				uploads = new File(System.getProperty("upload.location")+m.session.getId()+"/"+m.type+"/",String.valueOf(m.getId()) + "." +  m.mimetype);

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


	/**
	 * valid GET endoint: /multimedia/{session_id}/[multimedia_id]
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String [] parameters = null;
		int error = 0;

		if(request.getPathInfo() == null){
			error = 113;
		}
		else
		{
			parameters = request.getPathInfo().split("/");
			if(parameters.length <= 1){
				error = 113; 
			}
			else {
				if(parameters.length <= 2){
					if(Util.isInteger(parameters[1])){
						getMultimediaContent(Long.parseLong(parameters[1]), response, request);
					}
					else{
						error = 116;
					}
				}
				else{
					if(parameters.length >= 3){
						if(Util.isInteger(parameters[1]) && Util.isInteger(parameters[2])){

							String extra_param = "content";

							if(parameters.length > 3){
								extra_param = parameters[3];
							}

							getMultimediaContent(Long.parseLong(parameters[1]), Long.parseLong(parameters[2]), response, request,extra_param);
						}
						else{
							if(Util.isInteger(parameters[1]) && !Util.isInteger(parameters[2])){
								getMultimediaContent(Long.parseLong(parameters[1]), parameters[2], response, request);
							}
							else{
								error = 116;
							}
						}
					}
					else{
						error = 120;
					}
				}
			}
		}
		if (error != 0){
			response.getWriter().println(new Error(error).toJson());
		}
	}

	/**
	 * GET on /multimedia/session_id/multimedia_id. The response is the multimedia content itself
	 */
	public void getMultimediaContent(long id, long sub_id, HttpServletResponse response, HttpServletRequest request, String meta) throws IOException{

		List<MultimediaContent> results = null;

		Session session = HibernateSessionManager.getSession();
		ServletOutputStream out = response.getOutputStream();

		try {
			results = Util.doQuery(session, "SELECT M FROM MultimediaContent AS M WHERE M.session.id = :id AND M.id = :sub_id", "id", id, "sub_id", sub_id);
		}catch (Exception e1){
			if(session.isOpen())
				session.close();
			out.println(new Error(106).toJson().toString());
			return;
		}

		if(results != null && results.size() == 0){

			out.println(new Error(118).toJson().toString());
		}
		else
		{
			MultimediaContent m = results.get(0);

			//now check the permission
			if(!ACL_RuleManager.Check(Util.getUser(request), "GETMULTIMEDIA", m, session)){
				response.setContentType("application/json");
				out.println(new Error(135).toJson().toString());
				response.setStatus(403);

				if(session.isOpen()){
					session.close();
				}
				return;
			}

			switch(meta){

			case "thumb":
				//sets the response type according top the type of the multimedia content
				response.setContentType(m.type + "/" + m.mimetype);

				Blob img_data = m.getThumbnail();

				int blobLength;
				byte[] blobAsBytes = null;
				try {
					blobLength = (int) img_data.length();
					blobAsBytes = img_data.getBytes(1, blobLength);
					out.write(blobAsBytes, 0, blobAsBytes.length);
					out.flush();
				} catch (SQLException e) {
					response.setContentType("application/json");
					out.println(new Error(131).toJson().toString());
				}  
				break;

			case "meta":
				out.println(m.toJson().toString());
				break;

			case "content":	
				//sets the response type according top the type of the multimedia content
				response.setContentType(m.type + "/" + m.mimetype);

				if(!m.type.equals("video")){
					//very temporary solution for upload problems on session 1350
					if(id == 1350 && m.type.equals("image") && m.view.equals("mobile")){
						response.sendRedirect("/pelars/uploads/images/" + m.session + "_" + m.id + "." + m.mimetype);
						if(session.isOpen())
							session.close();
					}

					File file = new File(System.getProperty("upload.location") + m.getSession().getId() + "/" + m.getType() + "/" + m.getId() + "." + m.getMimetype());
					response.setContentLength((int)file.length());

					FileInputStream in = new FileInputStream(file);

					// Copy the contents of the file to the output stream
					byte[] buf = new byte[4048];
					int count = 0;
					while ((count = in.read(buf)) >= 0) {
						out.write(buf, 0, count);
					}
					out.close();
					in.close(); 
				}
				else{

					if(session.isOpen())
						session.close();

					//TODO: issue, must redirect also the token parameter if present(or store it as a cookie...)
					String token = Util.getToken(request);
					response.sendRedirect("/pelars/uploads/" + m.getSession().getId() + "/" + m.getType() + "/" + m.getId() + "." + m.getMimetype() + "?token=" + token);
				}

				break;
			}
		}

		if(session.isOpen())
			session.close();
	}

	/**
	 * GET on /multimedia/session_id/. The response is list of JSON objects
	 */
	public void getMultimediaContent(long id, HttpServletResponse response, HttpServletRequest request) throws IOException{

		List<MultimediaContent> results = null;
		PrintWriter out = response.getWriter();

		Session session = HibernateSessionManager.getSession();
		response.addHeader("Content-Type", "application/json");
		String req_phase = request.getParameter("phase");
		String htmlreq = request.getParameter("html");

		try{
			if (req_phase == null){
				results = Util.doQuery(session, "SELECT M FROM MultimediaContent AS M WHERE M.session.id = :id", "id", id);
			}
			else{
				results = Util.doQuery(session, "SELECT M FROM MultimediaContent AS M WHERE M.session.id = :id AND M.phase = :phase", "id", id, "phase", req_phase);
			}
		}catch (Exception e1){
			if(session.isOpen())
				session.close();
			out.println(new Error(106).toJson().toString());
			return;
		}

		if(results.size() == 0)
			out.println(new Status("Empty").toJson());
		else{

			//now check rights and return error if not permitted
			if(!ACL_RuleManager.Check(Util.getUser(request), "GETMULTIMEDIA", results.get(0),session)){
				response.setStatus(403);
				out.println(new Error(135).toJson());

				if(session.isOpen()){
					session.close();
				}
				return;
			}

			JSONArray oj = new JSONArray();
			for(MultimediaContent m : results){

				JSONObject o = m.toJson(htmlreq != null);
				oj.put(o);

			}
			try{
				out.println(oj.toString(4));
			}catch (JSONException e){
				out.println(new Error(119).toJson());
			}
		}
		if(session.isOpen())
			session.close();
	}

	public static boolean isView(String type){

		if(MultimediaContent.contains(type)){
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param id
	 * @param type
	 * @param response
	 * @param request
	 * outputs all the multimedia content descriptors associates to the session id with a certain type "type"
	 * @throws IOException 
	 */
	public void getMultimediaContent(long id, String type, HttpServletResponse response, HttpServletRequest request) throws IOException{

		List<MultimediaContent> results = null;
		PrintWriter out = response.getWriter();

		Session session = HibernateSessionManager.getSession();
		response.addHeader("Content-Type", "application/json");
		String req_phase = request.getParameter("phase");
		String htmlreq = request.getParameter("html");

		String tp = "type";

		if(isView(type)){
			tp = "view";
		}

		try{
			if (req_phase == null){
				results = Util.doQuery(session, "SELECT M FROM MultimediaContent AS M WHERE M." +tp+" = :t AND M.session.id = :id", "id", id,"t",type);
			}
			else{
				results = Util.doQuery(session, "SELECT M FROM MultimediaContent AS M WHERE M."+tp+" = :t AND M.session.id = :id AND M.phase = :phase", "id", id, "phase", req_phase, "t", type);
			}
		}catch (Exception e1){
			if(session.isOpen())
				session.close();
			out.println(new Error(106).toJson().toString());
			return;
		}

		if(results.size() == 0)
			out.println(new Status("Empty").toJson());
		else{

			//now check rights and return error if not permitted
			if(!ACL_RuleManager.Check(Util.getUser(request), "GETMULTIMEDIA", results.get(0),session)){
				response.setStatus(403);
				out.println(new Error(135).toJson());

				if(session.isOpen()){
					session.close();
				}
				return;
			}

			JSONArray oj = new JSONArray();
			for(MultimediaContent m : results){

				JSONObject o = m.toJson(htmlreq != null);
				oj.put(o);

			}
			try{
				out.println(oj.toString(4));
			}catch (JSONException e){
				out.println(new Error(119).toJson());
			}
		}
		if(session.isOpen())
			session.close();
	}

	/**
	 * valid DELETE endoint: /multimedia/{session_id}/[multimedia_id]
	 */
	public void doDelete(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {

		String [] parameters = null;
		PrintWriter out = response.getWriter();

		response.addHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null)
			out.println(new Error(113).toJson());
		else{
			parameters = request.getPathInfo().split("/");
			if(parameters.length <= 1){
				out.println(new Error(113).toJson());
			}
			else{
				if(parameters.length <= 2){
					if(Util.isInteger(parameters[1])){
						deleteMultimediaContent(Long.parseLong(parameters[1]), request, response);
					}
					else
					{
						out.println(new Error(116).toJson());
					}
				}
				else{
					if(parameters.length >= 3){

						if(Util.isInteger(parameters[1]) && Util.isInteger(parameters[2])){
							deleteMultimediaContent(Long.parseLong(parameters[1]), Long.parseLong(parameters[2]), request, response);
						}
						else
						{
							out.println(new Error(116).toJson());
						}
					}else
					{
						out.println(new Error(120).toJson());
					}
				}
			}
		}
	}

	/**
	 deletes a specific multimedia object
	 */	
	public void deleteMultimediaContent(long id, long request, HttpServletRequest req, HttpServletResponse response ) throws IOException {

		PrintWriter out = response.getWriter();

		Session session = HibernateSessionManager.getSession();

		MultimediaContent m = Util.doQueryUnique(session, "SELECT M FROM MultimediaContent AS M WHERE M.session.id = :id AND M.id = :request","id", id, "request", request);

		if (m == null){
			out.println(new Error(118).toJson());
		}
		else{
			if (!ACL_RuleManager.Check(Util.getUser(req), "DELETEMULTIMEDIA", m, session)){
				out.println(new Error(135).toJson());
			}
			else{
				//delete media content AND file
				try {
					Util.executeCommand("rm " + System.getProperty("upload.location") + m.getSession() + "/" + m.getType() + "/" + m.getId() + "." + m.getMimetype());
				} catch (Exception e) {
					response.setStatus(500);
					if(session.isOpen())
						session.close();
					return;

				}

				Util.delete(session, m);

				out.println(new Status("Success").toJson());
			}
		}

		if(session.isOpen())
			session.close();
	}

	/**
	 deletes all multimedia data in a PELARS session
	 */	
	public void deleteMultimediaContent(long id, HttpServletRequest req, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		Session session = HibernateSessionManager.getSession();

		List<MultimediaContent> m = null;
		try {
			m = Util.doQuery(session, "SELECT M FROM MultimediaContent AS M WHERE M.session.id = :id", "id", id);
		} catch (Exception e) {

		}

		if (m.size() == 0 || m == null){
			out.println(new Error(118).toJson());
		}
		else{
			if (!ACL_RuleManager.Check(Util.getUser(req), "DELETEMULTIMEDIA", m.get(0), session)){
				out.println(new Error(135).toJson());
			}
			else{

				//TODO: delete also the files
				try {
					Util.executeCommand("rm -r " + System.getProperty("upload.location") + id);
				} catch (Exception e) {
					response.setStatus(500);
					if(session.isOpen())
						session.close();
					return;
				}

				Util.delete(session, m.toArray());

				out.println(new Status("Success").toJson());
			}
		}

		if(session.isOpen())
			session.close();
	}

	/**
	 * valid POST endoint: /multimedia/session_id/multimedia_id
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String [] parameters = null;

		response.addHeader("Content-Type", "application/json");

		PrintWriter out = response.getWriter();
		StringBuffer jb = new StringBuffer();
		String line = null;
		JSONObject content = null;
		try{
			BufferedReader reader = request.getReader();
			while((line = reader.readLine()) != null)
				jb.append(line);
			content = new JSONObject(jb.toString());
		}catch(Exception e){ 
			out.println(new Error(114).toJson());
			return;
		}

		if(request.getPathInfo() == null){
			out.println(new Error(113).toJson());
			return;
		}

		parameters = request.getPathInfo().split("/");

		if(parameters.length <= 2)
			out.println(new Error(113).toJson());
		else
		{
			if(Util.isInteger(parameters[1]) && Util.isInteger(parameters[2]))
				updateMultimediaContent(Long.parseLong(parameters[1]), Long.parseLong(parameters[2]), request, response, content);
			else
				out.println(new Error(116).toJson());
		}
	}

	public void updateMultimediaContent(long id,  long sub_id, HttpServletRequest request, HttpServletResponse response,  JSONObject content ) throws IOException{

		List<MultimediaContent> results = null;

		PrintWriter out = response.getWriter();
		Session session = HibernateSessionManager.getSession();

		try{
			results = Util.doQuery(session, "SELECT M FROM MultimediaContent AS M WHERE M.session.id = :id AND M.id = :sub_id", 
					"id", id, "sub_id", sub_id );
		}catch(Exception e1){
			if(session.isOpen())
				session.close();
			out.println(new Error(106).toJson().toString());
			return;
		}

		if(results != null && results.size() != 0){

			MultimediaContent m = results.get(0);

			//check here i the user is allowed
			if(!ACL_RuleManager.Check(Util.getUser(request), "POSTMULTIMEDIA", m, session)){

				response.setStatus(403);
				out.println(new Error(135).toJson());
				if(session.isOpen()){
					session.close();
				}
				return;
			}

			//if change type -> change also mimetype and data
			boolean type_change = false;
			try{
				m.type =  content.getString("type");
				type_change = true;
			}catch(JSONException e){}

			//if change mimetype -> change also data
			boolean mimetype_change = false;
			try{
				m.mimetype =  content.getString("mimetype");
				mimetype_change = true;
			}catch(JSONException e){
				if (type_change) {
					out.println(new Error(132).toJson());
					if(session.isOpen())
						session.close();
					return;
				}
			}

			Blob m_data;
			try{
				m_data = Hibernate.getLobCreator(session).createBlob(content.getString("data").getBytes());
				File uploads = new File(System.getProperty("upload.location")+m.session+"/"+m.type+"/",String.valueOf(m.getId()) + "." +  m.mimetype);

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

			}catch(JSONException e){
				if (type_change || mimetype_change) {
					out.println(new Error(133).toJson());
					if(session.isOpen())
						session.close();
					return;
				}
			}

			try{
				m.view = content.getString("view");
			}catch(JSONException e){}

			try{
				m.creator = content.getString("creator");
			}catch(JSONException e){}

			try{
				m.triggering = content.getString("trigger");
			}catch(JSONException e){}

			Util.update(session, m);
			out.println(new Status("Success").toJson());
		}else{
			out.println(new Error(118).toJson());
		}
		if(session.isOpen())
			session.close();
	}
}
