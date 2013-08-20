package com.elster.jupiter.orm;

public class TransactionRequiredException extends PersistenceException {
	
	private static final long serialVersionUID = 1;
	
	public TransactionRequiredException() {
		super(ExceptionTypes.TRANSACTION_REQUIRED, "Transaction required for this operation.");
	}
	
}
