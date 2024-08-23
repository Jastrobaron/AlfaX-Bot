package xyz.rtsvk.alfax.util.text;

import xyz.rtsvk.alfax.util.storage.FileManager;
import xyz.rtsvk.alfax.util.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to manage messages from language files
 * @author Jastrobaron
 */
public class MessageManager {

	/** Logger */
	private static final Logger logger = new Logger(MessageManager.class);
	/** Message manager cache */
	private static final Map<String, MessageManager> cache = new HashMap<>();
	/** ID of the default language */
	private static MessageManager defaultLanguage;
	/** Flag to specify whether to force the use of the default language */
	private static boolean forceDefaultLanguage;

	/** The message map */
	private final Map<String, String> messages;

	/**
	 * @param key the key of the message
	 * @return the message
	 */
	public String getMessage(String key) {
		return this.messages.getOrDefault(key, key);
	}

	/**
	 * Gets all messages that match a key pattern
	 * @param key key pattern to match
	 * @return the message
	 */
	public List<String> matchMessages(String key) {
		return this.messages.keySet().stream().filter(k -> k.matches(key)).toList();
	}

	/**
	 * Formats a message with arguments
	 * @param key the key of the message
	 * @param args the arguments
	 * @return the formatted message
	 */
	public String formatMessage(String key, Object... args) {
		String message = this.getMessage(key);
		return TextUtils.format(message, args);
	}

	/**
	 * Formats a message with arguments
	 * @param key the key of the message
	 * @return the formatted message
	 */
	public FormattedString getFormattedString(String key) {
		return FormattedString.create(this.getMessage(key));
	}

	/**
	 * Returns a map of messages from a language file.
	 * @param language the language code
	 * @return a map of messages
	 * @throws IOException if an error occurred while reading the file
	 * @throws IllegalArgumentException if the language file was not found
	 */
	public static MessageManager getMessages(String language) throws IOException {
		if (forceDefaultLanguage) {
			return defaultLanguage;
		} else if (cache.containsKey(language)) {
			return cache.get(language);
		}

		InputStream resource = MessageManager.class.getClassLoader().getResourceAsStream("languages/lang_" + language + ".properties");
		if (resource == null) {
			try {
				resource = new FileInputStream(FileManager.getLangFile(language));
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Language file not found: " + language);
			}
		}

		logger.info("Loading non-cached language file: " + language);

		byte[] buffer = new byte[256];
		StringBuilder content = new StringBuilder();
		while (resource.available() > 0) {
			int read = resource.read(buffer);
			content.append(new String(buffer, 0, read));
		}
		resource.close();

		String[] lines = content.toString().split("\n");
		MessageManager manager = new MessageManager();
		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].trim();
			int eqIdx = lines[i].indexOf('=');
			if (eqIdx == -1) {
				continue;
			}
			String key = lines[i].substring(0, eqIdx);
			String value = lines[i].substring(eqIdx + 1);
			manager.messages.put(key, value);
		}

		cache.put(language, manager);
		return manager;
	}

	/**
	 * Get the default language pack
	 * @return the default language pack
	 */
	public static MessageManager getDefaultLanguage() {
		return defaultLanguage;
	}

	/**
	 * Set the default language
	 * @param language the default language
	 * @throws IOException if an error occurs when loading the default language
	 */
	public static void setDefaultLanguage(String language) throws IOException {
		defaultLanguage = getMessages(language);
	}

	/**
	 * Set whether to force the default language to be used
	 * @param force whether to force the use of the default language
	 */
	public static void setForceDefaultLanguage(boolean force) {
		forceDefaultLanguage = force;
	}

	/**
	 * Clears the cache
	 */
	public static void clearCache() {
		cache.clear();
	}

	/**
	 * Private class constructor.
	 */
	private MessageManager() {
		this.messages = new HashMap<>();
	}
}
