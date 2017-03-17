
package authorization;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;

public final class PasswordService{

	/**
	decodes the password according to the sequence of bytes salt
	 */
	public static byte[] getHash(String password, byte[] salt) throws NoSuchAlgorithmException, UnsupportedEncodingException{
	       MessageDigest digest = MessageDigest.getInstance("SHA-256");
	       digest.reset();
	       digest.update(salt);
	       return digest.digest(password.getBytes("UTF-8"));
	}

	/**
	 * From a base 64 representation, returns the corresponding byte[] 
	 * @param data String The base64 representation
	 * @return byte[]
	 * @throws IOException
	 */
	public static byte[] base64ToByte(String data) throws IOException{
		Base64 decoder = new Base64();
		byte[] saltArray = decoder.decode(data);
		return saltArray;
	}

	/**
	 * From a byte[] returns a base 64 representation
	 * @param data byte[]
	 * @return String
	 * @throws IOException
	 */
	public static String byteToBase64(byte[] data){
		Base64 encoder = new Base64();
		return encoder.encodeToString(data);
	}
}
