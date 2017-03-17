package websockets;

/**
 * 
 * @author Lorenzo Landolfi
 * keep alive thread linked to a specific aliver websocket. KeepAliver increments timer while Aliver 
 * sets the time to 0 as soon as it receives a message, if no message are received within 2 seconds 
 * session is considered dead
 *
 */
public class KeepAliver implements Runnable{

	Aliver aliver;

	public KeepAliver(Aliver a){
		aliver = a;
	}

	@Override
	public void run() {

		while(aliver.getTime() < 2000){
			try {
				Thread.sleep(500);
				aliver.setTime(aliver.getTime() + 500);
			} catch (InterruptedException e) {}
		}

		if (Aliver.online_sessions.contains(aliver.cur_session.getId()))
			Aliver.online_sessions.remove(aliver.cur_session.getId());
	}
}
