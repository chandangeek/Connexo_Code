package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.QueryExecutor;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides code reuse opportunities to build SQL queries that will
 * match {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s against a {@link ComTaskExecutionFilterSpecification}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (17:22)
 */
public abstract class AbstractComTaskExecutionFilterSqlBuilder extends AbstractTaskFilterSqlBuilder {

    private final Set<DeviceType> deviceTypes;
    private final Set<ComTask> comTasks;
    private final Set<ComSchedule> comSchedules;
    private final QueryExecutor<Device> queryExecutor;
    private final List<EndDeviceGroup> deviceGroups;
    private final Set<String> allowedDeviceStates;

    public AbstractComTaskExecutionFilterSqlBuilder(Clock clock, QueryExecutor<Device> queryExecutor) {
        super(clock);
        this.deviceTypes = new HashSet<>();
        this.comTasks = new HashSet<>();
        this.comSchedules = new HashSet<>();
        this.deviceGroups = Collections.emptyList();
        this.queryExecutor = queryExecutor;
        this.allowedDeviceStates = Collections.emptySet();
    }

    public AbstractComTaskExecutionFilterSqlBuilder(Clock clock, ComTaskExecutionFilterSpecification filter, QueryExecutor<Device> queryExecutor) {
        super(clock);
        this.deviceTypes = new HashSet<>(filter.deviceTypes);
        this.comTasks = new HashSet<>(filter.comTasks);
        this.comSchedules = new HashSet<>(filter.comSchedules);
        this.deviceGroups = new ArrayList<>(filter.deviceGroups);
        this.queryExecutor = queryExecutor;
        this.allowedDeviceStates = Collections.emptySet();
    }

    protected void appendWhereClause(ServerComTaskStatus taskStatus) {
        taskStatus.completeFindBySqlBuilder(this.getActualBuilder(), this.getClock());
        this.appendNonStatusWhereClauses();
    }

    protected void appendNonStatusWhereClauses() {
        this.appendDeviceTypeSql();
        this.appendDeviceInStateSql();
        this.appendComTaskSql();
        this.appendComSchedulesSql();
    }

    private void appendDeviceTypeSql() {
        this.appendDeviceTypeSql(this.communicationTaskAliasName(), this.deviceTypes);
    }

    protected void appendDeviceInGroupSql() {
        this.appendDeviceInGroupSql(this.deviceGroups, this.queryExecutor, communicationTaskAliasName());
    }

    protected void appendDeviceInStateSql(){
        this.appendDeviceInStateSql(communicationTaskAliasName(), this.allowedDeviceStates);
    }

    private void appendComTaskSql() {
        if (!this.comTasks.isEmpty()) {
            this.appendWhereOrAnd();
            this.append("((discriminator =");
            this.addString(ComTaskExecutionImpl.SHARED_SCHEDULE_COM_TASK_EXECUTION_DISCRIMINATOR);
            this.append("and comschedule in (select comschedule from SCH_COMTASKINCOMSCHEDULE where ");
            this.appendInClause("comtask", this.comTasks);
            this.append(")) or (discriminator =");
            this.addString(ComTaskExecutionImpl.MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR);
            this.append(" and ");
            this.appendInClause("comtask", this.comTasks);
            this.append("))");
        }
    }

    private void appendComSchedulesSql() {
        if (!this.comSchedules.isEmpty()) {
            this.appendWhereOrAnd();
            this.append("(discriminator =");
            this.addString(ComTaskExecutionImpl.SHARED_SCHEDULE_COM_TASK_EXECUTION_DISCRIMINATOR);
            this.append(" and ");
            this.appendInClause("comschedule", this.comSchedules);
            this.append(")");
        }
    }

}