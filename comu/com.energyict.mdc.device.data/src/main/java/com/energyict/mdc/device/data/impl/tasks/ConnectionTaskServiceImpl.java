/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.ConnectionInitiationTask;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.InboundConnectionTask;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.EventType;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionBuilderImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFields;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import org.joda.time.DateTimeConstants;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
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
    private static final Logger LOGGER = Logger.getLogger(ConnectionTaskServiceImpl.class.getName());

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
    public void releaseInterruptedConnectionTasks(ComPort comPort) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_CONNECTIONTASK.name() + " SET ");
        sqlBuilder.append(ConnectionTaskFields.COM_PORT.fieldName());
        sqlBuilder.append(" = NULL WHERE ");
        sqlBuilder.append(ConnectionTaskFields.COM_PORT.fieldName());
        sqlBuilder.append(" = ");
        sqlBuilder.addLong(comPort.getId());
        deviceDataModelService.executeUpdate(sqlBuilder);
    }

    private long toSeconds(Instant time) {
        return time.toEpochMilli() / DateTimeConstants.MILLIS_PER_SECOND;
    }

    @Override
    public Optional<ConnectionTask> findConnectionTask(long id) {
        return deviceDataModelService.dataModel().mapper(ConnectionTask.class).getOptional(id);
    }

    @Override
    public Optional<ConnectionTask> findAndLockConnectionTaskByIdAndVersion(long id, long version) {
        return deviceDataModelService.dataModel().mapper(ConnectionTask.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<OutboundConnectionTask> findOutboundConnectionTask(long id) {
        return deviceDataModelService.dataModel().mapper(OutboundConnectionTask.class).getOptional(id);
    }

    @Override
    public Optional<InboundConnectionTask> findInboundConnectionTask(long id) {
        return deviceDataModelService.dataModel().mapper(InboundConnectionTask.class).getOptional(id);
    }

    @Override
    public Optional<ScheduledConnectionTask> findScheduledConnectionTask(long id) {
        return deviceDataModelService.dataModel().mapper(ScheduledConnectionTask.class).getOptional(id);
    }

    @Override
    public Optional<ConnectionInitiationTask> findConnectionInitiationTask(long id) {
        return deviceDataModelService.dataModel().mapper(ConnectionInitiationTask.class).getOptional(id);
    }

    @Override
    public Optional<ConnectionTask> findConnectionTaskForPartialOnDevice(PartialConnectionTask partialConnectionTask, Device device) {
        Condition condition =
                where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).
                        and(where(ConnectionTaskFields.OBSOLETE_DATE.fieldName()).isNull()).
                        and(where(ConnectionTaskFields.PARTIAL_CONNECTION_TASK.fieldName()).isEqualTo(partialConnectionTask));
        return deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(condition).stream().findFirst();
    }

    @Override
    public List<Long> findConnectionTasksForPartialId(long partialConnectionTaskId) {
        List<Long> connectionTaskIds = new ArrayList<>();
        SqlBuilder sqlBuilder = new SqlBuilder("select id from " + TableSpecs.DDC_CONNECTIONTASK + " where OBSOLETE_DATE is null and PARTIALCONNECTIONTASK =");
        sqlBuilder.addLong(partialConnectionTaskId);
        try (Connection connection = deviceDataModelService.dataModel().getConnection(true)) {
            try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        connectionTaskIds.add(resultSet.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        return connectionTaskIds;
    }

    @Override
    public List<ConnectionTask> findConnectionTasksByDevice(Device device) {
        Condition condition =
                where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).
                        and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(condition);
    }

    @Override
    public List<ConnectionTask> findAllConnectionTasksByDevice(Device device) {
        return deviceDataModelService.dataModel().mapper(ConnectionTask.class).find(ConnectionTaskFields.DEVICE.fieldName(), device);
    }

    @Override
    public List<InboundConnectionTask> findInboundConnectionTasksByDevice(Device device) {
        Condition condition = where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return deviceDataModelService.dataModel().mapper(InboundConnectionTask.class).select(condition);
    }

    @Override
    public List<ScheduledConnectionTask> findScheduledConnectionTasksByDevice(Device device) {
        Condition condition = where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return deviceDataModelService.dataModel().mapper(ScheduledConnectionTask.class).select(condition);
    }

    @Override
    public Optional<ConnectionTask> findDefaultConnectionTaskForDevice(Device device) {
        return device.getConnectionTasks().stream().filter(ConnectionTask::isDefault).findAny().map(ConnectionTask.class::cast);
    }

    @Override
    public Optional<ConnectionTask> findConnectionTaskByDeviceAndConnectionFunction(Device device, ConnectionFunction connectionFunction) {
        return findAllConnectionTasksByDevice(device)
                .stream()
                .filter(Predicates.not(ConnectionTask::isObsolete))
                .filter(ct -> ct.getPartialConnectionTask().getConnectionFunction().isPresent() && ct.getPartialConnectionTask().getConnectionFunction().get().getId() == connectionFunction.getId())
                .findFirst();
    }

    @Override
    public List<ConnectionTask> findConnectionTasksByStatus(TaskStatus status) {
        return deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(ServerConnectionTaskStatus.forTaskStatus(status).condition());
    }

    /**
     * Returns a QueryExecutor that supports building a sub-query to match
     * that the ConnectionTask's device is in a QueryEndDeviceGroup.
     *
     * @return The QueryExecutor
     */
    private QueryExecutor<Device> deviceFromDeviceGroupQueryExecutor() {
        return deviceDataModelService.dataModel().query(Device.class, DeviceConfiguration.class, DeviceType.class);
    }

    @Override
    public List<ConnectionTask> findConnectionTasksByFilter(ConnectionTaskFilterSpecification filter, int pageStart, int pageSize) {
        ConnectionTaskFilterSqlBuilder sqlBuilder =
                new ConnectionTaskFilterSqlBuilder(
                        filter,
                        deviceDataModelService.clock(),
                        deviceFromDeviceGroupQueryExecutor());
        DataMapper<ConnectionTask> dataMapper = deviceDataModelService.dataModel().mapper(ConnectionTask.class);
        return fetchConnectionTasks(dataMapper, sqlBuilder.build(dataMapper, pageStart + 1, pageSize)); // SQL is 1-based
    }

    @Override
    public List<ConnectionTypePluggableClass> findConnectionTypeByFilter(ConnectionTaskFilterSpecification filter) {
        // TODO provide native query....
        List<ConnectionTypePluggableClass> connectionTypePluggableClasses = new ArrayList<>();
        List<String> javaClassNames = findConnectionTasksByFilter(filter, 0, Integer.MAX_VALUE - 1).stream().map(ct -> ct.getPluggableClass().getJavaClassName()).collect(Collectors.toList());
        protocolPluggableService.findAllConnectionTypePluggableClasses().stream().
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
    public ConnectionTask updateProtocolDialectConfigurationProperties(ConnectionTask connectionTask, ProtocolDialectConfigurationProperties properties) {
        if (connectionTask instanceof ServerConnectionTask) {
            ((ServerConnectionTask) connectionTask).setProtocolDialectConfigurationProperties(properties);
            connectionTask.save();
        } else {
            throw new UnsupportedOperationException("ConnectionTask is not of type 'ServerConnectionTask'");
        }
        return connectionTask;
    }

    @Override
    public void setDefaultConnectionTask(ConnectionTask newDefaultConnectionTask) {
        doSetDefaultConnectionTask(newDefaultConnectionTask.getDevice(), (ConnectionTaskImpl) newDefaultConnectionTask);
    }

    public void doSetDefaultConnectionTask(final Device device, final ConnectionTaskImpl newDefaultConnectionTask) {
        clearOldDefault(device, newDefaultConnectionTask);
        if (newDefaultConnectionTask != null) {
            newDefaultConnectionTask.setAsDefault();
        }
    }

    private void clearOldDefault(Device device, ConnectionTaskImpl newDefaultConnectionTask) {
        List<ConnectionTask> connectionTasks = device.getConnectionTasks().stream().filter(ConnectionTask::isDefault).collect(Collectors.toList());
        connectionTasks
                .stream()
                .filter(connectionTask -> isPreviousDefault(newDefaultConnectionTask, connectionTask))
                .map(ConnectionTaskImpl.class::cast)
                .forEach(connectionTask -> {
                    connectionTask.clearDefault();
                    eventService.postEvent(EventType.CONNECTIONTASK_CLEARDEFAULT.topic(), connectionTask);
                });
    }

    @Override
    public void clearDefaultConnectionTask(Device device) {
        doSetDefaultConnectionTask(device, null);
    }

    private boolean isPreviousDefault(ConnectionTask newDefaultConnectionTask, ConnectionTask connectionTask) {
        return connectionTask.isDefault()
                && ((newDefaultConnectionTask == null)
                || (connectionTask.getId() != newDefaultConnectionTask.getId()));
    }

    @Override
    public void setConnectionTaskHavingConnectionFunction(ConnectionTask<?, ?> connectionTask, Optional<ConnectionFunction> oldConnectionFunction) {
        clearConnectionTaskConnectionFunction(connectionTask, oldConnectionFunction);
        eventService.postEvent(EventType.CONNECTIONTASK_SETASCONNECTIONFUNCTION.topic(), connectionTask);
    }

    @Override
    public void clearConnectionTaskConnectionFunction(ConnectionTask<?, ?> connectionTask, Optional<ConnectionFunction> oldConnectionFunction) {
        oldConnectionFunction.ifPresent(connectionFunction -> eventService.postEvent(EventType.CONNECTIONTASK_CLEARCONNECTIONFUNCTION.topic(), Pair.of(connectionTask, connectionFunction)));
    }

    @Override
    public <T extends ConnectionTask> T attemptLockConnectionTask(T connectionTask, ComPort comPort) {
        Optional<ConnectionTask> lockResult = deviceDataModelService.dataModel().mapper(ConnectionTask.class).lockNoWait(connectionTask.getId());
        if (lockResult.isPresent()) {
            T lockedConnectionTask = (T) lockResult.get();
            if (lockedConnectionTask.getExecutingComPort() == null) {
                ((ConnectionTaskImpl) lockedConnectionTask).updateExecutingComPort(comPort);
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
        return deviceDataModelService.dataModel().mapper(ConnectionTask.class).lock(id);
    }

    @Override
    public void unlockConnectionTask(ConnectionTask connectionTask) {
        unlockConnectionTask((ConnectionTaskImpl) connectionTask);
    }

    private void unlockConnectionTask(ConnectionTaskImpl connectionTask) {
        connectionTask.updateExecutingComPort(null);
    }

    @Override
    public boolean attemptUnlockConnectionTask(ConnectionTask connectionTask) {
        Optional<ConnectionTask> lockResult = deviceDataModelService.dataModel().mapper(ConnectionTask.class).lockNoWait(connectionTask.getId());
        if (lockResult.isPresent()) {
            ((ConnectionTaskImpl) lockResult.get()).updateExecutingComPort(null);
            return true;
        }

        return false;
    }

    @Override
    public boolean hasConnectionTasks(ComPortPool comPortPool) {
        List<ConnectionTask> connectionTasks =
                deviceDataModelService.dataModel().query(ConnectionTask.class).
                        select(where("comPortPool").isEqualTo(comPortPool),
                                new Order[0], false, new String[0],
                                1, 1);
        return !connectionTasks.isEmpty();
    }

    @Override
    public boolean hasConnectionTasks(PartialConnectionTask partialConnectionTask) {
        Condition condition = where(ConnectionTaskFields.PARTIAL_CONNECTION_TASK.fieldName()).isEqualTo(partialConnectionTask).
                and(where(ConnectionTaskFields.OBSOLETE_DATE.fieldName()).isNull());
        List<ConnectionTask> connectionTasks = deviceDataModelService.dataModel().query(ConnectionTask.class).select(condition, new Order[0], false, new String[0], 1, 1);
        return !connectionTasks.isEmpty();
    }

    @Override
    public List<ComSession> findAllSessionsFor(ConnectionTask<?, ?> connectionTask) {
        return deviceDataModelService.dataModel().mapper(ComSession.class).
                select(where(ComSessionImpl.Fields.CONNECTION_TASK.fieldName()).isEqualTo(connectionTask));
    }

    @Override
    public ComSessionBuilder buildComSession(ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Instant startTime) {
        return new ComSessionBuilderImpl(deviceDataModelService.dataModel(), connectionTask, comPortPool, comPort, startTime);
    }

    @Override
    public Optional<ComSession> findComSession(long id) {
        return deviceDataModelService.dataModel().mapper(ComSession.class).getOptional(id);
    }

    @Override
    public List<ComSession> findComSessions(ComPort comPort) {
        return deviceDataModelService.dataModel().mapper(ComSession.class).find("comPort", comPort);
    }

    @Override
    public List<ComSession> findComSessions(ComPortPool comPortPool) {
        return deviceDataModelService.dataModel().mapper(ComSession.class).find("comPortPool", comPortPool);
    }

    @Override
    public List<ConnectionTask> findLockedByComPort(ComPort comPort) {
        Condition condition = where(ConnectionTaskFields.COM_PORT.fieldName()).isEqualTo(comPort);
        return deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(condition);
    }

    public List<ConnectionTask> findTimedOutConnectionTasksByComPort(ComPort comPort) {
        Set<ConnectionTask> timedOutComTasks = new HashSet<>();
        deviceDataModelService.engineConfigurationService().findContainingComPortPoolsForComPort((OutboundComPort) comPort)
                .forEach(pool -> timedOutComTasks.addAll(findTimedOutConnectionTasksByPool(pool)));

        return timedOutComTasks.stream().collect(Collectors.toList());
    }

    @Override
    public List<PartialConnectionTask> findPartialConnectionTasks(){
        List<PartialConnectionTask> partialConnectionTasks = new ArrayList<>();
        Iterator<ConnectionTask> connectionTaskIterator = deviceDataModelService.dataModel()
                .mapper(ConnectionTask.class)  .fetcher(new SqlBuilder("select * from " + TableSpecs.DDC_CONNECTIONTASK +
                        " where id in (select min(id) from ddc_connectiontask GROUP BY partialconnectiontask)"))
                .iterator();
        while (connectionTaskIterator.hasNext()){
            partialConnectionTasks.add(connectionTaskIterator.next().getPartialConnectionTask());
        }
        return partialConnectionTasks;
        // all partial connections are fetched from DB (5000). After that I filter them and only 1-5 remains
        // THis method could be optimized, to fetch from DB directly last 1-5 results
    }

    @Override
    public long getConnectionTasksCount(ConnectionTaskFilterSpecification filter) {
        ConnectionTaskFilterSqlBuilder sqlBuilder =
                new ConnectionTaskFilterSqlBuilder(
                        filter,
                        deviceDataModelService.clock(),
                        deviceFromDeviceGroupQueryExecutor());
        try (Connection connection = deviceDataModelService.dataModel().getConnection(false)) {
            try (PreparedStatement statement = sqlBuilder.buildCount().prepare(connection)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    long count = 0;
                    while (resultSet.next()) {
                        count += resultSet.getLong(1);
                    }
                    return count;
                }
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private List<ConnectionTask> findTimedOutConnectionTasksByPool(OutboundComPortPool comPortPool) {
        int timeOutSeconds = comPortPool.getTaskExecutionTimeout().getSeconds();
        Instant lastValidInstant = deviceDataModelService.clock().instant().minusSeconds(timeOutSeconds);
        Condition condition =
                where(ConnectionTaskFields.OBSOLETE_DATE.fieldName()).isNull()
                        .and(where(ConnectionTaskFields.COM_PORT.fieldName()).isNotNull())
                        .and(where(ConnectionTaskFields.COM_PORT_POOL.fieldName()).isEqualTo(comPortPool))
                        .and(where(ConnectionTaskFields.LAST_COMMUNICATION_START.fieldName()).isLessThan(lastValidInstant));
        LOGGER.warning("Looking for busy connection tasks started before " + lastValidInstant + " on pool " + comPortPool);
        return deviceDataModelService.dataModel().query(ConnectionTask.class).select(condition);
    }

}
