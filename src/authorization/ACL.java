package authorization;

import org.hibernate.Session;

import pelarsServer.User;

public class ACL implements Permissible{
	
	public long id;
	
	/**
	 * name of the operation to be performed on some data. 
	 * E.g. GETSESSION:read information about a specific session
	 */
	public String operation;
	
	/**
	 * power level of the user to perform the operation on data (can be from 1 to 3)
	 * 1:user's data, 2:user's group data, 3:all data
	 */
	public int level;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public boolean belongs(User u, Session ssession) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean belongsToGroup(User u, Session session) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
