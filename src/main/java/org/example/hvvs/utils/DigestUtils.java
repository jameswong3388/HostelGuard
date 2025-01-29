package org.example.hvvs.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for MD5 and SHA encryption
 * 
 * @author hkq
 */

public class DigestUtils {
	
	public static final String MD5 = "MD5", SHA1 = "SHA-1", SHA256 = "SHA-256"; 
	
	/**
	 * Encrypt using MD5 or SHA<br/>
	 * 
	 * @return Returns encrypted string in hexadecimal format, returns null if error occurs
	 */
	public static String digest(String mess, String method) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(method);
			md.update(mess.getBytes());
			byte[] digest = md.digest();
			
			// Convert digest to hexadecimal string format
			char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',  
	                'A', 'B', 'C', 'D', 'E', 'F' };
			char[] result = new char[digest.length * 2]; 
			int k = 0;  
            for (int i = 0; i < digest.length; i++) { 
                byte byte0 = digest[i];
                result[k++] = hexDigits[byte0 >>> 4 & 0xf];
                result[k++] = hexDigits[byte0 & 0xf];
            }  
			
            return new String(result);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Encrypt using SHA-256<br/>
	 * 
	 * @return Returns encrypted string in hexadecimal format, returns null if error occurs
	 */
	public static String sha256Digest(String mess) {
		return digest(mess, SHA256);
	}
}
