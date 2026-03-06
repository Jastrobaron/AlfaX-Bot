package xyz.rtsvk.alfax.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

	public static PrintStream STDOUT = System.out, STDERR = System.err;
	private static String logFile;
	private final String tag;

	public static void setLogFile(String fileName) {
		logFile = fileName;
	}

	public Logger(String tag) {
		this.tag = tag;
	}

	public Logger(Class<?> cl) {
		this(cl.getSimpleName());
	}

	private static void print(String tag, String message, String severity) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		message = format(timestamp) + " " + severity + " at " + tag + " -> " + message;

		System.out.println(message);

		if (logFile == null) return;
		try {
			File file = new File(logFile);
			file.createNewFile();
			FileWriter write = new FileWriter(file, true);
			BufferedWriter writer = new BufferedWriter(write);
			writer.write(message);
			writer.newLine();
			writer.close();
			write.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String format(String timestamp) {
		StringBuilder ts = new StringBuilder(timestamp.substring(0, 23));
		while (ts.length() <= 23)
			ts.append('0');
		return ts.toString();
	}

	public static void info(String tag, String msg) {
		print(tag, msg, "INFO");
	}

	public static void warn(String tag, String msg) {
		print(tag, msg, "WARNING");
	}

	public static void error(String tag, String msg) {
		print(tag, msg, "ERROR");
	}

	public void info(String msg) {
		info(this.tag, msg);
	}

	public void warn(String msg) {
		warn(this.tag, msg);
	}

	public void error(String msg) {
		error(this.tag, msg);
	}

	public String getTag() {
		return this.tag;
	}
}
