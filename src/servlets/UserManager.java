package servlets;

import hibernateMapping.HibernateSessionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import authorization.Permissible;

import pelarsServer.Error;
import pelarsServer.PelarsSession;
import pelarsServer.User;


/**
 * Servlet managing PELARS Users
 */
@WebServlet("/user/*")
public class UserManager extends HttpServlet {


	private static final long serialVersionUID = 7L;
	Session session;

	/**
	 * Correct endpoint /user/
	 */
	public void doPut(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {

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

		if(request.getPathInfo() == null || request.getPathInfo().split("/").length < 2)
			newUser(request, response, content);
		else{
			out.println(new Error(120).toJson());
		}
	}

	void newUser(HttpServletRequest request, HttpServletResponse response, JSONObject content) throws IOException {

		PrintWriter out = response.getWriter();
		String name = null;
		String affiliation = null;
		String namespace = null;
		//the default role at the beginning is unauthorized: not allowed access to any REST operation or WEB resources
		String role = "unauthorized";
		String passwd, email;

		session = HibernateSessionManager.getSession();

		//must fill all the fields
		try{
			name = content.getString("name");
			affiliation = content.getString("affiliation");
			namespace = content.getString("namespace");
			passwd = content.getString("password");
			email = content.getString("email");
		}catch (JSONException e){
			out.println(new Error(114).toJson());
			return;
		}

		User u = new User();
		u.name = name;
		u.affiliation = affiliation;
		u.namespace = namespace;
		u.email = email;
		u.role = role;

		try {
			//encrypt and sets the User password
			u.encryptPassword(passwd);
		} catch (NoSuchAlgorithmException e) {
			out.println(new Error(128));
			return;
		}

		//check there are no users with the same email
		List<User> users = null;
		try {
			users = Util.doQuery(session, "Select U from User as U where U.email = :email", "email", email);
		} catch (Exception e1) {
			if(session.isOpen())
				session.close();
			out.println(new Error(106).toJson());
			return;
		}

		if(users.size() > 0){

			JSONObject already_present = new JSONObject();
			try {
				already_present.put("message", "User already present");
				already_present.put("code", 108);
				already_present.put("id", users.get(0).getId());
			} catch (JSONException e) {}

			response.setStatus(403);
			out.println(already_present);
		}
		else{
			Util.save(session, u);
			out.println(new Status(u.getId(),"Success").toJson());
		}

		if(session.isOpen())
			session.close();
	}

	/**
	 * Correct endpoint /user/{user_id}
	 */
	public void doPost(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		String [] parameters = null;
		StringBuffer jb = new StringBuffer();
		String line = null;
		JSONObject content = null;

		response.addHeader("Content-Type", "application/json");

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

		parameters = request.getPathInfo().split("/");

		if(parameters.length >= 2)
			if(Util.isInteger(parameters[1]))
				updateUser(Long.parseLong(parameters[1]), response, request, content);
			else 
				out.println(new Error(116).toJson());
		else
			out.println(new Error(113).toJson());
	}

	/**
	 * updates user identified by parameter "id"
	 */
	public void updateUser( long id, HttpServletResponse response, HttpServletRequest request , JSONObject content) throws IOException{

		PrintWriter out = response.getWriter();

		session = HibernateSessionManager.getSession();

		List<User> results = null;
		List<PelarsSession> to_delete = null;
		try{
			results = Util.doQuery(session, "SELECT U FROM User AS U WHERE U.id = :id", "id", id);
		}catch (Exception e1){
			if(session.isOpen())
				session.close();
			out.println(new Error(106).toJson());
			return;
		}

		//each modification is optional
		if(results != null && results.size() != 0){

			String delete = null;
			User u = results.get(0);

			//check if user is allowed
			if(!ACL_RuleManager.Check(Util.getUser(request), "POSTUSER", u, session)){

				response.setStatus(403);
				out.println(new Error(135).toJson());

				if(session.isOpen()){
					session.close();
				}
				return;
			}

			try{
				u.name = content.getString("new_name");
			}catch(JSONException e){}
			try{
				u.affiliation =  content.getString("affiliation");
			}catch(JSONException e){}
			try{
				String role = content.getString("role");
				//only administrator can change roles
				u.role = role;
			}catch(JSONException e){}

			try{
				u.namespace = content.getString("namespace");
			}catch(JSONException e){}

			Util.update(session, u);

			try{
				delete = content.getString("delete_sessions");
			}
			catch(JSONException e){}

			if (delete != null && delete.equals("true")) {
				try{
					to_delete = Util.doQuery(session,"SELECT S from PelarsSession as S where S.user = :u","u",u);
				}
				catch(Exception e){
					if(session.isOpen())
						session.close();
					out.println(new Error(106).toJson());
					return;
				}

				for(PelarsSession b : to_delete){
					b.deleteData(session);
					Util.delete(session, b);
				}
			}


			out.println(new Status("Success").toJson());
		}else{
			//no users matching
			out.println(new Error(109).toJson());
		}
		if(session.isOpen())
			session.close();
	}

	/**
	 * Correct endpoint /user/[user_id]
	 */
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();

		response.addHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null)
			getUser(response, request);
		else{
			String [] parameters = request.getPathInfo().split("/");
			if(parameters.length <= 1)
				getUser(response, request);
			else{
				if(Util.isInteger(parameters[1]))
					getUser(Long.parseLong(parameters[1]), response, request);
				else 
					out.println(new Error(116).toJson());
			}
		}
	}

	/**
	 * prints all the users in the database
	 */
	void getUser(HttpServletResponse response, HttpServletRequest request) throws IOException {

		PrintWriter out = response.getWriter();

		session = HibernateSessionManager.getSession();

		User user = Util.getUser(request);

		List<? extends Permissible> results = null;
		try{
		
				results = Util.doQuery(session, "SELECT U FROM User AS U ");	
				results = ACL_RuleManager.Check(user, "GETUSER", results, session);
			
		}catch (Exception e1){
			if(session.isOpen())
				session.close();
			out.println(new Error(106).toJson());
			return;
		} 

		if(results.size() == 0)
			out.println(new Status("Empty").toJson());
		else{
			JSONArray oj = new JSONArray();
			for(Permissible u : results){
				
				User uu = (User)u;
				
				JSONObject o = uu.toJson();
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
	 * prints the JSON representation of the user identified by "id"
	 */
	void getUser(long id, HttpServletResponse response, HttpServletRequest request) throws IOException {

		PrintWriter out = response.getWriter();

		session = HibernateSessionManager.getSession();
		List<User> results = null;
		try{
			results = Util.doQuery(session, "SELECT U FROM User AS U WHERE U.id = :id", "id" , id);
		}catch (Exception e1) {
			if(session.isOpen())
				session.close();
			out.println(new Error(106).toJson());
			return;
		}

		if(results != null && results.size() != 0){

			if(!ACL_RuleManager.Check(Util.getUser(request), "GETUSER", results.get(0), session)){

				response.setStatus(403);
				out.println(new Error(135).toJson());
			}
			else{
				out.println(results.get(0).toJson());
			}
		}
		else{
			out.println(new Error(109).toJson());
		}

		if(session.isOpen())
			session.close();
	}

	/**
	 * correct endpoint: /user/{user_id}
	 */
	public void doDelete(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		String [] parameters = null;

		response.addHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null || request.getPathInfo().equals("/"))
			out.println(new Error(113).toJson());
		else{
			parameters = request.getPathInfo().split("/");
			if (Util.isInteger(parameters[1]))
				deleteUser(Long.parseLong(parameters[1]), request, response);
			else
				out.println(new Error(116).toJson());
		}
	}

	/**
	 *deletes the user identified by "id"
	 */
	public void deleteUser(long id, HttpServletRequest request, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();

		session = HibernateSessionManager.getSession();
		List<User> results = null;
		try{
			results = Util.doQuery(session, "SELECT U FROM User AS U WHERE U.id = :id", "id", id);
		}catch (Exception e){
			if(session.isOpen())
				session.close();
			out.println(new Error(106).toJson());
			return;
		}

		if(results != null && results.size() == 0)
			out.println(new Error(109).toJson());
		else{
			//check if user is allowed
			if(!ACL_RuleManager.Check(Util.getUser(request), "DELETEUSER", results.get(0), session)){

				response.setStatus(403);
				out.println(new Error(135).toJson());

				if(session.isOpen()){
					session.close();
				}
				return;
			}

			//Delete all the associated sessions and the data associated to each session
			List<PelarsSession> assoc = null;
			try {
				assoc = Util.doQuery(session, "SELECT S FROM PelarsSession AS S WHERE S.user.id = :id", "id", id);
			} catch (Exception e) {
				if(session.isOpen())
					session.close();
				out.println(new Error(106).toJson());
				return;
			}
			for (PelarsSession p : assoc){
				//before deleting the session, delete all the data associated to it
				p.deleteData(session);
				Util.delete(session, p);
			}

			Util.delete(session, results.get(0));
			out.println(new Status("Success").toJson());
		}
		if(session.isOpen())
			session.close();
	}
}
