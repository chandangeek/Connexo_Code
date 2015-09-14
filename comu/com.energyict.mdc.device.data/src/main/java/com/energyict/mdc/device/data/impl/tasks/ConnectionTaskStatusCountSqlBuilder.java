package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

import java.util.Optional;
import java.util.Set;

/**
 * Builds the SQL query that counts {@link ConnectionTask}s
 * for a set of {@link TaskStatus}es.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-07 (14:08)
 */
class ConnectionTaskStatusCountSqlBuilder extends ConnectionTaskBreakdownSqlBuilder {

    ConnectionTaskStatusCountSqlBuilder(Set<ServerConnectionTaskStatus> taskStatusses, EndDeviceGroup deviceGroup, ConnectionTaskServiceImpl connectionTaskService) {
        super(Optional.empty(), taskStatusses, deviceGroup, connectionTaskService);
    }

}