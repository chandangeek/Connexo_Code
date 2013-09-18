package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.NestedTransactionException;
import com.elster.jupiter.transaction.NotInTransactionException;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component(name="com.elster.jupiter.transaction.impl")
public class TransactionServiceImpl implements TransactionService {
	private volatile ThreadPrincipalService threadPrincipalService;
	private volatile DataSource dataSource;
	private final ThreadLocal<TransactionContextImpl> transactionContextHolder = new ThreadLocal<>();
	
	public TransactionServiceImpl() {		
	}
	
	@Override
	public <T> T execute(Transaction<T> transaction) {
        if (isInTransaction()) {
            throw new NestedTransactionException();
        }
        try {
            return doExecute(transaction);
        } catch (SQLException ex) {
            throw new CommitException(ex);
        }
    }
	
	@Reference
	public void setBootstrapService(BootstrapService bootStrapService) throws SQLException {
		this.dataSource = bootStrapService.createDataSource();
	}
	
	public void setRollbackOnly() {
        if (isInTransaction()) {
            transactionContextHolder.get().setRollbackOnly();
        } else {
            throw new NotInTransactionException();
        }
	}
	
	@Reference
	public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
		this.threadPrincipalService = threadPrincipalService;		
	}
	
	Connection getConnection() throws SQLException {
        return isInTransaction() ? transactionContextHolder.get().getConnection() : newConnection(true);
	}
	
	DataSource getDataSource() {
		return dataSource;
	}

	Connection newConnection(boolean autoCommit) throws SQLException {
		Connection result = dataSource.getConnection();
		threadPrincipalService.setEndToEndMetrics(result);
        result.setAutoCommit(autoCommit);
        return result;
    }

    private <T> T doExecute(Transaction<T> transaction) throws SQLException {
		TransactionContextImpl transactionContext = new TransactionContextImpl(this);
		transactionContextHolder.set(transactionContext);
        T result = null;
		boolean commit = false;
		try {
			result = transaction.perform();
			commit = true;
		} finally {
			transactionContextHolder.remove();
			transactionContext.terminate(commit);						
		}
        return result;
	}

    private boolean isInTransaction() {
        return transactionContextHolder.get() != null;
    }
}
