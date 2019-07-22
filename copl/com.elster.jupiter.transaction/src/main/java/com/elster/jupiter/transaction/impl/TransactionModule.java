/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionBuilder;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionEvent;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.streams.ExceptionThrowingRunnable;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;
import com.elster.jupiter.util.time.StopWatch;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import javax.sql.DataSource;

public class TransactionModule extends AbstractModule {

    private final boolean printSql;

    public TransactionModule() {
        this(false);
    }

    public TransactionModule(boolean printSql) {
        this.printSql = printSql;
    }

    @Override
    protected void configure() {
        requireBinding(BootstrapService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(Publisher.class);
        bindConstant().annotatedWith(Names.named("printSql")).to(printSql);
        bind(TransactionService.class).to(TransactionServiceImpl.class).in(Scopes.SINGLETON);
        bind(DataSource.class).to(TransactionalDataSource.class).in(Scopes.SINGLETON);
    }

    public enum FakeTransactionService implements TransactionService {
        INSTANCE;

        @Override
        public <T, E extends Throwable> T execute(ExceptionThrowingSupplier<T, E> transaction) throws E {
            return transaction.get();
        }

        @Override
        public <E extends Throwable> TransactionEvent run(ExceptionThrowingRunnable<E> transaction) throws E {
            transaction.run();
            return new TransactionEvent(true, new StopWatch(false), 0, 0);
        }

        @Override
        public <R, E extends Throwable> R executeInIndependentTransaction(ExceptionThrowingSupplier<R, E> transaction) throws E {
            return execute(transaction);
        }

        @Override
        public <E extends Throwable> TransactionEvent runInIndependentTransaction(ExceptionThrowingRunnable<E> transaction) throws E {
            return run(transaction);
        }

        @Override
        public TransactionContext getContext() {
            return new FakeTransactionContext();
        }

        @Override
        public TransactionBuilder builder() {
            return null;
        }

        @Override
        public boolean isInTransaction() {
            return false;
        }

        private class FakeTransactionContext implements TransactionContext {
            @Override
            public void close() {

            }

            @Override
            public void commit() {

            }

            @Override
            public TransactionEvent getStats() {
                return new TransactionEvent(false, new StopWatch(false), 0, 0);
            }
        }
    }
}
