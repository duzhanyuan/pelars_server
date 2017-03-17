package authorization;

import org.hibernate.Session;

import pelarsServer.User;

/**
 * 
 * @author Lorenzo Landolfi
 * this class declares the interface for the access control policy
 *
 */

public interface Permissible extends Cloneable{
	
	public abstract boolean belongs(User u, Session ssession);
	
	public abstract boolean belongsToGroup(User u, Session session);


}
