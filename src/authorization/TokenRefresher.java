package authorization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import servlets.Util;

import hibernateMapping.HibernateSessionManager;
import org.hibernate.Session;

/**
 * 
 * @author Lorenzo Landolfi
 * this class is in charge of removing from the database the expired tokens
 *
 */
public class TokenRefresher implements Runnable {

	@Override
	/**
	 * removes the expired tokens once every hour
	 */
	public void run() {

		List<String> to_remove = new ArrayList<String>();

		while(true){

			String dec = "";
			//remove from MAIN MEMORY
			try {
				Thread.sleep(3600 * 3 * 1000);
			} catch (InterruptedException e) {
			}

			HashSet<String> ref = null;
			//get the set of the tokens

			ref = TokenService.getMap();

			Iterator<String> it = ref.iterator();
			while(it.hasNext()){
				String t = it.next();
				try {
					dec = TokenService.decrypt(t);
					if(TokenService.isExpired(dec)){
						to_remove.add(t);
					}
				} catch (Exception e) {}
			}

			//must remove all in one step to avoid indexing problems
			for(String t : to_remove)
				TokenService.remove(t);

			to_remove.clear();

			//REMOVE FROM DATABASE
			Session session = HibernateSessionManager.getSession();

			List<Token> current_tokens =  Util.doQuery(session, "SELECT T FROM Token AS T");

			if(current_tokens != null){
				for (Token t : current_tokens){
					try {
						dec = TokenService.decrypt(t.value);
							if(TokenService.isExpired(dec)){
						Util.delete(session, t);
							}
					} catch (Exception e) {}
				}
			}

			if (session.isOpen()){
				session.close();
			}
		}
	}
}