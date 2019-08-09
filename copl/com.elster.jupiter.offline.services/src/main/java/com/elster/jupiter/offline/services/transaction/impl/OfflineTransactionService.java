/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.offline.services.transaction.impl;

import com.elster.jupiter.transaction.*;
import com.elster.jupiter.util.streams.ExceptionThrowingRunnable;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;
import com.elster.jupiter.util.time.StopWatch;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-20 (15:37)
 */
//@Component(name="com.elster.jupiter.offline.transaction", service=TransactionService.class)
public class OfflineTransactionService implements TransactionService {

    private final List<TransactionContext> contexts = new ArrayList<>();

    public OfflineTransactionService() {
    }

    @Override
    public <E extends Throwable> TransactionEvent runInIndependentTransaction(ExceptionThrowingRunnable<E> transaction) throws E {
        return run(transaction);
    }

    @Override
    public <T, E extends Throwable> T execute(ExceptionThrowingSupplier<T, E> transaction) throws E {
        return execute(transaction);
    }

    @Override
    public <R, E extends Throwable> R executeInIndependentTransaction(ExceptionThrowingSupplier<R, E> transaction) throws E {
        return execute(transaction);
    }

    public List<TransactionContext> getContexts() {
        return Collections.unmodifiableList(this.contexts);
    }

    @Override
    public TransactionContext getContext() {
        FakeTransactionContext context = new FakeTransactionContext();
        this.contexts.add(context);
        return context;
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