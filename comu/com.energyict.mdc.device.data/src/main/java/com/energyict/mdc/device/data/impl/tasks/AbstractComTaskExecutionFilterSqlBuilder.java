package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.QueryExecutor;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.report.AbstractTaskFilterSqlBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.energyict.mdc.device.data.impl.tasks.DeviceStateSqlBuilder.DEVICE_STATE_ALIAS_NAME;

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
    private final Set<DefaultState> restrictedDeviceStates;
    private final QueryExecutor<Device> queryExecutor;
    private final List<EndDeviceGroup> deviceGroups;

    public AbstractComTaskExecutionFilterSqlBuilder(Clock clock, ComTaskExecutionFilterSpecification filter, QueryExecutor<Device> queryExecutor) {
        super(clock);
        this.deviceTypes = new HashSet<>(filter.deviceTypes);
        this.comTasks = new HashSet<>(filter.comTasks);
        this.comSchedules = new HashSet<>(filter.comSchedules);
        this.restrictedDeviceStates = DefaultState.fromKeys(filter.restrictedDeviceStates);
        this.deviceGroups = new ArrayList<>(filter.deviceGroups);
        this.queryExecutor = queryExecutor;
    }

    ClauseAwareSqlBuilder newActualBuilderForRestrictedStates() {
        ClauseAwareSqlBuilder actualBuilder = ClauseAwareSqlBuilder
                .withExcludedStates(
                        DEVICE_STATE_ALIAS_NAME,
                        this.restrictedDeviceStates,
                        this.getClock().instant());
        this.setActualBuilder(actualBuilder);
        return actualBuilder;
    }

    protected void appendDeviceStateJoinClauses() {
        this.appendDeviceStateJoinClauses(communicationTaskAliasName());
    }

    void appendDeviceStateJoinClauses(String deviceContainerAliasName) {
        this.append(" join ");
        this.append(TableSpecs.DDC_DEVICE.name());
        this.append(" dev on ");
        this.append(deviceContainerAliasName);
        this.append(".device = dev.id ");
        this.append(" join ");
        this.append(DeviceStateSqlBuilder.DEVICE_STATE_ALIAS_NAME);
        this.append(" kd on dev.meterid = kd.id ");
    }

    protected void appendWhereClause(ServerComTaskStatus taskStatus) {
        taskStatus.completeFindBySqlBuilder(this.getActualBuilder(), this.getClock());
        this.appendNonStatusWhereClauses();
    }

    protected void appendNonStatusWhereClauses() {
        this.appendDeviceTypeSql();
        this.appendDeviceInGroupSql();
        this.appendComTaskSql();
        this.appendComSchedulesSql();
    }

    private void appendDeviceTypeSql() {
        this.appendDeviceTypeSql(this.communicationTaskAliasName(), this.deviceTypes);
    }

    protected void appendDeviceInGroupSql() {
        this.appendDeviceInGroupSql(this.deviceGroups, this.queryExecutor, communicationTaskAliasName());
    }

    private void appendComTaskSql() {
        if (!this.comTasks.isEmpty()) {
            this.appendWhereOrAnd();
            this.append("((discriminator =");
            this.addString(ComTaskExecutionImpl.SHARED_SCHEDULE_COM_TASK_EXECUTION_DISCRIMINATOR);
            this.append("and comschedule in (select comschedule from SCH_COMTASKINCOMSCHEDULE where ");
            this.appendInClause("comtask", this.comTasks);
            this.append(")) or (discriminator in (");
            this.addString(ComTaskExecutionImpl.MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR);
            this.append(", ");
            this.addString(ComTaskExecutionImpl.FIRMWARE_COM_TASK_EXECUTION_DISCRIMINATOR);
            this.append(") and ");
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