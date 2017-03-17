package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.reflections.Reflections;

import pelarsServer.BaseData;
import pelarsServer.Error;

@WebServlet("/op/help/*")
public class OperationHelper extends HttpServlet{

	private static final long serialVersionUID = 1L;

	private static HashMap<String,List<String>> operation_map = new HashMap<String,List<String>>();

	static{

		ArrayList<String> mean_fields = new ArrayList<String>();
		mean_fields.add("table");
		mean_fields.add("field");
		mean_fields.add("session");
		mean_fields.add("phase");

		operation_map.put("mean", mean_fields);
		operation_map.put("variance", mean_fields);
		operation_map.put("multistatistics", mean_fields);
		
		operation_map.put("filter", Arrays.asList(new String[] {"table", "session", "expression","phase"}));
		operation_map.put("hand_speed", Arrays.asList(new String[]{"session","phase"}));
		operation_map.put("num_faces", Arrays.asList(new String[]{"session", "time","phase"}));
		operation_map.put("media_info", Arrays.asList(new String[]{"session","phase"}));
		operation_map.put("presence", Arrays.asList(new String[]{"session","phase"}));
		operation_map.put("pipeline", Arrays.asList(new String[]{"session", "operations"}));
		operation_map.put("map", Arrays.asList(new String[]{"session", "operation", "window_size", "overlap", "parallelism"}));
		operation_map.put("pipeline", Arrays.asList(new String[]{"session", "operations"}));
		

		Reflections reflections = new Reflections("pelarsServer");

		Set<Class<? extends BaseData>> allClasses = reflections.getSubTypesOf(BaseData.class);
		List<String> sub_names = new ArrayList<String>(allClasses.size());

		for (Class<?> candidateClass : allClasses) {


			List<Field> fieldList = new ArrayList<Field>();
			fieldList.addAll(Arrays.asList(candidateClass.getDeclaredFields()));

			List<String> field_names = new ArrayList<String>(fieldList.size());

			for(Field f : fieldList){
				if(!java.lang.reflect.Modifier.isStatic(f.getModifiers())){
					field_names.add(f.getName());
				}
			}

			operation_map.put(candidateClass.getSimpleName(), field_names);

			sub_names.add(candidateClass.getSimpleName());

		}

		operation_map.put("table", sub_names);

	}

	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {

		String [] parameters = null;

		response.addHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null)
			getHelp(response, request);
		else{
			parameters = request.getPathInfo().split("/");
			if(parameters.length <= 1)
				getHelp(response, request);
			else{
				if(!Util.isInteger(parameters[1])){
					getHelp(parameters[1], response, request);
				}
				else{
					PrintWriter out = response.getWriter();
					out.println(new Error(116).toJson());
				}	
			}
		}
	}

	/**
	 * 
	 */
	public void getHelp(String key, 
			HttpServletResponse response, HttpServletRequest request) throws IOException {

		PrintWriter out = response.getWriter();

		JSONArray jsmap = new JSONArray(operation_map.get(key));

		out.println(jsmap);


	}

	/**
	prints all the PELARS session representation 
	 */
	public void getHelp(HttpServletResponse response, HttpServletRequest request) throws IOException {

		PrintWriter out = response.getWriter();

		out.println(operation_map.toString());


	}

}
