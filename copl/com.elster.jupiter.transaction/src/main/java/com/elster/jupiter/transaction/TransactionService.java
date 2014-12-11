package com.elster.jupiter.transaction;

public interface TransactionService {

	/*
	 * Start a transaction, and returns a TransactionContext
	 * Clients should always call this from  a try with resources statement.
	 * Make sure to call context.commit() at the end of the try block, or the tx will rollback
	 * Most clients should use the execute method, but the TransactionContext gives access to the tx statistics if needed
	 */
	TransactionContext getContext();
	/*
	 * Standard way of executing a transaction that returns a value. Any exception throw, by transaction.perform() will rollback 
	 * the tx and passed to caller.
	 */
	<T> T execute(Transaction<T> transaction);
	/*
	 * if we overload execute with Runnable, mocking the transactionService with Mockito is more difficult
	 * so we pick a different name for executing a void tx.
	 * the return value will often be ignored, but can be used to access the tx statistics.
	 */
	default TransactionEvent run(Runnable transaction) {
		try (TransactionContext context = getContext()) {
    		transaction.run();
    		context.commit();
    		return context.getStats();
    	}
	}
	
}
