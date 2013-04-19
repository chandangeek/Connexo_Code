package com.elster.jupiter.transaction.impl;

import java.sql.*;
import javax.sql.DataSource;

import com.elster.jupiter.transaction.*;

public class TransactionServiceImpl implements TransactionService {
	
	final private DataSource source;
	final private ThreadLocal<TransactionContextImpl> transactionContexts = new ThreadLocal<TransactionContextImpl>();
	
	TransactionServiceImpl(DataSource source) {
		this.source = source;
	}

	private Connection newConnection() throws SQLException {
		Connection result = source.getConnection();
		result.setAutoCommit(true);
		return result;
	}
	
	@Override
	public void execute(Runnable runnable) {
		TransactionContextImpl transactionContext = transactionContexts.get();
		if (transactionContext == null) {
			try {
				doExecute(runnable);
			} catch (SQLException ex) {
				throw new CommitException(ex);
			}
		} else {
			throw new NestedTransactionException();
		}
	}
	
	private void doExecute(Runnable runnable) throws SQLException {
		TransactionContextImpl transactionContext = new TransactionContextImpl(source);
		transactionContexts.set(transactionContext);
		boolean commit = false;
		try {
			runnable.run();
			commit = true;
		} finally {
			transactionContexts.remove();
			transactionContext.terminate(commit);						
		}
	}
	
	Connection getConnection() throws SQLException {
		TransactionContextImpl transactionContext = transactionContexts.get();
		return transactionContext == null ? newConnection() : transactionContext.getConnection();
	}
	
	DataSource getDataSource() {
		return source;
	}

	@Override
	public void setRollbackOnly() {
		TransactionContextImpl transactionContext = transactionContexts.get();
		if (transactionContext == null) {
			throw new NotInTransactionException();
		} else {
			transactionContext.setRollbackOnly();
		}
		
	}
}
