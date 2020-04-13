/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;
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

    private ConnectionTaskBreakdownSqlExecutor(DataModel dataModel, Optional<EndDeviceGroup> deviceGroup, Optional<AmrSystem> amrSystem) {
        super(dataModel, deviceGroup, amrSystem);
    }

    static ConnectionTaskBreakdownSqlExecutor systemWide(DataModel dataModel) {
        return new ConnectionTaskBreakdownSqlExecutor(dataModel, Optional.empty(), Optional.empty());
    }

    static ConnectionTaskBreakdownSqlExecutor forGroup(EndDeviceGroup deviceGroup, AmrSystem mdcAmrSystem, DataModel dataModel) {
        return new ConnectionTaskBreakdownSqlExecutor(dataModel, Optional.of(deviceGroup), Optional.of(mdcAmrSystem));
    }

    @Override
    protected SqlBuilder beforeDeviceGroupSql(Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder("WITH ");
        sqlBuilder.append(" alldata as ( ");
        sqlBuilder.append(" select * from MV_CONTASKBREAKDOWN ");
        sqlBuilder.append(this.deviceContainerAliasName());
        sqlBuilder.append(" where 1=1 ");
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