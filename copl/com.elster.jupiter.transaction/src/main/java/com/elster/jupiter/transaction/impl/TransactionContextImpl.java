package com.elster.jupiter.transaction.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.transaction.*;
import com.elster.jupiter.util.time.StopWatch;
 
class TransactionContextImpl implements Subscriber {	
	private final TransactionServiceImpl  transactionService;
	private Connection connection;
	private boolean rollback;
	private int statementCount;
	private int fetchCount;
	
	TransactionContextImpl(TransactionServiceImpl transactionService)  {
		this.transactionService = transactionService;				
	}

	Connection getConnection() throws SQLException {
		if (connection == null) {
			connection = transactionService.newConnection(false);			
		}
		return new ConnectionInTransaction(connection);
	}

	<T> T execute(Transaction<T> transaction) throws SQLException {
		StopWatch stopWatch = new StopWatch(true);
		ServiceLocator locator = Bus.getServiceLocator();
		locator.addThreadSubscriber(this);		
		try {
			return doExecute(transaction);
		} finally {
			stopWatch.stop();
			locator.removeThreadSubscriber(this);
			locator.publish(new TransactionEvent(transaction,rollback,stopWatch,statementCount,fetchCount));
		}		
	}
	
	<T> T doExecute(Transaction<T> transaction) throws SQLException {		
		T result = null;
		boolean commit = false;
		try {			
			result = transaction.perform();
			commit = true;
		} finally {						
			terminate(commit);			
		}
		return result;
	}
	
	void terminate(boolean commit) throws SQLException {
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
