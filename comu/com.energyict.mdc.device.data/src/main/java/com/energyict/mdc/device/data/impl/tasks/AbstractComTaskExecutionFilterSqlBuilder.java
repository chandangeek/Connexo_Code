package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

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
    private Set<ComTask> comTasks;
    private Set<ComSchedule> comSchedules;

    public AbstractComTaskExecutionFilterSqlBuilder(Clock clock, ComTaskExecutionFilterSpecification filter) {
        super(clock);
        this.deviceTypes = new HashSet<>(filter.deviceTypes);
        this.comTasks = new HashSet<>(filter.comTasks);
        this.comSchedules = new HashSet<>(filter.comSchedules);
    }

    protected void appendWhereClause(ServerComTaskStatus taskStatus) {
        taskStatus.completeFindBySqlBuilder(this.getActualBuilder(), this.getClock());
        this.appendNonStatusWhereClauses();
    }

    protected void appendNonStatusWhereClauses() {
        this.appendDeviceTypeSql();
        this.appendComTaskSql();
        this.appendComSchedulesSql();
    }

    private void appendDeviceTypeSql() {
        this.appendDeviceTypeSql(this.comTaskExecutionTableName(), this.deviceTypes);
    }

    private void appendComTaskSql() {
        if (!this.comTasks.isEmpty()) {
            this.appendWhereOrAnd();
            this.append("(discriminator =");
            this.addString(ComTaskExecutionImpl.SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR);
            this.append("and comschedule in (select comschedule from SCH_COMTASKINCOMSCHEDULE where ");
            this.appendInClause("comtask", this.comTasks);
            this.append(")) or ((discriminator =");
            this.addString(ComTaskExecutionImpl.AD_HOC_COM_TASK_EXECUTION_DISCRIMINATOR);
            this.append("or discriminator =");
            this.addString(ComTaskExecutionImpl.MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR);
            this.append(") and ");
            this.appendInClause("comtask", this.comTasks);
            this.append(")");
        }
    }

    private void appendComSchedulesSql() {
        if (!this.comSchedules.isEmpty()) {
            this.appendWhereOrAnd();
            this.append("(discriminator =");
            this.addString(ComTaskExecutionImpl.SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR);
            this.append(" and ");
            this.appendInClause("comschedule", this.comSchedules);
            this.append(")");
        }
    }

}