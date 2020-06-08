/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.ConnectionFunction;
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

import static com.energyict.mdc.device.data.impl.InstallerV10_8Impl.getComTaskDTHeatMapStatement;
import static com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl.ComTaskExecType.FIRMWARE_COM_TASK_EXECUTION_DISCRIMINATOR;
import static com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl.ComTaskExecType.MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR;

public class UpgraderV10_9 implements Upgrader {

    private static final int SIZE = 100;//100 for optimization
    private static final String TABLE = TableSpecs.DDC_COMTASKEXEC.name();
    private static final int MIN_REFRESH_INTERVAL = 5;

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
            List<Device> devices;
            do {
                devices = deviceService.findAllDevices(Condition.TRUE).paged(from, SIZE).find();
                String querySQL = createExecutionsList(devices);
                from += SIZE;
                if (!querySQL.equals("")) {
                    execute(statement, querySQL);
                }
            }
            while (!devices.isEmpty());
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        recreateJob();
    }

    private String createExecutionsList(List<Device> devices) {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO ")
                .append(TABLE)
                .append(" (ID,VERSIONCOUNT,CREATETIME,MODTIME,USERNAME,DISCRIMINATOR,DEVICE,COMTASK,COMSCHEDULE,NEXTEXECUTIONSPECS" +
                        ",LASTEXECUTIONTIMESTAMP,NEXTEXECUTIONTIMESTAMP,COMPORT,OBSOLETE_DATE,PRIORITY,USEDEFAULTCONNECTIONTASK,CURRENTRETRYCOUNT" +
                        ",PLANNEDNEXTEXECUTIONTIMESTAMP,EXECUTIONPRIORITY,EXECUTIONSTART,LASTSUCCESSFULCOMPLETION,LASTEXECUTIONFAILED,ONHOLD,CONNECTIONTASK" +
                        ",IGNORENEXTEXECSPECS,CONNECTIONFUNCTION,LASTSESSION,LASTSESS_HIGHESTPRIOCOMPLCODE,LASTSESS_SUCCESSINDICATOR) ");
        boolean first = true;
        for (Device device : devices) {
            Set<Long> comTaskIdsWithExecution = device.getComTaskExecutions().stream().map(comTaskExecution -> comTaskExecution.getComTask().getId()).collect(Collectors.toSet());
            List<ComTaskEnablement> comTasksWithoutExecutions = device.getDeviceConfiguration().getComTaskEnablements().stream()
                    .filter(comTaskEnablement -> !comTaskIdsWithExecution.contains(comTaskEnablement.getComTask().getId()))
                    .collect(Collectors.toList());
            for (ComTaskEnablement comTaskEnablement : comTasksWithoutExecutions) {
                id++;
                if (!first) {
                    query.append("UNION ALL");
                } else {
                    first = false;
                }
                query.append(buildInsertString(comTaskEnablement, device));
            }
        }
        return first ? "" : query.toString();
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

    private String insertRowSql(ComTaskEnablement comTaskEnablement, int discriminator, Long connectiontaskid, long deviceid) {
        StringBuilder query = new StringBuilder();
        query
                .append(" SELECT '").append(id).append("', ")
                .append("'").append(1).append("', ")
                .append("'").append(Instant.now().toEpochMilli()).append("', ")
                .append("'").append(Instant.now().toEpochMilli()).append("', ")
                .append("'").append("Installer").append("', ")
                .append("'").append(discriminator).append("', ")
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
                .append(comTaskEnablement.getConnectionFunction().map(ConnectionFunction::getId).map(id -> "'" + id + "', ").orElse("null, "))
                .append("null, ")
                .append("null, ")
                .append("null\n")
                .append("FROM DUAL\n");
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
        return dataModel.getRefreshJob("REF_MV_COMTASKDTHEATMAP", "MV_COMTASKDTHEATMAP",
                getComTaskDTHeatMapStatement(), MIN_REFRESH_INTERVAL);
    }
}
