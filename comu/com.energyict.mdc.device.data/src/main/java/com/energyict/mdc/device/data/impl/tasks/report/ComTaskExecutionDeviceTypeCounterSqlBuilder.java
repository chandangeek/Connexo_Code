/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.orm.QueryExecutor;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.AbstractComTaskExecutionFilterSqlBuilder;
import com.energyict.mdc.device.data.impl.tasks.ServerComTaskStatus;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.time.Clock;

/**
 * Builds the SQL query thats counts ComTasksExecutions
 * for a single {@link TaskStatus}, grouping them by the {@link com.energyict.mdc.scheduling.model.ComSchedule}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-03 (13:41)
 */
class ComTaskExecutionDeviceTypeCounterSqlBuilder extends AbstractComTaskExecutionFilterSqlBuilder {

    private ServerComTaskStatus taskStatus;

    ComTaskExecutionDeviceTypeCounterSqlBuilder(ServerComTaskStatus taskStatus, Clock clock, ComTaskExecutionFilterSpecification filterSpecification, QueryExecutor<Device> queryExecutor) {
        super(clock, filterSpecification, queryExecutor);
        this.taskStatus = taskStatus;
    }

    public void appendTo(ClauseAwareSqlBuilder sqlBuilder) {
        this.setActualBuilder(sqlBuilder);
        this.appendSelectClause();
        this.appendFromClause();
        this.appendJoinClauses();
        this.appendWhereClause();
        this.appendGroupByClause();
    }

    private void appendSelectClause() {
        this.append("select '");
        this.append(this.taskStatus.getPublicStatus().name());
        this.append("', dev.devicetype, count(*)");
    }

    private void appendFromClause() {
        this.append(" from ");
        this.append(TableSpecs.DDC_COMTASKEXEC.name());
        this.append(" ");
        this.append(communicationTaskAliasName());
    }

    private void appendJoinClauses() {
        this.appendDeviceStateJoinClauses();
    }

    private void appendWhereClause() {
        this.appendWhereClause(this.taskStatus);
    }

    @Override
    protected void appendStatusWhereClauses(ServerComTaskStatus taskStatus) {
        taskStatus.completeFindBySqlBuilder(this.getActualBuilder(), this.getClock());
    }

    private void appendGroupByClause() {
        this.append(" group by dev.devicetype");
    }

}