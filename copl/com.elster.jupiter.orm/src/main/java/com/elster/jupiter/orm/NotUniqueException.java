package com.elster.jupiter.orm;

/**
 * Thrown when a query that should return at most one instance, returns more than one.
 */
public class NotUniqueException extends RuntimeException {

}
