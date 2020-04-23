/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.device.data.impl.PreparedStatementProvider;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.util.EnumSet;
import java.util.Set;

/**
 * Builds the SQL query that counts {@link ConnectionTask}s
 * for a set of {@link TaskStatus}es.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-07 (14:08)
 */
class ConnectionTaskStatusCountSqlBuilder implements PreparedStatementProvider {

    private final EnumSet<ServerConnectionTaskStatus> taskStatusses;
    private final boolean includeBusyTasks;
    private final EndDeviceGroup deviceGroup;
    private final ConnectionTaskReportServiceImpl connectionTaskService;
    private SqlBuilder sqlBuilder;

    ConnectionTaskStatusCountSqlBuilder(Set<ServerConnectionTaskStatus> taskStatusses, EndDeviceGroup deviceGroup, ConnectionTaskReportServiceImpl connectionTaskService) {
        super();
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
            this.sqlBuilder.append("'");
            this.sqlBuilder.append(", count(*) from busyCT ");
            this.sqlBuilder.append(" UNION ALL ");
        }
        this.sqlBuilder.append("select taskStatus ");
        this.sqlBuilder.append(", count(*) from notBusyCT group by taskStatus ");
    }

    private void appendWithClauses() {
        this.sqlBuilder = new SqlBuilder("WITH ");
        if (this.includeBusyTasks) {
            this.appendBusyComTaskExecutionWithClause();
            this.sqlBuilder.append(", ");
        }
        this.appendNotBusyConnectionTaskWithClause(this.connectionTaskService.clock());
    }

    private void appendBusyComTaskExecutionWithClause() {
        this.sqlBuilder.append(" busyCT as (");
        this.sqlBuilder.append(" select * from MV_BUSYCONTASKSTCOUNT connT where 1=1 ");
        this.appendDeviceInGroupSql();
        this.sqlBuilder.append(")");
    }

    private void appendNotBusyConnectionTaskWithClause(Clock clock) {
        this.sqlBuilder.append("notBusyCT as (");
        this.sqlBuilder.append("    SELECT /*+ NO_MERGE */ 1 dummy");
        this.sqlBuilder.append(",");
        this.sqlBuilder.append("      CASE");
        this.taskStatusses.forEach(each -> each.appendBreakdownCaseClause(this.sqlBuilder, clock));
        this.sqlBuilder.append("      END taskStatus");
        this.sqlBuilder.append("    FROM MV_NOTBUSYCONTASKSTCOUNT connT where 1=1 ");
        this.appendDeviceInGroupSql();
        this.sqlBuilder.append(")");
    }

    private void appendDeviceInGroupSql() {
        this.connectionTaskService.appendDeviceGroupConditions(this.deviceGroup, this.sqlBuilder, "connT");
    }
}