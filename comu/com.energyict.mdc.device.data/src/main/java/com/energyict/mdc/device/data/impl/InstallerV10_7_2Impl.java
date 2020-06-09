/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

public class InstallerV10_7_2Impl implements FullInstaller {
    private final DataModel dataModel;
    private final OrmService ormService;

    @Inject
    public InstallerV10_7_2Impl(DataModel dataModel, OrmService ormService) {
        this.dataModel = dataModel;
        this.ormService = ormService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        execute(dataModel, getConnectionTasksLastComSessionsWithAtLeastOneFailedTaskViewStatement());
        execute(dataModel, getConnectionTaskLastComSessionSuccessIndicatorCountStatement());
        execute(dataModel, getconnectionTypeHeatMapStatement());
        if (ormService.isTest()) {
            return;
        }
        execute(dataModel, getRefreshMvConnectionTypeHeatMapJobStatement());
        execute(dataModel, getRefreshMvCTLCSWithAtLstOneFTJobStatement());
        execute(dataModel, getRefreshMvCTLCSSucIndCountJobStatement());
    }

    private String getConnectionTasksLastComSessionsWithAtLeastOneFailedTaskViewStatement(){
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" CREATE TABLE MV_CTLCSWithAtLstOneFT AS ");
        sqlBuilder.append(" select ct.* from DDC_CONNECTIONTASK ct  ");
        sqlBuilder.append(" join DDC_DEVICE dev on ct.device = dev.id,  ");
        sqlBuilder.append(" MTR_ENDDEVICESTATUS ES, ");
        sqlBuilder.append(" FSM_STATE FS ");
        sqlBuilder.append(" where FS.OBSOLETE_TIMESTAMP IS NULL  ");
        sqlBuilder.append(" and FS.NAME not in ('dlc.default.inStock', 'dlc.default.decommissioned') ");
        sqlBuilder.append(" and ES.STARTTIME  <= round((SYSDATE - date '1970-01-01')*24*60*60*1000)  ");
        sqlBuilder.append(" and ES.ENDTIME > round((SYSDATE - date '1970-01-01')*24*60*60*1000)  ");
        sqlBuilder.append(" and ES.STATE = FS.ID ");
        sqlBuilder.append(" and ES.enddevice = dev.meterid ");
        sqlBuilder.append(" and ct.obsolete_date is null  ");
        sqlBuilder.append(" and ct.nextexecutiontimestamp is not null  ");
        sqlBuilder.append(" and ct.lastSessionSuccessIndicator = 0  ");
        sqlBuilder.append(" and exists (  ");
        sqlBuilder.append("  select * from DDC_COMTASKEXECSESSION ctes  ");
        sqlBuilder.append("  where ctes.COMSESSION = ct.lastSession  ");
        sqlBuilder.append("  and ctes.SUCCESSINDICATOR > 0  ");
        sqlBuilder.append(" )");
        return sqlBuilder.toString();
    }

    private String getConnectionTaskLastComSessionSuccessIndicatorCountStatement(){
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" CREATE TABLE MV_CTLCSSucIndCount AS  ");
        sqlBuilder.append(" select ct.* from DDC_CONNECTIONTASK ct  ");
        sqlBuilder.append(" join DDC_DEVICE dev on ct.device = dev.id,   ");
        sqlBuilder.append(" MTR_ENDDEVICESTATUS ES,  ");
        sqlBuilder.append(" FSM_STATE FS  ");
        sqlBuilder.append(" where FS.OBSOLETE_TIMESTAMP IS NULL   ");
        sqlBuilder.append(" and FS.NAME not in ('dlc.default.inStock', 'dlc.default.decommissioned')  ");
        sqlBuilder.append(" and ES.STARTTIME  <= round((SYSDATE - date '1970-01-01')*24*60*60*1000)   ");
        sqlBuilder.append(" and ES.ENDTIME > round((SYSDATE - date '1970-01-01')*24*60*60*1000)   ");
        sqlBuilder.append(" and ES.STATE = FS.ID  ");
        sqlBuilder.append(" and ES.enddevice = dev.meterid  ");
        sqlBuilder.append(" and ct.obsolete_date is null  ");
        sqlBuilder.append(" and ct.nextexecutiontimestamp is not null  ");
        sqlBuilder.append(" and ct.lastsession is not null");
        return sqlBuilder.toString();
    }

    private String getconnectionTypeHeatMapStatement(){
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" CREATE TABLE MV_ConnectionTypeHeatMap AS  ");
        sqlBuilder.append(" select ct.*, failedTask.comSession failedTask_comSession  ");
        sqlBuilder.append(" from DDC_CONNECTIONTASK ct  ");
        sqlBuilder.append(" LEFT JOIN  ");
        sqlBuilder.append(" (   select comsession from DDC_COMTASKEXECSESSION  ");
        sqlBuilder.append("     where successindicator > 0 group by comsession  ");
        sqlBuilder.append(" ) failedTask on ct.lastSession = failedTask.comSession, ");
        sqlBuilder.append(" DDC_DEVICE dev,  ");
        sqlBuilder.append(" MTR_ENDDEVICESTATUS ES,  ");
        sqlBuilder.append(" FSM_STATE FS  ");
        sqlBuilder.append(" where FS.OBSOLETE_TIMESTAMP IS NULL   ");
        sqlBuilder.append(" and FS.NAME not in ('dlc.default.inStock', 'dlc.default.decommissioned')  ");
        sqlBuilder.append(" and ES.STARTTIME  <= round((SYSDATE - date '1970-01-01')*24*60*60*1000)   ");
        sqlBuilder.append(" and ES.ENDTIME > round((SYSDATE - date '1970-01-01')*24*60*60*1000)   ");
        sqlBuilder.append(" and ES.STATE = FS.ID  ");
        sqlBuilder.append(" and ES.enddevice = dev.meterid  ");
        sqlBuilder.append(" and ct.device = dev.id  ");
        sqlBuilder.append(" and ct.obsolete_date is null  ");
        sqlBuilder.append(" and ct.status = 0");
        return sqlBuilder.toString();
    }

    private String getRefreshMvConnectionTypeHeatMapJobStatement(){
        return dataModel.getRefreshJob("REF_MV_ConnectionTypeHeatMap", "MV_CONNECTIONTYPEHEATMAP",
                getconnectionTypeHeatMapStatement(), 5);
    }

    private String getRefreshMvCTLCSWithAtLstOneFTJobStatement(){
        return dataModel.getRefreshJob("REF_MV_CTLCSWithAtLstOneFT", "MV_CTLCSWithAtLstOneFT",
                getConnectionTasksLastComSessionsWithAtLeastOneFailedTaskViewStatement(), 5);
    }

    private String getRefreshMvCTLCSSucIndCountJobStatement(){
        return dataModel.getRefreshJob("REF_MV_CTLCSSucIndCount", "MV_CTLCSSucIndCount",
                getConnectionTaskLastComSessionSuccessIndicatorCountStatement(), 5);
    }
}
