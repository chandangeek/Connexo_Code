/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.data.impl.tasks.ServerComTaskStatus;
import com.energyict.mdc.device.data.tasks.CommunicationTaskBreakdowns;

import java.time.Instant;
import java.util.Optional;

/**
 * Builds and executes the query that produces the data
 * for the {@link CommunicationTaskBreakdowns}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-25 (16:40)
 */
@LiteralSql
class CommunicationTaskBreakdownSqlExecutor extends AbstractBreakdownSqlExecutor {

    private static final String DEVICE_STATE_ALIAS_NAME = "enddevices";

    private CommunicationTaskBreakdownSqlExecutor(DataModel dataModel, Optional<EndDeviceGroup> deviceGroup, Optional<AmrSystem> amrSystem) {
        super(dataModel, deviceGroup, amrSystem);
    }

    static CommunicationTaskBreakdownSqlExecutor systemWide(DataModel dataModel) {
        return new CommunicationTaskBreakdownSqlExecutor(dataModel, Optional.empty(), Optional.empty());
    }

    static CommunicationTaskBreakdownSqlExecutor forGroup(EndDeviceGroup deviceGroup, AmrSystem mdcAmrSystem, DataModel dataModel) {
        return new CommunicationTaskBreakdownSqlExecutor(dataModel, Optional.of(deviceGroup), Optional.of(mdcAmrSystem));
    }

    @Override
    protected SqlBuilder sql() {
        SqlBuilder sqlBuilder = new SqlBuilder("SELECT TASKTYPE, STATUS, COMSCHEDULE, NVL(SUM(\"COUNT\"),0)");
        sqlBuilder.append("FROM DASHBOARD_COMTASK ct ");
        sqlBuilder.append(" WHERE QUERYTYPE = 'COMTASK_Q1' ");
        this.deviceGroup.ifPresent(deviceGroup -> this.appendDeviceGroupSql(deviceGroup, sqlBuilder));
        sqlBuilder.append(" GROUP BY TASKTYPE, STATUS, COMSCHEDULE ");
        sqlBuilder.append(" ORDER BY TASKTYPE, STATUS");
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