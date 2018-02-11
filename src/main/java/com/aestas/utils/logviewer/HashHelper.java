package com.aestas.utils.logviewer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class HashHelper {
	public static String getHashString(String string) {
		
		String encryptedString = "";
		if(string != null && string.trim().length() > 0) {
			MessageDigest messageDigest;
			try {
				messageDigest = MessageDigest.getInstance("SHA-256");
				byte[] hashByteArray = messageDigest.digest(string.getBytes());
				encryptedString = byteArray2Hex(hashByteArray);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		
		return encryptedString;
	}
	private static String byteArray2Hex(final byte[] hash) {
	    Formatter formatter = new Formatter();
	    for (byte b : hash) {
	        formatter.format("%02x", b);
	    }
	    return formatter.toString();
	}
}
