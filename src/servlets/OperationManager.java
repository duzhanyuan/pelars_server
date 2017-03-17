package servlets;

import hibernateMapping.HibernateSessionManager;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import operations.*;
import operations.utilities.*;

import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.dialect.function.SQLFunction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;
import pelarsServer.Error;
import pelarsServer.OpDetail;
import pelarsServer.PelarsSession;

@WebServlet("/op/*")
public class OperationManager extends HttpServlet  {

	//List of all the supported functions
	private static final Map<String,SQLFunction> functions;

	//fill "functions with all the functions available in INNODB language (before deployment)"
	static{
		Dialect dialect = new MySQL5InnoDBDialect();
		functions = dialect.getFunctions();
	}

	private static final long serialVersionUID = 3L;

	/**
	 valid PUT endpoint: /op/
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
		}catch (Exception e) { 
			out.println(new Error(114).toJson());
		}

		//accept also /op/
		if(request.getPathInfo() == null || request.getPathInfo().split("/").length < 2)
			newJob(out, content, request);
		else{
			out.println(new Error(120).toJson());
		}
	}

	public static void newJob(PrintWriter p, JSONObject content,HttpServletRequest request){

		Operation operation = null;

		try {
			operation = parseOp(content);
		} catch (JSONException e) {
			p.println(new Error(114).toJson());
			return;
		} catch (OpException e) {
			p.println(new Error(134,e.getLocalizedMessage()).toJson());
			// p.println(new Error(134).toJson());
			return;
		}
		catch (Exception e) {
			p.println(e.getMessage());
			return;
		}

		Session session = HibernateSessionManager.getSession();

		OpDetail op = new OpDetail();
		op.status = OpDetail.Status.QUEUED;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		op.submission_time = dateFormat.format(date);
		op.name = operation.name;

		if(operation.cur_session != 0){
			
			PelarsSession ps = Util.doQueryUnique(session, "SELECT F FROM PelarsSession AS F "
					+ "WHERE F.id = :cid", "cid", operation.cur_session);
			op.setSession(ps);
		}
		else{
			op.setSession(null);
		}

		//saving generates automatically the identifier of the OpDetail entity
		Util.save(session, op);

		//assign the same id of the OpDetail to the job
		operation.id = op.getId();
		operation.user = Util.getUser(request);
		//push the job in the queue
		Scheduler.pushTask(operation);

		//TODO: we can add the synchronous operation feature by polling the state
		boolean sync = false;
		try{
			sync = content.getBoolean("sync");
		}catch(JSONException e){}

		OpDetail.Status stat = op.getStatus();

		if(sync){	

			if(session.isOpen())
				session.close();

			while(stat != OpDetail.Status.TERMINATED && stat != OpDetail.Status.FAILED){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					p.println(new Error(125).toJson());
					return;
				}
				session = HibernateSessionManager.getSession();
				OpDetail mop = Util.doQueryUnique(session, "SELECT O FROM OpDetail AS O WHERE O.id = :mid", "mid", op.getId());
				stat = mop.getStatus();
				if(session.isOpen())
					session.close();
			}
			//TODO return result at the end
			getResult(op.getId(),p);
		}
		else{
			p.println(new Status(op.getId(),"Submitted").toJson());
		}

		if(session.isOpen())
			session.close();
	}

	public static long newJob(JSONObject content) throws JSONException, OpException{

		Operation operation = null;

		operation = parseOp(content);

		Session session = HibernateSessionManager.getSession();

		OpDetail op = new OpDetail();
		op.status = OpDetail.Status.QUEUED;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		op.submission_time = dateFormat.format(date);
		op.name = operation.name;

		PelarsSession ps = Util.doQueryUnique(session, "SELECT F FROM PelarsSession AS F "
				+ "WHERE F.id = :cid", "cid", operation.cur_session);


		if (ps == null){

			if (session.isOpen()){
				session.close();
			}
			return 0;
		}

		op.setSession(ps);

		//saving generates automatically the identifier of the OpDetail entity
		Util.save(session, op);

		if(session.isOpen())
			session.close();

		//assign the same id of the OpDetail to the job
		operation.id = op.getId();
		//push the job in the queue
		Scheduler.pushTask(operation);
		return operation.id;
	}

	/**
	Utility to check the correct URL if getting on /op/result
	 */
	private boolean checkResultMode(String[] parameters, PrintWriter out){

		if(!(parameters.length > 2)){
			out.println(new Error(113).toJson());
			return false;
		}

		if(!Util.isInteger(parameters[1])){
			out.println(new Error(116).toJson());
			return false;
		}

		return true;
	}

	/**
	 valid GET endpoints: /op/{op_id}, or /op/result/{op_id}
	 */
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws IOException{

		PrintWriter out = response.getWriter();

		response.addHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null){
			out.println(new Error(113).toJson());
			return;
		}

		String[] parameters = request.getPathInfo().split("/");

		if(parameters.length > 1){

			if(parameters.length > 2 && parameters[2].equals("result") && checkResultMode(parameters,out))

				getResult(Long.parseLong(parameters[1]), out);

			else{

				if(Util.isInteger(parameters[1])){
					getTask(Long.parseLong(parameters[1]),out);
				}
				else
					out.println(new Error(116).toJson());
			}
		}
		else{
			out.println(new Error(113).toJson());
		}
	}

	/**
	get the status of job identified by "id" parameter
	 */
	private void getTask(Long id, PrintWriter out){

		Session session = HibernateSessionManager.getSession();

		List<OpDetail> results = null;
		try{
			results = Util.doQuery(session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id" ,id);
		} catch(Exception e1) {
			if(session.isOpen())
				session.close();
			out.println(new Error(106).toJson());
			return;
		}

		if(results != null && results.size() == 1){

			OpDetail opd = results.get(0);
			JSONObject to_return = new JSONObject();
			try{
				to_return.put("status", opd.getStatus());
				to_return.put("submission time", opd.getSubmission_time());

				if(opd.getStatus() == OpDetail.Status.TERMINATED){
					to_return.put("execution time", opd.getExecution_time());
				}
				out.println(to_return);
			}catch (JSONException e) {
				Util.Error(out, 114, session);
			}	
		}
		else{
			out.println(new Error(127).toJson());
		}

		if(session.isOpen())
			session.close();
	}

	/**
	get the result of job identified by "id" parameter
	 */
	private static void getResult(Long id, PrintWriter out){

		Session session = HibernateSessionManager.getSession();

		List<OpDetail> results = null;
		try{
			results = Util.doQuery(session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id" ,id);
		} catch(Exception e1) {
			if(session.isOpen())
				session.close();
			out.println(new Error(106).toJson());
			return;
		}

		if(results != null && results.size() == 1){

			switch(results.get(0).getStatus()){

			case FAILED:
				out.println(new Error(results.get(0).getErrorDescription()).toJson());
				break;

			case TERMINATED:
				//if is stream must retrieve the results in the table of results
				if(results.get(0).getIs_stream() == true) {

					String query = "SELECT S.data FROM StreamElement S WHERE S.task = :id";
					List<BaseData> stream = null;

					try{
						stream = Util.doQuery(session, query, "id", id);
					}catch (Exception e) {
						if(session.isOpen())
							session.close();
						out.println(new Error(106).toJson());
						return;
					}

					JSONArray array = new JSONArray();

					for(int i = 0; i< stream.size(); i++){
						array.put(stream.get(i).toJson());
					}

					out.println(array);
				}
				//else the result is stored as a STRING in the OpDetail entity
				else{

					JSONObject jo = new JSONObject();
					try{
						JSONObject content = new JSONObject(results.get(0).result);
						jo.put("result", content);
					}catch (JSONException e) {
						try {

							JSONArray array = new JSONArray(results.get(0).result);
							jo = new JSONObject().put("result", array);
							out.println(jo);
							return;

						}catch (JSONException  | java.lang.ClassCastException e1) {
							try{
								jo.put("result", results.get(0).result);
							}
							catch(JSONException e2){
								if (session.isOpen())
									session.close();
								out.println(new Error(106).toJson());
								return;
							} 
						}
					}
					out.println(jo);
				}
				break;

			default: 
				//QUEUED OR EXECUTING
				out.println(new Error("No results available yet").toJson());
				break;
			}
		}
		else {
			out.println(new Error(127).toJson());
		}
		if (session.isOpen())
			session.close();
	}


	public static Operation parseOp(JSONObject content) throws JSONException, OpException {
		Operation operation;
		String type = null;

		type = content.getString("type");

		if(functions.containsKey(type)){
			operation = new StandardOperation(content);
		}
		else {
			switch(type) {
			case "mean": 
				operation = new Mean(content);
				break;

			case "interval" :
				operation = new TimeInterval(content);
				break;

			case "track" :
				operation = new OperationTracker(content);
				break;

			case "variance" : 
				operation = new Variance(content);
				break;

			case "pipeline" : 
				operation = new Pipeline(content);
				break;

			case "filter" : 
				operation = new Filter(content);
				break;

			case "multistatistics" : 
				operation = new Multistatistic(content);
				break;

			case "frequency" : 
				operation = new Frequency(content);
				break;

			case "map" : 
				operation = new operations.Map(content);
				break;

			case "time_looking" : 
				operation = new Times(content);
				break;

			case "hand_speed" :
				operation = new HandFrequency(content);
				break;

			case "media_info" :
				operation = new MultimediaData(content);
				break;

			case "presence" :
				operation = new Presence(content);
				break;

			case "set_distances" :
				operation = new SetDistances(content);
				break;

			case "set_time" :
				operation = new SetSessionTime(content);
				break;

			case "convert_session_time" :
				operation = new ConvertSessionTime(content);
				break;

			case "num_faces" :
				operation = new NumFaces(content);
				break;

			case "plot_faces" :
				operation = new PlotFaces(content);
				break;

			case "generate_thumbnails" :
				operation = new GenerateThumbnails(content);
				break;

			case "video_snapshots" :
				operation = new VideoFromSnapshots(content);
				break;

			case "make_files" :
				operation = new FileMaker(content);
				break;

			case "base64" :
				operation = new Base64(content);
				break;

			case "restore_faces" :
				operation = new RestoreFaces(content);
				break;

			case "proximity" :
				operation = new Proximity(content);
				break;

			case "programming_time" :
				operation = new ProgrammingTime(content);
				break;

			case "object_detect" :
				operation = new ObjectDetector(content);
				break;

			case "group" : 
				operation = new Group(content);
				break;

			case "merge" :
				operation = new Merge(content);
				break;

			case "clean" : 
				operation = new CleanSessions(content);
				break;

			default:
				throw new OpException("unrecognized operation type");
			}
		}
		return operation;
	}
}




