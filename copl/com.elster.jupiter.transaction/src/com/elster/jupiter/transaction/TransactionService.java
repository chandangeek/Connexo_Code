package com.elster.jupiter.transaction;

public interface TransactionService {

	<T> T execute(Transaction<T> transaction);

}
