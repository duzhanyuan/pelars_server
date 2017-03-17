package pelarsServer;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is used to manage error. Errors can be created by specifying an
 * error code and can be printed as Json content using the toJson() method.
 *
 * @param  code 	an integer code identifying the error
 * @param  message	a string message associated with the error code
 * @param  ht    	a static HashMap to contain the association between error codes and messages   
 */
public class Error {

	int code;
	String message;
	private static final HashMap<Integer, String> ht = new HashMap<Integer, String>();

	static{
		ht.put(100, "Session already present when creating a new one");
		ht.put(101, "Session not present when closing the session");
		ht.put(102, "Invalid parameter/s");
		ht.put(103, "Unknown operation");
		ht.put(104, "Generic error");
		ht.put(105, "Wrong type specified");
		ht.put(106, "Generic error");
		ht.put(107, "Session not present");
		ht.put(108, "User already present");
		ht.put(109, "User not present");
		ht.put(110, "Template already present");
		ht.put(111, "Template not present");
		ht.put(112, "Session not present");
		ht.put(113, "Operation not available on the endpoint");
		ht.put(114, "Unable to parse Json content");
		ht.put(115, "Unable to parse data blob");
		ht.put(116, "Expected integer id as input");
		ht.put(117, "Expected string name as input");
		ht.put(118, "Multimedia content not present");
		ht.put(119, "Error formatting json output");
		ht.put(120, "Unused parameters in query");
		ht.put(121, "Can't determine message type");
		ht.put(122, "Can't read hand");
		ht.put(123, "Can't read object");
		ht.put(124, "Can't read face");
		ht.put(125, "Internal server error"); //not even number of arguments in doQuery(...)
		ht.put(126, "Unrecognized operation type");
		ht.put(127, "Task not present");
		ht.put(128, "Problems during password encription");
		ht.put(129, "Incorrect username or password");
		ht.put(130, "Database inconsistant Salt or Digested Password altered");
		ht.put(131, "Can't parse multimedia content");
		ht.put(132, "type change must affect also mimetype");
		ht.put(133, "type or mimetype change must affect also data");
		ht.put(134, "Unrecognized operation type");
		ht.put(135, "You are not allowed to access such resource");
		ht.put(136, "Collector phase not considered");
		ht.put(137, "Wrong type");
		ht.put(138, "Incorrect data");
		ht.put(139, "File not found");
		ht.put(140, "multimedia already present");
		ht.put(141, "only 'webcam' or 'kinect' is accepted");

	}

	public Error(int code_){
		code = code_;
		message = ht.get(code_);
	}

	public Error(String m){
		message = m;
		code = 0;
	}

	public Error (int code_, String m){
		message = m;
		code = code_;
	}

	public JSONObject toJson(){
		JSONObject jo = new JSONObject();
		try {
			jo.put("code", code);
			jo.put("message", message);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jo;
	}

	public String getMessage(){
		return message;
	}
}
