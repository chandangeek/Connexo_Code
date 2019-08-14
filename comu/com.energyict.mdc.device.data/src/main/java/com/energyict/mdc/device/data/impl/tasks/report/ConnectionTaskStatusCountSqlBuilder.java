/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskStatus;

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

    ConnectionTaskStatusCountSqlBuilder(Set<ServerConnectionTaskStatus> taskStatusses, EndDeviceGroup deviceGroup, ConnectionTaskReportServiceImpl connectionTaskService) {
        super(Optional.empty(), taskStatusses, deviceGroup, connectionTaskService);
    }

}