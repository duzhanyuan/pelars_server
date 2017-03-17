package servlets;

import hibernateMapping.HibernateSessionManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Error;
import pelarsServer.MultimediaContent;

@WebServlet("/snapshot/*")
public class SnapshotManager extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
			if(parameters.length <= 2){
				error = 113; 
			}
			else {
				if(parameters.length <= 3){
					if(Util.isInteger(parameters[1])){
						getSnapshots(Long.parseLong(parameters[1]), Double.parseDouble(parameters[2]), response, request);
					}
					else{
						error = 116;
					}
				}
			}
		}
		if (error != 0){
			response.getWriter().println(new Error(error).toJson());
		}
	}

	public void getSnapshots(long id, double time, HttpServletResponse response, HttpServletRequest request) throws IOException{

		//TODO find all the multimedia with trigger = manual close to time parameter

		PrintWriter out = response.getWriter();

		double start = time - 3000.0;
		double end = time + 3000.0;

		Session session = HibernateSessionManager.getSession();

		String query = "SELECT M FROM MultimediaContent AS M WHERE M.session.id = :id AND M.time <= :end AND M.time >= :start AND M.type = 'image' AND M.triggering = 'manual'";

		List<MultimediaContent> results = Util.doQuery(session, query, "id", id, "end", end, "start", start);


		if(results.size() == 0){
			out.println(new Status("Empty").toJson());
			if(session.isOpen()){
				session.close();
			}
			return;
		}

		//now check the permission
		if(!ACL_RuleManager.Check(Util.getUser(request), "GETMULTIMEDIA", results.get(0), session)){
			response.setContentType("application/json");
			out.println(new Error(135).toJson().toString());
			response.setStatus(403);

			if(session.isOpen()){
				session.close();
			}
			return;
		}

		JSONArray oj = new JSONArray();

		if(results.size() == 3){
			for(MultimediaContent m : results){

				JSONObject o = m.toJson(false);
				oj.put(o);
			}
		}
		else{


			double min_time = Double.MAX_VALUE;
			ArrayList<MultimediaContent> current = new ArrayList<MultimediaContent>();

			for(short i=0; i<3; i++){

				String view = "";
				min_time = Double.MAX_VALUE;
				MultimediaContent current_close = null;
				current.clear();

				switch(i){

				case 0:
					view = "people";
					break;
				case 1:
					view = "screen";
					break;
				case 2: 
					view = "workspace";
					break;
				default:
					break;
				}

				for (MultimediaContent m: results){
					if(m.getView().equals(view)){
						current.add(m);
					}
				}

				for(MultimediaContent m:current){
					if(Math.abs(m.time - time) < min_time){
						current_close = m;
						min_time = Math.abs(m.time - time);
					}
				}

				if(current_close != null){
					oj.put(current_close.toJson());
				}
			}
		}

		try {
			out.println(oj.toString(4));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
