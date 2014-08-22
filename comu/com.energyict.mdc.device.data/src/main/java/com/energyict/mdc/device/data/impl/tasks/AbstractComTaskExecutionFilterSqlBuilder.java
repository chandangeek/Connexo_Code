package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;

import com.elster.jupiter.util.time.Clock;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides code reuse opportunities to build SQL queries that will
 * match {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s against a {@link ComTaskExecutionFilterSpecification}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (17:22)
 */
public abstract class AbstractComTaskExecutionFilterSqlBuilder extends AbstractTaskFilterSqlBuilder {

    private Set<DeviceType> deviceTypes;

    public AbstractComTaskExecutionFilterSqlBuilder(Clock clock, ComTaskExecutionFilterSpecification filter) {
        super(clock);
        this.deviceTypes = new HashSet<>(filter.deviceTypes);
    }

    protected void appendWhereClause(ServerComTaskStatus taskStatus) {
        taskStatus.completeFindBySqlBuilder(this.getActualBuilder(), this.getClock());
        this.appendDeviceTypeSql();
    }

    private void appendDeviceTypeSql() {
        this.appendDeviceTypeSql(this.comTaskExecutionTableName(), this.deviceTypes);
    }

}