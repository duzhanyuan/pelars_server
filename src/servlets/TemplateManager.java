package servlets;


import hibernateMapping.HibernateSessionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Error;
import pelarsServer.Template;

/**
* Servlet managing templates
 */
@WebServlet("/template/*")
public class TemplateManager extends HttpServlet {

	private static final long serialVersionUID = 6L;
	Session session;

	/**
	 *correct endpoint: /template/
	 */
	public void doPut(HttpServletRequest request, 
			HttpServletResponse response) throws IOException{

		PrintWriter out = response.getWriter();
		StringBuffer jb = new StringBuffer();
		String line = null;
		JSONObject content = null;

		response.addHeader("Content-Type", "application/json");

		try{
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
			content = new JSONObject(jb.toString());
		}catch (Exception e){ 
			out.println(new Error(114).toJson());
		}

		String [] parameters = null;

		if(request.getPathInfo() == null)
			newObject(response, content);
		else{
			parameters = request.getPathInfo().split("/");
			if(parameters.length < 2){
				newObject(response, content);
			}
			else{
				out.println(new Error(120).toJson());
			}
		}
	}

	public void newObject( HttpServletResponse response, JSONObject content) throws IOException {

		List<Template> results = null;
		PrintWriter out = response.getWriter();
		Template t = new Template();
		session = HibernateSessionManager.getSession();
		
		//check whether exists a template with the same name
		try{
			results = Util.doQuery(session, "SELECT T FROM Template AS T WHERE T.name = :name", "name", content.getString("name"));
			if(results == null){
				if(session.isOpen())
					session.close();
				return;
			}
			
			if(results.size() != 0){
				out.println(new Error(110).toJson());
			}
			else{	
				t.name = content.getString("name");		
				t.category =  content.getString("category");	
				t.description = content.getString("description");
				t.data = Hibernate.getLobCreator(session).createBlob(content.getString("data").getBytes());		
				t.namespace = content.getString("namespace");

				Util.save(session, t);
				out.println(new Status(t.getId() , "Success").toJson());
			}
		}
		catch(Exception e){
			out.println(new Error(114));
		}
		if(session.isOpen())
			session.close();
	}

	/**
	 *correct endpoint: /template/{template_id}
	 */
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {

		String [] parameters = null;
		PrintWriter out = response.getWriter();

		response.addHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null){
			getTemplate(response, request);
		}
		else{
			parameters = request.getPathInfo().split("/");
			if(parameters.length <= 1)
				getTemplate(response, request);
			else{
				if(Util.isInteger(parameters[1]))
					getTemplate(Long.parseLong(parameters[1]), response, request);
				else
					out.println(new Error(116).toJson());
			}
		}
	}

	/**
	 *prints all the templates of the database or according to a namespace
	 */
	public void getTemplate(HttpServletResponse response, HttpServletRequest request)throws IOException {

		String namespace = null;
		List<Template> results = null;
		PrintWriter out = response.getWriter();

		session = HibernateSessionManager.getSession();

		namespace = request.getParameter("namespace");
		if(namespace != null){
			try{
				results = Util.doQuery(session, "SELECT T FROM Template AS T WHERE T.namespace = :namespace",
						"namespace", namespace);
			} catch(Exception e) {
				if(session.isOpen())
					session.close();
				out.println(new Error(106).toJson());
				return;
			}
		}
		else{
			try {
				results = Util.doQuery(session, "SELECT T FROM Template AS T");
			} catch (Exception e) {
				if(session.isOpen())
					session.close();
				out.println(new Error(106).toJson());
				return;
			}
		}

		if(results.size() == 0)
			out.println(new Status("Empty").toJson());
		else{
			JSONArray oj = new JSONArray();
			for(Template t : results){
				JSONObject o;
				try{
					o = t.toJson();
					oj.put(o);
				} catch(SQLException e) {
					out.println(new Error(115).toJson());
				}
			}
			out.println(oj.toString());
		}

		if(session.isOpen())
			session.close();
	}
	
	/**
	 *prints the template identified by "id" parameter
	 */
	public void getTemplate(long id, HttpServletResponse response, HttpServletRequest request) throws IOException {

		List<Template> results = null;
		PrintWriter out = response.getWriter();

		session = HibernateSessionManager.getSession();

		try{
			results = Util.doQuery(session, "SELECT T FROM Template AS T where T.id = :id", "id", id);
		}catch (Exception e1){
			if (session.isOpen())
				session.close();
			out.println(new Error(106).toJson());
			return;
		}
		if(results.size() != 0)
			try{
				out.println(results.get(0).toJson());
			}catch (SQLException e) {
				out.println(new Error(115).toJson());
			}
		else
			out.println(new Error(111).toJson());
		
		if(session.isOpen())
			session.close();
	}

	
	/**
	 *correct endpoint /template/template_id
	 */
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		String [] parameters = null;

		response.addHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null)
			out.println(new Error(113).toJson());
		else{
			parameters = request.getPathInfo().split("/");

			if(parameters.length > 1){
				if(Util.isInteger(parameters[1]))
					deleteTemplate(Long.parseLong(parameters[1]), response);
				else
					out.println(new Error(116).toJson());
			}
			else{
				out.println(new Error(113).toJson());
			}
		}
	}

	
	/**
	 *deletes the template identified by "id" parameter
	 */
	public void deleteTemplate(long id, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();

		session = HibernateSessionManager.getSession();
		//removed is the number of deleted templates
		int removed = Util.doUpdate(session, "delete Template AS T" + " where T.id = :id","id", id);

		if(removed == 0)
			out.println(new Error(111).toJson());
		else{
			out.println(new Status("Success").toJson());
		}
		if(session.isOpen())
			session.close();
	}

	
	/**
	 *correct endpoint /template/template_id
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		String [] parameters = null;
		StringBuffer jb = new StringBuffer();
		String line = null;
		JSONObject content = null;

		response.addHeader("Content-Type", "application/json");

		try{
			BufferedReader reader = request.getReader();
			while((line = reader.readLine()) != null)
				jb.append(line);
			content = new JSONObject(jb.toString());
		}catch (Exception e) { 
			out.println(new Error(114).toJson());
			return;
		}

		if(request.getPathInfo() == null)
			out.println(new Error(113).toJson());
		else{
			parameters = request.getPathInfo().split("/");
			if(parameters.length >= 2)
				if(Util.isInteger(parameters[1]))
					updateTemplate(Long.parseLong(parameters[1]), response, content);
				else
					out.println(new Error(116).toJson());
			else 
				out.println(new Error(113).toJson());
		}
	}
	
	
	/**
	 *updates the template identified by the "id" parameter
	 */
	public void updateTemplate(long id, 
			HttpServletResponse response, JSONObject content) throws IOException{

		PrintWriter out = response.getWriter();
		List<Template> results = null;

		session = HibernateSessionManager.getSessionFactory().openSession();

		try{
			results = Util.doQuery(session, "SELECT T FROM Template AS T WHERE T.id = :id", "id", id);
		} catch (Exception e1){
			if (session.isOpen())
				session.close();
			out.println(new Error(106).toJson());
			return;
		}

		Template t = new Template();
		if(results.size() != 0){
			
			t = results.get(0);
			try{
				t.name = content.getString("new_name");
			}
			catch(JSONException e){}
			try{
				t.category =  content.getString("category");
			}
			catch(JSONException e){}
			try{
				t.description = content.getString("description");
			}
			catch(JSONException e){}
			try{
				t.data = Hibernate.getLobCreator(session).createBlob(content.getString("data").getBytes());
			}
			catch(JSONException e){}

			Util.update(session, t);
			out.println(new Status("Success").toJson());

		}else
			out.println(new Error(111).toJson());

		if(session.isOpen())
			session.close();
	}
}