/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskStatus;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.util.Optional;
import java.util.Set;

/**
 * Builds the SQL query that counts {@link ConnectionTask}s
 * for a set of {@link TaskStatus}es broken down by the
 * {@link com.energyict.mdc.engine.config.ComPortPool}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-07 (14:08)
 */
class ConnectionTaskComPortPoolBreakdownSqlBuilder extends ConnectionTaskBreakdownSqlBuilder {

    ConnectionTaskComPortPoolBreakdownSqlBuilder(Set<ServerConnectionTaskStatus> taskStatusses, EndDeviceGroup deviceGroup, ConnectionTaskReportServiceImpl connectionTaskService) {
        super(Optional.of("comportpool"), taskStatusses, deviceGroup, connectionTaskService);
    }

}