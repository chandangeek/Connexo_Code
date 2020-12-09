/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.QueryExecutor;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.report.AbstractTaskFilterSqlBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskStatus.BUSY_TASK_ALIAS_NAME;

/**
 * Provides code reuse opportunities to build SQL queries that will
 * match {@link ComTaskExecution}s against a {@link ComTaskExecutionFilterSpecification}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (17:22)
 */
public abstract class AbstractComTaskExecutionFilterSqlBuilder extends AbstractTaskFilterSqlBuilder {

    private final String deviceName;
    private final Set<DeviceType> deviceTypes;
    private final Set<ComTask> comTasks;
    private final Set<ComSchedule> comSchedules;
    private final Set<EndDeviceStage> restrictedDeviceStages;
    private final QueryExecutor<Device> queryExecutor;
    private final List<EndDeviceGroup> deviceGroups;
    private final boolean showSlaveComTaskExecutions;

    public AbstractComTaskExecutionFilterSqlBuilder(Clock clock, ComTaskExecutionFilterSpecification filter, QueryExecutor<Device> queryExecutor) {
        super(clock);
        this.deviceName = filter.deviceName;
        this.showSlaveComTaskExecutions = filter.showSlaveComTaskExecutions;
        this.deviceTypes = new HashSet<>(filter.deviceTypes);
        this.comTasks = new HashSet<>(filter.comTasks);
        this.comSchedules = new HashSet<>(filter.comSchedules);
        this.restrictedDeviceStages = EndDeviceStage.fromKeys(filter.restrictedDeviceStages);
        this.deviceGroups = new ArrayList<>(filter.deviceGroups);
        this.queryExecutor = queryExecutor;
    }

    ClauseAwareSqlBuilder getBuilderForRestrictedStages() {
            return ClauseAwareSqlBuilder
                    .existingExcludedStages(
                            DeviceStageSqlBuilder.DEVICE_STAGE_ALIAS_NAME,
                            this.restrictedDeviceStages,
                            this.getClock().instant());

    }

    ClauseAwareSqlBuilder newActualBuilder() {
        ClauseAwareSqlBuilder actualBuilder = ClauseAwareSqlBuilder.getWithBuilder();
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
        this.append(DeviceStageSqlBuilder.DEVICE_STAGE_ALIAS_NAME);
        this.append(" kd on dev.meterid = kd.id ");
        this.append(" left join DDC_HIPRIOCOMTASKEXEC hp ON hp.comtaskexecution = cte.id ");
    }

    void appendDeviceAndBusyTaskStateJoinClauses(String deviceContainerAliasName) {
        this.append(" join ");
        this.append(TableSpecs.DDC_DEVICE.name());
        this.append(" dev on ");
        this.append(deviceContainerAliasName);
        this.append(".device = dev.id ");
        this.append(" left outer join " + BUSY_TASK_ALIAS_NAME  + " bt on bt.connectiontask = ct.id ");
        this.append(" left join DDC_HIPRIOCOMTASKEXEC hp ON hp.comtaskexecution = cte.id ");
    }

    protected void appendWhereClause(ServerComTaskStatus taskStatus) {
        this.appendStatusWhereClauses(taskStatus);
        this.appendNonStatusWhereClauses();
    }

    protected abstract void appendStatusWhereClauses(ServerComTaskStatus taskStatus);

    protected void appendNonStatusWhereClauses() {
        this.appendDeviceTypeSql();
        this.appendDeviceInGroupSql();
        this.appendComTaskSql();
        this.appendComSchedulesSql();
        this.appendDeviceNameSql(this.deviceName, showSlaveComTaskExecutions);
    }

    private void appendDeviceTypeSql() {
        this.appendDeviceTypeSql(this.deviceTypes);
    }

    protected void appendDeviceInGroupSql() {
        this.appendDeviceInGroupSql(this.deviceGroups, communicationTaskAliasName());
    }

    private void appendComTaskSql() {
        if (!this.comTasks.isEmpty()) {
            this.appendWhereOrAnd();
            this.append("(discriminator in (");
            this.addString(String.valueOf(ComTaskExecutionImpl.ComTaskExecType.MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR.ordinal()));
            this.append(", ");
            this.addString(String.valueOf(ComTaskExecutionImpl.ComTaskExecType.SHARED_SCHEDULE_COM_TASK_EXECUTION_DISCRIMINATOR.ordinal()));
            this.append(", ");
            this.addString(String.valueOf(ComTaskExecutionImpl.ComTaskExecType.FIRMWARE_COM_TASK_EXECUTION_DISCRIMINATOR.ordinal()));
            this.append(") and ");
            this.appendInClause("comtask", this.comTasks);
            this.append(")");
        }
    }

    private void appendComSchedulesSql() {
        if (!this.comSchedules.isEmpty()) {
            this.appendWhereOrAnd();
            this.append("(discriminator =");
            this.addString(String.valueOf(ComTaskExecutionImpl.ComTaskExecType.SHARED_SCHEDULE_COM_TASK_EXECUTION_DISCRIMINATOR.ordinal()));
            this.append(" and ");
            this.appendInClause("comschedule", this.comSchedules);
            this.append(")");
        }
    }

}