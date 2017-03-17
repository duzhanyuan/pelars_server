package pelarsServer;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;

import authorization.PasswordService;
import authorization.Permissible;

/**
 * 
 * @author Lorenzo Landolfi, Giacomo Dabisias
 * class representing information about a user. Password are stored encrypted with SHA-256 using random seed 
 * for each user
 *
 */
public class User implements Permissible{

	public long id;
	public String role;
	public String name;
	public String affiliation;
	//here we can have a list of namespaces or research groups
	public String namespace;
	public String email;
	private String password;
	private String salt;

	public String getEmail(){
		return email;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getPassword(){
		return password;
	}

	public void setPassword(String s){
		password = s;
	}

	public void encryptPassword(String s) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		// Uses a secure Random not a simple Random
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		// Salt generation 64 bits long
		byte[] bSalt = new byte[8];
		random.nextBytes(bSalt);
		// Digest computation
		byte[] bDigest = PasswordService.getHash(s, bSalt);
		password = PasswordService.byteToBase64(bDigest);
		salt = PasswordService.byteToBase64(bSalt);
	}

	public String getSalt(){
		return salt;
	}

	public void setSalt(String s){
		salt = s;
	}

	public String getRole(){
		return role;
	}

	public void setRole(String role){
		this.role = role;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getAffiliation(){
		return affiliation;
	}

	public void setAffiliation(String affiliation){
		this.affiliation = affiliation;
	}

	public long getId(){
		return id;
	}
	public void setId(long id){
		this.id = id;
	}

	public String getNamespace(){
		return namespace;
	}

	public void setNamespace(String namespace){
		this.namespace = namespace;
	}

	public List<PelarsSession >getMySessions(){
		return null;
	}

	public void setMySessions(List<PelarsSession> m){

	}

	public JSONObject toJson(){
		JSONObject jo = new JSONObject();
		try{
			jo.put("name", name);
			jo.put("role", role);
			jo.put("affiliation", affiliation);
			jo.put("identifier", id);
			jo.put("namespace", namespace);
			jo.put("email" , email);

		}catch (JSONException e){
			e.printStackTrace();
		}
		return jo;
	}

	public boolean equals(User other){
		//id is enough?
		return other.getId() == this.getId();
	}

	@Override
	public boolean belongs(User u, Session session) {

		//must actually check whether u = this;
		return this.equals(u);
	}

	@Override
	public boolean belongsToGroup(User u, Session session) {

		return this.getNamespace().equals(u.getNamespace());
	}
}