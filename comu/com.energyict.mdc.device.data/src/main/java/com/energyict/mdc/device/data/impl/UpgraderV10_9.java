/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
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
import com.energyict.mdc.device.data.impl.ami.servicecall.ServiceCallCommands;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl.ComTaskExecType.FIRMWARE_COM_TASK_EXECUTION_DISCRIMINATOR;
import static com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl.ComTaskExecType.MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR;

public class UpgraderV10_9 implements Upgrader {
    private static final Logger LOGGER = Logger.getLogger(UpgraderV10_9.class.getName());
    private static final int SIZE = 100;//100 for optimization
    private final static String IPV6ADDRESS_SUBSCRIBER = "IPv6AddressSubscriber";
    private static final String TABLE = TableSpecs.DDC_COMTASKEXEC.name();
    private static final String ID_SEQUENCE_NAME = "ddc_comtaskexecid";
    private static final int MIN_REFRESH_INTERVAL = 5;

    private final DeviceService deviceService;
    private final DataModel dataModel;
    private final MessageService messageService;
    private final EventService eventService;
    private final InstallerV10_2Impl installerV10_2;

    private long id;

    @Inject
    UpgraderV10_9(DataModel dataModel,
                  DeviceService deviceService,
                  MessageService messageService,
                  EventService eventService,
                  InstallerV10_2Impl installerV10_2) {
        this.deviceService = deviceService;
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.eventService = eventService;
        this.installerV10_2 = installerV10_2;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9));
        EventType.CREDIT_AMOUNT_CREATED.createIfNotExists(eventService);
        EventType.CREDIT_AMOUNT_UPDATED.createIfNotExists(eventService);
        installerV10_2.createServiceCallTypeIfNotPresent(ServiceCallCommands.ServiceCallTypeMapping.updateCreditAmount);
        createUnsubscriberForMessageQueue();
        try (Connection connection = dataModel.getConnection(true);
             Statement statement = connection.createStatement()) {
            int from = 0;
            id = executeQuery(statement, "SELECT NVL(MAX(ID),0) FROM " + TABLE, this::toLong);
            List<Device> devices;
            do {
                devices = deviceService.findAllDevices(Condition.TRUE).paged(from, SIZE).find();
                String querySQL = createExecutionsList(devices);
                from += SIZE + 1;
                if (!querySQL.equals("")) {
                    execute(statement, querySQL);
                }
            }
            while (devices.size() > SIZE);
            execute(statement, "drop sequence " + ID_SEQUENCE_NAME);
            execute(statement, "create sequence " + ID_SEQUENCE_NAME + " start with " + id + " cache 1000");
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        updateJobProcedure();
        try {
            upgradeDDCTable();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
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
                    .orElse(null);
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

    private void updateJobProcedure() {
        try {
            execute(dataModel, InstallerV10_8_1Impl.getStoredProcedureScript("com_task_dashboard_procedure.sql"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errors on update of dashboard related procedures!", e);
        }
    }

    private Long toLong(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return resultSet.getLong(1);
        } else {
            return null;
        }
    }

    private void createUnsubscriberForMessageQueue() {
        execute(dataModel, "delete from APS_SUBSCRIBEREXECUTIONSPEC where SUBSCRIBERSPEC = '" + IPV6ADDRESS_SUBSCRIBER + "'");
        messageService.getDestinationSpec(EventService.JUPITER_EVENTS)
                .ifPresent(jupiterEvents -> {
                    boolean subscriberExists = jupiterEvents.getSubscribers()
                            .stream()
                            .anyMatch(s -> s.getName().equals(IPV6ADDRESS_SUBSCRIBER));

                    if (subscriberExists) {
                        jupiterEvents.unSubscribe(IPV6ADDRESS_SUBSCRIBER);
                    }
                });
    }

    private void upgradeDDCTable() throws SQLException {
        try (Connection connection = this.dataModel.getConnection(true)) {
            String query = "select * from user_cons_columns where constraint_name = 'FK_DDC_COMTASKEXEC_LASTSESS'";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        execute(dataModel, "ALTER TABLE DDC_COMTASKEXEC ADD CONSTRAINT FK_DDC_COMTASKEXEC_LASTSESS FOREIGN KEY (LASTSESSION) REFERENCES DDC_COMTASKEXECSESSION (Id) ON DELETE CASCADE");
                    } else {
                        execute(dataModel, "ALTER TABLE DDC_COMTASKEXEC DROP CONSTRAINT FK_DDC_COMTASKEXEC_LASTSESS DROP INDEX",
                                "ALTER TABLE DDC_COMTASKEXEC ADD CONSTRAINT FK_DDC_COMTASKEXEC_LASTSESS FOREIGN KEY (LASTSESSION) REFERENCES DDC_COMTASKEXECSESSION (Id) ON DELETE CASCADE");
                    }
                }
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }
}
