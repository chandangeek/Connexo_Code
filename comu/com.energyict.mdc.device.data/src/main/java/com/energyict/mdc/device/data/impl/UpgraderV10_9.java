/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.events.ComTaskExecutionCreatorEventHandler;

import javax.inject.Inject;

public class UpgraderV10_9 implements Upgrader {

    private final DeviceService deviceService;
    private final DataModel dataModel;
    private final ComTaskExecutionCreatorEventHandler comTaskExecutionCreatorEventHandler;

    @Inject
    UpgraderV10_9(DataModel dataModel, DeviceService deviceService, ComTaskExecutionCreatorEventHandler comTaskExecutionCreatorEventHandler) {
        this.deviceService = deviceService;
        this.dataModel = dataModel;
        this.comTaskExecutionCreatorEventHandler = comTaskExecutionCreatorEventHandler;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9));
        deviceService.findAllDevices(Condition.TRUE).stream().forEach(comTaskExecutionCreatorEventHandler::createComTaskExecutionsForDevice);
        recreateJob();
    }

    private void recreateJob() {
        execute(dataModel, dropJob("REF_MV_COMTASKDTHEATMAP"));
        execute(dataModel, getRefreshMvComTaskDTHeatMapJobStatement());
    }

    private String dropJob(String jobName) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" BEGIN ");
        sqlBuilder.append(" DBMS_SCHEDULER.DROP_JOB  ");
        sqlBuilder.append(" ( ");
        sqlBuilder.append(" JOB_NAME            => '").append(jobName).append("'");
        sqlBuilder.append(" ); ");
        sqlBuilder.append(" END;");
        return sqlBuilder.toString();
    }

    private String getRefreshMvComTaskDTHeatMapJobStatement() {
        return getRefreshJob("REF_MV_COMTASKDTHEATMAP", "MV_COMTASKDTHEATMAP",
                getComTaskDTHeatMapStatement(), 5);
    }

    private String getRefreshJob(String jobName, String tableName, String createTableStatement, int minRefreshInterval) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" BEGIN ");
        sqlBuilder.append(" DBMS_SCHEDULER.CREATE_JOB  ");
        sqlBuilder.append(" ( ");
        sqlBuilder.append(" JOB_NAME            => '").append(jobName).append("', ");
        sqlBuilder.append(" JOB_TYPE            => 'PLSQL_BLOCK', ");
        sqlBuilder.append(" JOB_ACTION          => ' ");
        sqlBuilder.append(" BEGIN ");
        sqlBuilder.append(" execute immediate ''DROP TABLE ").append(tableName).append("''; ");
        sqlBuilder.append(" execute immediate ");
        sqlBuilder.append(" ''");
        sqlBuilder.append(createTableStatement.replace("'", "''''"));
        sqlBuilder.append(" ''; ");
        sqlBuilder.append(" EXCEPTION ");
        sqlBuilder.append("    WHEN OTHERS THEN ");
        sqlBuilder.append(" 	  IF SQLCODE != -942 THEN ");
        sqlBuilder.append(" 		 RAISE; ");
        sqlBuilder.append(" 	  END IF; ");
        sqlBuilder.append(" END;', ");
        sqlBuilder.append(" NUMBER_OF_ARGUMENTS => 0, ");
        sqlBuilder.append(" START_DATE          => SYSTIMESTAMP, ");
        sqlBuilder.append(" REPEAT_INTERVAL     => 'FREQ=MINUTELY;INTERVAL=").append(minRefreshInterval).append("', ");
        sqlBuilder.append(" END_DATE            => NULL, ");
        sqlBuilder.append(" ENABLED             => TRUE, ");
        sqlBuilder.append(" AUTO_DROP           => FALSE, ");
        sqlBuilder.append(" COMMENTS            => 'JOB TO REFRESH' ");
        sqlBuilder.append(" ); ");
        sqlBuilder.append(" END;");
        return sqlBuilder.toString();
    }

    private String getComTaskDTHeatMapStatement() {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" CREATE TABLE MV_COMTASKDTHEATMAP");
        sqlBuilder.append(" AS select ");
        sqlBuilder.append(" 	  dev.DEVICETYPE ");
        sqlBuilder.append(" 	, cte.lastsess_highestpriocomplcode ");
        sqlBuilder.append(" 	, cte.device ");
        sqlBuilder.append(" from ");
        sqlBuilder.append("   DDC_COMTASKEXEC cte ");
        sqlBuilder.append("   join DDC_DEVICE dev on cte.device = dev.id ");
        sqlBuilder.append("   join ( ");
        sqlBuilder.append(" 	   select ");
        sqlBuilder.append(" 		  ES.enddevice id ");
        sqlBuilder.append(" 	   from ");
        sqlBuilder.append(" 		  MTR_ENDDEVICESTATUS ES ");
        sqlBuilder.append(" 		, ( ");
        sqlBuilder.append(" 			 select ");
        sqlBuilder.append(" 					FS.ID ");
        sqlBuilder.append(" 			 from ");
        sqlBuilder.append(" 					FSM_STATE FS ");
        sqlBuilder.append(" 			 where ");
        sqlBuilder.append(" 					FS.OBSOLETE_TIMESTAMP IS NULL ");
        sqlBuilder.append(" 					and FS.STAGE not in ");
        sqlBuilder.append(" 					( ");
        sqlBuilder.append(" 					   SELECT ");
        sqlBuilder.append(" 							  FSTG.ID ");
        sqlBuilder.append(" 					   FROM ");
        sqlBuilder.append(" 							  FSM_STAGE FSTG ");
        sqlBuilder.append(" 					   WHERE ");
        sqlBuilder.append(" 							  FSTG.NAME in ('mtr.enddevicestage.preoperational' ");
        sqlBuilder.append(" 										  , 'mtr.enddevicestage.postoperational') ");
        sqlBuilder.append(" 					) ");
        sqlBuilder.append(" 		  ) ");
        sqlBuilder.append(" 		  FS ");
        sqlBuilder.append(" 	   where ");
        sqlBuilder.append(" 		  ES.STARTTIME  <= round((SYSDATE - date '1970-01-01')*24*60*60*1000) ");
        sqlBuilder.append(" 		  and ES.ENDTIME > round((SYSDATE - date '1970-01-01')*24*60*60*1000) ");
        sqlBuilder.append(" 		  and ES.STATE   = FS.ID ");
        sqlBuilder.append("   ) kd on dev.meterid = kd.id ");
        sqlBuilder.append("   left join DDC_HIPRIOCOMTASKEXEC hp ON hp.comtaskexecution = cte.id ");
        sqlBuilder.append(" where ");
        sqlBuilder.append("   cte.obsolete_date       is null ");
        return sqlBuilder.toString();
    }
}
