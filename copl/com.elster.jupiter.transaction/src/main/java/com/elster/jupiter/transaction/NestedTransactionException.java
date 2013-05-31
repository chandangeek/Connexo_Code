package com.elster.jupiter.transaction;

/**
 * Thrown when attempting to execute a nested transaction.
 */
public class NestedTransactionException extends RuntimeException {
	
	private static final long serialVersionUID = 1;
	
	public NestedTransactionException() {
        super("Nested transactions are not allowed.");
	}
}
