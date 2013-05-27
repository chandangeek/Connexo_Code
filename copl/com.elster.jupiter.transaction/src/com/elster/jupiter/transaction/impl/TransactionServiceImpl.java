package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.NestedTransactionException;
import com.elster.jupiter.transaction.NotInTransactionException;
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
	private final ThreadLocal<TransactionContextImpl> transactionContexts = new ThreadLocal<>();
	
	public TransactionServiceImpl() {		
	}
	
	@Override
	public void execute(Runnable runnable) {
        if (isInTransaction()) {
            throw new NestedTransactionException();
        }
        try {
            doExecute(runnable);
        } catch (SQLException ex) {
            throw new CommitException(ex);
        }
    }
	
	@Reference
	public void setBootstrapService(BootstrapService bootStrapService) throws SQLException {
		this.dataSource = bootStrapService.createDataSource();
	}
	
	@Override
	public void setRollbackOnly() {
        if (isInTransaction()) {
            transactionContexts.get().setRollbackOnly();
        } else {
            throw new NotInTransactionException();
        }
	}
	
	@Reference
	public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
		this.threadPrincipalService = threadPrincipalService;		
	}
	
	Connection getConnection() throws SQLException {
        return isInTransaction() ? transactionContexts.get().getConnection() : newConnection(true);
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

    private boolean isInTransaction() {
        return transactionContexts.get() != null;
    }
}
