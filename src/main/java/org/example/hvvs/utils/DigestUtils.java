package org.example.hvvs.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for cryptographic operations including MD5, SHA, and HMAC
 * 
 * @author hkq
 */

public class DigestUtils {
	
	private static final Logger LOGGER = Logger.getLogger(DigestUtils.class.getName());
	
	public static final String MD5 = "MD5", SHA1 = "SHA-1", SHA256 = "SHA-256";
	
	// Static pepper value for password hashing (additional server-side secret)
	private static final String PASSWORD_PEPPER = "HostelGuard_Static_Pepper_Value_2024";
	
	/**
	 * Encrypt using MD5 or SHA<br/>
	 * 
	 * @return Returns encrypted string in hexadecimal format, returns null if error occurs
	 */
	public static String digest(String mess, String method) {
		if (mess == null) {
			LOGGER.warning("Attempted to digest null message");
			return null;
		}
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(method);
			md.update(mess.getBytes(StandardCharsets.UTF_8));
			byte[] digest = md.digest();
			
			return bytesToHex(digest);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.log(Level.SEVERE, "Digest algorithm not found: " + method, e);
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

	/**
	 * Generates HMAC-SHA256 signature for a message using a secret key
	 * @param secret The secret key for HMAC
	 * @param message The message to authenticate
	 * @return Hexadecimal string of the HMAC, or null on error
	 */
	public static String hmacSha256(String secret, String message) {
		if (secret == null || message == null) {
			LOGGER.warning("Attempted HMAC with null secret or message");
			return null;
		}
		
		try {
			Mac hmac = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
			hmac.init(secretKey);
			byte[] hmacBytes = hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
			return bytesToHex(hmacBytes);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			LOGGER.log(Level.SEVERE, "Error generating HMAC", e);
		}
		return null;
	}
	
	/**
	 * Special HMAC-SHA256 for password hashing that includes a static pepper
	 * This provides an additional layer of security beyond the salt
	 * 
	 * @param salt User-specific salt
	 * @param password Plain text password
	 * @return Hexadecimal string of the HMAC with pepper, or null on error
	 */
	public static String hmacSha256Password(String salt, String password) {
		if (salt == null || password == null) {
			LOGGER.warning("Attempted password HMAC with null salt or password");
			return null;
		}
		
		// Include the pepper with the salt for added protection
		String secretWithPepper = salt + PASSWORD_PEPPER;
		return hmacSha256(secretWithPepper, password);
	}
	
	/**
	 * Generates a strong HMAC-SHA512 signature for highly sensitive data
	 * 
	 * @param secret The secret key for HMAC
	 * @param message The message to authenticate
	 * @return Hexadecimal string of the HMAC-SHA512, or null on error
	 */
	public static String hmacSha512(String secret, String message) {
		if (secret == null || message == null) {
			LOGGER.warning("Attempted HMAC-SHA512 with null secret or message");
			return null;
		}
		
		try {
			Mac hmac = Mac.getInstance("HmacSHA512");
			SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
			hmac.init(secretKey);
			byte[] hmacBytes = hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
			return bytesToHex(hmacBytes);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			LOGGER.log(Level.SEVERE, "Error generating HMAC-SHA512", e);
		}
		return null;
	}

	/**
	 * Converts byte array to hexadecimal string
	 */
	private static String bytesToHex(byte[] bytes) {
		char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		char[] hexChars = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			int v = bytes[i] & 0xFF;
			hexChars[i * 2] = hexDigits[v >>> 4];
			hexChars[i * 2 + 1] = hexDigits[v & 0x0F];
		}
		return new String(hexChars);
	}
}
