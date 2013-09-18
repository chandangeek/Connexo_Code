package com.elster.jupiter.transaction.impl;

import java.sql.*;

class ConnectionInTransaction extends ConnectionWrapper {

	ConnectionInTransaction(Connection connection) {
		super(connection);
	}
	
	@Override
	PreparedStatement wrap(PreparedStatement statement,String text) {
		return statement;
	}
	
	@Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (autoCommit) {
            throw new UnsupportedOperationException();
        }
    }
	
	@Override
    public void commit() throws SQLException {
        throw new UnsupportedOperationException();
    }
	
    @Override
    public void rollback() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws SQLException {
        // silently ignore. 
    	// TransactionManager will return connection to the pool when transaction ends
    }


}
