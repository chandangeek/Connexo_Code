/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */
package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardBreakdownHandler implements TaskExecutor {
    private static final Logger LOGGER = Logger.getLogger(DashboardBreakdownHandler.class.getName());

    private final DashboardBreakdownSqlBuilder builder = new DashboardBreakdownSqlBuilder();
    private final DeviceDataModelService deviceDataModelService;
    private final OrmService ormService;

    public DashboardBreakdownHandler(DeviceDataModelService deviceDataModelService, OrmService ormService) {
        this.deviceDataModelService = deviceDataModelService;
        this.ormService = ormService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        LOGGER.log(Level.FINE, "Starting " + DashboardBreakdownHandler.class.getSimpleName() + " triggered at " + occurrence.getTriggerTime());
        List<QueryEndDeviceGroup> queryEndDeviceGroupList = ormService.getDataModel(MeteringGroupsService.COMPONENTNAME)
                .get()
                .stream(QueryEndDeviceGroup.class)
                .select(); // = find all dynamic device groups
        try (Connection connection = deviceDataModelService.dataModel().getConnection(true);
             Statement statement = connection.createStatement()) {
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
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        LOGGER.log(Level.FINE, DashboardBreakdownHandler.class.getSimpleName() + " has finished");
    }
}
