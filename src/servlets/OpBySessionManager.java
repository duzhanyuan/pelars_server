package servlets;

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

import pelarsServer.BaseData;
import pelarsServer.Error;
import pelarsServer.OpDetail;

@WebServlet("/content/*")
public class OpBySessionManager extends HttpServlet{

	private static final long serialVersionUID = 1999L;

	/**
	 valid GET endpoints: /content/{session_id}/{cutom_name}
	 */
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws IOException{

		PrintWriter out = response.getWriter();

		response.addHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null){
			try {
				out.println(pelarsServer.CrossSessions.getInstance().toJSON().toString(4));
			} catch (JSONException e) {out.println(e.getMessage());}
			return;
		}

		String[] parameters = request.getPathInfo().split("/");

		if(parameters.length < 2){
			try {
				out.println(pelarsServer.CrossSessions.getInstance().toJSON().toString(4));
				return;
			} catch (JSONException e) {out.println(e.getMessage());}
		}

		if (parameters.length == 2){
			//handle only session_id is present
			if (Util.isInteger(parameters[1])){
				getOpByName(Long.parseLong(parameters[1]),response,out);
			}
			else{
				getOpByName(parameters[1],response,out);
			}
			return;
		}

		if (parameters.length == 3){
			//handle case name is present

			if (Util.isInteger(parameters[1]) && !Util.isInteger(parameters[2])){
				getOpByName(Long.parseLong(parameters[1]),parameters[2],response,out);
			}
			else{
				out.println(new Error(113).toJson());
			}
			return;
		}

		out.println(new Error(113).toJson());
	}

	private void getOpByName(Long session_id, String name, HttpServletResponse res, PrintWriter out){

		List<OpDetail> op_info = null;
		List<String> mappings = new ArrayList<String>();
		List<Object> values = new ArrayList<Object>();

		mappings.add("ses");
		values.add(session_id);

		String query = "SELECT O FROM OpDetail AS O WHERE O.session.id = :ses AND O.name = :name";

		mappings.add("name");
		values.add(name);

		Session hsession = hibernateMapping.HibernateSessionManager.getSession();

		try {
			op_info = Util.doQuery(hsession, query, mappings, values);
		} catch (Exception e) {
			res.setStatus(500);
			out.println(e.getMessage());
			hsession.close();
			return;
		}
		if (op_info.size() == 0){
			res.setStatus(404);

			hsession.close();
			return;
		}

		JSONArray jsa = new JSONArray();
		for(int i = 0; i < op_info.size(); i++){
			try {
				jsa.put(getResult(op_info.get(i).id, hsession).put("id", op_info.get(i).id).putOpt("name", op_info.get(i).getName()));
			} catch (JSONException e) {
			}
		}

		try {
			out.println(jsa.toString(2));
		} catch (JSONException e) {
		}

		hsession.close();
	}

	private void getOpByName(Long session_id,HttpServletResponse res, PrintWriter out){

		List<OpDetail> op_info = null;
		List<String> mappings = new ArrayList<String>();
		List<Object> values = new ArrayList<Object>();

		mappings.add("ses");
		values.add(session_id);

		String query = "SELECT O FROM OpDetail AS O WHERE O.session.id = :ses";

		Session hsession = hibernateMapping.HibernateSessionManager.getSession();

		try {
			op_info = Util.doQuery(hsession, query, mappings, values);
		} catch (Exception e) {
			res.setStatus(500);
			out.println(e.getMessage());
			hsession.close();
			return;
		}

		if(op_info.size() == 0){

			res.setStatus(404);
			hsession.close();
			return;
		}

		JSONArray jsa = new JSONArray();
		for(int i = 0; i < op_info.size(); i++){
			try {
				jsa.put(getResult(op_info.get(i).id, hsession).put("id", op_info.get(i).id).putOpt("name", op_info.get(i).getName()));
			} catch (JSONException e) {

			}
		}

		try {
			out.println(jsa.toString(2));
		} catch (JSONException e) {
		}

		hsession.close();
	}

	private void getOpByName(String name, HttpServletResponse res, PrintWriter out){

		List<OpDetail> op_info = null;
		List<String> mappings = new ArrayList<String>();
		List<Object> values = new ArrayList<Object>();

		String query = "SELECT O FROM OpDetail AS O WHERE O.name = :name";

		mappings.add("name");
		values.add(name);

		Session hsession = hibernateMapping.HibernateSessionManager.getSession();

		try {
			op_info = Util.doQuery(hsession, query, mappings, values);
		} catch (Exception e) {
			res.setStatus(500);
			out.println(e.getMessage());
			hsession.close();
			return;
		}
		if (op_info.size() == 0){
			res.setStatus(404);

			hsession.close();
			return;
		}

		JSONArray jsa = new JSONArray();
		for(int i = 0; i < op_info.size(); i++){
			try {
				jsa.put(getResult(op_info.get(i).id, hsession).put("id", op_info.get(i).id).putOpt("name", op_info.get(i).getName()));
			} catch (JSONException e) {
			}
		}
		out.println(jsa.toString());

		hsession.close();
	}

	/**
	get the result of job identified by "id" parameter as a JSONObject
	 */
	private static JSONObject getResult(Long id, Session session) throws JSONException{

		OpDetail results = null;
		results = Util.doQueryUnique(session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id" ,id);
		JSONObject jo = new JSONObject();


		if(results != null && results.getStatus() == OpDetail.Status.TERMINATED && results.getResult() != null){

			//if is stream must retrieve the results in the table of results
			if(results.getIs_stream() == true) {

				String query = "SELECT S.data FROM StreamElement S WHERE S.task = :id";
				List<BaseData> stream = null;

				stream = Util.doQuery(session, query, "id", id);

				JSONArray array = new JSONArray();

				for(int i = 0; i< stream.size(); i++){
					array.put(stream.get(i).toJson());
				}
				try{
					jo.put("result", array);
				}catch(JSONException e){}
			}
			//else the result is stored as a STRING in the OpDetail entity
			else{
				try{
					JSONObject content = new JSONObject(results.result);
					jo.put("result", content);
				}catch (JSONException e) {
					try {

						JSONArray array = new JSONArray(results.result);
						jo = new JSONObject().put("result", array);

					}catch (JSONException  | java.lang.ClassCastException e1) {
						try{
							jo.put("result", results.result);
						}
						catch(JSONException e2){
						} 
					}
				}
			}
			return jo;
		}
		else{
			throw new JSONException("no result");
		}
	}

	/**
	 valid DELETE endpoints: /content/{session_id}/{cutom_name}
	 */
	public void doDelete(HttpServletRequest request, 
			HttpServletResponse response) throws IOException{

		PrintWriter out = response.getWriter();

		response.addHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null){
			out.println(new Error(113).toJson());
			return;
		}

		String[] parameters = request.getPathInfo().split("/");

		if (parameters.length == 2){
			//handle only session_id is present
			if (Util.isInteger(parameters[1])){
				delOpByName(Long.parseLong(parameters[1]),response,out);
			}
			else
				out.println(new Error(116).toJson());
			return;
		}

		if (parameters.length == 3){
			//handle case name is present

			if (Util.isInteger(parameters[1]) && !Util.isInteger(parameters[2])){
				delOpByName(Long.parseLong(parameters[1]),parameters[2],response,out);
			}
			else{
				out.println(new Error(113).toJson());
			}
			return;
		}

		out.println(new Error(113).toJson());
	}

	private void delOpByName(Long session_id,HttpServletResponse res, PrintWriter out){


		int deleted = 0;

		String query = "DELETE OpDetail O WHERE O.session.id = :ses";

		Session hsession = hibernateMapping.HibernateSessionManager.getSession();

		try {
			deleted = Util.doUpdate(hsession, query, "ses", session_id);
		} catch (Exception e) {
			res.setStatus(500);
			out.println(e.getMessage());
			hsession.close();
			return;
		}

		if(deleted == 0){
			out.println(new Status("Nothing deleted").toJson());
		}
		else{
			out.println(new Status("Success").toJson());
		}
		hsession.close();
	}

	private void delOpByName(Long session_id, String name, HttpServletResponse res, PrintWriter out){

		int deleted = 0;

		String query = "DELETE OpDetail O WHERE O.session.id = :ses AND O.name = :name";

		Session hsession = hibernateMapping.HibernateSessionManager.getSession();

		try {
			deleted = Util.doUpdate(hsession, query, "ses", session_id, "name", name);
		} catch (Exception e) {
			res.setStatus(500);
			out.println(e.getMessage());
			hsession.close();
			return;
		}

		if(deleted == 0){
			out.println(new Status("Nothing deleted").toJson());
		}
		else{
			out.println(new Status("Success").toJson());
		}
		hsession.close();
	}

}
