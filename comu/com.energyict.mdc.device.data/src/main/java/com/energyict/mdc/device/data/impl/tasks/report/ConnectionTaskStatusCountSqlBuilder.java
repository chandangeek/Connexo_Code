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
import java.util.stream.Collectors;

/**
 * Builds the SQL query that counts {@link ConnectionTask}s
 * for a set of {@link TaskStatus}es.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-07 (14:08)
 */
class ConnectionTaskStatusCountSqlBuilder implements PreparedStatementProvider {

    private final EnumSet<ServerConnectionTaskStatus> taskStatusses;
    private final EndDeviceGroup deviceGroup;
    private final ConnectionTaskReportServiceImpl connectionTaskService;
    private SqlBuilder sqlBuilder;

    ConnectionTaskStatusCountSqlBuilder(Set<ServerConnectionTaskStatus> taskStatusses, EndDeviceGroup deviceGroup, ConnectionTaskReportServiceImpl connectionTaskService) {
        super();
        this.taskStatusses = EnumSet.copyOf(taskStatusses);
        this.deviceGroup = deviceGroup;
        this.connectionTaskService = connectionTaskService;
    }

    @Override
    public PreparedStatement prepare(Connection connection) throws SQLException {
        this.build();
        return this.sqlBuilder.prepare(connection);
    }

    private void build() {
        this.sqlBuilder = new SqlBuilder("SELECT TASKSTATUS, SUM(\"COUNT\") ");
        this.sqlBuilder.append("FROM DASHBOARD_CONTASKBREAKDOWN ");
        this.sqlBuilder.append("WHERE GROUPERBY = 'None' ");
        if(deviceGroup != null){
            sqlBuilder.append(" AND MRID = '");
            sqlBuilder.append(deviceGroup.getMRID());
            sqlBuilder.append("' ");
        }
        this.sqlBuilder.append("AND TASKSTATUS IN (");
        this.sqlBuilder.append(getTaskStatussesToSelect());
        this.sqlBuilder.append(") ");
        this.sqlBuilder.append(" GROUP BY TASKSTATUS ");
    }

    private String getTaskStatussesToSelect(){
        return taskStatusses.stream().map(ServerConnectionTaskStatus::name)
                .map(name -> String.format("'%s'", name))
                .collect(Collectors.joining(","));
    }

}