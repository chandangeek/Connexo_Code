package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.DeviceDataModelServiceImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CommunicationDashboardBreakdownHandler implements TaskExecutor {

    private final DeviceDataModelService deviceDataModelService;

    private List<QueryEndDeviceGroup> queryEndDeviceGroupList;

    private DashboardBreakdownSqlBuilder builder;

    public CommunicationDashboardBreakdownHandler(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public synchronized void execute(TaskOccurrence occurrence) {
        DeviceDataModelServiceImpl dataModelService = (DeviceDataModelServiceImpl) deviceDataModelService;
        queryEndDeviceGroupList = dataModelService.getMeteringGroupsService().findQueryEndDeviceGroup().find(); // = find all dynamic device groups
        builder = new DashboardBreakdownSqlBuilder(queryEndDeviceGroupList);
        deleteRequiredDashboardTables();
        try (Connection connection = deviceDataModelService.dataModel().getConnection(true);
             PreparedStatement comStmnt = builder.getCommProcedure().prepare(connection);
             PreparedStatement conStmnt = builder.getConnProcedure().prepare(connection)) {
            comStmnt.execute();
            conStmnt.executeQuery();
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        updateConnectionTables();
        Logger.getLogger(CommunicationDashboardBreakdownHandler.class.getName()).log(Level.INFO, "CommunicationDashboardBreakdownHandler Execiuting Tasks");
        System.out.println("CommunicationDashboardBreakdownHandler Executing Tasks" + deviceDataModelService);
    }

    public void updateConnectionTables() {
        try (Connection connection = deviceDataModelService.dataModel().getConnection(true);
             Statement statement = connection.createStatement()) {
            statement.execute(builder.getConnTaskBreakDown().toString());
            statement.execute(builder.getDashboardCtlcswithatlstoneft().toString());
            statement.execute(builder.getDashboardCtlcsSucIndCount().toString());
            statement.execute(builder.getDashboardConTypeHeatMapBuilder().toString());
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    public void deleteRequiredDashboardTables() {
        try (Connection connection = deviceDataModelService.dataModel().getConnection(true);
             Statement statement = connection.createStatement()) {
          /*  statement.execute("BEGIN\n" +
                    "   EXECUTE IMMEDIATE 'DROP TABLE IF EXISTS' MV_CONNECTIONDATA;\n" +
                    "END;");*/
            statement.execute("delete from dashboard_comtask");
            statement.execute("delete from DASHBOARD_CONTASKBREAKDOWN");
            statement.execute("delete from DASHBOARD_CONTYPEHEATMAP");
            statement.execute("delete from DASHBOARD_CTLCSSUCINDCOUNT");
            statement.execute("delete from DASHBOARD_CTLCSWITHATLSTONEFT");
            if (deviceDataModelService.dataModel().doesTableExist("MV_CONNECTIONDATA")) {
                statement.execute("drop table MV_CONNECTIONDATA");
            }
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }

    }

    @Override
    public void postFailEvent(EventService eventService, TaskOccurrence occurrence, String cause) {
        throw new UnsupportedOperationException("Unsupported operation");
    }


}
