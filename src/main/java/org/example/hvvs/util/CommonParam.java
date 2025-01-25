package org.example.hvvs.util;

/**
 * Defines constants for cookie and session parameters
 * 
 * @author hkq
 *
 */

public class CommonParam {
	/* ================= Session attribute names and values =================== */
	public static final String SESSION_SELF = "self";				// Stores User or Admin object
	public static final String SESSION_ROLE = "role";				// Role type
	public static final String SESSION_ROLE_RESIDENT = "RESIDENT";
	public static final String SESSION_ROLE_SECURITY_STAFF = "SECURITY_STAFF"; 
	public static final String SESSION_ROLE_MANAGING_STAFF = "MANAGING_STAFF";
	
	/* ================= Cookie related =================== */
	public static final String COOKIE_AUTO_LOGIN = "AUTO_LOGIN";	// Auto login cookie name
}
