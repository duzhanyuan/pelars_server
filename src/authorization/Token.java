package authorization;

import java.util.UUID;


/**
 * @author Lorenzo Landolfi
 * class representing access tokens. in its field they are stored encrypted: a long number, user's IP address, role and a timestamp
 */
public class Token{

	public long t_id;
	
	/**
	 * value is always the actual encrypted value
	 */
	public String value;
	
	public Token(){}

	public Token(String s){
		value = s;
	}

	public Token(String ip, String role, String email){
		setValue(ip, role, email);
	}

	public long getT_id(){
		return t_id;
	}

	public void setT_id(long id){
		this.t_id = id;
	}

	public String getValue(){
		return value;
	}

	public void setValue(String value){
		this.value = value;
	}

	public boolean equals(Token t){
		return value.equals(t.getValue());
	}
	
	/**
	 * 
	 * @return the IP address from the encrypted value
	 * @throws Exception
	 */
	public String getIp() throws Exception{

		String ip = TokenService.decrypt(value);

		ip = ip.substring(ip.indexOf('|')+1);
		ip = ip.substring(0, ip.indexOf('|'));
		return ip;
	}
	
	/**
	 * 
	 * @return the role from the encrypted value
	 * @throws Exception
	 */
	public String getRole() throws Exception{

		String role = TokenService.decrypt(value);

		role = role.substring(role.indexOf('|')+1);
		role = role.substring(role.indexOf('|')+1);
		role = role.substring(0, role.indexOf('|'));
		return role;
	}

	/**
	 * 
	 * @return the user id from the encrypted value
	 * @throws Exception
	 */
	public String getId() throws Exception{
		String id = TokenService.decrypt(value);

		id = id.substring(id.indexOf('|')+1);
		id = id.substring(id.indexOf('|')+1);
		id = id.substring(id.indexOf('|')+1);
		id = id.substring(0, id.indexOf('|'));
		return id;
	}
	
	/**
	 * concatenates the parameters as a String separated by '|', encrypts it and set value
	 * @param ip the IP address of the machine the message is coming from
	 * @param role role of the user associated to the request
	 * @param email email identifying the user
	 */
	public void setValue(String ip, String role, String email){

		Long time = System.currentTimeMillis();
		//string to be encrypted is: x | ip | role | id | timestamp
		String key = UUID.randomUUID().toString().toUpperCase() + 
				"|" + ip +
				"|" + role +
				"|" + email +
				"|" +  time.toString();
		try {
			value = TokenService.encrypt(key);
		} catch (Exception e) { }
	}
}
