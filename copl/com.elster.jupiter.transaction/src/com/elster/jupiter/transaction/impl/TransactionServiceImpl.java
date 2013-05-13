package com.elster.jupiter.transaction.impl;

import java.sql.*;
import javax.sql.DataSource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.*;

@Component(name="com.elster.jupiter.transaction.impl")
public class TransactionServiceImpl implements TransactionService {
	
	private volatile ThreadPrincipalService threadPrincipalService;
	private volatile DataSource dataSource;
	private final ThreadLocal<TransactionContextImpl> transactionContexts = new ThreadLocal<TransactionContextImpl>();
	
	public TransactionServiceImpl() {		
	}

	Connection newConnection(boolean autoCommit) throws SQLException {
		Connection result = dataSource.getConnection();
		result.setAutoCommit(autoCommit);
		threadPrincipalService.setEndToEndMetrics(result);		
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
		TransactionContextImpl transactionContext = new TransactionContextImpl(this);
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
		return transactionContext == null ? newConnection(true) : transactionContext.getConnection();
	}
	
	DataSource getDataSource() {
		return dataSource;
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
	
	@Reference
	public void setBootstrapService(BootstrapService bootStrapService) throws SQLException {
		this.dataSource = bootStrapService.createDataSource();
	}
	
	@Reference
	public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
		this.threadPrincipalService = threadPrincipalService;		
	}
}
