/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */
package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.DeviceDataModelServiceImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DashboardBreakdownHandler implements TaskExecutor {

    private final DeviceDataModelService deviceDataModelService;

    private List<QueryEndDeviceGroup> queryEndDeviceGroupList;

    private DashboardBreakdownSqlBuilder builder;

    public DashboardBreakdownHandler(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public synchronized void execute(TaskOccurrence occurrence) {
        DeviceDataModelServiceImpl dataModelService = (DeviceDataModelServiceImpl) deviceDataModelService;
        queryEndDeviceGroupList = dataModelService.getMeteringGroupsService().findQueryEndDeviceGroup().find(); // = find all dynamic device groups
        builder = new DashboardBreakdownSqlBuilder(queryEndDeviceGroupList);
        deleteRequiredDashboardTables();
        try (Connection connection = deviceDataModelService.dataModel().getConnection(true);
             PreparedStatement dynamicGroup = builder.createGlobalTableForDynamicDeviceGroup().prepare(connection);
             PreparedStatement comStmnt = builder.getCommProcedure().prepare(connection);
             PreparedStatement conStmnt = builder.getConnProcedure().prepare(connection)) {
            dynamicGroup.execute();
            comStmnt.execute();
            conStmnt.execute();
            updateConnectionTables(connection);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        Logger.getLogger(DashboardBreakdownHandler.class.getName()).log(Level.INFO, "Task executing by CommunicationDashboardBreakdownHandler");
    }

    public void updateConnectionTables(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(builder.getConnTaskBreakDown().toString());
            statement.execute(builder.getDashboardCtlcswithatlstoneft().toString());
            statement.execute(builder.getDashboardCtlcsSucIndCount().toString());
            statement.execute(builder.getDashboardConTypeHeatMapBuilder().toString());
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
    }

    public void deleteRequiredDashboardTables() {
        try (Connection connection = deviceDataModelService.dataModel().getConnection(true);
             Statement statement = connection.createStatement()) {
            statement.execute("delete from dashboard_comtask");
            statement.execute("delete from DASHBOARD_CONTASKBREAKDOWN");
            statement.execute("delete from DASHBOARD_CONTYPEHEATMAP");
            statement.execute("delete from DASHBOARD_CTLCSSUCINDCOUNT");
            statement.execute("delete from DASHBOARD_CTLCSWITHATLSTONEFT");
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }

    }

    @Override
    public void postFailEvent(EventService eventService, TaskOccurrence occurrence, String cause) {
        throw new UnsupportedOperationException("Unsupported operation");
    }


}
