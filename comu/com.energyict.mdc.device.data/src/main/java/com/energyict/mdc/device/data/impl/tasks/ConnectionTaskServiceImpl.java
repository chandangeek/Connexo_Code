/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.EventType;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionBuilderImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFields;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import org.joda.time.DateTimeConstants;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link ConnectionTaskService} interface.
 * Implementation note: no need for @Component annotation as this
 * component is dynamically registered as part of the activation of the
 * {@link com.energyict.mdc.device.data.impl.DeviceDataModelServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (09:01)
 */
@LiteralSql
public class ConnectionTaskServiceImpl implements ServerConnectionTaskService {

    private final DeviceDataModelService deviceDataModelService;
    private final EventService eventService;
    private final ProtocolPluggableService protocolPluggableService;

    @Inject
    public ConnectionTaskServiceImpl(DeviceDataModelService deviceDataModelService, EventService eventService, ProtocolPluggableService protocolPluggableService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
        this.eventService = eventService;
        this.protocolPluggableService = protocolPluggableService;
    }

    @Override
    public void releaseInterruptedConnectionTasks(ComServer comServer) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_CONNECTIONTASK.name() + " SET comserver = NULL WHERE comserver = ");
        sqlBuilder.addLong(comServer.getId());
        this.deviceDataModelService.executeUpdate(sqlBuilder);
    }

    @Override
    public void releaseTimedOutConnectionTasks(ComServer outboundCapableComServer) {
        List<ComPortPool> containingComPortPoolsForComServer = this.deviceDataModelService.engineConfigurationService().findContainingComPortPoolsForComServer(outboundCapableComServer);
        for (ComPortPool comPortPool : containingComPortPoolsForComServer) {
            this.releaseTimedOutConnectionTasks((OutboundComPortPool) comPortPool);
        }
    }

    private void releaseTimedOutConnectionTasks(OutboundComPortPool outboundComPortPool) {
        long now = this.toSeconds(this.deviceDataModelService.clock().instant());
        int timeOutSeconds = outboundComPortPool.getTaskExecutionTimeout().getSeconds();
        this.deviceDataModelService.executeUpdate(this.releaseTimedOutConnectionTasksSqlBuilder(outboundComPortPool, now, timeOutSeconds));
    }

    private SqlBuilder releaseTimedOutConnectionTasksSqlBuilder(OutboundComPortPool outboundComPortPool, long now, int timeOutSeconds) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append("   set comserver = null");
        sqlBuilder.append(" where id in (select connectiontask from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" where id in (");
        TimedOutTasksSqlBuilder.appendTimedOutComTaskExecutionSql(sqlBuilder, outboundComPortPool, now, timeOutSeconds);
        sqlBuilder.append("))");
        return sqlBuilder;
    }

    private long toSeconds(Instant time) {
        return time.toEpochMilli() / DateTimeConstants.MILLIS_PER_SECOND;
    }

    @Override
    public Optional<ConnectionTask> findConnectionTask(long id) {
        return this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).getOptional(id);
    }

    @Override
    public Optional<ConnectionTask> findAndLockConnectionTaskByIdAndVersion(long id, long version) {
        return this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<OutboundConnectionTask> findOutboundConnectionTask(long id) {
        return this.deviceDataModelService.dataModel().mapper(OutboundConnectionTask.class).getOptional(id);
    }

    @Override
    public Optional<InboundConnectionTask> findInboundConnectionTask(long id) {
        return this.deviceDataModelService.dataModel().mapper(InboundConnectionTask.class).getOptional(id);
    }

    @Override
    public Optional<ScheduledConnectionTask> findScheduledConnectionTask(long id) {
        return this.deviceDataModelService.dataModel().mapper(ScheduledConnectionTask.class).getOptional(id);
    }

    @Override
    public Optional<ConnectionInitiationTask> findConnectionInitiationTask(long id) {
        return this.deviceDataModelService.dataModel().mapper(ConnectionInitiationTask.class).getOptional(id);
    }

    @Override
    public Optional<ConnectionTask> findConnectionTaskForPartialOnDevice(PartialConnectionTask partialConnectionTask, Device device) {
        Condition condition =
                    where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).
                and(where(ConnectionTaskFields.OBSOLETE_DATE.fieldName()).isNull()).
                and(where(ConnectionTaskFields.PARTIAL_CONNECTION_TASK.fieldName()).isEqualTo(partialConnectionTask));
        return this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(condition).stream().findFirst();
    }

    @Override
    public List<Long> findConnectionTasksForPartialId(long partialConnectionTaskId) {
        List<Long> connectionTaskIds = new ArrayList<>();
        SqlBuilder sqlBuilder = new SqlBuilder("select id from " + TableSpecs.DDC_CONNECTIONTASK + " where OBSOLETE_DATE is null and PARTIALCONNECTIONTASK =");
        sqlBuilder.addLong(partialConnectionTaskId);
        try (Connection connection = this.deviceDataModelService.dataModel().getConnection(true)) {
            try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        connectionTaskIds.add(resultSet.getLong(1));
                    }
                }
            }
        }
        catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        return connectionTaskIds;
    }

    @Override
    public List<ConnectionTask> findConnectionTasksByDevice(Device device) {
        Condition condition =
                    where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).
                and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(condition);
    }

    @Override
    public List<ConnectionTask> findAllConnectionTasksByDevice(Device device) {
        return this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).find(ConnectionTaskFields.DEVICE.fieldName(), device.getId());
    }

    @Override
    public List<InboundConnectionTask> findInboundConnectionTasksByDevice(Device device) {
        Condition condition = where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return this.deviceDataModelService.dataModel().mapper(InboundConnectionTask.class).select(condition);
    }

    @Override
    public List<ScheduledConnectionTask> findScheduledConnectionTasksByDevice(Device device) {
        Condition condition = where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return this.deviceDataModelService.dataModel().mapper(ScheduledConnectionTask.class).select(condition);
    }

    @Override
    public Optional<ConnectionTask> findDefaultConnectionTaskForDevice(Device device) {
        Condition condition = where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).
                          and(where("isDefault").isEqualTo(true)).
                          and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        List<ConnectionTask> connectionTasks = this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(condition);
        if (connectionTasks.size() == 1) {
            return Optional.of(connectionTasks.get(0));
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public List<ConnectionTask> findConnectionTasksByStatus(TaskStatus status) {
        return this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(ServerConnectionTaskStatus.forTaskStatus(status).condition());
    }

    /**
     * Returns a QueryExecutor that supports building a sub-query to match
     * that the ConnectionTask's device is in a QueryEndDeviceGroup.
     *
     * @return The QueryExecutor
     */
    private QueryExecutor<Device> deviceFromDeviceGroupQueryExecutor() {
        return this.deviceDataModelService.dataModel().query(Device.class, DeviceConfiguration.class, DeviceType.class);
    }

    @Override
    public List<ConnectionTask> findConnectionTasksByFilter(ConnectionTaskFilterSpecification filter, int pageStart, int pageSize) {
        ConnectionTaskFilterSqlBuilder sqlBuilder =
                new ConnectionTaskFilterSqlBuilder(
                        filter,
                        this.deviceDataModelService.clock(),
                        this.deviceFromDeviceGroupQueryExecutor());
        DataMapper<ConnectionTask> dataMapper = this.deviceDataModelService.dataModel().mapper(ConnectionTask.class);
        return this.fetchConnectionTasks(dataMapper, sqlBuilder.build(dataMapper, pageStart + 1, pageSize)); // SQL is 1-based
    }

    @Override
    public List<ConnectionTypePluggableClass> findConnectionTypeByFilter(ConnectionTaskFilterSpecification filter) {
        // TODO provide native query....
        List<ConnectionTypePluggableClass> connectionTypePluggableClasses = new ArrayList<>();
        List<String> javaClassNames = this.findConnectionTasksByFilter(filter, 0, Integer.MAX_VALUE - 1).stream().map(ct -> ct.getPluggableClass().getJavaClassName()).collect(Collectors.toList());
        this.protocolPluggableService.findAllConnectionTypePluggableClasses().stream().
                filter(pluggableClass -> javaClassNames.contains(pluggableClass.getJavaClassName())).
                forEach(connectionTypePluggableClasses::add);

        return connectionTypePluggableClasses;
    }

    private List<ConnectionTask> fetchConnectionTasks(DataMapper<ConnectionTask> dataMapper, SqlBuilder sqlBuilder) {
        try (Fetcher<ConnectionTask> fetcher = dataMapper.fetcher(sqlBuilder)) {
            Iterator<ConnectionTask> connectionTaskIterator = fetcher.iterator();
            List<ConnectionTask> connectionTasks = new ArrayList<>();
            while (connectionTaskIterator.hasNext()) {
                connectionTasks.add(connectionTaskIterator.next());
            }
            return connectionTasks;
        }
    }

    @Override
    public void setDefaultConnectionTask(ConnectionTask newDefaultConnectionTask) {
        this.doSetDefaultConnectionTask(newDefaultConnectionTask.getDevice(), (ConnectionTaskImpl) newDefaultConnectionTask);
    }

    public void doSetDefaultConnectionTask(final Device device, final ConnectionTaskImpl newDefaultConnectionTask) {
        this.clearOldDefault(device, newDefaultConnectionTask);
        if (newDefaultConnectionTask != null) {
            newDefaultConnectionTask.setAsDefault();
        }
    }

    private void clearOldDefault(Device device, ConnectionTaskImpl newDefaultConnectionTask) {
        List<ConnectionTask> connectionTasks = this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).find(ConnectionTaskFields.DEVICE.fieldName(), device);
        connectionTasks
                .stream()
                .filter(connectionTask -> isPreviousDefault(newDefaultConnectionTask, connectionTask))
                .map(ConnectionTaskImpl.class::cast)
                .forEach(connectionTask -> {
                    connectionTask.clearDefault();
                    this.eventService.postEvent(EventType.CONNECTIONTASK_CLEARDEFAULT.topic(), connectionTask);
                });
    }

    @Override
    public void clearDefaultConnectionTask(Device device) {
        this.doSetDefaultConnectionTask(device, null);
    }

    private boolean isPreviousDefault(ConnectionTask newDefaultConnectionTask, ConnectionTask connectionTask) {
        return connectionTask.isDefault()
                && ((newDefaultConnectionTask == null)
                || (connectionTask.getId() != newDefaultConnectionTask.getId()));
    }

    @Override
    public <T extends ConnectionTask> T attemptLockConnectionTask(T connectionTask, ComServer comServer) {
        Optional<ConnectionTask> lockResult = this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).lockNoWait(connectionTask.getId());
        if (lockResult.isPresent()) {
            T lockedConnectionTask = (T) lockResult.get();
            if (lockedConnectionTask.getExecutingComServer() == null) {
                ((ConnectionTaskImpl) lockedConnectionTask).updateExecutingComServer(comServer);
                return lockedConnectionTask;
            } else {
                // No database lock but business lock is already set
                return null;
            }
        } else {
            // ConnectionTask no longer exists, attempt to lock fails
            return null;
        }
    }

    @Override
    public ConnectionTask attemptLockConnectionTask(long id) {
        return this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).lock(id);
    }

    @Override
    public void unlockConnectionTask(ConnectionTask connectionTask) {
        this.unlockConnectionTask((ConnectionTaskImpl) connectionTask);
    }

    private void unlockConnectionTask(ConnectionTaskImpl connectionTask) {
        connectionTask.updateExecutingComServer(null);
    }

    @Override
    public boolean hasConnectionTasks(ComPortPool comPortPool) {
        List<ConnectionTask> connectionTasks =
                this.deviceDataModelService.dataModel().query(ConnectionTask.class).
                        select(where("comPortPool").isEqualTo(comPortPool),
                                new Order[0], false, new String[0],
                                1, 1);
        return !connectionTasks.isEmpty();
    }

    @Override
    public boolean hasConnectionTasks(PartialConnectionTask partialConnectionTask) {
        Condition condition = where(ConnectionTaskFields.PARTIAL_CONNECTION_TASK.fieldName()).isEqualTo(partialConnectionTask).
                and(where(ConnectionTaskFields.OBSOLETE_DATE.fieldName()).isNull());
        List<ConnectionTask> connectionTasks = this.deviceDataModelService.dataModel().query(ConnectionTask.class).select(condition, new Order[0], false, new String[0], 1, 1);
        return !connectionTasks.isEmpty();
    }

    @Override
    public List<ComSession> findAllSessionsFor(ConnectionTask<?, ?> connectionTask) {
        return this.deviceDataModelService.dataModel().mapper(ComSession.class).
                select(where(ComSessionImpl.Fields.CONNECTION_TASK.fieldName()).isEqualTo(connectionTask));
    }

    @Override
    public ComSessionBuilder buildComSession(ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Instant startTime) {
        return new ComSessionBuilderImpl(this.deviceDataModelService.dataModel(), connectionTask, comPortPool, comPort, startTime);
    }

    @Override
    public Optional<ComSession> findComSession(long id) {
        return this.deviceDataModelService.dataModel().mapper(ComSession.class).getOptional(id);
    }

    @Override
    public List<ComSession> findComSessions(ComPort comPort) {
        return this.deviceDataModelService.dataModel().mapper(ComSession.class).find("comPort", comPort);
    }

    @Override
    public List<ComSession> findComSessions(ComPortPool comPortPool) {
        return this.deviceDataModelService.dataModel().mapper(ComSession.class).find("comPortPool", comPortPool);
    }

    @Override
    public List<ConnectionTask> findLockedByComServer(ComServer comServer) {
        Condition condition = where(ConnectionTaskFields.COM_SERVER.fieldName()).isEqualTo(comServer);
        return this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(condition);
    }
}
