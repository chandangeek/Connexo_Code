package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Transaction;

import java.sql.SQLException;

/**
 * Provides a default implementation for the {@link ScheduledJobTransactionExecutor}
 * interface that uses the {@link EnvironmentImpl} to execute the {@link Transaction}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-11 (08:42)
 */
public class ScheduledJobTransactionExecutorDefaultImplementation implements ScheduledJobTransactionExecutor {

    public ScheduledJobTransactionExecutorDefaultImplementation () {
        super();
    }

    @Override
    public <T> T execute (Transaction<T> scheduledJobTransaction) {
        try {
            return transactionExecutor().execute(scheduledJobTransaction);
        }
        catch (BusinessException | SQLException e) {
            // Currently, the execution of ScheduledJobs does not throw any BusinessExceptions
            throw new ApplicationException(e);
        }
    }

    private TransactionExecutor transactionExecutor() {
        return TransactionExecutorProvider.instance.get().getTransactionExecutor();
    }

}