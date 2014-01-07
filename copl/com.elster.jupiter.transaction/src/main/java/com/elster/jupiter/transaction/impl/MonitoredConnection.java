package com.elster.jupiter.transaction.impl;

import java.sql.*;

class MonitoredConnection extends ConnectionWrapper {

	private final TransactionServiceImpl transactionService;
	
	MonitoredConnection(TransactionServiceImpl transactionService) throws SQLException {
		super(transactionService.getConnection());
		this.transactionService = transactionService;
	}
	
	@Override
	PreparedStatement wrap(PreparedStatement statement,String text) {
		return new MonitoredStatement(transactionService , statement,text);
	}


}
