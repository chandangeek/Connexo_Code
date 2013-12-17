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
	private final ThreadLocal<TransactionState> transactionStateHolder = new ThreadLocal<>();
	
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
    	try (TransactionContext context = getContext()) {
    		T result = transaction.perform();
    		context.commit();
    		return result;
    	} 
    }
	
    public TransactionContext getContext() {
    	if (isInTransaction()) {
    		throw new NestedTransactionException();
    	}
    	TransactionState transactionState = new TransactionState(this);
		transactionStateHolder.set(transactionState);
		return new TransactionContextImpl(this);
    }
    
    private TransactionEvent terminate(boolean commit) {
    	try {
    		return transactionStateHolder.get().terminate(commit);
    	} catch (SQLException ex) {
    		throw new CommitException(ex);
    	} finally {
    		transactionStateHolder.remove();
    	}
    }
    
    TransactionEvent commit() {
    	return terminate(true);
    }
    
    TransactionEvent rollback() {
    	return terminate(false);
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
            transactionStateHolder.get().setRollbackOnly();
        } else {
            throw new NotInTransactionException();
        }
	}
	
	
	
	Connection getConnection() throws SQLException {
        return isInTransaction() ? transactionStateHolder.get().getConnection() : newConnection(true);
	}
	
	DataSource getDataSource() {
		return dataSource;
	}

	Connection newConnection(boolean autoCommit) throws SQLException {
		Connection result = dataSource.getConnection();
		if (result == null) {
			throw new SQLException("DataSource getConnection returned null");
		}
		threadPrincipalService.setEndToEndMetrics(result);
        result.setAutoCommit(autoCommit);
        return result;
    }


    private boolean isInTransaction() {
        return transactionStateHolder.get() != null;
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
