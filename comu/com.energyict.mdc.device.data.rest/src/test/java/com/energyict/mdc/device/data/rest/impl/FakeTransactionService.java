package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionBuilder;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionEvent;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.StopWatch;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-20 (15:37)
 */
public class FakeTransactionService implements TransactionService {

    @Override
    public <T> T execute(Transaction<T> transaction) {
        return transaction.perform();
    }

    @Override
    public TransactionEvent run(Runnable transaction) {
        transaction.run();
        return new TransactionEvent(true, new StopWatch(false), 0, 0);
    }

    @Override
    public TransactionBuilder builder() {
        return null;
    }

    @Override
    public TransactionEvent run(Runnable transaction) {
        transaction.run();
        return new TransactionEvent(true, new StopWatch(false), 0, 0);
    }

    @Override
    public TransactionContext getContext() {
        return new FakeTransactionContext();
    }

    @Override
    public TransactionBuilder builder() {
        return null;
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