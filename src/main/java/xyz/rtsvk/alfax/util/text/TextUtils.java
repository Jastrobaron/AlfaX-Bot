package xyz.rtsvk.alfax.util.text;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Class containing utility methods related to string processing
 * @author Jastrobaron
 */
public class TextUtils {

	/** Empty string constant */
	public static final String EMPTY_STRING = "";
	/** List of characters that can be used as quotation marks */
	public static final List<Character> QUOTES = List.of('"', '\'');

	/**
	 * Generates a string containing random alphanumeric characters
	 * @param n string length
	 * @return the random string
	 */
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

	/**
	 * Calculates the hash of a string
	 * @param input string to be encrypted
	 * @return SHA-512 hash of the input string as hex string
	 * @throws NoSuchAlgorithmException if the SHA-512 hashing algorithm does not exist (should not happen)
	 */
	public static String hash(String input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		md.update(input.getBytes());
		BigInteger hash = new BigInteger(1, md.digest());
		StringBuilder result = new StringBuilder(hash.toString(16));
		while(result.length() < 128) {
			result.insert(0, "0");
		}
		return result.toString();
	}

	/**
	 * Creates a formatted string, use `${i}` (where `i` represents the argument index)
	 * to reference the arguments within the format.
	 * @param format the format
	 * @param args arguments
	 * @return the formatted string
	 */
	public static String format(String format, Object... args) {
		FormattedString fmtStr = FormattedString.create(format);
		for (int i = 0; i < args.length; i++) {
			fmtStr.addParam(i + "", args[i]);
		}
		return fmtStr.build();
	}

	/**
	 * Matches the string to the pattern
	 * @param string the string
	 * @param pattern pattern
	 * @return true if string matches the pattern, false otherwise
	 */
	public static boolean match(String string, String pattern) {
		int patternIdx = 0;
		for (int i = 0; i < string.length(); i++) {
			char character = string.charAt(i);
			char patternChar = pattern.charAt(patternIdx);

			if (patternChar == character) patternIdx++;
			else if (patternChar == '*') {
				if (patternIdx < pattern.length() - 1 && string.charAt(i+1) == pattern.charAt(patternIdx+1)) patternIdx++;
			}
			else return false;
		}
		return true;
	}

	/**
	 * Removes quotation marks from the string (if quoted), silently do nothing when the string is not quoted
	 * @param string to remove quotes from
	 * @return string with removed quotation marks
	 */
	public static String removeQuotes(String string) {
		for (Character quoteMk : QUOTES) {
			String quotes = quoteMk.toString();
			if (string.startsWith(quotes) && string.endsWith(quotes)) {
				int qLen = quotes.length();
				return string.substring(qLen, string.length() - qLen);
			}
		}
		return string;
	}
}
