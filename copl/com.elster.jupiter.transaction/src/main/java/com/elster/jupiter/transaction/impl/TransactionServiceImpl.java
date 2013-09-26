package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.*;
import java.util.logging.Logger;

import org.osgi.service.component.annotations.*;
import org.osgi.service.event.EventAdmin;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component(name="com.elster.jupiter.transaction", service=TransactionService.class)
public class TransactionServiceImpl implements TransactionService, ServiceLocator {
	private volatile ThreadPrincipalService threadPrincipalService;
	private volatile DataSource dataSource;
	private volatile Publisher publisher;
	private volatile AtomicReference<EventAdmin> eventAdminHolder = new AtomicReference<>();
	private final ThreadLocal<TransactionContextImpl> transactionContextHolder = new ThreadLocal<>();
	private volatile boolean sqlLog;
	private volatile boolean sqlEvent;
	
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
	
	public void unSetEventAdminService(EventAdmin eventAdminService) {
		this.eventAdminHolder.compareAndSet(eventAdminService, null);
	}
	
	@Reference
	public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
		this.threadPrincipalService = threadPrincipalService;		
	}
	
	@Reference
	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;		
	}
	
	@Reference(cardinality=ReferenceCardinality.OPTIONAL) 
	public void setEventAdminService(EventAdmin eventAdminService) {
		this.eventAdminHolder.set(eventAdminService);
	}
	
	@Activate
    public void activate(Map<String, Object> props) {
    	if (props != null) {
    		sqlLog = Boolean.TRUE == props.get("sqllog");
    		sqlEvent = Boolean.TRUE == props.get("sqlevent");    		 
    	}
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
    
    @Override
    public void publish(SqlEvent event) {
    	publisher.publish(event);
    	if(sqlLog) {
    		Logger.getLogger("com.elster.jupiter.transaction").info(event.toString());
    	}
    	if (sqlEvent) {
    		EventAdmin eventAdmin = eventAdminHolder.get();
    		if (eventAdmin != null) {
    			eventAdmin.postEvent(event.toOsgiEvent());
    		}
    	}
    }
    
  
}
