package com.elster.jupiter.orm;

/**
 * Thrown when an operation is performed outside a Transaction, which needs to be performed from within a Transaction.
 */
public class TransactionRequiredException extends PersistenceException {
	
	private static final long serialVersionUID = 1;
	
	public TransactionRequiredException() {
		super(ExceptionTypes.TRANSACTION_REQUIRED, "Transaction required for this operation.");
	}
	
}
