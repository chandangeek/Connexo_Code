package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.transaction.TransactionService;

import java.sql.Connection;
import java.sql.SQLException;

public interface ServiceLocator {
	Connection getConnection() throws SQLException;
	OrmClient getOrmClient();
	TransactionService getTransactionService();
	Publisher getPublisher();
}
