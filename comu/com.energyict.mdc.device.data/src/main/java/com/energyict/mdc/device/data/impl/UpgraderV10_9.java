/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.FirmwareManagementTask;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl.ComTaskExecType.FIRMWARE_COM_TASK_EXECUTION_DISCRIMINATOR;
import static com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl.ComTaskExecType.MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR;

public class UpgraderV10_9 implements Upgrader {

    private static final int SIZE = 100;//100 for optimization
    private static final String TABLE = TableSpecs.DDC_COMTASKEXEC.name();

    private final DeviceService deviceService;
    private final DataModel dataModel;

    private long id;

    @Inject
    UpgraderV10_9(DataModel dataModel, DeviceService deviceService) {
        this.deviceService = deviceService;
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9));
        try (Connection connection = dataModel.getConnection(true);
             Statement statement = connection.createStatement()) {
            int from = 0;
            id = executeQuery(statement, "SELECT NVL(MAX(ID),0) FROM " + TABLE, this::toLong);
            List<Device> devices = deviceService.findAllDevices(Condition.TRUE).paged(from, SIZE).find();
            while (!devices.isEmpty()) {
                String querySQL = createExecutionsList(devices);
                from += SIZE;
                execute(statement, querySQL);
                devices = deviceService.findAllDevices(Condition.TRUE).paged(from, SIZE).find();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        recreateJob();
    }

    private String createExecutionsList(List<Device> devices) {
        StringBuilder query = new StringBuilder();
        query.append("INSERT ALL ");
        for (Device device : devices) {
            Set<Long> comTaskIdsWithExecution = device.getComTaskExecutions().stream().map(comTaskExecution -> comTaskExecution.getComTask().getId()).collect(Collectors.toSet());
            List<ComTaskEnablement> comTasksWithoutExecutions = device.getDeviceConfiguration().getComTaskEnablements().stream()
                    .filter(comTaskEnablement -> !comTaskIdsWithExecution.contains(comTaskEnablement.getComTask().getId()))
                    .collect(Collectors.toList());
            for (ComTaskEnablement comTaskEnablement : comTasksWithoutExecutions) {
                id++;
                query.append(buildInsertString(comTaskEnablement, device));
            }
        }
        query.append(" SELECT * FROM DUAL ");
        return query.toString();
    }

    private String buildInsertString(ComTaskEnablement comTaskEnablement, Device device) {
        String query = "";
        Long connectiontaskid = null;
        if (comTaskEnablement.hasPartialConnectionTask()) {
            connectiontaskid = device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                    .map(HasId::getId)
                    .findFirst()
                    .orElseGet(() -> null);
        } else if (comTaskEnablement.usesDefaultConnectionTask()) {
            Optional<ConnectionTask<?, ?>> connectionTask = device.getConnectionTasks().stream().filter(ConnectionTask::isDefault).findFirst();
            connectiontaskid = connectionTask.isPresent() ? connectionTask.get().getId() : null;
        }
        if (comTaskEnablement.getComTask().getProtocolTasks().stream().anyMatch(protocolTask -> protocolTask instanceof FirmwareManagementTask)) {
            query = insertRowSql(comTaskEnablement, FIRMWARE_COM_TASK_EXECUTION_DISCRIMINATOR.ordinal(), connectiontaskid, device.getId());
        } else {
            query = insertRowSql(comTaskEnablement, MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR.ordinal(), connectiontaskid, device.getId());
        }
        return query;
    }

    private String insertRowSql(ComTaskEnablement comTaskEnablement, int descriminator, Long connectiontaskid, long deviceid) {
        StringBuilder query = new StringBuilder();
        query.append(" INTO ")
                .append(TABLE)
                .append(" (ID,VERSIONCOUNT,CREATETIME,MODTIME,USERNAME,DISCRIMINATOR,DEVICE,COMTASK,COMSCHEDULE,NEXTEXECUTIONSPECS,LASTEXECUTIONTIMESTAMP,NEXTEXECUTIONTIMESTAMP,COMPORT,OBSOLETE_DATE,PRIORITY,USEDEFAULTCONNECTIONTASK,CURRENTRETRYCOUNT,PLANNEDNEXTEXECUTIONTIMESTAMP,EXECUTIONPRIORITY,EXECUTIONSTART,LASTSUCCESSFULCOMPLETION,LASTEXECUTIONFAILED,ONHOLD,CONNECTIONTASK,IGNORENEXTEXECSPECS,CONNECTIONFUNCTION,LASTSESSION,LASTSESS_HIGHESTPRIOCOMPLCODE,LASTSESS_SUCCESSINDICATOR) ")
                .append(" values ('").append(this.id).append("', ")
                .append("'").append(1).append("', ")
                .append("'").append(Instant.now().toEpochMilli()).append("', ")
                .append("'").append(Instant.now().toEpochMilli()).append("', ")
                .append("'").append("root").append("', ")
                .append("'").append(descriminator).append("', ")
                .append("'").append(deviceid).append("', ")
                .append("'").append(comTaskEnablement.getComTask().getId()).append("', ")
                .append("null, ")
                .append("null, ")
                .append("null, ")
                .append("null, ")
                .append("null, ")//comport
                .append("null, ")
                .append("'").append(comTaskEnablement.getPriority()).append("', ")
                .append("'").append(comTaskEnablement.usesDefaultConnectionTask() ? 1 : 0).append("', ")
                .append("'").append(0).append("', ")
                .append("null, ")
                .append("'").append(comTaskEnablement.getPriority()).append("', ")
                .append("null, ")
                .append("null, ")
                .append("'").append(0).append("', ")
                .append("'").append(0).append("', ")
                .append(connectiontaskid == null ? "null, " : "'" + connectiontaskid + "', ")
                .append("'").append(comTaskEnablement.isIgnoreNextExecutionSpecsForInbound() ? 1 : 0).append("', ")
                .append(comTaskEnablement.getConnectionFunction().isPresent() ? "'" + comTaskEnablement.getConnectionFunction().get().getId() + "', " : "null, ")
                .append("null, ")
                .append("null, ")
                .append("null)\n");
        return query.toString();
    }

    private Long toLong(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return resultSet.getLong(1);
        } else {
            return null;
        }
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
