package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.pubsub.*;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.*;

import org.osgi.service.component.annotations.*;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


@Component(name="com.elster.jupiter.transaction", service=TransactionService.class)
public class TransactionServiceImpl implements TransactionService, ServiceLocator {
	private volatile ThreadPrincipalService threadPrincipalService;
	private volatile DataSource dataSource;
	private volatile Publisher publisher;
	private final ThreadLocal<TransactionContextImpl> transactionContextHolder = new ThreadLocal<>();
	
	public TransactionServiceImpl() {		
	}

    @Inject
    public TransactionServiceImpl(BootstrapService bootstrapService, ThreadPrincipalService threadPrincipalService, Publisher publisher) {
        this.threadPrincipalService = threadPrincipalService;
        this.publisher = publisher;
        doSetBootstrapService(bootstrapService);
        doActivate();
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
	public void setBootstrapService(BootstrapService bootStrapService) {
        doSetBootstrapService(bootStrapService);
    }

    private void doSetBootstrapService(BootstrapService bootStrapService) {
        this.dataSource = bootStrapService.createDataSource();
    }

    @Reference
	public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
		this.threadPrincipalService = threadPrincipalService;		
	}
	
	@Reference
	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;		
	}
	
	@Activate
    public void activate() {
        doActivate();
    }

    private void doActivate() {
        Bus.setServiceLocator(this);
    }

    public void setRollbackOnly() {
        if (isInTransaction()) {
            transactionContextHolder.get().setRollbackOnly();
        } else {
            throw new NotInTransactionException();
        }
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
		try {
			return transactionContext.execute(transaction);			
		} finally {
			transactionContextHolder.remove();						
		}
	}

    private boolean isInTransaction() {
        return transactionContextHolder.get() != null;
    }
    
    @Override 
    public void addThreadSubscriber(Subscriber subscriber) {
    	publisher.addThreadSubscriber(subscriber);
    }
    
    @Override 
    public void removeThreadSubscriber(Subscriber subscriber) {
    	publisher.removeThreadSubscriber(subscriber);
    }
    
    @Override
    public void publish(Object event) {
    	publisher.publish(event);
    }
  
}
