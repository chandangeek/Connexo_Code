package com.energyict.mdc.engine.impl.scheduling;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.impl.core.ScheduledJobTransactionExecutor;
import com.energyict.mdc.common.Transaction;
import org.fest.assertions.api.Fail;

import java.sql.SQLException;

/**
 * Provides an implementation for the {@link ScheduledJobTransactionExecutor} that simply
 * invoces the {@link Transaction} without any actual database context.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-11 (08:44)
 */
public class ScheduledJobTransactionExecutorTestImpl implements ScheduledJobTransactionExecutor {

    public ScheduledJobTransactionExecutorTestImpl () {
        super();
    }

    @Override
    public <T> T execute (Transaction<T> scheduledJobTransaction) {
        try {
            return scheduledJobTransaction.doExecute();
        }
        catch (BusinessException e) {
            e.printStackTrace(System.err);
            Fail.fail("Unexpected BusinessException caused by the execution of a ScheduledJob", e);
            return null;
        }
        catch (SQLException e) {
            e.printStackTrace(System.err);
            Fail.fail("Unexpected SQLException caused by the execution of a ScheduledJob", e);
            return null;
        }
    }

}