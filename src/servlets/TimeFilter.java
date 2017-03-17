package servlets;

import hibernateMapping.HibernateSessionManager;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.PelarsSession;


public class TimeFilter implements Filter{

	public void init(FilterConfig config) throws ServletException{}
	public void destroy(){}

	/**
	 * Filter used to reduce the output of time related data according to a given interval
	 */
	public void doFilter(ServletRequest  request, ServletResponse response, FilterChain chain) throws IOException, ServletException{

		//wrap the response 
		ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse)response);
		//execute filtering only after the server produced the response
		chain.doFilter(request, responseWrapper);

		String s = responseWrapper.toString();

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		Double f,t;
		boolean epoch_option = false;

		String from = req.getParameter("from");
		String to = req.getParameter("to");
		String epoch = req.getParameter("epoch");
		
		if (from == null){
			f = 0.0;
		}
		else{
			f = Double.parseDouble(from);
		}
		
		if (to == null){
			t = Double.MAX_VALUE;
		}
		else{
			t = Double.parseDouble(to);
		}
		
		if (epoch == null){
			epoch_option = false;
		}
		else{
			epoch_option = true;
		}

		Double start_time = 0.0;

		PrintWriter out = response.getWriter();

		if(from != null && to != null) {
			//time can be in epoch or according to the starting time of the session
			JSONArray array = null;
			JSONArray copy = new JSONArray();

			try{
				array = new JSONArray(s);
			} catch (JSONException e) {
				res.setStatus(500);
			} 

			if (epoch_option == false){

				Long id = null;

				try{
					id = array.getJSONObject(0).getLong("session");
				}catch(JSONException e){}
				//get the BaseData from DB
				Session session = HibernateSessionManager.getSession();
				PelarsSession b = Util.doQueryUnique(session, "SELECT B from PelarsSession AS B WHERE B.id = :m_id", "m_id", id);

				start_time = Double.parseDouble(b.getStart());
				session.close();
			}

			JSONObject current = null;
			for(int i = 0; i < array.length(); ++i){
				Double time = null;

				try {
					current = array.getJSONObject(i);
					time = current.getDouble("time");
				}catch (JSONException e) {
					try {
						time = current.getDouble("start");
					} catch (JSONException e1) {}
				}

				if(time - start_time >= f && time - start_time <= t) {
					copy.put(current);
				}
			}
			//get rid of the old response
			out.flush();
			//substitute with the filtered one
			out.println(copy);
		}	
		else {
			//TODO: not sure if needed
			out.write(responseWrapper.toString());
		}
	}
}
