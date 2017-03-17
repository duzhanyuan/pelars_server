package authorization;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Lorenzo Landolfi
 * this class has the purpose of implementing access control policy to 
 * users roles
 *
 */
public class ACL_Rule extends ACL{
	
	/**
	 * user's role
	 */
	public String role;

	public ACL_Rule(){}

	public ACL_Rule(String role, String operation, int level){

		this.role = role;
		this.operation = operation;
		this.level = level;
	}

	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}


	public JSONObject toJson(){
		JSONObject js = new JSONObject();

		try {
			js.put("role", role);
			js.put("operation", operation);
			js.put("level", level);
		} catch (JSONException e) {
		}

		return js;
	}

}
