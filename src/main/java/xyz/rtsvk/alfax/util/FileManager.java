package xyz.rtsvk.alfax.util;

import java.io.File;
import java.io.IOException;

public class FileManager {
	public static final File TMP_DIR = new File("tmp");
	public static final File WEB_ROOT = new File("web");

	public static void createDirectories() {
		TMP_DIR.mkdirs();
		WEB_ROOT.mkdirs();
	}

	public static File createTmpFile(String extension) throws IOException {
		File file = new File(TMP_DIR, System.currentTimeMillis() + extension);
		file.createNewFile();
		return file;
	}

	public static File getWebFile(String path) {
		return new File(WEB_ROOT, path);
	}

	public static boolean isWebFile(String path) {
		return getWebFile(path).exists();
	}

	public static void deleteTmpFiles() {
		TMP_DIR.delete();
	}
}
