/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionBuilder;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

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
    public <T> T execute(Transaction<T> transaction) {
        return transaction.perform();
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