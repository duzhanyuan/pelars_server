package websockets;

import hibernateMapping.HibernateSessionManager;

import java.io.IOException;
import java.util.List;

import javax.websocket.EncodeException;
import javax.websocket.OnMessage;
import javax.websocket.server.ServerEndpoint;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Audio;
import pelarsServer.BaseData;
import pelarsServer.Button;
import pelarsServer.Error;
import pelarsServer.Face;
import pelarsServer.Hand;
import pelarsServer.Ide;
import pelarsServer.KeyLog;
import pelarsServer.Particle;
import pelarsServer.PelarsObject;
import servlets.JSONEncoder;
import servlets.Util;


@ServerEndpoint(value = "/upload/{session_id}",
encoders = {
		JSONEncoder.class
})

/**
 * 
 * @author Lorenzo Landolfi, Giacomo Dabisias
 * This websocket implementation is used to collect data from a broken session. It has exactly the same behaviour 
 * of the Collector websocket but for each data, it checks whether it is already present in the database 
 *
 */

public class Upload extends Collector{


	private boolean alreadyIs(BaseData b, String type) throws IOException {

		type = type.substring(0,1).toUpperCase() + type.substring(1);
		Session session = HibernateSessionManager.getSession();
		List<BaseData> a = null;
		try {
			a = Util.doQuery(session, "SELECT B from " + type + " AS" +
					" B WHERE B.time = :time AND B.session = :s_id","time",b.time,"s_id",cur_session);
		} catch (Exception e) {
			throw new IOException();
		}

		if(session.isOpen())
			session.close();

		if (a != null && a.size() > 0){
			for(BaseData base : a){
				if (base.equals(b)){
					return true;
				}
			}
		}

		return false;
	}


	@OnMessage
	public void onTextMessage(String message, javax.websocket.Session client_session) throws IOException, EncodeException{

		JSONObject obj = null;
		String type = null;
		boolean is_array = false;
		JSONArray current_a = null;

		//it can receive either a JSON object or a JSON array, both called "obj"
		try{
			obj = new JSONObject(message);
			type = obj.getJSONObject("obj").getString("type");
		}catch (JSONException e) {
			try {
				is_array = true;
				//sends always samples of the same type for each text message
				current_a =  obj.getJSONArray("obj");
				type = current_a.getJSONObject(0).getString("type");
			} catch (JSONException e1) {
				String to_append = "";
				if (is_array){
					to_append = current_a.toString();
				}
				else {
					to_append = obj.toString();
				}
				client_session.getBasicRemote().sendText("Error parsing one JSON object: " + to_append);
				return;
			}
		}

		Session session = HibernateSessionManager.getSession();
		//parses according to the "type" field
		switch (type) {
		case "hand":
			try {
				Hand h = new Hand();
				obj = new JSONObject(message).getJSONObject("obj");
				h.num = obj.getInt("id");
				h.tx = Float.parseFloat(obj.getString("tx"));
				h.ty = Float.parseFloat(obj.getString("ty"));
				h.tz = Float.parseFloat(obj.getString("tz"));
				h.rx = Float.parseFloat(obj.getString("rx"));
				h.ry = Float.parseFloat(obj.getString("ry"));
				h.rz = Float.parseFloat(obj.getString("rz"));
				h.rw = Float.parseFloat(obj.getString("rw"));
				h.time = (long) Double.parseDouble(obj.getString("time"));

				h.session = cur_session;

				if(!alreadyIs(h,type))
					Util.save(session, h);


			} catch (JSONException e) {
				client_session.getBasicRemote().sendText("Error parsing one JSON object: " );
			}
			break;
		case "object":
			try {
				PelarsObject o = new PelarsObject();
				obj = new JSONObject(message).getJSONObject("obj");
				o.num = obj.getInt("id");
				o.pos_x = Float.parseFloat(obj.getString("x"));
				o.pos_y = Float.parseFloat(obj.getString("y"));
				o.pos_z = Float.parseFloat(obj.getString("z"));
				o.time = (long) Double.parseDouble(obj.getString("time"));
				o.session = cur_session;

				if(!alreadyIs(o,type))
					Util.save(session, o);

			} catch (JSONException e) {
				client_session.getBasicRemote().sendText("Error parsing one JSON object: " );
			}
			break;

		case "face":

			try {
				JSONArray objs = new JSONObject(message).getJSONArray("obj");
				for (int i = 0; i < objs.length(); ++i){
					Face f = new Face();
					f.num = objs.getJSONObject(i).getInt("id");
					f.pos_x0 = Float.parseFloat(objs.getJSONObject(i).getString("x"));
					f.pos_x1 = Float.parseFloat(objs.getJSONObject(i).getString("x1"));
					f.pos_y0 = Float.parseFloat(objs.getJSONObject(i).getString("y"));
					f.pos_y1 = Float.parseFloat(objs.getJSONObject(i).getString("y1"));
					f.pos_y2 = Float.parseFloat(objs.getJSONObject(i).getString("y2"));
					f.pos_x2 = Float.parseFloat(objs.getJSONObject(i).getString("x2"));
					f.pos_z0 = Float.parseFloat(objs.getJSONObject(i).getString("z"));
					f.pos_z1 = Float.parseFloat(objs.getJSONObject(i).getString("z1"));
					f.pos_z2 = Float.parseFloat(objs.getJSONObject(i).getString("z2"));
					f.distance = Double.parseDouble(objs.getJSONObject(i).getString("distance"));
					f.time = (long) Double.parseDouble(objs.getJSONObject(i).getString("time"));
					f.session = cur_session;

					if (!alreadyIs(f,type))
						Util.save(session, f);
				}
			} catch (JSONException e) {
				client_session.getBasicRemote().sendObject(new Error(124).toJson());
			}

			break;
		case "particle":
			try {
				Particle part = new Particle();
				obj = new JSONObject(message).getJSONObject("obj");
				part.name = obj.getString("name");
				part.data = Hibernate.getLobCreator(session).createBlob(obj.getString("data").getBytes());
				part.time = (long) Double.parseDouble(obj.getString("time"));
				part.session = cur_session;

				if(!alreadyIs(part,type))
					Util.save(session, part);

			} catch (Exception e) {
				client_session.getBasicRemote().sendText("Error parsing one JSON object: " );
			}
			break;
		case "button":
			try {
				Button part = new Button();
				obj = new JSONObject(message).getJSONObject("obj");
				part.name = obj.getString("name");
				part.data = Hibernate.getLobCreator(session).createBlob(obj.getString("data").getBytes());
				part.time = (long) Double.parseDouble(obj.getString("time"));
				part.session = cur_session;

				if(!alreadyIs(part,type))
					Util.save(session, part);

			} catch (Exception e) {
				client_session.getBasicRemote().sendText("Error parsing one JSON object: " );
			}
			break;
		case "logkey":
			try {
				KeyLog k = new KeyLog();
				obj = new JSONObject(message).getJSONObject("obj");
				k.time = (long) Double.parseDouble(obj.getString("time"));
				k.activity = obj.getString("activity");
				k.session = cur_session;
				k.timestamp = obj.getDouble("timestamp");

				if(!alreadyIs(k,type))
					Util.save(session, k);

			} catch (Exception e) {
				client_session.getBasicRemote().sendText("Error parsing one JSON object: " );
			}
			break;
		case "audio":
			try {
				Audio a = new Audio();
				obj = new JSONObject(message).getJSONObject("obj");
				a.time = (long) Double.parseDouble(obj.getString("time"));
				a.value = Float.parseFloat(obj.getString("value"));
				a.session = this.cur_session;

				if(!alreadyIs(a,type))
					Util.save(session, a);
			} catch (Exception e) {
				client_session.getBasicRemote().sendText("Error parsing one JSON object: " );
			}
			break;
		case "ide":
			try {
				Ide ide = new Ide();
				obj = new JSONObject(message).getJSONObject("obj");
				ide.time = (long) Double.parseDouble(obj.getString("time"));
				ide.opt = obj.getString("opt");
				ide.action_id = obj.getString("action_id");
				ide.session = cur_session;

				if(!alreadyIs(ide,type))
					Util.save(session, ide);

			} catch (Exception e) {
				client_session.getBasicRemote().sendText("Error parsing one JSON object: " );
			}
			break;
		default:
		{
			client_session.getBasicRemote().sendText("Error parsing one JSON object: " );
		}
		}

		if(session.isOpen())
			session.close();
	}

}
