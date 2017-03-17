package authorization;


/**
 * 
 * @author Lorenzo Landolfi
 * this class defines access control policy for users
 *
 */
public class ACL_RuleUser extends ACL{
	
	/**
	 * ID of the user accessing data
	 */
	public long user_id;


	public long getUser_id() {
		return user_id;
	}
	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}
}
