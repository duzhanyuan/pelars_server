package authorization;

import hibernateMapping.HibernateSessionManager;

import java.util.HashSet;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.hibernate.Session;

import org.apache.commons.codec.binary.Base64;

import servlets.Util;

/**
 * @author: Lorenzo Landolfi
 * A simple text cipher to encrypt/decrypt a string. 
 */
public class TokenService {
	
	private static byte[] linebreak = {}; // Remove Base64 encoder default linebreak
	private static String secret = "lwre51agq5hf1231"; // secret key length must be 16
	private static SecretKey key;
	private static Cipher cipher;
	private static Base64 coder;
	
	public final static int expire = 3600 * 12 * 1000; //token life time in milliseconds

	//public static ArrayList<Token> token_list;
	private static HashSet<String> token_list = new HashSet<String>();

	
	static{
		try{
			key = new SecretKeySpec(secret.getBytes(), "AES");
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
			coder = new Base64(32,linebreak,true);
			
			Thread refresher = new Thread(new TokenRefresher());
			refresher.start();
			
		}catch (Throwable t){
			t.printStackTrace();
		}
	}
	
	/**
	 * Acquires the set of encrypted tokens 
	 */
	public static synchronized HashSet<String>getMap(){
	
		return token_list;
	}

	/**
	 * produces a token from the IP address of the request and from the role of the user and saves it in the db
	 * @param ip IP address of the request
	 * @param role role of the user
	 * @param email user's email
	 * @return the generated token
	 * @throws InterruptedException
	 */
	public static synchronized Token produceToken(String ip, String role, String email) throws InterruptedException{
		
		Token mtoken = new Token(ip, role, email);

		token_list.add(mtoken.getValue());

		//here must save token in database
		Session session = HibernateSessionManager.getSession();
		Util.save(session, mtoken);

		if(session.isOpen()){
			session.close();
		}
		
		return mtoken;
	}

	/**
	 * Checks if the token is still valid
	 * @param to_check string embodying the token
	 * @param ip IP address of the request
	 * @return true if to_check is a valid token, false elsewhere
	 * @throws Exception
	 */
	public static synchronized boolean isValid(String to_check, String ip) throws Exception{
	
		//TODO: probably don't needed
		if(token_list.contains(to_check)){
			String dec = decrypt(to_check);
			if(!isExpired(dec) && checkIp(ip,dec))
				return true;
		}

		//check also in database
		Session session = HibernateSessionManager.getSession();

		Token tc = Util.doQueryUnique(session, "SELECT T FROM Token AS T WHERE T.value = :to_c", "to_c", to_check);
		boolean valid = false;

		if(tc != null){
			String dec = decrypt(to_check);
			if(!isExpired(dec) && checkIp(ip,dec)){
				valid = true;
			}
		}
		
		if(session.isOpen()){
			session.close();
		}
		
		return valid;
	}

	/**
	 * checks whether the request and the IP address of the token coming with it are equal
	 * @param ip IP address of the request
	 * @param token token carried by the request
	 * @return true if are equal, false elsewhere
	 * @throws Exception
	 */
	private static boolean checkIp(String ip, String token) throws Exception{

		token = token.substring(token.indexOf('|')+1);
		token = token.substring(0, token.indexOf('|'));

		return token.equals(ip);
	}
	
	/**
	 * removes the token from the list of valid tokens in main memory
	 * @param t token to be removed
	 */
	public static synchronized void remove (String t){

		token_list.remove(t);	
	}
	
	/**
	 * checks whether the token is expired
	 * @param dec the token as a string
	 * @return true if the dec token is expired, false elsewhere
	 * @throws Exception
	 */
	public static synchronized boolean isExpired(String dec) throws Exception{
		return System.currentTimeMillis() - getTime(dec) > expire;
	}
	
	/**
	 * retrieves the time information of the token
	 * @param token
	 * @return the epoch time in which the token has been created
	 * @throws Exception
	 */
	private static long getTime(String token) throws Exception{
		// here we must separate the random part from the time stamp
		return Long.parseLong(token.substring(token.lastIndexOf('|') + 1));
	}
	
	/**
	 * encrypts a String with AES/ECB/PKCS5Padding
	 * @param plainText String to ben encoded 
	 * @return the encrypted String
	 * @throws Exception
	 */
	public static String encrypt(String plainText) throws Exception{
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipherText = cipher.doFinal(plainText.getBytes());
		return  new String(coder.encode(cipherText));
	}
	
	/**
	 * Decodes an encrypted String
	 * @param codedText sequence to be decoded
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String codedText) throws Exception{
		byte[] encypted = coder.decode(codedText.getBytes());
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] decrypted = cipher.doFinal(encypted);  
		return new String(decrypted);
	}
}