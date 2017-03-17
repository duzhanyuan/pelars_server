package servlets;

import hibernateMapping.HibernateSessionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;


import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;

import authorization.PasswordService;
import authorization.Token;
import authorization.TokenService;


import pelarsServer.Error;
import pelarsServer.User;

/**
* @author Lorenzo Landolfi
*supports only POST, check the password of a user. Parameters are passed as HTTP form parameters :"user", "pwd"
* the submitted password is hashed and compared to the one stored in the DB. The seed used for hashing is the one 
* associated to the user parameter
 */
@WebServlet("/password/*")
public class PasswordManager extends HttpServlet {

	private static final long serialVersionUID = 11L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		PrintWriter out = response.getWriter();
		String [] parameters = null;
		StringBuffer jb = new StringBuffer();
		String line = null;
		JSONObject m = null;

		if(request.getPathInfo() == null){
			getAnswer(request, response);
		}
		else{
			parameters = request.getPathInfo().split("/");

			if (parameters.length == 1){
				out.println("path = 1");
				getAnswer(request, response);
				return;
			}

			if(parameters.length >= 2){

				try{
					BufferedReader reader = request.getReader();
					while ((line = reader.readLine()) != null)
						jb.append(line);
					m = new JSONObject(jb.toString());
				}catch(Exception e){ 
					out.println(new Error(114).toJson());
					return;
				}

				if(Util.isInteger(parameters[1]))
					updatePassword(Long.parseLong(parameters[1]), request,response, m);
				else
					updatePassword(parameters[1], request,response, m);
			}
			else 
				out.println(new Error(120).toJson());
		}
	}

	private void getAnswer(HttpServletRequest request ,HttpServletResponse response) throws IOException, ServletException {

		PrintWriter out = response.getWriter();
		response.addHeader("Content-Type", "application/json");

		Session session = HibernateSessionManager.getSession();
		String redirect = null;

		//parameter named p-url is used for redirection if password is correct
		try{
			redirect = request.getParameter("p-url");
		}
		catch(Exception e){}

		String user = request.getParameter("user");
		String pwd = request.getParameter("pwd");

		List<User> matching_users = null;

		//check if there is a user with the same email
		try{
			matching_users = Util.doQuery(session, "SELECT U FROM User AS U WHERE " + "U.email = :name", "name", user);
		} catch (Exception e1) {
		}

		if(session.isOpen())
			session.close();

		if(matching_users.size() == 0 || matching_users.get(0).role.equals("unauthorized")){

			if(redirect != null){
				response.sendRedirect("/pelars/unauthorized.jsp?p-url="+redirect);
			}
			else {
				response.sendRedirect("/pelars/unauthorized.jsp");
			}
			response.setStatus(401);

		}else{

			String digest = matching_users.get(0).getPassword();
			String salt = matching_users.get(0).getSalt();
			String role = matching_users.get(0).getRole();
			Long id = matching_users.get(0).id;

			if(digest == null || salt == null || role == null) {
				Util.Error(out, 130, session);
				return;
			}

			byte[] bDigest = PasswordService.base64ToByte(digest);
			byte[] bSalt = PasswordService.base64ToByte(salt);
			byte[] proposedDigest = null;

			// Compute the new DIGEST using the stored SALT
			try{
				proposedDigest = PasswordService.getHash(pwd, bSalt);
			} catch (NoSuchAlgorithmException e) {
				Util.Error(out, 128, session);
				return;
			}

			//same byte values: produce token
			if(Arrays.equals(proposedDigest, bDigest)){

				Token t = null;
				try {
					t = TokenService.produceToken(request.getRemoteAddr(),role, id.toString());
				} catch (InterruptedException e1) {
					response.setStatus(500);
					return;
				}

				//redirect != null means that the request has come from web-browser: set the cookie
				//TODO put an attribute instead
				Cookie loginCookie = new Cookie("token", t.getValue());
				loginCookie.setMaxAge(authorization.TokenService.expire / 1000);
				response.addCookie(loginCookie);


				if(redirect != null){

					if(redirect.equals("null")){
			            response.sendRedirect("welcome-page.jsp");
					}
					else{
			            response.sendRedirect(redirect);
					} 
				}

				//request performed from automated client
				if(redirect == null){
					JSONObject res = new JSONObject();
					try {
						res.put("token", t.getValue());
					} catch (JSONException e) {
					}

					out.println(res);
					response.setStatus(200);
					return;
				}
			}
			else{
				//redirect only if the request came from web browser 
				if(redirect != null){

					response.sendRedirect("/pelars/unauthorized.jsp?p-url=" + redirect);
				}
				response.setStatus(401);
			}
		}
	}

	private void updatePassword(String user_id, HttpServletRequest request, HttpServletResponse response, JSONObject content) throws IOException{

		Session session = HibernateSessionManager.getSession();
		PrintWriter out = response.getWriter();

		User result = null;

		try{
			result = Util.doQueryUnique(session, "SELECT U FROM User AS U WHERE U.email = :id", "id", user_id);
		}catch (Exception e1){
			if(session.isOpen())
				session.close();
			out.println(new Error(106).toJson());
			return;
		}

		if(session.isOpen()){
			session.close();
		}

		if (result == null){
			response.setStatus(403);
		}
		else{
			updatePassword(result.getId(), request, response, content);
		}

	}


	private void updatePassword(Long user_id, HttpServletRequest request, HttpServletResponse response, JSONObject content) throws IOException{

		PrintWriter out = response.getWriter();

		Session session = HibernateSessionManager.getSession();

		List<User> results = null;

		try{
			results = Util.doQuery(session, "SELECT U FROM User AS U WHERE U.id = :id", "id", user_id);
		}catch (Exception e1){
			if(session.isOpen())
				session.close();
			out.println(new Error(106).toJson());
			return;
		}

		User u = results.get(0);

		String old_password = null; 
		String new_password = null;

		try{
			old_password = content.getString("old_password");
			new_password = content.getString("new_password");
		}catch(JSONException e){

			response.setStatus(400);
			out.println(new Error(114).toJson());

			if(session.isOpen()){
				session.close();
			}

			return;
		}

		String digest = u.getPassword();
		String salt = u.getSalt();

		byte[] bDigest = PasswordService.base64ToByte(digest);
		byte[] bSalt = PasswordService.base64ToByte(salt);
		byte[] proposedDigest = null;

		// Compute the new DIGEST using the stored SALT
		try{
			proposedDigest = PasswordService.getHash(old_password, bSalt);
		} catch (NoSuchAlgorithmException e) {
			Util.Error(out, 128, session);
			return;
		}

		//same byte values: change password
		if(Arrays.equals(proposedDigest, bDigest)){

			try{
				u.encryptPassword(new_password);
			}catch (NoSuchAlgorithmException e) {}

			Util.update(session, u);

			if(session.isOpen()){
				session.close();
			}

			out.println(new Status("Success").toJson());
		}
		else{
			response.setStatus(400);
			out.println(new Error(138).toJson());
		}
	}
}
