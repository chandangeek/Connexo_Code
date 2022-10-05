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
import com.elster.jupiter.transaction.TransactionBuilder;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionEvent;
import com.elster.jupiter.transaction.TransactionProperties;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Registration;
import com.elster.jupiter.util.streams.ExceptionThrowingRunnable;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Component(name = "com.elster.jupiter.transaction", service = TransactionService.class, immediate = true)
public class TransactionServiceImpl implements TransactionService {
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile DataSource dataSource;
    private volatile Publisher publisher;
    private final ThreadLocal<TransactionState> transactionStateHolder = new ThreadLocal<>();
    private final ThreadLocal<TransactionProperties> transactionPropertiesHolder = new ThreadLocal<>();
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
    public <T, E extends Throwable> T execute(ExceptionThrowingSupplier<T, E> transaction) throws E {
        try (TransactionContext context = getContext()) {
            T result = transaction.get();
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
        transactionPropertiesHolder.set(new TransactionPropertiesImpl());
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

    @Override
    public TransactionEvent rollback() {
        return terminate(false);
    }

    @Reference
    public void setBootstrapService(BootstrapService bootStrapService) {
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

    @Override
    public TransactionProperties getTransactionProperties() {
        return transactionPropertiesHolder.get();
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

    @Override
    public <E extends Throwable> TransactionEvent runInIndependentTransaction(ExceptionThrowingRunnable<E> transaction) throws E {
        if (isInTransaction()) {
            Principal principal = threadPrincipalService.getPrincipal();
            try {
                return CompletableFuture.supplyAsync(() -> {
                    threadPrincipalService.set(principal);
                    try {
                        return run(transaction);
                    } catch (RuntimeException re) {
                        throw re;
                    } catch (Throwable e) {
                        throw new CheckedRuntimeException(e);
                    }
                }).get();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof CheckedRuntimeException) {
                    throw (E) cause.getCause();
                } else if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else  {
                    throw new RuntimeException(cause);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            return run(transaction);
        }
    }

    @Override
    public <R, E extends Throwable> R executeInIndependentTransaction(ExceptionThrowingSupplier<R, E> transaction) throws E {
        if (isInTransaction()) {
            Principal principal = threadPrincipalService.getPrincipal();
            try {
                return CompletableFuture.supplyAsync(() -> {
                    threadPrincipalService.set(principal);
                    try {
                        return execute(transaction);
                    } catch (RuntimeException re) {
                        throw re;
                    } catch (Throwable e) {
                        throw new CheckedRuntimeException(e);
                    }
                }).get();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof CheckedRuntimeException) {
                    throw (E) cause.getCause();
                } else if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw new RuntimeException(cause);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            return execute(transaction);
        }
    }

    private static class CheckedRuntimeException extends RuntimeException {
        private CheckedRuntimeException(Throwable cause) {
            super(cause);
        }
    }
}
