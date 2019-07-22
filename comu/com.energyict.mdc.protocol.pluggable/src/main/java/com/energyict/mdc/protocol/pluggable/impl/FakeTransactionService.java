/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.transaction.TransactionBuilder;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionEvent;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.streams.ExceptionThrowingRunnable;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;

/**
 * Provides an implementation for the {@link TransactionService}
 * that can be used for contexts that require the service
 * but are in fact already running in a transactional context
 * and would therefore cause a "Nested transactions are not allowed" exception.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-09 (15:33)
 */
public class FakeTransactionService implements TransactionService {

    private final TransactionService actualTransactionService;

    public FakeTransactionService(TransactionService actualTransactionService) {
        super();
        this.actualTransactionService = actualTransactionService;
    }

    @Override
    public <T, E extends Throwable> T execute(ExceptionThrowingSupplier<T, E> transaction) throws E {
        return transaction.get();
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
        return this.actualTransactionService.getContext();
    }

    @Override
    public TransactionBuilder builder() {
        return null;
    }

    @Override
    public boolean isInTransaction() {
        return this.actualTransactionService.isInTransaction();
    }
}
