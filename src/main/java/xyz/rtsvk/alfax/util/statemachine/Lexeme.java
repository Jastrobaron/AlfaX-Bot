package xyz.rtsvk.alfax.util.statemachine;

/**
 * Record representing the created token
 * @param type value of the type parameter
 * @param value of the token
 * @param <T> token type enum
 * @author Jastrobaron
 */
public record Lexeme<T>(T type, String value) {}
