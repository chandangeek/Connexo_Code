package com.energyict.mdc.engine;

import com.elster.jupiter.transaction.*;
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
    public TransactionBuilder builder() {
        return null;
    }

    @Override
    public TransactionContext getContext() {
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