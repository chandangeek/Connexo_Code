/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */
package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardBreakdownHandler implements TaskExecutor {
    private static final Logger LOGGER = Logger.getLogger(DashboardBreakdownHandler.class.getName());

    private final DashboardBreakdownSqlBuilder builder = new DashboardBreakdownSqlBuilder();
    private final DeviceDataModelService deviceDataModelService;
    private final MeteringGroupsService meteringGroupsService;

    public DashboardBreakdownHandler(DeviceDataModelService deviceDataModelService, MeteringGroupsService meteringGroupsService) {
        this.deviceDataModelService = deviceDataModelService;
        this.meteringGroupsService = meteringGroupsService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        Handler handler = occurrence.createTaskLogHandler().asHandler();
        LOGGER.addHandler(handler);
        LOGGER.fine("Starting " + DashboardBreakdownHandler.class.getSimpleName() + " triggered at " + occurrence.getTriggerTime());
        try (Connection connection = deviceDataModelService.dataModel().getConnection(true);
             Statement statement = connection.createStatement()) {
            List<QueryEndDeviceGroup> queryEndDeviceGroupList = meteringGroupsService.getQueryEndDeviceGroupQuery().select(Condition.TRUE); // = find all dynamic device groups
            // delete all previous records
            statement.execute("delete from DASHBOARD_COMTASK");
            statement.execute("delete from DASHBOARD_CONTASKBREAKDOWN");
            statement.execute("delete from DASHBOARD_CONTYPEHEATMAP");
            statement.execute("delete from DASHBOARD_CTLCSSUCINDCOUNT");
            statement.execute("delete from DASHBOARD_CTLCSWITHATLSTONEFT");
            // prepare temporary tables
            statement.execute(builder.getDynamicGroupDataQuery(queryEndDeviceGroupList));
            statement.execute(builder.getDashboardConTaskDataQuery());
            // fill in the dashboard tables
            statement.execute(builder.getDashboardComTaskDataQuery());
            statement.execute(builder.getDashboardConTaskBreakdownDataQuery());
            statement.execute(builder.getDashboardConTaskLastSessionWithAtLeastOneFailedTaskCountDataQuery());
            statement.execute(builder.getDashboardConTaskLastSessionSuccessIndicatorCountDataQuery());
            statement.execute(builder.getDashboardConTypeHeatMapDataQuery());
            // drop temporary tables
            if (deviceDataModelService.dataModel().doesTableExist("MV_CONNECTIONDATA")) {
                statement.execute("truncate table MV_CONNECTIONDATA");
                statement.execute("drop table MV_CONNECTIONDATA");
            }
            if (deviceDataModelService.dataModel().doesTableExist("DYNAMIC_GROUP_DATA")) {
                statement.execute("truncate table DYNAMIC_GROUP_DATA");
                statement.execute("drop table DYNAMIC_GROUP_DATA");
            }
            LOGGER.fine(DashboardBreakdownHandler.class.getSimpleName() + " has finished");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, DashboardBreakdownHandler.class.getSimpleName() + " has failed: " + ex.getMessage(), ex);
            occurrence.setToFailed();
        } finally {
            LOGGER.removeHandler(handler);
        }
    }
}
