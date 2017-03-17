package servlets;

import hibernateMapping.HibernateSessionManager;

import java.io.IOException;
import java.util.List;

import org.hibernate.Session;

import javax.websocket.EncodeException;
import javax.websocket.OnMessage;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONArray;


import pelarsServer.Error;
import pelarsServer.Face;
import pelarsServer.Hand;
import websockets.Collector;

/**
 * Collects all the samples from the client application, session must be specified for each connection
 */
@ServerEndpoint(value = "/stream/{session_id}",
encoders = {
		JSONEncoder.class
})
public class Stream extends Collector {


	@OnMessage
	public void onTextMessage(String message, javax.websocket.Session client_session) throws IOException, EncodeException  {

		Session session = HibernateSessionManager.getSession();

		List<Face> faces = null;
		List<Hand> hands = null;

		try {
			faces = Util.doQuery(session, "SELECT F FROM Face AS F WHERE F.session = :ses", "ses", cur_session);
			hands = Util.doQuery(session, "SELECT H FROM Hand AS H WHERE H.session = :ses", "ses", cur_session);
		} catch (Exception e) {
			client_session.getBasicRemote().sendText("eccezione");
		}

		if(session.isOpen())
			session.close();

		JSONArray jsa = new JSONArray();
		jsa.put(faces);
		jsa.put(hands);
		
		client_session.getBasicRemote().sendText("ciao");
		client_session.getBasicRemote().sendObject(new Error(124).toJson());
		client_session.getBasicRemote().sendText(jsa.toString());

	}
}


