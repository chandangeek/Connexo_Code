package com.elster.jupiter.transaction.impl;

import java.sql.Connection;
import java.sql.SQLException;
 
class TransactionContextImpl {	
	private final TransactionServiceImpl  transactionService;
	private Connection connection;
	private boolean rollback;
	
	TransactionContextImpl(TransactionServiceImpl transactionService) {
		this.transactionService = transactionService;
	}

	Connection getConnection() throws SQLException {
		if (connection == null) {
			connection = transactionService.newConnection(false);			
		}
		return new ConnectionWrapper(connection);
	}
	
	void terminate(boolean commit) throws SQLException {
		if (connection == null) {
            return;
        }
        try {
            if (commit && !rollback) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } finally {
            connection.close();
        }
	}

	void setRollbackOnly() {
		this.rollback = true;
	}
	
}
