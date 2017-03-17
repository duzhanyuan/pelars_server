package servlets;

import hibernateMapping.HibernateSessionManager;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;


import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayOutputStream;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.JSONArray;
import pelarsServer.BaseData;
import pelarsServer.Error;

import pelarsServer.*;

/**
 * 
 * Servlet supporting only GET. Gives all the data samples in a single learning session.
 * Example /data/188
 */
@WebServlet("/data/*")
public class DataManager extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static ArrayList<String> types = new ArrayList<String>();

	static{
		types.add("BaseData");
		types.add("Face");
		types.add("Hand");
		types.add("Particle");
		types.add("Ide");
		types.add("Audio");
		types.add("Button");
	}

	private static void prefetch(Cache cache){
		Session h_session = HibernateSessionManager.getSession();
		List<PelarsSession> good_sessions = 
				Util.doQuery(h_session, 
						"SELECT S FROM PelarsSession AS S WHERE S.is_valid = :valid", "valid", true);
		//TAKE all data from all the sessions and add to cache
		String type = "BaseData";
		// build key of the cache and fill in results as byte array
		for(int j=0; j<good_sessions.size();j++){

			String key = "data_" + good_sessions.get(j).getId() + "_" + type.toLowerCase() + "_null";

			byte [] bresults = null;

			List<? extends Data> results = null;

			try {
				results = getDataByPhase(null, h_session, good_sessions.get(j).getId() , type);
			} catch (Exception e) {}

			JSONArray oj = new JSONArray();

			// loooooooong
			for (Data b: results) {
				oj.put(b.toJson());
			}

			// looooonger
			try{
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				OutputStream yout = new GZIPOutputStream(bo);
				PrintWriter xout = new PrintWriter(yout);
				xout.println(oj.toString(4));
				xout.flush();
				yout.flush();
				yout.close();
				bo.close();
				bresults = bo.toByteArray();
			}
			catch(Exception e){}

			Element resultCacheElement = new Element(key, bresults);
			resultCacheElement.setEternal(true);
			cache.put(resultCacheElement);	
		}
	}
	
	@Override
	public void init(){
		//must spawn a thread that fetches the most likely accessed data
		Cache cache = (Cache) this.getServletContext().getAttribute("dbCache");
		Runnable r = new Runnable() {
	         public void run() {
	        	 prefetch(cache);
	         }
	     };
	     new Thread(r).start();
	}
	/**
	 * 
	 * @param req_phase
	 * @param session
	 * @param id
	 * @return 
	 * @throws Exception
	 * in case req_phase is not null it returns all the BaseData from session id belonging to the same phase req_phase. If req_phase is null 
	 * it returns all the data in session id
	 */
	public static List<? extends BaseData> getDataByPhase(String req_phase, Session session, long id, String...table) throws Exception{

		String subq = "";
		for(String t: table){
			subq += BaseData.classtoid.get(t) +", ";
		}
		subq = subq.substring(0, subq.length()-2);

		List<BaseData> all_results  = null;

		PelarsSession ses = Util.doQueryUnique(session, "SELECT S FROM PelarsSession AS S WHERE S.id = :id", "id", id);
		if(ses == null){
			throw new Exception("requested session not present");
		}

		if(req_phase != null){
			List<PhaseEntity> phases = Util.doQuery(session, "SELECT P FROM PhaseEntity AS P WHERE P.session.id = :id AND "
					+ "P.phase = :phase", "id",id,"phase",req_phase);

			all_results = Util.doQuery(true,false,session, "SELECT O FROM BaseData as O where O.class in (" + subq +") AND O.session.id = :id "
					+ "AND O.time >= :r1 AND O.time < :r2 ORDER BY O.time", "id", id, "r1",(double)phases.get(0).getStart(), "r2", (double)phases.get(0).getEnd());

			List<BaseData> results;

			for(int i=1; i<phases.size()-1;i++ ){

				results = Util.doQuery(false,false,session, "SELECT O FROM BaseData as O where O.class in (" + subq +") AND O.session.id = :id "
						+ "AND O.time >= :r1 AND O.time < :r2 ORDER BY O.time", "id", id,"r1",(double)phases.get(i).getStart(),"r2",(double)phases.get(i).getEnd());

				all_results.addAll(results);
			}

			results = Util.doQuery(false,true,session, "SELECT O FROM BaseData as O where O.class in (" + subq +") AND O.session.id = :id "
					+ "AND O.time >= :r1 AND O.time < :r2 ORDER BY O.time", "id", id,"r1",(double)phases.get(phases.size()-1).getStart(),"r2",(double)phases.get(phases.size()-1).getEnd());

			all_results.addAll(results);
		}
		else{
			all_results = Util.doQuery(session, "SELECT O FROM BaseData as O where O.class in (" + subq +") AND O.session.id = :id ORDER BY O.time", "id", id);
		}

		return all_results;
	}

	private static List<Data> fetchBaseData(String phase, Session session, long id) throws Exception{

		List<Data> results = new ArrayList<Data>();

		for(int i=1; i<types.size(); i++){
			String type = types.get(i);
			results.addAll(getDataByPhase(phase, session, id, type));
		}

		return results;
	}

	/**
	 * 
	 * @param req_phase
	 * @param session
	 * @param id
	 * @return 
	 * @throws Exception
	 * in case req_phase is not null it returns all the BaseData from session id belonging to the same phase req_phase. If req_phase is null 
	 * it returns all the data in session id
	 */

	public static List<? extends Data> getDataByPhase(String req_phase, Session session, long id, String table) throws Exception{

		List<Data> all_results  = null;

		PelarsSession ses = Util.doQueryUnique(session, "SELECT S FROM PelarsSession AS S WHERE S.id = :id", "id", id);
		if(ses == null){
			throw new Exception("requested session not present");
		}

		if(req_phase != null){
			List<PhaseEntity> phases = Util.doQuery(session, "SELECT P FROM PhaseEntity AS P WHERE P.session.id = :id AND "
					+ "P.phase = :phase", "id",id,"phase",req_phase);

			all_results = Util.doQuery(true,false,session, "SELECT O FROM " + table + " as O where O.session.id = :id "
					+ "AND O.time >= :r1 AND O.time < :r2 ORDER BY O.time", "id", id, "r1",(double)phases.get(0).getStart(), "r2", (double)phases.get(0).getEnd());

			List<Data> results;

			for(int i=1; i<phases.size()-1;i++ ){

				results = Util.doQuery(false,false,session, "SELECT O FROM " + table + " as O where O.session.id = :id "
						+ "AND O.time >= :r1 AND O.time < :r2 ORDER BY O.time", "id", id,"r1",(double)phases.get(i).getStart(),"r2",(double)phases.get(i).getEnd());

				all_results.addAll(results);
			}

			results = Util.doQuery(false,true,session, "SELECT O FROM " + table + " as O where O.session.id = :id "
					+ "AND O.time >= :r1 AND O.time < :r2 ORDER BY O.time", "id", id,"r1",(double)phases.get(phases.size()-1).getStart(),"r2",(double)phases.get(phases.size()-1).getEnd());

			all_results.addAll(results);
		}
		else{
			if(!table.equals("BaseData")){
				all_results = Util.doQuery(session, "SELECT O FROM " + table + " as O where O.session.id = :id ORDER BY O.time", "id", id);
			}
			else{
				all_results = fetchBaseData(req_phase,session,id);
			}

		}
		return all_results;
	}

	public int checkType(String s){

		for (int i = 0; i < types.size(); ++i){
			if (types.get(i).equalsIgnoreCase(s)){
				return i;
			}
		}
		return -1;
	}

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

				if(parameters.length > 2){
					try{
						GetData(Long.parseLong(parameters[1]), parameters[2], response, request);
					}catch (Exception e) {
						response.setHeader("Content-Type", "text/plain");
						out.println(e.getMessage());
					}
				}
				else{

					try {
						GetData(Long.parseLong(parameters[1]), response, request);
					} catch (Exception e) {
						response.setHeader("Content-Type", "text/plain");
						out.println(e.getMessage());
					}
				}
			}
			else{
				out.println(new Error(116).toJson());
			}
		}
	}


	public void GetData(Long id, String type, HttpServletResponse response, HttpServletRequest request) throws Exception{

		Session session = HibernateSessionManager.getSession();

		int type_idx = this.checkType(type);

		if(type_idx != -1){

			type = types.get(type_idx);
			List<? extends Data> results = null;

			//TODO: immediately check whether id is the id of a session belonging to user calling
			PelarsSession s = SessionManager.getSession(id,session);
			if (s == null){
				//return that the session does not exist
				PrintWriter out = response.getWriter();
				out.println(new Error(107).toJson());
				if (session.isOpen()){
					session.close();
				}
				return;
			}
			else{
				if(!ACL_RuleManager.Check(Util.getUser(request), "GETDATA", s, session)){
					response.setStatus(403);
					PrintWriter out = response.getWriter();
					out.println(new Error(135).toJson());
					if(session.isOpen()){
						session.close();
					}
					return;
				}				
			}
			String encodings = request.getHeader("Accept-Encoding");
			boolean gzipreq = 	(encodings != null && encodings.indexOf("gzip") != -1);					

			if(gzipreq && s.is_valid == true)
			{
				// build key of the cache and fill in results as byte array
				String key = "data_" + id + "_" + type.toLowerCase() + "_" + request.getParameter("phase");
				Cache cache = (Cache) this.getServletContext().getAttribute("dbCache");
				Element element = cache.get(key);
				byte [] bresults;

				response.setHeader("Content-Encoding", "gzip");
				response.setHeader("Vary", "Accept-Encoding");
				OutputStream out = response.getOutputStream(); 

				if (element != null && !s.isAlive()) {
					response.setHeader("EHCache", "True");
					long nt = System.nanoTime();
					bresults = (byte[])element.getObjectValue(); // get object from cache
					long ntb = System.nanoTime();
					response.setHeader("EHCacheElapsed", ""+(ntb-nt));
					response.setHeader("EHCacheSize", ""+(bresults.length));
				} else {

					results = getDataByPhase(request.getParameter("phase"), session, id, type);

					JSONArray oj = new JSONArray();

					// loooooooong
					for (Data b: results) {
						oj.put(b.toJson());
					}

					// looooonger
					ByteArrayOutputStream bo = new ByteArrayOutputStream();
					OutputStream yout = new GZIPOutputStream(bo);
					PrintWriter xout = new PrintWriter(yout);
					xout.println(oj.toString(4));
					xout.flush();
					yout.flush();
					yout.close();
					bo.close();
					bresults = bo.toByteArray();

					Element resultCacheElement = new Element(key, bresults);
					resultCacheElement.setEternal(true);
					cache.put(resultCacheElement);		

				}
				// long as well
				response.setHeader("Content-Length", ""+bresults.length);
				out.write(bresults);
			}
			else
			{
				response.setHeader("Content-Type", "application/json");
				results = getDataByPhase(request.getParameter("phase"), session, id, type);
				if(session.isOpen())
					session.close();
				JSONArray oj = new JSONArray();

				for (Data b: results){
					oj.put(b.toJson());
				}
				if(oj.length() > 0)
				{
					PrintWriter out = response.getWriter();
					out.println(oj.toString(4));
				}
				else
				{
					PrintWriter out = response.getWriter();
					out.println(new Status("Empty").toJson());						
				}
			}
		}
		else{
			PrintWriter out = response.getWriter();
			out.println(new Error(137).toJson());
		}

		response.setStatus(200);

		if(session.isOpen())
			session.close();

	}

	/**
	 * Prints all the data relative to session "id"as a JSON array
	 */
	public void GetData(Long id, HttpServletResponse response, HttpServletRequest request) throws Exception{
		//TODO: try to get the the composition of the tables instead that one polym query
		GetData(id,"BaseData", response, request);
	}

	/**
	 * Intended just to invalidate the cache. It does not modify data. After the call of this method
	 * all not multimedia data are removed from cache
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();

		if(request.getPathInfo() == null){
			out.println(new Error(113).toJson());
			return;
		}

		String [] parameters = request.getPathInfo().split("/");

		if(request.getPathInfo() == null || parameters.length <= 1){
			out.println(new Error(113).toJson());
		}
		else{
			invalidateCache(Long.parseLong(parameters[1]), response, out);
		}
	}

	/**
	 * 
	 * @param session_id: session to invalidate cache
	 * @param response 
	 * invalidates all the possible cache elements relative to session session_id
	 * @throws IOException 
	 */

	public void invalidateCache(long session_id, HttpServletResponse response, PrintWriter out) throws IOException{

		response.setHeader("Content-Type", "text/plain");

		//for now compute all possible Strings 
		String base_string = "data_" + session_id + "_";
		ArrayList<String> cache_strings = new ArrayList<String>();
		for(String t : types){
			cache_strings.add(base_string + t.toLowerCase() +"_null");
		}

		//query all the phases of the session
		Session h_session = HibernateSessionManager.getSession();
		ArrayList<String> to_append = new ArrayList<String>();
		List<PhaseEntity> phases = Util.doQuery(h_session, "SELECT P FROM PhaseEntity AS P WHERE P.session.id = :id", "id",session_id);
		for(String s : cache_strings){
			for(PhaseEntity p : phases){
				String n_string = new String(s.replace("null", p.getPhase()));
				to_append.add(n_string);
			}
		}

		cache_strings.addAll(to_append);

		Cache cache = (Cache) this.getServletContext().getAttribute("dbCache");
		Element element;

		//out.println(cache_strings.toString());

		for(String s : cache_strings){
			element = cache.get(s);
			if(element!= null){
				element.setEternal(false);
				element.setTimeToLive(1);
			}
		}

		out.println("cache of session " + session_id + " invalidated");
	}
}