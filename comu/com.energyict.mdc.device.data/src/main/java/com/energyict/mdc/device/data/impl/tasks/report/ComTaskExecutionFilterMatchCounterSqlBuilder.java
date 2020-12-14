/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.orm.QueryExecutor;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.tasks.AbstractComTaskExecutionFilterSqlBuilder;
import com.energyict.mdc.device.data.impl.tasks.ServerComTaskStatus;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;

import java.time.Clock;

/**
 * Builds the SQL query thats counts {@link ConnectionTask}s
 * that match a {@link ConnectionTaskFilterSpecification} for a single {@link TaskStatus}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-06 (13:06)
 */
class ComTaskExecutionFilterMatchCounterSqlBuilder extends AbstractComTaskExecutionFilterSqlBuilder {

    private ServerComTaskStatus taskStatus;

    ComTaskExecutionFilterMatchCounterSqlBuilder(ServerComTaskStatus taskStatus, ComTaskExecutionFilterSpecification filter, Clock clock, QueryExecutor<Device> queryExecutor) {
        super(clock, filter, queryExecutor);
        this.taskStatus = taskStatus;
    }

    public void appendTo(ClauseAwareSqlBuilder sqlBuilder) {
        this.setActualBuilder(sqlBuilder);
        this.appendSelectClause();
        this.appendFromClause();
        this.appendWhereClause();
    }

    private void appendSelectClause() {
        this.append("select '");
        this.append(this.taskStatus.getPublicStatus().name());
        this.append("', count(*)");
    }

    private void appendFromClause() {
        this.append(" from ctes ");
        this.append(communicationTaskAliasName());
    }

    private void appendWhereClause() {
        //this.appendWhereOrAnd();
        this.appendWhereClause(this.taskStatus);
        this.appendDeviceInGroupSql();
    }

    @Override
    protected void appendStatusWhereClauses(ServerComTaskStatus taskStatus) {
        taskStatus.completeCountSqlBuilder(this.getActualBuilder(), this.getClock().instant());
    }

}