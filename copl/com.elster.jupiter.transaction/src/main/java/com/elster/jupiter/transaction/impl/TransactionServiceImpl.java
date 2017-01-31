/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.NestedTransactionException;
import com.elster.jupiter.transaction.NotInTransactionException;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionBuilder;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionEvent;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Registration;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


@Component(name="com.elster.jupiter.transaction", service=TransactionService.class)
public class TransactionServiceImpl implements TransactionService {
	private volatile ThreadPrincipalService threadPrincipalService;
	private volatile DataSource dataSource;
	private volatile Publisher publisher;
	private final ThreadLocal<TransactionState> transactionStateHolder = new ThreadLocal<>();
	private volatile boolean printSql;

	public TransactionServiceImpl() {
	}

    @Inject
    public TransactionServiceImpl(BootstrapService bootstrapService, ThreadPrincipalService threadPrincipalService, Publisher publisher,
    		@Named("printSql") boolean printSql) {
        setThreadPrincipalService(threadPrincipalService);
        setPublisher(publisher);
        setBootstrapService(bootstrapService);
        this.printSql = printSql;
    }

    @Override
	public <T> T execute(Transaction<T> transaction) {
    	try (TransactionContext context = getContext()) {
    		T result = transaction.perform();
    		context.commit();
    		return result;
    	}
    }

    @Override
    public TransactionContext getContext() {
    	if (isInTransaction()) {
    		throw new NestedTransactionException();
    	}
    	TransactionState transactionState = new TransactionState(this);
		transactionStateHolder.set(transactionState);
		return new TransactionContextImpl(this);
    }

    @Override
    public TransactionBuilder builder() {
    	return new TransactionBuilderImpl(this, threadPrincipalService);
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

	@Override
	public boolean isInTransaction() {
		return transactionStateHolder.get() != null;
    }

    Registration addThreadSubscriber(Subscriber subscriber) {
    	return publisher.addThreadSubscriber(subscriber);
    }

    void publish(Object event) {
    	publisher.publish(event);
    }

    boolean printSql() {
    	return printSql;
    }

    void printSql(boolean printSql) {
    	this.printSql = printSql;
    }

}