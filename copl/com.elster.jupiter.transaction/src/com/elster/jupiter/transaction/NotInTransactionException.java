package com.elster.jupiter.transaction;

/**
 * Thrown when a transaction related operation is performed outside of a transaction.
 */
public class NotInTransactionException extends RuntimeException {
	
	private static final long serialVersionUID = 1;
	
	public NotInTransactionException() {
        super("A transaction related operation was performed outside of a transaction");
	}
}
