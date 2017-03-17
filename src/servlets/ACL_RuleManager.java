package servlets;



import hibernateMapping.HibernateSessionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import authorization.ACL_Rule;
import authorization.ACL_RuleUser;
import authorization.Permissible;
import authorization.ACL;

import pelarsServer.Error;
import pelarsServer.User;


@WebServlet("/acl")
public class ACL_RuleManager extends HttpServlet{

	private static final long serialVersionUID = 17987L;

	private static int getLevel(User u, String request, Session session){

		boolean no_session = false;
		if (session == null){
			session = hibernateMapping.HibernateSessionManager.getSession();
			no_session = true;
		}

		ACL_Rule role_rule = Util.doQueryUnique(session, "SELECT R from ACL_Rule as R WHERE R.role = :u_role AND R.operation = :op", "u_role", u.role, "op", request);
		ACL_RuleUser user_rule = Util.doQueryUnique(session, "SELECT R from ACL_RuleUser as R WHERE R.user_id = :u_id AND R.operation = :op", "u_id", u.id, "op", request);

		if (no_session){
			session.close();
		}

		int role_level = (role_rule == null) ? 0 : role_rule.level;
		int user_level = (user_rule == null) ? 0 : user_rule.level;

		return Math.max(role_level, user_level);
	}


	//returns level of permission for the requested servlet operation
	public static boolean Check(User u, String request, Permissible object, Session session){

		if (u == null || object == null){
			return false;
		}

		switch(getLevel(u, request, session)){

		case 1:
			//get the user us from the Permissible object check if u and us are the same
			return object.belongs(u, session);
		case 2:
			//get the user's group uss from the Permissible object and check whether 
			return object.belongsToGroup(u, session);
			//	return true;
		case 3: 
			return true;
		default:
			return false;
		}
	}
	
	public static List<? extends Permissible> Check(User u, String request,List<? extends Permissible> objects, Session session){
		
		for( Iterator<? extends Permissible> iter = objects.listIterator(); iter.hasNext();){
			Permissible o  = iter.next();
			
			if(!Check(u,request,o,session)){
				iter.remove();
			}
		}
		return objects;
	}

	public static boolean Check(User u, String request, Session session){

		if (u == null){
			return false;
		}


		if (getLevel(u,request,session) == 0){
			return false;
		}
		else{
			return true;
		}
	}
	
	public static boolean Check(User u, String request){

		if (u == null){
			return false;
		}

		if (getLevel(u,request,null) == 0){
			return false;
		}
		else{
			return true;
		}
	}


	//gets all the acl rules. Only for administrators
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String [] parameters = null;

		response.addHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null)
			getACL(request, response);
		else{
			parameters = request.getPathInfo().split("/");
			if(parameters.length <= 1)
				getACL(request, response);
			else{
				PrintWriter out = response.getWriter();
				out.println(new Error(116).toJson());
			}
		}
	}

	private void getACL(HttpServletRequest request, HttpServletResponse response) throws IOException{

		PrintWriter out = response.getWriter();
		Session session = HibernateSessionManager.getSession();

		if (Check(Util.getUser(request), "ACLMANAGE", session)){	

			List<ACL_Rule> acl_rules = null;

			try {
				acl_rules = Util.doQuery(session, "SELECT A FROM ACL_Rule AS A");
			} catch (Exception e) {

			}

			if (acl_rules.size() > 0){
				JSONArray jarray = new JSONArray();
				for (int i = 0; i < acl_rules.size(); i++){
					jarray.put(acl_rules.get(i).toJson());
				}

				out.println(jarray);
			}
			else{
				out.println(new Status("Empty").toJson());
			}

		}
		else{
			out.println(new Error(135).toJson());
		}

		if(session.isOpen()){
			session.close();
		}

	}


	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{

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
					updateRule(Long.parseLong(parameters[1]),m, request,response);
				else
					out.println(new Error(116).toJson());
			else 
				out.println(new Error(120).toJson());
		}
	}

	private void updateRule(long id, JSONObject content,HttpServletRequest request, HttpServletResponse response) throws IOException{

		PrintWriter out  = response.getWriter();

		response.addHeader("Content-Type", "application/json");

		Session session = HibernateSessionManager.getSession();

		ACL_Rule rule = Util.doQueryUnique(session, "SELECT A FROM ACL_Rule AS A WHERE A.id = :id", "id", id);

		if (rule == null){
			out.println(new Status("Empty"));
		}
		else{
			if(Check(Util.getUser(request), "ACLMANAGE", rule, session)){
				try {
					rule.role = content.getString("role");
				} catch (JSONException e) {}
				try{	
					rule.level = content.getInt("level");
				} catch (JSONException e){}

				out.println(new Status("Success"));	
			}
			else{
				out.println(new Error(135).toJson());
			}
		}

		if (session.isOpen()){
			session.close();
		}
	}

	public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException{

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
		//accept /acl and /acl/
		if(request.getPathInfo() == null || request.getPathInfo().split("/").length < 2)
			newRule(request, content, response);
		else{
			out.println(new Error(120).toJson());
		}
	}

	public void newRule(HttpServletRequest request, JSONObject content, HttpServletResponse response) throws IOException{

		PrintWriter out = response.getWriter();

		String role = null;
		Integer level = null;
		String op = null;

		try {
			role = content.getString("role");
			level = content.getInt("level");
			op = content.getString("operation");
		} catch (JSONException e) {
			out.println(new Error(114).toJson());
			return;
		}

		Session session = HibernateSessionManager.getSession();

		if (Check(Util.getUser(request), "ACLMANAGE", session)){

			ACL_Rule rule = new ACL_Rule(role,op,level);
			Util.save(session, rule);
			out.println(new Status("Success").toJson());
		}
		else{
			out.println(new Error(135).toJson());
		}

		if(session.isOpen()){
			session.close();
		}


	}

	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException{

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
					deleteRule(Long.parseLong(parameters[1]),m, request,response);
				else
					out.println(new Error(116).toJson());
			else 
				out.println(new Error(120).toJson());
		}
	}

	private void deleteRule(long id, JSONObject content,HttpServletRequest request, HttpServletResponse response) throws IOException{

		PrintWriter out  = response.getWriter();

		response.addHeader("Content-Type", "application/json");

		Session session = HibernateSessionManager.getSession();

		ACL_Rule rule = Util.doQueryUnique(session, "SELECT A FROM ACL_Rule AS A WHERE A.id = :id", "id", id);

		if (rule == null){
			out.println(new Status("Empty"));
		}
		else{
			if(Check(Util.getUser(request), "ACLMANAGE", rule, session)){

				Util.delete(session, rule);

				out.println(new Status("Success"));	
			}
			else{
				out.println(new Error(135).toJson());
			}
		}

		if (session.isOpen()){
			session.close();
		}
	}


}
