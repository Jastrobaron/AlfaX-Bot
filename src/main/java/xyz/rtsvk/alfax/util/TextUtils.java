package xyz.rtsvk.alfax.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TextUtils {

	public static String getRandomString(int n) {
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
				+ "0123456789"
				+ "abcdefghijklmnopqrstuvxyz";
		StringBuilder sb = new StringBuilder(n);

		for (int i = 0; i < n; i++) {
			int index = (int)(alphabet.length() * Math.random());
			sb.append(alphabet.charAt(index));
		}

		return sb.toString();
	}

	public static String hash(String input) throws NoSuchAlgorithmException {
		String result = input;
		if(input != null) {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(input.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			result = hash.toString(16);
			while(result.length() < 128) {
				result = "0" + result;
			}
		}
		return result;
	}
}
