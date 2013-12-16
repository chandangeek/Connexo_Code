package com.elster.jupiter.transaction;

public interface TransactionContext extends AutoCloseable {
	public void close();
	public void commit();
}
