package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;

import com.elster.jupiter.util.time.Clock;

/**
 * Provides code reuse opportunities to build SQL queries that will
 * match {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s against a {@link ComTaskExecutionFilterSpecification}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (17:22)
 */
public abstract class AbstractComTaskExecutionFilterSqlBuilder extends AbstractTaskFilterSqlBuilder {

    public AbstractComTaskExecutionFilterSqlBuilder(Clock clock) {
        super(clock);
    }

    protected void appendWhereClause(ServerComTaskStatus taskStatus) {
        taskStatus.completeFindBySqlBuilder(this.getActualBuilder(), this.getClock());
    }

}