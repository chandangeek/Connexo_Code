package com.elster.jupiter.transaction;

public interface TransactionService {
	void execute(Runnable runnable);
	void setRollbackOnly();
}
