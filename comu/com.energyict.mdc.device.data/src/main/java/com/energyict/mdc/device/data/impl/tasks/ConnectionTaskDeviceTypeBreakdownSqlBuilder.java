package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.util.Optional;
import java.util.Set;

/**
 * Builds the SQL query that counts {@link ConnectionTask}s
 * for a set of {@link TaskStatus}es broken down by the
 * {@link com.energyict.mdc.device.config.DeviceType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-07 (14:08)
 */
class ConnectionTaskDeviceTypeBreakdownSqlBuilder extends ConnectionTaskBreakdownSqlBuilder {

    ConnectionTaskDeviceTypeBreakdownSqlBuilder(Set<ServerConnectionTaskStatus> taskStatusses, EndDeviceGroup deviceGroup, ConnectionTaskServiceImpl connectionTaskService) {
        super(Optional.of("devicetype"), taskStatusses, deviceGroup, connectionTaskService);
    }

    @Override
    protected void appendConnectionTaskSelectClauseInWithClause(SqlBuilder sqlBuilder) {
        super.appendConnectionTaskSelectClauseInWithClause(sqlBuilder);
        sqlBuilder.append(", dev.devicetype");
    }

    @Override
    protected void appendConnectionTaskFromClauseInWithClause(SqlBuilder sqlBuilder) {
        super.appendConnectionTaskFromClauseInWithClause(sqlBuilder);
        sqlBuilder.append(" JOIN DDC_DEVICE DEV on connT.device = dev.id");
    }

}