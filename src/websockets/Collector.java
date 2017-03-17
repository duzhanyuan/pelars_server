package websockets;

import hibernateMapping.HibernateSessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import pelarsServer.Audio;
import pelarsServer.Button;
import pelarsServer.Calibration;
import pelarsServer.Error;
import pelarsServer.Face;
import pelarsServer.Hand;
import pelarsServer.KeyLog;
import pelarsServer.Particle;
import pelarsServer.PelarsObject;
import pelarsServer.PelarsSession;
import pelarsServer.Ide;
import servlets.JSONEncoder;
import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi, Giacomo Dabisias
 * Collects all the samples from the client application, session must be specified for each connection as 
 * URL parameter. This web socket is used only for store non multimedia data.  
 *
 */
@ServerEndpoint(value = "/collector/{session_id}",
encoders = {
		JSONEncoder.class
})
public class Collector {

	public PelarsSession cur_session; 

	@OnOpen
	public void onOpen(@PathParam("session_id") long session_id, javax.websocket.Session client_session) throws IOException {

		Session session = HibernateSessionManager.getSession();
		List<PelarsSession> m_session = null;
		try {
			m_session = Util.doQuery(session, "SELECT S FROM PelarsSession as S WHERE S.id = :id", "id", session_id);
		} catch (Exception e) {
			if(session.isOpen())
				session.close();
			return;
		}

		//session_id must be valid
		if(m_session.size() == 0){
			if(session.isOpen())
				session.close();
			throw new IOException("Not valid session");
		}
		else {
			cur_session = m_session.get(0);
		}

		if(session.isOpen())
			session.close();
	}

	@OnClose
	public void onClose(javax.websocket.Session client_session) {
	}


	@OnMessage
	public void onTextMessage(String message, javax.websocket.Session client_session) throws IOException, EncodeException  {

		JSONObject obj = null;
		String type = null;

		//it can receive either a JSON object or a JSON array, both called "obj"
		try{
			obj = new JSONObject(message);
			type = obj.getJSONObject("obj").getString("type");
		}catch (JSONException e) {
			try {
				//sends always samples of the same type for each text message
				type = obj.getJSONArray("obj").getJSONObject(0).getString("type");
			} catch (JSONException e1) {
				client_session.getBasicRemote().sendObject(new Error(121).toJson());
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
				//	h.open = Boolean.parseBoolean(obj.getString("pose"));
				//h.open = true;
				h.session = cur_session;

				Util.save(session, h);
			} catch (JSONException e) {
				client_session.getBasicRemote().sendObject(new Error(122).toJson());
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

				Util.save(session, o);
			} catch (JSONException e) {
				client_session.getBasicRemote().sendObject(new Error(123).toJson());
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
					f.time = Double.parseDouble(objs.getJSONObject(i).getString("time"));
					f.session = cur_session;

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

				Util.save(session, part);
			} catch (Exception e) {
				client_session.getBasicRemote().sendObject(new Error(123).toJson());
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

				Util.save(session, part);
			} catch (Exception e) {
				client_session.getBasicRemote().sendObject(new Error(123).toJson());
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

				Util.save(session, k);
			} catch (Exception e) {
				client_session.getBasicRemote().sendObject(new Error(123).toJson());
			}
			break;
		case "audio":
			try {
				Audio a = new Audio();
				obj = new JSONObject(message).getJSONObject("obj");
				a.time = (long) Double.parseDouble(obj.getString("time"));
				a.value = Float.parseFloat(obj.getString("value"));
				a.session = cur_session;

				Util.save(session, a);
			} catch (Exception e) {
				client_session.getBasicRemote().sendObject(new Error(123).toJson());
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

				Util.save(session, ide);
			} catch (Exception e) {
				client_session.getBasicRemote().sendObject(new Error(123).toJson());
			}
			break;
		case "calibration":
			try {
				
				Calibration calibration = new Calibration();
				obj = new JSONObject(message).getJSONObject("obj");
				calibration.type = obj.getString("camera");
				calibration.session = cur_session;

				JSONArray array = obj.getJSONArray("parameters");
				List<Double> params = new ArrayList<Double>();

				for (int i = 0; i < array.length(); i++){
					params.add(array.getDouble(i));
				}

				calibration.setParameters(params);

				Util.save(session, calibration);

			} catch (Exception e) {
				//TODO: get the right calibration and update it wit distortion and intrinsics
				try{
				Calibration c = Util.doQueryUnique(session, "SELECT C FROM Calibration AS C WHERE C.session = :ses AND C.type = :cam", 
						"ses", cur_session, "cam", obj.getString("camera"));
				
				ArrayList<Double> intrinsics = new ArrayList<Double>();
				ArrayList<Double> distortion = new ArrayList<Double>();
				JSONArray narray = obj.getJSONArray("dist");
				
				for(int i=0; i<narray.length(); i++){
					distortion.add(narray.getDouble(i));
				}
				
				narray = obj.getJSONArray("intrinsics");
				for(int i=0; i<narray.length();i++){
					intrinsics.add(narray.getDouble(i));
				}
				
				c.setDistorsion_parameters(distortion);
				c.setIntrinsic_parameters(intrinsics);
					
				Util.update(session, c);
				
			}catch(JSONException e1){}}
			break;
		default:
		{
			client_session.getBasicRemote().sendObject(new Error(114).toJson());
		}
		}

		if(session.isOpen())
			session.close();
	}
}


