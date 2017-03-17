package servlets;

import hibernateMapping.HibernateSessionManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.sql.rowset.serial.SerialBlob;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.context.internal.ManagedSessionContext;

import authorization.Token;

import pelarsServer.Error;
import pelarsServer.User;

/**
 * 
 * @author Lorenzo Landolfi, Giacomo Dabisias
 *	Utility functions class
 */
public class Util{

	public static final double CCD_width = 4.8; //mm
	public static final double focal_length = 3.67; //mm
	public static final int timeout = 1000; 

	//The following is valid only if the frames are 640*480
	public static double focal_length_pixel = 489.3; 
	/**
	returns true if the passed string is a number
	 */
	public static boolean isNumeric(String str)  
	{  
		try{  
			@SuppressWarnings("unused")
			double d = Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe){  
			return false;  
		}  
		return true;  
	}

	/**
	returns true if the passed string is an integer
	 */
	public static boolean isInteger(String s){
		try{ 
			Integer.parseInt(s); 
		}catch(NumberFormatException e){ 
			return false; 
		}catch(NullPointerException e){
			return false;
		}
		return true;
	}

	/**
	saves a single object in db
	 */
	public static <T> void save(Session s, T ts) {
		try{
			ManagedSessionContext.bind(s);
			s.getTransaction().setTimeout(timeout);
			s.beginTransaction();
			s.save(ts);
			commit(s);
		}
		catch(Exception e){
			rollback(s);
		}
	}

	/**
	saves an array of objects of the same type in database
	 */
	public static <T> void save(Session s, T[] ts){
		try{
			ManagedSessionContext.bind(s);
			s.beginTransaction();
			for (int i=0; i<ts.length; i++)
				s.save(ts[i]);
			commit(s);
		}
		catch(Exception e){
			rollback(s);
		}
	}

	/**
	deletes a single object from the db
	 */
	public static <T> void delete(Session s, T ts){
		try{
			ManagedSessionContext.bind(s);
			s.getTransaction().setTimeout(timeout);
			s.beginTransaction();
			s.delete(ts);
			commit(s);
		}
		catch(Exception e){
			rollback(s);
		}
	}

	/**
	deletes an array of objects of the same type from the database
	 */
	public static <T> void delete(Session s, T[] ts){
		try{
			ManagedSessionContext.bind(s);
			s.getTransaction().setTimeout(timeout);
			s.beginTransaction();
			for (int i=0; i<ts.length; i++)
				s.delete(ts[i]);
			commit(s);
		}
		catch (Exception e){
			rollback(s);
		}
	}

	/**
	updatess a single object of the db
	 */
	public static <T> void update(Session s, T ts){
		try{
			ManagedSessionContext.bind(s);
			s.beginTransaction();
			s.update(ts);
			commit(s);
		}
		catch(Exception e){
			rollback(s);
		}
	}

	/**
	updates an array of objects of the same type 
	 */
	public static <T> void update(Session s, T[] ts){
		try{
			ManagedSessionContext.bind(s);
			s.beginTransaction();
			for (int i=0; i<ts.length; i++)
				s.update(ts[i]);
			commit(s);
		}
		catch (Exception e){
			rollback(s);
		}
	}

	/**
	returns true if table "t" has no elements
	 */
	public static boolean isEmpty(Session session, String t){

		ManagedSessionContext.bind(session);
		session.getTransaction().setTimeout(timeout);
		session.beginTransaction();
		Integer c = ((Integer) session.createQuery("select count(*) from " + t).iterate().next() ).intValue();
		commit(session);
		return c == 0;
	}

	/**
	execute a query returning a list of generic objects, mapping and objects are stored in lists
	 */
	public static <T>List<T> doQuery(Session s, String st, List<String> mapping, List<Object> values ){

		ManagedSessionContext.bind(s);
		s.getTransaction().setTimeout(timeout);
		s.beginTransaction();
		Query query = s.createQuery(st);

		for (int j = 0; j < mapping.size(); j++){
			query.setParameter(mapping.get(j), values.get(j));
		}

		@SuppressWarnings("unchecked")
		List<T> results = query.list();
		commit(s);
		return results;
	}


	/**
	execute a query returning a list of generic objects, params are "mapping1","object1","mapping2","object2",...
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> doQuery(Session s, String st, Object... params){

		ManagedSessionContext.bind(s);
		s.getTransaction().setTimeout(timeout);
		s.beginTransaction();

		Query query = s.createQuery(st);
		int i = 0;
		String key = null; 
		Object value = null;

		for(java.lang.Object obj : params){
			if (i % 2 == 0){ key = obj.toString(); }
			else { value = obj;}
			i++;
			query.setParameter(key,value);
		}
		List<T> results = query.list();
		commit(s);

		return results;
	}

	/**
	execute a query returning a list of generic objects, without parameters
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> doQuery(Session s, String st){

		ManagedSessionContext.bind(s);
		s.getTransaction().setTimeout(timeout);
		s.beginTransaction();

		Query query = s.createQuery(st);

		List<T> results = query.list();
		commit(s);

		return results;
	}


	@SuppressWarnings("unchecked")
	public static <T> List<T> doQuery(boolean start, boolean end,Session s, String st, Object... params){
		if (start){
			ManagedSessionContext.bind(s);
			s.getTransaction().setTimeout(timeout);
			s.beginTransaction();
		}
		Query query = s.createQuery(st);
		int i = 0;
		String key = null; 
		Object value = null;

		for(java.lang.Object obj : params){
			if (i % 2 == 0){ key = obj.toString(); }
			else { value = obj;}
			i++;
			query.setParameter(key,value);
		}

		List<T> results = query.list();
		if(end){
			commit(s);
		}
		return results;
	}


	/**
	execute a query returning a single object, params are "mapping1","object1","mapping2","object2",...
	 */
	public static <T> T doQueryUnique(Session s, String st, Object... params){
		ManagedSessionContext.bind(s);
		s.getTransaction().setTimeout(timeout);
		s.beginTransaction();
		Query query = s.createQuery(st);
		int i = 0;
		String key = null; 
		Object value = null;
		T result_true = null;

		for(java.lang.Object obj : params){
			if (i % 2 == 0){ key = obj.toString(); }
			else { value = obj;}
			i++;
			query.setParameter(key,value);
		}

		try{
			@SuppressWarnings("unchecked")
			T result = (T)query.uniqueResult();
			result_true = result;
		} catch(Exception e){
			@SuppressWarnings("unchecked")
			List<T> results = query.list();
			if (results.size() > 0){
				result_true = results.get(0);
			}
		}
		commit(s);
		return result_true;
	}



	/**
	execute a query returning the number of modified objects, params are "mapping1","object1","mapping2","object2",...
	 * can't be used for saving 
	 */
	public static int doUpdate(Session s, String st, Object... params){

		ManagedSessionContext.bind(s);
		s.getTransaction().setTimeout(timeout);
		s.beginTransaction();
		Query query = s.createQuery(st);
		int i = 0;
		String key = null; 
		Object value = null;

		for (java.lang.Object obj : params){
			if (i % 2 == 0){ 
				key = obj.toString(); 
			}
			else{ 
				value = obj;
			}
			i++;
			query.setParameter(key,value);
		}
		int r  = query.executeUpdate();
		commit(s);
		return r;
	}

	/**
	Converts a String to a java.SQL.Blob
	 */
	public static Blob toBlob(String s){
		byte[] bytes = s.getBytes();
		Blob blob = null;
		try{
			blob = new SerialBlob(bytes);
		}catch (SQLException e1){}
		return blob;
	}


	/**
	prints error message and closes an Hibernate session
	 */
	public static void Error(PrintWriter p, int errno, Session session){
		p.println(new Error(errno).toJson());
		if(session.isOpen())
			session.close();
	}

	/**
	commits a transaction if it was not committed or rolled back (also synchronize database)
	 */
	static public void commit(Session s){
		if(!s.getTransaction().wasCommitted() || !s.getTransaction().wasRolledBack())
			s.getTransaction().commit();
	}

	/**
	rollbacks a transaction if it was not committed or rolled back
	 */
	static public void rollback(Session s){
		if((!s.getTransaction().wasCommitted() || !s.getTransaction().wasRolledBack()) && s.getTransaction().isActive())
			s.getTransaction().rollback();
		s.close();
	}

	static public String getToken(HttpServletRequest request){

		String m_token = request.getParameter("token");
		String h_token = request.getHeader("X-Auth-Token");
		Token t = null;
		//if there is not the parameter token, then it must be in a cookie
		if(m_token == null && h_token == null){

			String token = null;
			Cookie[] cookies = request.getCookies();

			if(cookies != null){
				for(Cookie cookie : cookies){
					if(cookie.getName().equals("token")) {
						token = cookie.getValue();
						t = new Token(token);
					}
				}
			}
		}
		//there is the token as parameter 
		else {
			if (h_token != null)
				t = new Token(h_token);
			else
				t = new Token(m_token);
		}
		return t.getValue();
	}

	static public User getUser(HttpServletRequest request){


		User u = null;
		String m_token = request.getParameter("token");
		String h_token = request.getHeader("X-Auth-Token");
		Long id = null;
		Token t;
		//if there is not the parameter token, then it must be in a cookie
		if(m_token == null && h_token == null){

			String token = null;
			Cookie[] cookies = request.getCookies();

			if(cookies != null){
				for(Cookie cookie : cookies){
					if(cookie.getName().equals("token")) {
						token = cookie.getValue();
						t = new Token(token);
						try {
							id = Long.parseLong(t.getId());
							//				role = t.getRole();
						} catch (Exception e) {}
					}
				}
			}
		}
		//there is the token as parameter 
		else {
			if (h_token != null)
				t = new Token(h_token);
			else
				t = new Token(m_token);
			try {
				id = Long.parseLong(t.getId());
			} catch (Exception e) {}
		}

		Session s = HibernateSessionManager.getSession();
		try {
			u = Util.doQueryUnique(s, "SELECT U FROM User AS U WHERE U.id = :id ", "id", id);
		} catch (Exception e) {}

		if(s.isOpen()){
			s.close();
		}

		return u;
	}
	
	/**
	 * 
	 * @param command: unix shell command to be executed
	 * @return the standard output string 
	 * @throws Exception if something is written in the standard error buffer
	 */
	public static String executeCommand(String command) throws Exception {

		StringBuffer output = new StringBuffer();
		StringBuffer erroroutput = new StringBuffer();
		Process p;
		boolean error = false;

		p = Runtime.getRuntime().exec(command);
		p.waitFor();
		BufferedReader reader_error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line = "";	

		while ((line = reader.readLine())!= null) {
			output.append(line + "\n");
		}
		while ((line = reader_error.readLine())!= null) {
			erroroutput.append(line + "\n");
			error = true;
		}

		if(error){
			throw new Exception(erroroutput.toString());
		}

		return output.toString();
	}

}


