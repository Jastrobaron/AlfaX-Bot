package xyz.rtsvk.alfax;

import org.junit.jupiter.api.Test;
import xyz.rtsvk.alfax.commands.CommandProcessor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandProcessorTest {

	@Test
	public void splitCommandStringTest() {
		List<String> expectedOutput = List.of("Hello", "my", "name", "is", "David", "Gonzalez");
		assertEquals(expectedOutput, CommandProcessor.splitCommandString("Hello my name is David Gonzalez"));

		expectedOutput = List.of("Hello", "my", "name", "is", "David Gonzalez");
		assertEquals(expectedOutput, CommandProcessor.splitCommandString("Hello my name is \"David Gonzalez\""));

		expectedOutput = List.of("Hello", "my name", "is", "David", "Gonzalez");
		assertEquals(expectedOutput, CommandProcessor.splitCommandString("Hello \"my name\" is David Gonzalez"));

		expectedOutput = List.of("Hello", "my name", "is", "David Gonzalez");
		assertEquals(expectedOutput, CommandProcessor.splitCommandString("Hello \"my name\" is \"David Gonzalez\""));
	}
}
