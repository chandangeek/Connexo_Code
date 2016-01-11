package com.energyict.mdc.device.data.impl.tasks.report;

import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerComTaskStatus;
import com.energyict.mdc.device.data.tasks.ConnectionTaskBreakdowns;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

/**
 * Builds and executes the query that produces the data
 * for the {@link ConnectionTaskBreakdowns}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-10-05 (12:37)
 */
@LiteralSql
class ConnectionTaskBreakdownSqlExecutor extends AbstractBreakdownSqlExecutor {

    private static final String BASE_SQL =
            "WITH ctsFromCtes as (\n" +
            "  SELECT connectiontask\n" +
            "    FROM " + TableSpecs.DDC_COMTASKEXEC.name() + "\n" +
            "   WHERE comport is not null\n" +
            "     AND obsolete_date is null\n" +
            "   GROUP BY connectiontask\n" +
            "),\n" +
            "alldata as (\n" +
            "  SELECT ct.connectiontypepluggableclass,\n" +
            "         ct.comportpool,\n" +
            "         dev.devicetype,\n" +
            "         CASE WHEN ctsFromCtes.connectiontask IS NOT NULL\n" +
            "                OR ct.comserver          IS NOT NULL" +
            "              THEN '" + ServerComTaskStatus.Busy.name() + "'\n" +
            "              WHEN (discriminator = '" + ConnectionTaskImpl.INBOUND_DISCRIMINATOR + "' AND status > 0)\n" +
            "                OR (discriminator = '" + ConnectionTaskImpl.SCHEDULED_DISCRIMINATOR + "' AND (status > 0 OR nextExecutionTimestamp is null))" +
            "              THEN '" + ServerComTaskStatus.OnHold.name() + "'\n" +
            "              WHEN nextexecutiontimestamp <= ?" +
            "              THEN '" + ServerComTaskStatus.Pending.name() + "'\n" +
            "              WHEN currentretrycount = 0\n" +
            "               AND nextexecutiontimestamp > ?\n" +
            "               AND lastsuccessfulcommunicationend is null" +
            "              THEN '" + ServerComTaskStatus.NeverCompleted.name() + "'\n" +
            "              WHEN currentretrycount > 0\n" +
            "               AND nextexecutiontimestamp > ?" +
            "              THEN '" + ServerComTaskStatus.Retrying.name() + "'\n" +
            "              WHEN currentretrycount = 0\n" +
            "               AND lastExecutionFailed = 1\n" +
            "               AND nextexecutiontimestamp > ?\n" +
            "               AND lastsuccessfulcommunicationend is not null" +
            "              THEN '" + ServerComTaskStatus.Failed.name() + "'\n" +
            "              WHEN currentretrycount = 0\n" +
            "               AND lastExecutionFailed = 0\n" +
            "               AND nextexecutiontimestamp > ?\n" +
            "               AND lastsuccessfulcommunicationend is not null" +
            "              THEN '" + ServerComTaskStatus.Waiting.name() + "'\n" +
            "              ELSE 'Unknown'\n" +
            "          END taskStatus\n" +
            "         --\n" +
            "    FROM DDC_CONNECTIONTASK ct\n" +
            "         JOIN DDC_DEVICE DEV on ct.device = dev.id\n" +
            "         LEFT OUTER JOIN ctsFromCtes on ct.id = ctsFromCtes.connectiontask\n" +
            "   WHERE ct.status = 0\n" +
            "     AND ct.obsolete_date is null\n" +
            "     AND ct.nextexecutiontimestamp is not null\n";
    private static final String SQL_REMAINDER =
            "),\n" +
            "grouped as (\n" +
            "  SELECT connectiontypepluggableclass,\n" +
            "         comportpool,\n" +
            "         devicetype,\n" +
            "         taskStatus,\n" +
            "         count(*) as counter\n" +
            "    FROM alldata\n" +
            "   GROUP BY connectiontypepluggableclass, comportpool, devicetype, taskstatus\n" +
            ")\n" +
            "SELECT '" + BreakdownType.None.name() + "', taskStatus, null as item, nvl(sum(counter), 0)\n" +
            "  FROM grouped\n" +
            " GROUP BY taskStatus\n" +
            "UNION ALL\n" +
            "SELECT '" + BreakdownType.ComPortPool.name() + "', taskStatus, comportpool, nvl(sum(counter), 0)\n" +
            "  FROM grouped\n" +
            " GROUP BY taskStatus, comportpool\n" +
            "UNION ALL\n" +
            "SELECT '" + BreakdownType.ConnectionType.name() + "', taskStatus, connectiontypepluggableclass, nvl(sum(counter), 0)\n" +
            "  FROM grouped\n" +
            " GROUP BY taskStatus, connectiontypepluggableclass\n" +
            "UNION ALL\n" +
            "SELECT '" + BreakdownType.DeviceType.name() + "', taskStatus, devicetype, nvl(sum(counter), 0)\n" +
            "  FROM grouped\n" +
            " GROUP BY taskStatus, devicetype\n" +
            " ORDER BY 1, 2, 3";

    private static final int NUMBER_OF_UTC_SECONDS_BINDS = 5;

    static ConnectionTaskBreakdownSqlExecutor systemWide(DataModel dataModel) {
        return new ConnectionTaskBreakdownSqlExecutor(dataModel, Optional.<EndDeviceGroup>empty(), Optional.<AmrSystem>empty());
    }

    static ConnectionTaskBreakdownSqlExecutor forGroup(EndDeviceGroup deviceGroup, AmrSystem mdcAmrSystem, DataModel dataModel) {
        return new ConnectionTaskBreakdownSqlExecutor(dataModel, Optional.of(deviceGroup), Optional.of(mdcAmrSystem));
    }

    private ConnectionTaskBreakdownSqlExecutor(DataModel dataModel, Optional<EndDeviceGroup> deviceGroup, Optional<AmrSystem> amrSystem) {
        super(dataModel, deviceGroup, amrSystem);
    }

    @Override
    protected String beforeDeviceStateSql() {
        return BASE_SQL;
    }

    @Override
    protected int bindBeforeDeviceStateSql(PreparedStatement statement, Instant now, int startPosition) {
        return startPosition;
    }

    @Override
    protected String taskStatusSql() {
        return SQL_REMAINDER;
    }

    @Override
    protected String deviceContainerAliasName() {
        return "ct";
    }

    @Override
    protected int bindTaskStatusSql(PreparedStatement statement, Instant now, int startPosition) throws SQLException {
        int bindPosition = startPosition;
        for (int i = 0; i < NUMBER_OF_UTC_SECONDS_BINDS; i++) {
            statement.setLong(bindPosition++, now.getEpochSecond());
        }
        return bindPosition;
    }

}