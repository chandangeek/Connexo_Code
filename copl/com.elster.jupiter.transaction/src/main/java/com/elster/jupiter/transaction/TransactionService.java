/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction;

import com.elster.jupiter.util.streams.ExceptionThrowingRunnable;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface TransactionService {

    /*
     * Start a transaction, and returns a TransactionContext
     * Clients should always call this from  a try with resources statement.
     * Make sure to call context.commit() at the end of the try block, or the tx will rollback
     * Most clients should use the execute method, but the TransactionContext gives access to the tx statistics if needed
     */
    TransactionContext getContext();

    /*
     * Standard way of executing a transaction that returns a value. Any exception throw, by transaction.perform() will rollback
     * the tx and passed to caller.
     */
    <T, E extends Throwable> T execute(ExceptionThrowingSupplier<T, E> transaction) throws E;

    /*
     * if we overload execute with Runnable, mocking the transactionService with Mockito is more difficult
     * so we pick a different name for executing a void tx.
     * the return value will often be ignored, but can be used to access the tx statistics.
     */
    default <E extends Throwable> TransactionEvent run(ExceptionThrowingRunnable<E> transaction) throws E {
        try (TransactionContext context = getContext()) {
            transaction.run();
            context.commit();
            return context.getStats();
        }
    }

    <R, E extends Throwable> R executeInIndependentTransaction(ExceptionThrowingSupplier<R, E> transaction) throws E;

    <E extends Throwable> TransactionEvent runInIndependentTransaction(ExceptionThrowingRunnable<E> transaction) throws E;

    /*
     * TransactionBuilder provides an easy interface to setup the security context before executing a tx
     */
    TransactionBuilder builder();


    /*
    This is due to an architectural limitation
    Provides a means to force rollback in certain specific cases. Avoid overriding/using this method unless necessary.
    Exceptions should never be used as a means of controlling workflow (i.e. mdc.device.lifecycle)
    TODO - on improvements stories do a refactor on all sections of code that use unsafe/non-atomic/stale data prone coding practices to manage transactions. Remove this method from interface before merge into master
    */

    default TransactionEvent rollback() {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Indicates if there is a transaction running or not.
     *
     * @return true is there is a transaction running, false else
     * @since 1.1
     */
    boolean isInTransaction();

    default TransactionProperties getTransactionProperties() {
        throw new UnsupportedOperationException();
    }
}
