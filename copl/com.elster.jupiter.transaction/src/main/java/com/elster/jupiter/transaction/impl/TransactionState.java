package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.transaction.SqlEvent;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionEvent;
import com.elster.jupiter.util.time.StopWatch;

import java.sql.Connection;
import java.sql.SQLException;
 
class TransactionState implements Subscriber {	
	private final TransactionServiceImpl  transactionService;
	private Connection connection;
	private boolean rollback;
	private int statementCount;
	private int fetchCount;
	private StopWatch stopWatch;
	
	TransactionState(TransactionServiceImpl transactionService)  {
		this.transactionService = transactionService;
		stopWatch = new StopWatch(true);
		Bus.addThreadSubscriber(this);
	}

	Connection getConnection() throws SQLException {
		if (connection == null) {
			connection = transactionService.newConnection(false);			
		}
		return new ConnectionInTransaction(connection);
	}

	void terminate(boolean commit) throws SQLException {
		try {
			if (connection == null) {
				return;
			}
			try {
				if (commit && !rollback) {
					connection.commit();
				} else {
					rollback = true;
					connection.rollback();
				}
			} finally {        	        
				connection.close();
			}
		} finally {
			stopWatch.stop();
			Bus.removeThreadSubscriber(this);
			Bus.publish(new TransactionEvent(rollback,stopWatch,statementCount,fetchCount));
		}
	}

	void setRollbackOnly() {
		this.rollback = true;
	}

	public void handle(Object rawEvent , Object... details) {
		if (rawEvent instanceof SqlEvent) {
			SqlEvent event = (SqlEvent) rawEvent;
			statementCount++;
			fetchCount += event.getFetchCount();			
		}
	}
	
	@Override
	public Class<?>[] getClasses() {
		return new Class<?>[] { SqlEvent.class };
	}
}
