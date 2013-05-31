package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.SQLException;
import com.elster.jupiter.transaction.TransactionService;

public interface ServiceLocator {
	Connection getConnection() throws SQLException;
	OrmClient getOrmClient();
	TransactionService getTransactionService();
}
