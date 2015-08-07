package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.PreparedStatementProvider;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumSet;
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

    private final String groupByAspect;
    private final EnumSet<ServerConnectionTaskStatus> taskStatusses;
    private final boolean includeBusyTasks;
    private final EndDeviceGroup deviceGroup;
    private final ConnectionTaskServiceImpl connectionTaskService;
    private SqlBuilder sqlBuilder;

    public ConnectionTaskBreakdownSqlBuilder(String groupByAspect, Set<ServerConnectionTaskStatus> taskStatusses, EndDeviceGroup deviceGroup, ConnectionTaskServiceImpl connectionTaskService) {
        super();
        this.groupByAspect = groupByAspect;
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
        this.appendWithClauses();
        if (this.includeBusyTasks) {
            this.sqlBuilder.append("select '");
            this.sqlBuilder.append(ServerConnectionTaskStatus.Busy.name());
            this.sqlBuilder.append("', ");
            this.sqlBuilder.append(this.groupByAspect);
            this.sqlBuilder.append(", count(*) from CT where (exists (select * from ");
            this.sqlBuilder.append(BUSY_TASK_ALIAS_NAME);
            this.sqlBuilder.append(" where ");
            this.sqlBuilder.append(BUSY_TASK_ALIAS_NAME);
            this.sqlBuilder.append(".connectiontask = id) or comserver is not null) group by ");
            this.sqlBuilder.append(this.groupByAspect);
            this.sqlBuilder.append(" UNION ALL ");
        }
        this.sqlBuilder.append("select taskStatus, ");
        this.sqlBuilder.append(this.groupByAspect);
        this.sqlBuilder.append(", count(*) from notBusyCT group by taskStatus, ");
        this.sqlBuilder.append(this.groupByAspect);
    }

    private void appendWithClauses() {
        this.sqlBuilder = new SqlBuilder("WITH ");
        this.appendBusyComTaskExecutionWithClause();
        this.sqlBuilder.append(", ");
        this.appendConnectionTaskWithClause();
        if (this.needsNotBusyConnectionTaskWithClause()) {
            this.sqlBuilder.append(", ");
            this.appendNotBusyConnectionTaskWithClause();
        }
    }

    private void appendBusyComTaskExecutionWithClause() {
        this.sqlBuilder.append(BUSY_TASK_ALIAS_NAME);
        this.sqlBuilder.append(" as (select connectiontask --+ NO_MERGE from DDC_COMTASKEXEC where comport is not null and obsolete_date is null)");
    }

    private void appendConnectionTaskWithClause() {
        this.sqlBuilder.append("CT as (");
        this.sqlBuilder.append("    SELECT --+ NO_MERGE ");
        this.appendConnectionTaskSelectClauseInWithClause(this.sqlBuilder);
        this.appendConnectionTaskFromClauseInWithClause(this.sqlBuilder);
        this.sqlBuilder.append("     WHERE connT.status = 0");
        this.sqlBuilder.append("       AND connT.obsolete_date is null");
        this.sqlBuilder.append("       AND connT.nextexecutiontimestamp is not null");
        this.appendRestrictedStatesClause();
        this.appendDeviceInGroupSql();
        this.sqlBuilder.append(")");
    }

    protected void appendConnectionTaskSelectClauseInWithClause(SqlBuilder sqlBuilder) {
        sqlBuilder.append("connT.id, connT.discriminator, connT.comserver, connT.status, connT.currentretrycount, connT.lastsuccessfulcommunicationend, connT.nextexecutiontimestamp, connT.lastExecutionFailed, connT.comportpool");
    }

    protected void appendConnectionTaskFromClauseInWithClause(SqlBuilder sqlBuilder) {
        sqlBuilder.append(" FROM DDC_CONNECTIONTASK connT");
    }

    private void appendNotBusyConnectionTaskWithClause() {
        this.sqlBuilder.append("notBusyCT as (");
        this.sqlBuilder.append("    SELECT /*+ NO_MERGE */ ");
        this.sqlBuilder.append(this.groupByAspect);
        this.sqlBuilder.append(",");
        this.sqlBuilder.append("      CASE");
        this.taskStatusses.stream().forEach(each -> each.appendBreakdownCaseClause(this.sqlBuilder, this.connectionTaskService.clock()));
        this.sqlBuilder.append("      END taskStatus");
        this.sqlBuilder.append("    FROM CT WHERE not exists (SELECT 1 FROM busytask WHERE busytask.connectiontask = id)");
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

    private void appendRestrictedStatesClause() {
        this.connectionTaskService.appendRestrictedStatesClause(this.sqlBuilder, "ct");
    }

    protected void appendDeviceInGroupSql() {
        this.connectionTaskService.appendDeviceGroupConditions(this.deviceGroup, this.sqlBuilder, "connT");
    }

}