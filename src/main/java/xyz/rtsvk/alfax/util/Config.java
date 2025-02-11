package xyz.rtsvk.alfax.util;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Class responsible for handling the input configuration
 * @author Jastrobaron
 */
public class Config extends LinkedHashMap<String, Object> {


	/** Command line argument prefix */
	private static final String ARG_PREFIX = "--";

	/** Key-value separator sequence */
	private static final String ARG_ASSIGN = "=";

	/** Comment prefix */
	private static final String COMMENT = "#";

	/** Logger tag */
	private static final String TAG = "Config";

	/**
	 * Creates the configuration from the command line arguments
	 * @param args command line arguments
	 * @return parsed configuration
	 */
	public static Config fromCommandLineArgs(String[] args) {
		Config cfg = new Config();

		// parse input args
		cfg.putAll(parse(args, true));

		if (cfg.containsKey("config")) {
			String fileName = String.valueOf(cfg.remove("config"));
			File file = new File(fileName);

			if (!file.exists()) {
				Logger.warn(TAG, "Specified config file does not exist. Skipping...");
				return cfg;
			}

			try (Scanner reader = new Scanner(new FileInputStream(file))) {
				StringBuilder content = new StringBuilder();
				while (reader.hasNextLine())
					content.append(reader.nextLine()).append("\n");
				Config fileCfg = parse(content.toString().split("\n"), false);
				fileCfg.forEach(cfg::putIfAbsent);
			}
			catch (Exception e) {
				Logger.error(TAG, "Error loading the config file: " + e.getMessage());
			}
		}

		return cfg;
	}

	/**
	 * Parses the arguments into a valid config
	 * @param args input arguments
	 * @param checkPrefix whether to check for argument prefix ('--')
	 * @return the parsed config
	 */
	private static Config parse(String[] args, boolean checkPrefix) {
		Config cfg = new Config();

		for (String arg : args) {
			if (arg.isEmpty()) continue;

			if (checkPrefix) {
				if (!arg.startsWith(ARG_PREFIX)) continue;
				else arg = arg.substring(ARG_PREFIX.length());
			}
			else if (arg.startsWith(COMMENT)) continue;

			int commentIdx = arg.indexOf(COMMENT);
			commentIdx = commentIdx == -1 ? arg.length() : commentIdx;

			int eqIdx = arg.indexOf(ARG_ASSIGN);
			if (eqIdx == -1) {
				cfg.put(arg.substring(0, commentIdx), "");
			}
			else {
				String key = arg.substring(0, eqIdx);
				String value = arg.substring(eqIdx + 1, commentIdx);

				cfg.put(key, value);
			}
		}

		return cfg;
	}

	/**
	 * Loads the configuration from the default-config.properties resource
	 * @return default configuration
	 * @throws IOException if the resource cannot be read
	 */
	public static Config defaultConfig() throws IOException {
		StringBuilder raw = new StringBuilder();
		try (InputStream stream = Config.class.getResourceAsStream("/default-config.properties")) {
			if (stream == null) throw new FileNotFoundException("Default config file not found!");

			byte[] buffer = new byte[64];
			int read;
			while ((read = stream.read(buffer)) != -1) {
				raw.append(new String(buffer, 0, read).trim());
			}
		}

		return parse(raw.toString().split("\n"), false);
	}

	/**
	 * Copies the default-config.properties resource to a new file to generate a file with the default configuration
	 * @param filename name of the new file
	 * @throws IOException if the resource cannot be read or the output file cannot be written to
	 */
	public static void copyDefaultConfig(String filename) throws IOException {
		InputStream resource = Config.class.getResourceAsStream("default-config.properties");
		if (resource == null) throw new FileNotFoundException("Default config file not found!");

		FileOutputStream outFile = new FileOutputStream(filename);

		byte[] buffer = new byte[64];
		int read;
		while ((read = resource.read(buffer)) != -1) {
			outFile.write(buffer, 0, read);
		}

		outFile.close();
		resource.close();
	}

	/**
	 * Writes the configuration object into a config file
	 * @param filename name of the file
	 * @throws IOException if the file cannot be written to
	 */
	public void write(String filename) throws IOException {
		FileWriter writer = new FileWriter(filename);
		for (Map.Entry<String, Object> entry : this.entrySet()) {
			writer.append(entry.getKey());
			writer.append("=");
			writer.append(entry.getValue().toString());
			writer.append("\n");
		}
		writer.close();
	}

	/**
	 * Returns the string value of the config entry
	 * @param key key to the value
	 * @param def default value to return if the key is not found
	 * @return string value of the entry
	 */
	public String getStringOrDefault(String key, String def) {
		return String.valueOf(this.getOrDefault(key, def)).trim();
	}

	/**
	 * Returns the string value of the config entry
	 * @param key key to the value
	 * @return string value of the entry
	 */
	public String getString(String key) {
		return getStringOrDefault(key, null);
	}

	/**
	 * Returns the integer value of the config entry
	 * @param key key to the value
	 * @param def default value to return if the key is not found
	 * @return integer value of the entry
	 */
	public int getIntOrDefault(String key, int def) {
		Object value = this.get(key);
		return value != null ? Integer.parseInt(value.toString()) : def;
	}

	/**
	 * Returns the integer value of the config entry
	 * @param key key to the value
	 * @return integer value of the entry
	 */
	public int getInt(String key) {
		return getIntOrDefault(key, 0);
	}

	/**
	 * Returns the boolean value of the config entry
	 * @param key key to the value
	 * @param def default value to return if the key is not found
	 * @return boolean value of the entry
	 */
	public boolean getBooleanOrDefault(String key, boolean def) {
		Object value = this.get(key);
		return value != null ? Boolean.parseBoolean(value.toString()) : def;
	}

	/**
	 * Returns the boolean value of the config entry
	 * @param key key to the value
	 * @return boolean value of the entry
	 */
	public boolean getBoolean(String key) {
		return getBooleanOrDefault(key, false);
	}

	/**
	 * Hidden class constructor
	 */
	private Config() {
		super();
	}
}
