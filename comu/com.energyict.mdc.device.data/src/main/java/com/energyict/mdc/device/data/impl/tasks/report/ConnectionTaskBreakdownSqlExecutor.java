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
    protected SqlBuilder sql() {
        SqlBuilder sqlBuilder = new SqlBuilder("SELECT GROUPERBY, TASKSTATUS, ITEM, NVL(SUM(\"COUNT\"),0) ");
        sqlBuilder.append("FROM DASHBOARD_CONTASKBREAKDOWN ct ");
        sqlBuilder.append(" WHERE 1=1 ");
        this.deviceGroup.ifPresent(deviceGroup -> this.appendDeviceGroupSql(deviceGroup, sqlBuilder));
        sqlBuilder.append(" GROUP BY GROUPERBY, TASKSTATUS, ITEM ");
        sqlBuilder.append(" ORDER BY GROUPERBY, TASKSTATUS");
        return sqlBuilder;
    }

    @Override
    protected SqlBuilder beforeDeviceGroupSql(Instant now) {
        return new SqlBuilder("");
    }

    @Override
    protected SqlBuilder taskStatusSql(Instant now) {
        return new SqlBuilder("");
    }

    @Override
    protected String deviceContainerAliasName() {
        return "ct";
    }

}