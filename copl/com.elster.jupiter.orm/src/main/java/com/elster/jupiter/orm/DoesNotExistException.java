package com.elster.jupiter.orm;

/**
 * Thrown when an instance that should exist, is not found to.
 */
public class DoesNotExistException extends RuntimeException {

	public DoesNotExistException() {
		System.out.println("Does not exist");
	}
}
