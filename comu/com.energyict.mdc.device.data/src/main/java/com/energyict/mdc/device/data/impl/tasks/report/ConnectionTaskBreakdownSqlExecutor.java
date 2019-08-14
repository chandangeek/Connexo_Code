/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.DeviceStateSqlBuilder;
import com.energyict.mdc.device.data.impl.tasks.ServerComTaskStatus;
import com.energyict.mdc.device.data.tasks.ConnectionTaskBreakdowns;

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

    static ConnectionTaskBreakdownSqlExecutor systemWide(DataModel dataModel) {
        return new ConnectionTaskBreakdownSqlExecutor(dataModel, Optional.empty(), Optional.empty());
    }

    static ConnectionTaskBreakdownSqlExecutor forGroup(EndDeviceGroup deviceGroup, AmrSystem mdcAmrSystem, DataModel dataModel) {
        return new ConnectionTaskBreakdownSqlExecutor(dataModel, Optional.of(deviceGroup), Optional.of(mdcAmrSystem));
    }

    private ConnectionTaskBreakdownSqlExecutor(DataModel dataModel, Optional<EndDeviceGroup> deviceGroup, Optional<AmrSystem> amrSystem) {
        super(dataModel, deviceGroup, amrSystem);
    }

    @Override
    protected SqlBuilder beforeDeviceGroupSql(Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder("WITH ");
        DeviceStateSqlBuilder
                .forDefaultExcludedStates("enddevices")
                .appendRestrictedStatesWithClause(sqlBuilder, now);
        sqlBuilder.append(", ");
        sqlBuilder.append("ctsFromCtes as (");
        sqlBuilder.append("  SELECT connectiontask");
        sqlBuilder.append("    FROM ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append("   WHERE comport is not null");
        sqlBuilder.append("     AND obsolete_date is null");
        sqlBuilder.append("   GROUP BY connectiontask");
        sqlBuilder.append("),");
        sqlBuilder.append("alldata as (");
        sqlBuilder.append("  SELECT ct.connectiontypepluggableclass,");
        sqlBuilder.append("         ct.comportpool,");
        sqlBuilder.append("         dev.devicetype,");
        sqlBuilder.append("         CASE WHEN ctsFromCtes.connectiontask IS NOT NULL");
        sqlBuilder.append("                OR ct.comserver          IS NOT NULL");
        sqlBuilder.append("              THEN '" + ServerComTaskStatus.Busy.name() + "'");
        sqlBuilder.append("              WHEN (   discriminator = '");
        sqlBuilder.append(ConnectionTaskImpl.INBOUND_DISCRIMINATOR);
        sqlBuilder.append("'");
        sqlBuilder.append("                    AND status > 0)");
        sqlBuilder.append("                OR (   discriminator = '");
        sqlBuilder.append(ConnectionTaskImpl.SCHEDULED_DISCRIMINATOR);
        sqlBuilder.append("'");
        sqlBuilder.append("                    AND (status > 0 OR nextExecutionTimestamp is null))");
        sqlBuilder.append("              THEN '");
        sqlBuilder.append(ServerComTaskStatus.OnHold.name());
        sqlBuilder.append("'");
        sqlBuilder.append("              WHEN nextexecutiontimestamp <=");
        sqlBuilder.addLong(now.getEpochSecond());
        sqlBuilder.append("              THEN '");
        sqlBuilder.append(ServerComTaskStatus.Pending.name());
        sqlBuilder.append("'");
        sqlBuilder.append("              WHEN currentretrycount = 0");
        sqlBuilder.append("               AND nextexecutiontimestamp >");
        sqlBuilder.addLong(now.getEpochSecond());
        sqlBuilder.append("               AND lastsuccessfulcommunicationend is null");
        sqlBuilder.append("              THEN '");
        sqlBuilder.append(ServerComTaskStatus.NeverCompleted.name());
        sqlBuilder.append("'");
        sqlBuilder.append("              WHEN currentretrycount > 0");
        sqlBuilder.append("               AND nextexecutiontimestamp >");
        sqlBuilder.addLong(now.getEpochSecond());
        sqlBuilder.append("              THEN '");
        sqlBuilder.append(ServerComTaskStatus.Retrying.name());
        sqlBuilder.append("'");
        sqlBuilder.append("              WHEN currentretrycount = 0");
        sqlBuilder.append("               AND lastExecutionFailed = 1");
        sqlBuilder.append("               AND nextexecutiontimestamp >");
        sqlBuilder.addLong(now.getEpochSecond());
        sqlBuilder.append("               AND lastsuccessfulcommunicationend is not null");
        sqlBuilder.append("              THEN '");
        sqlBuilder.append(ServerComTaskStatus.Failed.name());
        sqlBuilder.append("'");
        sqlBuilder.append("              WHEN currentretrycount = 0");
        sqlBuilder.append("               AND lastExecutionFailed = 0");
        sqlBuilder.append("               AND nextexecutiontimestamp >");
        sqlBuilder.addLong(now.getEpochSecond());
        sqlBuilder.append("               AND lastsuccessfulcommunicationend is not null");
        sqlBuilder.append("              THEN '");
        sqlBuilder.append(ServerComTaskStatus.Waiting.name());
        sqlBuilder.append("'");
        sqlBuilder.append("              ELSE '" + ServerComTaskStatus.ProcessingError.name() + "'");
        sqlBuilder.append("          END taskStatus");
        sqlBuilder.append("    FROM DDC_CONNECTIONTASK ct");
        sqlBuilder.append("         JOIN DDC_DEVICE DEV on ct.device = dev.id");
        sqlBuilder.append("         JOIN enddevices kd on dev.meterid = kd.id");
        sqlBuilder.append("         LEFT OUTER JOIN ctsFromCtes on ct.id = ctsFromCtes.connectiontask");
        sqlBuilder.append("   WHERE ct.status = 0");
        sqlBuilder.append("     AND ct.obsolete_date is null");
        sqlBuilder.append("     AND ct.nextexecutiontimestamp is not null");
        return sqlBuilder;
    }

    @Override
    protected SqlBuilder taskStatusSql(Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder("),");
        sqlBuilder.append("grouped as (");
        sqlBuilder.append("  SELECT connectiontypepluggableclass,");
        sqlBuilder.append("         comportpool,");
        sqlBuilder.append("         devicetype,");
        sqlBuilder.append("         taskStatus,");
        sqlBuilder.append("         count(*) as counter");
        sqlBuilder.append("    FROM alldata");
        sqlBuilder.append("   GROUP BY connectiontypepluggableclass, comportpool, devicetype, taskstatus");
        sqlBuilder.append(")");
        sqlBuilder.append("SELECT '");
        sqlBuilder.append(BreakdownType.None.name());
        sqlBuilder.append("', taskStatus, null as item, nvl(sum(counter), 0)");
        sqlBuilder.append("  FROM grouped");
        sqlBuilder.append(" GROUP BY taskStatus ");
        sqlBuilder.append("UNION ALL ");
        sqlBuilder.append("SELECT '");
        sqlBuilder.append(BreakdownType.ComPortPool.name());
        sqlBuilder.append("', taskStatus, comportpool, nvl(sum(counter), 0)");
        sqlBuilder.append("  FROM grouped");
        sqlBuilder.append(" GROUP BY taskStatus, comportpool ");
        sqlBuilder.append("UNION ALL ");
        sqlBuilder.append("SELECT '");
        sqlBuilder.append(BreakdownType.ConnectionType.name());
        sqlBuilder.append("', taskStatus, connectiontypepluggableclass, nvl(sum(counter), 0)");
        sqlBuilder.append("  FROM grouped");
        sqlBuilder.append(" GROUP BY taskStatus, connectiontypepluggableclass ");
        sqlBuilder.append("UNION ALL ");
        sqlBuilder.append("SELECT '");
        sqlBuilder.append(BreakdownType.DeviceType.name());
        sqlBuilder.append("', taskStatus, devicetype, nvl(sum(counter), 0)");
        sqlBuilder.append("  FROM grouped");
        sqlBuilder.append(" GROUP BY taskStatus, devicetype");
        sqlBuilder.append(" ORDER BY 1, 2, 3");
        return sqlBuilder;
    }

    @Override
    protected String deviceContainerAliasName() {
        return "ct";
    }

}