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
}
