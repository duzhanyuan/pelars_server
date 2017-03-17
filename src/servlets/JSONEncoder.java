package servlets;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.json.JSONException;
import org.json.JSONObject;
 

public class JSONEncoder implements Encoder.Text<JSONObject> {

	@Override
	public void destroy() {
	}

	@Override
	public void init(EndpointConfig arg0) {
	}

	@Override
	public String encode(JSONObject obj) throws EncodeException {
		try {
			return obj.toString(4);
		} catch (JSONException e) {
			return "Error in encoding JSON";
		}
	}

}
