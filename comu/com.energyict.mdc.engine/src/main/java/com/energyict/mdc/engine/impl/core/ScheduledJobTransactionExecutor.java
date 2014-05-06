package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.common.BusinessException;

/**
 * Executes {@link ScheduledJob}s in a {@link Transaction}al context.
 * Given that the execution of ScheduledJob is not actually throwing
 * any {@link BusinessException} or
 * {@link java.sql.SQLException} these exceptions have deliberatly
 * been left out from all of this interface's method signatures.
 * In other words the fact that these exceptions are not
 * part of the method signatures is part of the design.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-11 (08:37)
 */
public interface ScheduledJobTransactionExecutor {

    /**
     * Executes the specified {@link Transaction} that is assumed
     * to be executing {@link ScheduledJob}s.
     *
     * @param scheduledJobTransaction The Transaction that will execute a ScheduledJob
     * @return The transaction's return result
     */
    public <T> T execute (Transaction<T> scheduledJobTransaction);

}