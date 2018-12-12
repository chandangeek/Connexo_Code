/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionBuilder;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionEvent;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-20 (15:37)
 */
public class FakeTransactionService implements TransactionService {

    private final List<TransactionContext> contexts = new ArrayList<>();

    @Override
    public <T> T execute(Transaction<T> transaction) {
        return transaction.perform();
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