package operations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Data;

import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi
 * this operation filters data samples according to a boolean expression
 */
public class Filter extends OperationStream{

	private static final Set<String> operators = new HashSet<String>();
	
	public String expression;

	static{
		operators.add(">");
		operators.add("<");
		operators.add("<=");
		operators.add(">=");
		operators.add("=");
		operators.add("AND");
		operators.add("OR");
	}

	public enum PARSE{ 
		ORDINARY,
		OPERATOR,
		NUMERIC
	}

	public Filter(JSONObject content) throws JSONException{
		super(content);

		expression = content.getString("expression");
	}

	private PARSE Tokenization(String h){

		if (operators.contains(h)){
			return PARSE.OPERATOR;
		}

		if(Util.isNumeric(h)){
			return PARSE.NUMERIC;
		}

		return PARSE.ORDINARY;
	}
	
	/**
	 * Parses an sql style logical expression
	 */
	public void run(List<? extends Data> objs) throws Exception{

		int counter = 0;
		result = new ArrayList<Data>();

		//String q will be the expression understandable to the JS engine
		String q = "";

		//splits the expression in words and returns an iterator to them
		StringTokenizer st = new StringTokenizer(expression);

		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("js");  

		while(st.hasMoreTokens()){

			String h = st.nextToken();

			switch(Tokenization(h)){
			case ORDINARY:
				q += " " + "b." + h;
				break;
				//just map to standard JAVA boolean operators
			case OPERATOR:
				if (h.equals("AND")){
					h = "&&";
				}
				if(h.equals("OR")){
					h = "||";
				}
				q += " " + h;
				break;
			case NUMERIC:
				//not sure if needed
				engine.put("alias" + counter, Double.parseDouble(h));
				q = q + " alias" + counter;
				counter++;
				break;
			default:
				break;
			}
		} 

		for(Data b : objs){
			engine.put("b", b);
			Object r = engine.eval(q);
			if((boolean)r){
				result.add(b);
			}
		}
	}

}
