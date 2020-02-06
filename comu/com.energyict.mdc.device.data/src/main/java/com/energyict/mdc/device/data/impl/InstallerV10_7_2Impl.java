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
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" BEGIN ");
        sqlBuilder.append(" DBMS_SCHEDULER.CREATE_JOB  ");
        sqlBuilder.append(" ( ");
        sqlBuilder.append(" JOB_NAME            => 'REF_MV_ConnectionTypeHeatMap', ");
        sqlBuilder.append(" JOB_TYPE            => 'PLSQL_BLOCK', ");
        sqlBuilder.append(" JOB_ACTION          => ' ");
        sqlBuilder.append(" BEGIN ");
        sqlBuilder.append(" execute immediate ");
        sqlBuilder.append(" ''CREATE  TABLE  MV_CONNECTIONTYPEHEATMAP_NEW AS select ct.*, failedTask.comSession failedTask_comSession  ");
        sqlBuilder.append(" from DDC_CONNECTIONTASK ct  ");
        sqlBuilder.append(" LEFT JOIN  ");
        sqlBuilder.append(" (   select comsession from DDC_COMTASKEXECSESSION  ");
        sqlBuilder.append(" 	where successindicator > 0 group by comsession  ");
        sqlBuilder.append(" ) failedTask on ct.lastSession = failedTask.comSession, ");
        sqlBuilder.append(" DDC_DEVICE dev,  ");
        sqlBuilder.append(" MTR_ENDDEVICESTATUS ES,  ");
        sqlBuilder.append(" FSM_STATE FS  ");
        sqlBuilder.append(" where FS.OBSOLETE_TIMESTAMP IS NULL   ");
        sqlBuilder.append(" and FS.NAME not in (''''dlc.default.inStock'''', ''''dlc.default.decommissioned'''')  ");
        sqlBuilder.append(" and ES.STARTTIME  <= round((SYSDATE - date ''''1970-01-01'''')*24*60*60*1000)   ");
        sqlBuilder.append(" and ES.ENDTIME > round((SYSDATE - date ''''1970-01-01'''')*24*60*60*1000)   ");
        sqlBuilder.append(" and ES.STATE = FS.ID  ");
        sqlBuilder.append(" and ES.enddevice = dev.meterid  ");
        sqlBuilder.append(" and ct.device = dev.id  ");
        sqlBuilder.append(" and ct.obsolete_date is null  ");
        sqlBuilder.append(" and ct.status = 0''; ");
        sqlBuilder.append(" execute immediate ''DROP TABLE MV_CONNECTIONTYPEHEATMAP''; ");
        sqlBuilder.append(" execute immediate ''RENAME MV_CONNECTIONTYPEHEATMAP_NEW TO MV_CONNECTIONTYPEHEATMAP''; ");
        sqlBuilder.append(" EXCEPTION ");
        sqlBuilder.append("    WHEN OTHERS THEN ");
        sqlBuilder.append(" 	  IF SQLCODE != -942 THEN ");
        sqlBuilder.append(" 		 RAISE; ");
        sqlBuilder.append(" 	  END IF; ");
        sqlBuilder.append(" END;', ");
        sqlBuilder.append(" NUMBER_OF_ARGUMENTS => 0, ");
        sqlBuilder.append(" START_DATE          => SYSTIMESTAMP, ");
        sqlBuilder.append(" REPEAT_INTERVAL     => 'FREQ=MINUTELY;INTERVAL=5', ");
        sqlBuilder.append(" END_DATE            => NULL, ");
        sqlBuilder.append(" ENABLED             => TRUE, ");
        sqlBuilder.append(" AUTO_DROP           => FALSE, ");
        sqlBuilder.append(" COMMENTS            => 'JOB TO REFRESH' ");
        sqlBuilder.append(" ); ");
        sqlBuilder.append(" END;");
        return sqlBuilder.toString();
    }

    private String getRefreshMvCTLCSWithAtLstOneFTJobStatement(){
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" BEGIN ");
        sqlBuilder.append(" DBMS_SCHEDULER.CREATE_JOB  ");
        sqlBuilder.append(" ( ");
        sqlBuilder.append(" JOB_NAME            => 'REF_MV_CTLCSWithAtLstOneFT', ");
        sqlBuilder.append(" JOB_TYPE            => 'PLSQL_BLOCK', ");
        sqlBuilder.append(" JOB_ACTION          => ' ");
        sqlBuilder.append(" BEGIN ");
        sqlBuilder.append(" execute immediate ");
        sqlBuilder.append(" ''CREATE  TABLE MV_CTLCSWithAtLstOneFT_NEW AS select ct.* from DDC_CONNECTIONTASK ct  ");
        sqlBuilder.append(" join DDC_DEVICE dev on ct.device = dev.id,  ");
        sqlBuilder.append(" MTR_ENDDEVICESTATUS ES, ");
        sqlBuilder.append(" FSM_STATE FS ");
        sqlBuilder.append(" where FS.OBSOLETE_TIMESTAMP IS NULL  ");
        sqlBuilder.append(" and FS.NAME not in (''''dlc.default.inStock'''', ''''dlc.default.decommissioned'''') ");
        sqlBuilder.append(" and ES.STARTTIME  <= round((SYSDATE - date ''''1970-01-01'''')*24*60*60*1000)  ");
        sqlBuilder.append(" and ES.ENDTIME > round((SYSDATE - date ''''1970-01-01'''')*24*60*60*1000)  ");
        sqlBuilder.append(" and ES.STATE = FS.ID ");
        sqlBuilder.append(" and ES.enddevice = dev.meterid ");
        sqlBuilder.append(" and ct.obsolete_date is null  ");
        sqlBuilder.append(" and ct.nextexecutiontimestamp is not null  ");
        sqlBuilder.append(" and ct.lastSessionSuccessIndicator = 0  ");
        sqlBuilder.append(" and exists (  ");
        sqlBuilder.append("  select * from DDC_COMTASKEXECSESSION ctes  ");
        sqlBuilder.append("  where ctes.COMSESSION = ct.lastSession  ");
        sqlBuilder.append("  and ctes.SUCCESSINDICATOR > 0  ");
        sqlBuilder.append(" )''; ");
        sqlBuilder.append(" execute immediate ''DROP TABLE MV_CTLCSWithAtLstOneFT''; ");
        sqlBuilder.append(" execute immediate ''RENAME MV_CTLCSWithAtLstOneFT_NEW TO MV_CTLCSWithAtLstOneFT''; ");
        sqlBuilder.append(" EXCEPTION ");
        sqlBuilder.append("    WHEN OTHERS THEN ");
        sqlBuilder.append(" 	  IF SQLCODE != -942 THEN ");
        sqlBuilder.append(" 		 RAISE; ");
        sqlBuilder.append(" 	  END IF; ");
        sqlBuilder.append(" END;', ");
        sqlBuilder.append(" NUMBER_OF_ARGUMENTS => 0, ");
        sqlBuilder.append(" START_DATE          => SYSTIMESTAMP, ");
        sqlBuilder.append(" REPEAT_INTERVAL     => 'FREQ=MINUTELY;INTERVAL=5', ");
        sqlBuilder.append(" END_DATE            => NULL, ");
        sqlBuilder.append(" ENABLED             => TRUE, ");
        sqlBuilder.append(" AUTO_DROP           => FALSE, ");
        sqlBuilder.append(" COMMENTS            => 'JOB TO REFRESH' ");
        sqlBuilder.append(" ); ");
        sqlBuilder.append(" END;");
        return sqlBuilder.toString();
    }

    private String getRefreshMvCTLCSSucIndCountJobStatement(){
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" BEGIN ");
        sqlBuilder.append(" DBMS_SCHEDULER.CREATE_JOB  ");
        sqlBuilder.append(" ( ");
        sqlBuilder.append(" JOB_NAME            => 'REF_MV_CTLCSSucIndCount', ");
        sqlBuilder.append(" JOB_TYPE            => 'PLSQL_BLOCK', ");
        sqlBuilder.append(" JOB_ACTION          => ' ");
        sqlBuilder.append(" BEGIN ");
        sqlBuilder.append(" execute immediate ");
        sqlBuilder.append(" ''CREATE TABLE MV_CTLCSSucIndCount_NEW AS  ");
        sqlBuilder.append(" select ct.* from DDC_CONNECTIONTASK ct  ");
        sqlBuilder.append(" join DDC_DEVICE dev on ct.device = dev.id,   ");
        sqlBuilder.append(" MTR_ENDDEVICESTATUS ES,  ");
        sqlBuilder.append(" FSM_STATE FS  ");
        sqlBuilder.append(" where FS.OBSOLETE_TIMESTAMP IS NULL   ");
        sqlBuilder.append(" and FS.NAME not in (''''dlc.default.inStock'''', ''''dlc.default.decommissioned'''')  ");
        sqlBuilder.append(" and ES.STARTTIME  <= round((SYSDATE - date ''''1970-01-01'''')*24*60*60*1000)   ");
        sqlBuilder.append(" and ES.ENDTIME > round((SYSDATE - date ''''1970-01-01'''')*24*60*60*1000)   ");
        sqlBuilder.append(" and ES.STATE = FS.ID  ");
        sqlBuilder.append(" and ES.enddevice = dev.meterid  ");
        sqlBuilder.append(" and ct.obsolete_date is null  ");
        sqlBuilder.append(" and ct.nextexecutiontimestamp is not null  ");
        sqlBuilder.append(" and ct.lastsession is not null''; ");
        sqlBuilder.append(" execute immediate ''DROP TABLE MV_CTLCSSucIndCount''; ");
        sqlBuilder.append(" execute immediate ''RENAME MV_CTLCSSucIndCount_NEW TO MV_CTLCSSucIndCount''; ");
        sqlBuilder.append(" EXCEPTION ");
        sqlBuilder.append("    WHEN OTHERS THEN ");
        sqlBuilder.append(" 	  IF SQLCODE != -942 THEN ");
        sqlBuilder.append(" 		 RAISE; ");
        sqlBuilder.append(" 	  END IF; ");
        sqlBuilder.append(" END;', ");
        sqlBuilder.append(" NUMBER_OF_ARGUMENTS => 0, ");
        sqlBuilder.append(" START_DATE          => SYSTIMESTAMP, ");
        sqlBuilder.append(" REPEAT_INTERVAL     => 'FREQ=MINUTELY;INTERVAL=5', ");
        sqlBuilder.append(" END_DATE            => NULL, ");
        sqlBuilder.append(" ENABLED             => TRUE, ");
        sqlBuilder.append(" AUTO_DROP           => FALSE, ");
        sqlBuilder.append(" COMMENTS            => 'JOB TO REFRESH' ");
        sqlBuilder.append(" ); ");
        sqlBuilder.append(" END;");
        return sqlBuilder.toString();
    }
}
