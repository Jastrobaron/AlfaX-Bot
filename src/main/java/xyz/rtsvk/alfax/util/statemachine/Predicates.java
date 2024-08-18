package xyz.rtsvk.alfax.util.statemachine;

import java.util.List;
import java.util.function.Predicate;

/**
 * Class containing various predicates to be used with state machines
 * @author Jastrobaron
 */
public final class Predicates {

    /**
     * Predicate that checks whether the input character equals ANY of the specified characters
     * @param chars list of characters to check
     * @return the predicate object
     */
    public static Predicate<Character> charEquals(List<Character> chars) {
        return chars::contains;
    }

    /**
     * Predicate that checks whether the input character equals ANY of the specified characters
     * @param chars arbitrary array of characters to check
     * @return the predicate object
     */
    public static Predicate<Character> charEquals(Character... chars) {
        return charEquals(List.of(chars));
    }

    /**
     * Predicate that checks whether the input character equals ANYTHING EXCEPT one of the specified characters
     * @param chars list of characters to check
     * @return the predicate object
     */
    public static Predicate<Character> anyCharExcept(List<Character> chars) {
        return charEquals(chars).negate();
    }

    /**
     * Predicate that checks whether the input character equals ANYTHING EXCEPT one of the specified characters
     * @param chars arbitrary array of characters to check
     * @return the predicate object
     */
    public static Predicate<Character> anyCharExcept(Character... chars) {
        return charEquals(chars).negate();
    }

    /**
     * Predicate that checks whether the input string equals ANY of the specified strings
     * @param strings list of strings to check
     * @return the predicate object
     */
    public static Predicate<String> stringEquals(List<String> strings) {
        return strings::contains;
    }

    /**
     * Predicate that checks whether the input string equals ANY of the specified strings
     * @param strings arbitrary array of strings to check
     * @return the predicate object
     */
    public static Predicate<String> stringEquals(String... strings) {
        return stringEquals(List.of(strings));
    }

    /**
     * Predicate that checks whether the input string equals ANYTHING EXCEPT one of the specified string
     * @param strings list of strings to check
     * @return the predicate object
     */
    public static Predicate<String> anyStringExcept(List<String> strings) {
        return stringEquals(strings).negate();
    }

    /**
     * Predicate that checks whether the input strings equals ANYTHING EXCEPT one of the specified strings
     * @param strings arbitrary array of strings to check
     * @return the predicate object
     */
    public static Predicate<String> anyStringExcept(String... strings) {
        return stringEquals(strings).negate();
    }

    /**
     * Predicate that checks whether the input string is a valid identifier
     * @return the predicate object
     */
    public static Predicate<String> isValidIdentifier() {
        return s -> s.chars().allMatch(Character::isJavaIdentifierPart);
    }
}
