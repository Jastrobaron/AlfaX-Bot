package xyz.rtsvk.alfax;

import org.junit.jupiter.api.Test;
import xyz.rtsvk.alfax.util.text.FormattedString;
import java.util.Map;

public class FormattedStringTest {

	@Test
	void createAndBuildTest() {
		assert FormattedString.create("hello").build().equals("hello");
	}

	@Test
	void addSingleParamTest() {
		assert FormattedString.create("Hello ${name}").addParam("name", "David").build().equals("Hello David");
	}

	@Test
	void addMultipleParamTest() {
		assert FormattedString.create("${greeting} ${name}")
				.addParam("greeting", "Hello")
				.addParam("name", "David")
				.build().equals("Hello David");
	}

	@Test
	void addParamMapTest() {
		Map<String, Object> paramMap = Map.of(
				"greet", "Hello",
				"name", "David");
		assert FormattedString.create("${greet} ${name}")
				.addParams(paramMap)
				.build().equals("Hello David");
	}

}
