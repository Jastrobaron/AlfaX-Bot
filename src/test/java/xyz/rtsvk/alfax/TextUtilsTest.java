package xyz.rtsvk.alfax;

import org.junit.jupiter.api.Test;
import xyz.rtsvk.alfax.util.text.TextUtils;

import java.security.NoSuchAlgorithmException;

public class TextUtilsTest {

	@Test
	void hashTest() throws NoSuchAlgorithmException {
		assert TextUtils.hash("hello").equals("9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca72323c3d99ba5c11d7c7acc6e14b8c5da0c4663475c2e5c3adef46f73bcdec043");
	}

	@Test
	void formatTest() {
		assert TextUtils.format("Hello ${0}", "brother").equals("Hello brother");
		assert TextUtils.format("Hello ${0}", 25).equals("Hello 25");
		assert TextUtils.format("Hello ${0}", 25.5).equals("Hello 25.5");
	}

	@Test
	void matchTest() {
		assert TextUtils.match("hello", "hello");
		assert TextUtils.match("hello", "h*o");
		assert !TextUtils.match("hello", "olleh");
		assert TextUtils.match("/abc.txt", "*");
		assert TextUtils.match("/", "/*");
		assert TextUtils.match("abcd", "*cd");
	}
}
