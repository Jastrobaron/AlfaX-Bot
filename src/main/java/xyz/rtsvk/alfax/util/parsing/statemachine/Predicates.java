package xyz.rtsvk.alfax.util.parsing.statemachine;

import java.util.List;
import java.util.function.Predicate;

/**
 * Class containing various predicates to be used with state machines
 * @author Jastrobaron
 */
public final class Predicates<T> {

    /**
     * Predicate that checks whether the input character equals ANY of the specified characters
     * @param chars list of characters to check
     * @return the predicate object
     */
    public static <T> Predicate<T> equal(List<T> chars) {
        return chars::contains;
    }

    /**
     * Predicate that checks whether the input character equals ANY of the specified characters
     * @param chars arbitrary array of characters to check
     * @return the predicate object
     */
    @SafeVarargs
    public static <T> Predicate<T> equal(T... chars) {
        return equal(List.of(chars));
    }

    /**
     * Predicate that checks whether the input character equals ANYTHING EXCEPT one of the specified characters
     * @param chars list of characters to check
     * @return the predicate object
     */
    public static <T> Predicate<T> anyExcept(List<T> chars) {
        return equal(chars).negate();
    }

    /**
     * Predicate that checks whether the input character equals ANYTHING EXCEPT one of the specified characters
     * @param chars arbitrary array of characters to check
     * @return the predicate object
     */
    @SafeVarargs
    public static <T> Predicate<T> anyExcept(T... chars) {
        return equal(chars).negate();
    }

    /**
     * Predicate that checks whether the input string is a valid identifier
     * @return the predicate object
     */
    public static Predicate<String> isValidIdentifier() {
        return s -> s.chars().allMatch(Character::isJavaIdentifierPart);
    }
}
