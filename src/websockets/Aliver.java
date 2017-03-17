package websockets;


import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;


import servlets.JSONEncoder;

/**
 * 
 */
@ServerEndpoint(value = "/aliver/{session_id}",
encoders = {
		JSONEncoder.class
})

/**
 * 
 * @author Lorenzo Landolfi, Giacomo Dabisias
 *  websocket used to store keep alive information of a session. 
 *  If no data is received within 2 seconds, session is considered dead
 *
 */

public class Aliver extends Collector {

	//must be synchronized
	public static Set<Long> online_sessions = Collections.synchronizedSet(new HashSet<Long>());

	public long time;	
	Thread survivor;

	private boolean first_data = true;

	public synchronized void setTime(long t){
		time = t;
	}

	public synchronized long getTime(){
		return time;
	}



	@OnOpen
	public void onOpen(@PathParam("session_id") long session_id, javax.websocket.Session client_session) throws IOException {

		//check the session is valid
		super.onOpen(session_id, client_session);

		first_data = true;

	}

	@OnMessage
	public void onTextMessage(String message, javax.websocket.Session client_session) throws IOException, EncodeException  {

		//client_session.getBasicRemote().sendText(String.valueOf(time));

		//update list of online sessions
		if(!online_sessions.contains(this.cur_session.getId()))
			online_sessions.add(this.cur_session.getId());	

		setTime(0);

		if(first_data){

			//initialize new timer thread
			survivor = (new Thread( new KeepAliver(this)));

			survivor.start();
			
			first_data = false;
		}

	}

	@OnClose
	public void onClose(javax.websocket.Session client_session) {

		online_sessions.remove(this.cur_session.getId());

	}


}
