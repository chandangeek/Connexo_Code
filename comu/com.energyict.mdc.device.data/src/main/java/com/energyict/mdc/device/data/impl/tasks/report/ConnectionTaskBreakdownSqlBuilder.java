/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.device.data.impl.PreparedStatementProvider;
import com.energyict.mdc.device.data.impl.tasks.DeviceStateSqlBuilder;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskStatus.BUSY_TASK_ALIAS_NAME;

/**
 * Builds the SQL query that counts {@link ConnectionTask}s
 * for a set of {@link TaskStatus}es broken down by
 * some aspect of the ConnectionTask that is specified at creation time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-06 (13:06)
 */
abstract class ConnectionTaskBreakdownSqlBuilder implements PreparedStatementProvider {

    private final GroupByAspect groupByAspect;
    private final EnumSet<ServerConnectionTaskStatus> taskStatusses;
    private final boolean includeBusyTasks;
    private final EndDeviceGroup deviceGroup;
    private final ConnectionTaskReportServiceImpl connectionTaskService;
    private SqlBuilder sqlBuilder;

    ConnectionTaskBreakdownSqlBuilder(Optional<String> groupByAspect, Set<ServerConnectionTaskStatus> taskStatusses, EndDeviceGroup deviceGroup, ConnectionTaskReportServiceImpl connectionTaskService) {
        super();
        this.groupByAspect = groupByAspect.<GroupByAspect>map(GroupBySingleAspect::new).orElseGet(NoGrouping::new);
        this.taskStatusses = EnumSet.copyOf(taskStatusses);
        this.includeBusyTasks = taskStatusses.contains(ServerConnectionTaskStatus.Busy);
        this.taskStatusses.remove(ServerConnectionTaskStatus.Busy);
        this.deviceGroup = deviceGroup;
        this.connectionTaskService = connectionTaskService;
    }

    @Override
    public PreparedStatement prepare(Connection connection) throws SQLException {
        this.build();
        return this.sqlBuilder.prepare(connection);
    }

    private void build() {
        this.appendWithClauses(this.connectionTaskService.clock());
        if (this.includeBusyTasks) {
            this.sqlBuilder.append("select '");
            this.sqlBuilder.append(ServerConnectionTaskStatus.Busy.name());
            this.sqlBuilder.append("'");
            this.groupByAspect.appendToList(this.sqlBuilder);
            this.sqlBuilder.append(", count(*) from CT where (exists (select * from ");
            this.sqlBuilder.append(BUSY_TASK_ALIAS_NAME);
            this.sqlBuilder.append(" where ");
            this.sqlBuilder.append(BUSY_TASK_ALIAS_NAME);
            this.sqlBuilder.append(".connectiontask = id) or comserver is not null) ");
            this.groupByAspect.appendGroupByClause(this.sqlBuilder);
            this.sqlBuilder.append(" UNION ALL ");
        }
        this.sqlBuilder.append("select taskStatus ");
        this.groupByAspect.appendToList(this.sqlBuilder);
        this.sqlBuilder.append(", count(*) from notBusyCT group by taskStatus ");
        this.groupByAspect.appendToList(this.sqlBuilder);
    }

    private void appendWithClauses(Clock clock) {
        this.sqlBuilder = new SqlBuilder("WITH ");
        this.appendRestrictedStatesWithClause(clock);
        this.sqlBuilder.append(", ");
        this.appendBusyComTaskExecutionWithClause();
        this.sqlBuilder.append(", ");
        this.appendConnectionTaskWithClause();
        if (this.needsNotBusyConnectionTaskWithClause()) {
            this.sqlBuilder.append(", ");
            this.appendNotBusyConnectionTaskWithClause(this.connectionTaskService.clock());
        }
    }

    private void appendRestrictedStatesWithClause(Clock clock) {
        DeviceStateSqlBuilder
                .forDefaultExcludedStates("enddevices")
                .appendRestrictedStatesWithClause(this.sqlBuilder, clock.instant());
    }

    private void appendBusyComTaskExecutionWithClause() {
        this.sqlBuilder.append(BUSY_TASK_ALIAS_NAME);
        this.sqlBuilder.append(" as (select connectiontask, comport /*+ NO_MERGE */ from DDC_COMTASKEXEC where comport is not null and obsolete_date is null)");
    }

    private void appendConnectionTaskWithClause() {
        this.sqlBuilder.append("CT as (");
        this.sqlBuilder.append("    SELECT /*+ NO_MERGE */ ");
        this.appendConnectionTaskSelectClauseInWithClause(this.sqlBuilder);
        this.appendConnectionTaskFromClauseInWithClause(this.sqlBuilder);
        this.sqlBuilder.append("     WHERE connT.status = 0");
        this.sqlBuilder.append("       AND connT.obsolete_date is null");
        this.sqlBuilder.append("       AND connT.nextexecutiontimestamp is not null");
        this.appendDeviceInGroupSql();
        this.sqlBuilder.append(")");
    }

    protected void appendConnectionTaskSelectClauseInWithClause(SqlBuilder sqlBuilder) {
        sqlBuilder.append("connT.id, connT.discriminator, connT.comserver, connT.status, connT.currentretrycount, connT.lastsuccessfulcommunicationend, connT.nextexecutiontimestamp, connT.lastExecutionFailed, connT.connectiontypepluggableclass, connT.comportpool");
    }

    protected void appendConnectionTaskFromClauseInWithClause(SqlBuilder sqlBuilder) {
        sqlBuilder.append(" FROM DDC_CONNECTIONTASK connT");
        sqlBuilder.append(" JOIN DDC_DEVICE dev ON connT.device = dev.id");
        sqlBuilder.append(" JOIN enddevices kd ON dev.meterid = kd.id");
    }

    private void appendNotBusyConnectionTaskWithClause(Clock clock) {
        this.sqlBuilder.append("notBusyCT as (");
        this.sqlBuilder.append("    SELECT /*+ NO_MERGE */ 1 dummy");
        this.groupByAspect.appendToList(this.sqlBuilder);
        this.sqlBuilder.append(",");
        this.sqlBuilder.append("      CASE");
        this.taskStatusses.forEach(each -> each.appendBreakdownCaseClause(this.sqlBuilder, clock));
        this.sqlBuilder.append("      END taskStatus");
        this.sqlBuilder.append("    FROM CT WHERE not exists (SELECT 1 FROM busytask WHERE busytask.connectiontask = id and comport is not null)");
        this.sqlBuilder.append("              AND comserver is null)");
    }

    private boolean needsNotBusyConnectionTaskWithClause() {
        return !this.intersection(this.notBusyStatusses(), this.taskStatusses).isEmpty();
    }

    private EnumSet<ServerConnectionTaskStatus> notBusyStatusses() {
        return EnumSet.of(
                ServerConnectionTaskStatus.Pending,
                ServerConnectionTaskStatus.NeverCompleted,
                ServerConnectionTaskStatus.Retrying,
                ServerConnectionTaskStatus.Failed,
                ServerConnectionTaskStatus.Waiting);
    }

    private EnumSet<ServerConnectionTaskStatus> intersection(EnumSet<ServerConnectionTaskStatus> s1, EnumSet<ServerConnectionTaskStatus> s2) {
        EnumSet<ServerConnectionTaskStatus> intersection = EnumSet.copyOf(s1);
        intersection.retainAll(s2);
        return intersection;
    }

    private void appendDeviceInGroupSql() {
        this.connectionTaskService.appendDeviceGroupConditions(this.deviceGroup, this.sqlBuilder, "connT");
    }

    private interface GroupByAspect {
        void appendToList(SqlBuilder sqlBuilder);
        void appendGroupByClause(SqlBuilder sqlBuilder);
    }

    private class NoGrouping implements GroupByAspect {
        @Override
        public void appendToList(SqlBuilder sqlBuilder) {
            // No grouping so we are not adding anything to the list
        }

        @Override
        public void appendGroupByClause(SqlBuilder sqlBuilder) {
            // No grouping so we are not appending a group by clase
        }
    }

    private class GroupBySingleAspect implements GroupByAspect {
        private final String aspectName;

        private GroupBySingleAspect(String aspectName) {
            super();
            this.aspectName = aspectName;
        }

        @Override
        public void appendToList(SqlBuilder sqlBuilder) {
            sqlBuilder.append(", ");
            sqlBuilder.append(this.aspectName);
        }

        @Override
        public void appendGroupByClause(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" group by ");
            sqlBuilder.append(this.aspectName);
        }
    }

}