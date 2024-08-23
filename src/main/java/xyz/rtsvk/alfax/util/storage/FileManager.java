package xyz.rtsvk.alfax.util.storage;

import java.io.File;
import java.io.IOException;

public class FileManager {
	public static final File TMP_DIR = new File("tmp");
	public static final File WEB_ROOT = new File("web");
	public static final File LANG_DIR = new File("lang");

	public static void init() {
		TMP_DIR.mkdirs();
		WEB_ROOT.mkdirs();
		LANG_DIR.mkdirs();
	}

	public static File createTmpFile(String filename) throws IOException {
		File file = new File(TMP_DIR, filename);
		file.createNewFile();
		return file;
	}

	public static File getLangFile(String lang) {
		return new File(LANG_DIR, lang + ".properties");
	}

	public static File getWebFile(String path) {
		return new File(WEB_ROOT, path);
	}

	public static boolean isWebFile(String path) {
		return getWebFile(path).exists();
	}

	public static void close() {
		TMP_DIR.delete();
	}
}
