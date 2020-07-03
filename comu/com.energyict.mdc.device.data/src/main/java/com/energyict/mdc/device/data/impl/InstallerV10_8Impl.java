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

public class InstallerV10_8Impl implements FullInstaller {
    private final DataModel dataModel;
    private final OrmService ormService;

    @Inject
    public InstallerV10_8Impl(DataModel dataModel, OrmService ormService) {
        this.dataModel = dataModel;
        this.ormService = ormService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        execute(dataModel, getConnectionTasksBreakDownStatement());
        execute(dataModel, getCommunicationTasksBreakDownStatement());
        execute(dataModel, getComTaskDTHeatMapStatement());
        execute(dataModel, getComTaskExWithDevStsStatement());
        if (ormService.isTest()) {
            return;
        }
        execute(dataModel, getRefreshMvConnectionTasksBreakDownJobStatement());
        execute(dataModel, getRefreshMvCommunicationTasksBreakDownJobStatement());
        execute(dataModel, getRefreshMvComTaskDTHeatMapJobStatement());
        execute(dataModel, getRefreshMvComTaskExWithDevStsJobStatement());
    }

    private String getConnectionTasksBreakDownStatement() {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" CREATE TABLE MV_CONTASKBREAKDOWN");
        sqlBuilder.append(" AS SELECT ");
        sqlBuilder.append(" ct.connectiontypepluggableclass ");
        sqlBuilder.append(" , ct.device ");
        sqlBuilder.append(" , ct.comportpool ");
        sqlBuilder.append(" , dev.devicetype ");
        sqlBuilder.append(" , CASE ");
        sqlBuilder.append(" 	WHEN ctsFromCtes.connectiontask IS NOT NULL ");
        sqlBuilder.append(" 					OR ct.comport   IS NOT NULL ");
        sqlBuilder.append(" 					THEN 'Busy' ");
        sqlBuilder.append(" 	WHEN ( discriminator = '1' AND status    > 0) ");
        sqlBuilder.append(" 		OR (discriminator = '2' ");
        sqlBuilder.append(" 			AND ( ");
        sqlBuilder.append(" 				status > 0 ");
        sqlBuilder.append(" 				OR nextExecutionTimestamp is null ");
        sqlBuilder.append(" 			) ");
        sqlBuilder.append(" 		) ");
        sqlBuilder.append(" 		THEN 'OnHold' ");
        sqlBuilder.append(" 	WHEN nextexecutiontimestamp <= round((SYSDATE - date '1970-01-01')*24*60*60) ");
        sqlBuilder.append(" 		THEN 'Pending' ");
        sqlBuilder.append(" 	WHEN currentretrycount = 0 ");
        sqlBuilder.append(" 		AND nextexecutiontimestamp > round((SYSDATE - date '1970-01-01')*24*60*60) ");
        sqlBuilder.append(" 		AND lastsuccessfulcommunicationend is null ");
        sqlBuilder.append(" 		THEN 'NeverCompleted' ");
        sqlBuilder.append(" 	WHEN currentretrycount > 0 ");
        sqlBuilder.append(" 		AND nextexecutiontimestamp > round((SYSDATE - date '1970-01-01')*24*60*60) ");
        sqlBuilder.append(" 		THEN 'Retrying' ");
        sqlBuilder.append(" 	WHEN currentretrycount = 0 ");
        sqlBuilder.append(" 		AND lastExecutionFailed = 1 ");
        sqlBuilder.append(" 		AND nextexecutiontimestamp > round((SYSDATE - date '1970-01-01')*24*60*60) ");
        sqlBuilder.append(" 		AND lastsuccessfulcommunicationend is not null ");
        sqlBuilder.append(" 		THEN 'Failed' ");
        sqlBuilder.append(" 	WHEN currentretrycount = 0 ");
        sqlBuilder.append(" 		AND lastExecutionFailed = 0 ");
        sqlBuilder.append(" 		AND nextexecutiontimestamp > round((SYSDATE - date '1970-01-01')*24*60*60) ");
        sqlBuilder.append(" 		AND lastsuccessfulcommunicationend is not null ");
        sqlBuilder.append(" 		THEN 'Waiting' ");
        sqlBuilder.append(" 		ELSE 'ProcessingError' ");
        sqlBuilder.append(" END taskStatus ");
        sqlBuilder.append(" FROM ");
        sqlBuilder.append(" DDC_CONNECTIONTASK ct ");
        sqlBuilder.append(" JOIN DDC_DEVICE DEV on ct.device = dev.id ");
        sqlBuilder.append(" JOIN( ");
        sqlBuilder.append("    select ES.enddevice id ");
        sqlBuilder.append("    from MTR_ENDDEVICESTATUS ES ");
        sqlBuilder.append(" 		, FSM_STATE FS ");
        sqlBuilder.append("    where ");
        sqlBuilder.append(" 	  ES.STARTTIME <= round((SYSDATE - date '1970-01-01')*24*60*60*1000) ");
        sqlBuilder.append(" 	  and ES.ENDTIME > round((SYSDATE - date '1970-01-01')*24*60*60*1000) ");
        sqlBuilder.append(" 	  and FS.OBSOLETE_TIMESTAMP IS NULL ");
        sqlBuilder.append(" 	  and FS.NAME not in ('dlc.default.inStock' ");
        sqlBuilder.append(" 						, 'dlc.default.decommissioned') ");
        sqlBuilder.append(" 	  and ES.STATE = FS.ID ");
        sqlBuilder.append(" 	) kd on dev.meterid = kd.id ");
        sqlBuilder.append(" LEFT OUTER JOIN ");
        sqlBuilder.append(" 	( ");
        sqlBuilder.append(" 	 SELECT ");
        sqlBuilder.append(" 			  connectiontask ");
        sqlBuilder.append(" 	 FROM ");
        sqlBuilder.append(" 			  DDC_COMTASKEXEC ");
        sqlBuilder.append(" 	 WHERE ");
        sqlBuilder.append(" 			  comport       is not null ");
        sqlBuilder.append(" 			  AND obsolete_date is null ");
        sqlBuilder.append(" 	 GROUP BY ");
        sqlBuilder.append(" 			  connectiontask ");
        sqlBuilder.append(" 	) ");
        sqlBuilder.append(" 	ctsFromCtes on ct.id = ctsFromCtes.connectiontask ");
        sqlBuilder.append(" WHERE ");
        sqlBuilder.append(" 	ct.status  = 0 ");
        sqlBuilder.append(" 	AND ct.obsolete_date is null ");
        sqlBuilder.append(" 	AND ct.nextexecutiontimestamp is not null");
        return sqlBuilder.toString();
    }

    private String getCommunicationTasksBreakDownStatement() {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" CREATE TABLE MV_COMTASKBREAKDOWN");
        sqlBuilder.append(" AS SELECT ");
        sqlBuilder.append(" 	cte.id ");
        sqlBuilder.append("   , cte.nextexecutiontimestamp ");
        sqlBuilder.append("   , cte.lastexecutiontimestamp ");
        sqlBuilder.append("   , cte.plannednextexecutiontimestamp ");
        sqlBuilder.append("   , cte.discriminator ");
        sqlBuilder.append("   , cte.nextexecutionspecs ");
        sqlBuilder.append("   , cte.comport ");
        sqlBuilder.append("   , cte.onhold ");
        sqlBuilder.append("   , cte.currentretrycount ");
        sqlBuilder.append("   , cte.lastsuccessfulcompletion ");
        sqlBuilder.append("   , cte.lastexecutionfailed ");
        sqlBuilder.append("   , cte.comtask ");
        sqlBuilder.append("   , cte.comschedule ");
        sqlBuilder.append("   , cte.device ");
        sqlBuilder.append("   , dev.devicetype ");
        sqlBuilder.append("   , CASE ");
        sqlBuilder.append(" 		WHEN ct.id IS NULL ");
        sqlBuilder.append(" 			THEN 0 ");
        sqlBuilder.append(" 			ELSE 1 ");
        sqlBuilder.append(" 	END as thereisabusytask ");
        sqlBuilder.append("   , CASE ");
        sqlBuilder.append(" 		WHEN hp.comtaskexecution = cte.id ");
        sqlBuilder.append(" 			THEN 1 ");
        sqlBuilder.append(" 			ELSE 0 ");
        sqlBuilder.append(" 	END as isapriotask ");
        sqlBuilder.append(" FROM ");
        sqlBuilder.append(" 	DDC_COMTASKEXEC cte ");
        sqlBuilder.append(" 	JOIN DDC_DEVICE dev	ON cte.device = dev.id ");
        sqlBuilder.append(" 	JOIN ");
        sqlBuilder.append(" 		( ");
        sqlBuilder.append(" 		   select ");
        sqlBuilder.append(" 				  ES.enddevice id ");
        sqlBuilder.append(" 		   from ");
        sqlBuilder.append(" 				  MTR_ENDDEVICESTATUS ES ");
        sqlBuilder.append(" 				, ( ");
        sqlBuilder.append(" 					 select ");
        sqlBuilder.append(" 						FS.ID ");
        sqlBuilder.append(" 					 from ");
        sqlBuilder.append(" 						FSM_STATE FS ");
        sqlBuilder.append(" 					 where ");
        sqlBuilder.append(" 						FS.OBSOLETE_TIMESTAMP IS NULL ");
        sqlBuilder.append(" 						and FS.NAME not in ('dlc.default.inStock' ");
        sqlBuilder.append(" 										  , 'dlc.default.decommissioned') ");
        sqlBuilder.append(" 				  ) ");
        sqlBuilder.append(" 				  FS ");
        sqlBuilder.append(" 		   where ");
        sqlBuilder.append(" 				  ES.STARTTIME  <= round((SYSDATE - date '1970-01-01')*24*60*60*1000) ");
        sqlBuilder.append(" 				  and ES.ENDTIME > round((SYSDATE - date '1970-01-01')*24*60*60*1000) ");
        sqlBuilder.append(" 				  and ES.STATE   = FS.ID ");
        sqlBuilder.append(" 		) kd ON dev.meterid = kd.id ");
        sqlBuilder.append(" 	LEFT OUTER JOIN DDC_CONNECTIONTASK ct ");
        sqlBuilder.append(" 		ON  cte.connectiontask            = ct.id ");
        sqlBuilder.append(" 			AND ct.comPort      is not null ");
        sqlBuilder.append(" 			AND ct.lastCommunicationStart > cte.nextExecutionTimestamp ");
        sqlBuilder.append(" 	LEFT JOIN DDC_HIPRIOCOMTASKEXEC hp ");
        sqlBuilder.append(" 		ON hp.comtaskexecution = cte.id ");
        sqlBuilder.append(" WHERE cte.obsolete_date is null");
        return sqlBuilder.toString();
    }

    static String getComTaskDTHeatMapStatement() {
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

    private String getComTaskExWithDevStsStatement() {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" CREATE TABLE MV_COMTASKEXWITHDEVSTS");
        sqlBuilder.append(" AS select ");
        sqlBuilder.append(" 	/* +INLINE */ ");
        sqlBuilder.append(" 	cte.obsolete_date ");
        sqlBuilder.append("   , cte.nextExecutionTimestamp ");
        sqlBuilder.append("   , cte.lastExecutionTimestamp ");
        sqlBuilder.append("   , cte.lastSuccessfulCompletion ");
        sqlBuilder.append("   , cte.currentretrycount ");
        sqlBuilder.append("   , cte.lastExecutionFailed ");
        sqlBuilder.append("   , cte.comport ");
        sqlBuilder.append("   , cte.onhold ");
        sqlBuilder.append("   , cte.device ");
        sqlBuilder.append("   , cte.id ");
        sqlBuilder.append("   , hp.comtaskexecution ");
        sqlBuilder.append("   , ct.id as thereisabusytask ");
        sqlBuilder.append("   , fs.stage ");
        sqlBuilder.append(" from ");
        sqlBuilder.append(" 	DDC_COMTASKEXEC cte ");
        sqlBuilder.append(" 	join DDC_DEVICE dev on cte.device = dev.id ");
        sqlBuilder.append(" 	LEFT OUTER JOIN DDC_CONNECTIONTASK ct ");
        sqlBuilder.append(" 		on ");
        sqlBuilder.append(" 		ct.id = cte.connectiontask ");
        sqlBuilder.append(" 		AND ct.comport IS NOT NULL ");
        sqlBuilder.append(" 		and ct.lastCommunicationStart > cte.nextExecutionTimestamp ");
        sqlBuilder.append(" 	LEFT JOIN DDC_HIPRIOCOMTASKEXEC hp ON hp.comtaskexecution = cte.id ");
        sqlBuilder.append(" 	, MTR_ENDDEVICESTATUS ES ");
        sqlBuilder.append(" 	, FSM_STATE FS ");
        sqlBuilder.append(" where ");
        sqlBuilder.append(" 	cte.obsolete_date is null ");
        sqlBuilder.append(" 	and ES.STARTTIME <= round((SYSDATE - date '1970-01-01')*24*60*60*1000) ");
        sqlBuilder.append(" 	and ES.ENDTIME > round((SYSDATE - date '1970-01-01')*24*60*60*1000) ");
        sqlBuilder.append(" 	and ES.STATE = FS.ID ");
        sqlBuilder.append(" 	and ES.enddevice = dev.meterid ");
        sqlBuilder.append(" 	and FS.OBSOLETE_TIMESTAMP IS NULL");
        return sqlBuilder.toString();
    }

    private String getRefreshMvConnectionTasksBreakDownJobStatement() {
        return dataModel.getRefreshJob("REF_MV_CONTASKBREAKDOWN", "MV_CONTASKBREAKDOWN",
                getConnectionTasksBreakDownStatement(), 5);
    }

    private String getRefreshMvCommunicationTasksBreakDownJobStatement() {
        return dataModel.getRefreshJob("REF_MV_COMTASKBREAKDOWN", "MV_COMTASKBREAKDOWN",
                getCommunicationTasksBreakDownStatement(), 5);
    }

    private String getRefreshMvComTaskDTHeatMapJobStatement() {
        return dataModel.getRefreshJob("REF_MV_COMTASKDTHEATMAP", "MV_COMTASKDTHEATMAP",
                getComTaskDTHeatMapStatement(), 5);
    }

    private String getRefreshMvComTaskExWithDevStsJobStatement() {
        return dataModel.getRefreshJob("REF_MV_COMTASKEXWITHDEVSTS", "MV_COMTASKEXWITHDEVSTS",
                getComTaskExWithDevStsStatement(), 5);
    }
}
