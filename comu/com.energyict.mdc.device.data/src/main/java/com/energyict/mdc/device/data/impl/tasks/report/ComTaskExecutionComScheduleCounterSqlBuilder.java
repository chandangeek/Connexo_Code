/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.orm.QueryExecutor;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.AbstractComTaskExecutionFilterSqlBuilder;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
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
class ComTaskExecutionComScheduleCounterSqlBuilder extends AbstractComTaskExecutionFilterSqlBuilder {

    private ServerComTaskStatus taskStatus;

    ComTaskExecutionComScheduleCounterSqlBuilder(ServerComTaskStatus taskStatus, Clock clock, ComTaskExecutionFilterSpecification filterSpecification, QueryExecutor<Device> queryExecutor) {
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
        this.append("', ctincs.comschedule, count(distinct cte.id)");
    }

    private void appendFromClause() {
        this.append(" from ");
        this.append(TableSpecs.DDC_COMTASKEXEC.name());
        this.append(" ");
        this.append(communicationTaskAliasName());
    }

    private void appendJoinClauses() {
        this.appendDeviceStateJoinClauses();
        this.append(" join sch_comschedule cs on cte.comschedule = cs.id");
        this.append(" join sch_comtaskincomschedule ctincs on ctincs.comschedule = cs.id");
    }

    private void appendWhereClause() {
        this.appendWhereOrAnd();
        this.append("cte.discriminator = ");
        this.append(String.valueOf(ComTaskExecutionImpl.ComTaskExecType.SHARED_SCHEDULE_COM_TASK_EXECUTION_DISCRIMINATOR.ordinal()));
        this.appendWhereClause(this.taskStatus);
        this.appendDeviceInGroupSql();
    }

    @Override
    protected void appendStatusWhereClauses(ServerComTaskStatus taskStatus) {
        taskStatus.completeFindBySqlBuilder(this.getActualBuilder(), this.getClock());
    }

    private void appendGroupByClause() {
        this.append(" group by ctincs.comschedule");
    }

}