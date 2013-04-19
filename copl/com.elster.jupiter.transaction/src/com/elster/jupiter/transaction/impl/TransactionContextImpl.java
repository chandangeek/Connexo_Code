package com.elster.jupiter.transaction.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;
 
class TransactionContextImpl {	
	private final DataSource source;
	private Connection connection;
	private boolean rollback = false;
	
	TransactionContextImpl(DataSource source) {
		this.source = source;
	}

	Connection getConnection() throws SQLException {
		if (connection == null) {
			connection = source.getConnection();
			connection.setAutoCommit(false);
		}
		return new ConnectionWrapper(connection);
	}
	
	void terminate(boolean commit) throws SQLException {
		if (connection != null) {
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
	}

	void setRollbackOnly() {
		this.rollback = true;
		
	}
	
}
