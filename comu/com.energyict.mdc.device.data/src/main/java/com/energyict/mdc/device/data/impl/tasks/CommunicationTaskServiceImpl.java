/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.ConfigPropertiesService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.OptimisticLockException;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.common.tasks.ServerComTaskExecution;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.ScheduledComTaskExecutionIdRange;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.configproperties.ConfigProperties;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComTaskExecutionSessionImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFields;
import com.energyict.mdc.device.data.tasks.PriorityComTaskExecutionFields;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;

import com.google.common.collect.Range;
import org.joda.time.DateTimeConstants;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link CommunicationTaskService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:37)
 */
@LiteralSql
public class CommunicationTaskServiceImpl implements ServerCommunicationTaskService {

    private static final Logger LOGGER = Logger.getLogger(CommunicationTaskServiceImpl.class.getName());

    private final DeviceDataModelService deviceDataModelService;
    private final PriorityComTaskService priorityComTaskService;
    private final ConfigPropertiesService configPropertiesService;
    private final BundleContext bundleContext;
    private final ComTaskExecutionBalancing comTaskExecutionBalancing = new ComTaskExecutionBalancing();
    private boolean isTrueMinimizedOn;
    private boolean isRandomizationOn;

    @Inject
    public CommunicationTaskServiceImpl(DeviceDataModelService deviceDataModelService, ConfigPropertiesService configPropertiesService, BundleContext bundleContext, PriorityComTaskService priorityComTaskService) {
        this.deviceDataModelService = deviceDataModelService;
        this.priorityComTaskService = priorityComTaskService;
        this.configPropertiesService = configPropertiesService;
        this.bundleContext = bundleContext;
    }

    @Override
    public void releaseInterruptedComTasks(ComPort comPort) {
        deviceDataModelService.executeUpdate(releaseInterruptedComTasksSqlBuilder(comPort));
        deviceDataModelService.executeUpdate(releaseInterruptedHighPriorityComTasksSqlBuilder(comPort));
        deviceDataModelService.executeUpdate(releaseInterruptedComTasksRelatedToHighPrioritySqlBuilder(comPort));
    }

    private SqlBuilder releaseInterruptedComTasksSqlBuilder(ComPort comPort) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_COMTASKEXEC.name() + " SET comport = null, executionStart = null WHERE comport = ");
        sqlBuilder.addLong(comPort.getId());
        return sqlBuilder;
    }

    private SqlBuilder releaseInterruptedHighPriorityComTasksSqlBuilder(ComPort comPort) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_HIPRIOCOMTASKEXEC.name() + " SET comport = null where comport = ");
        sqlBuilder.addLong(comPort.getId());
        return sqlBuilder;
    }

    private SqlBuilder releaseInterruptedComTasksRelatedToHighPrioritySqlBuilder(ComPort comPort) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_COMTASKEXEC.name() + " SET comport = null, executionStart = null ");
        sqlBuilder.append(" WHERE id in (");
        sqlBuilder.append("SELECT comtaskexecution from " + TableSpecs.DDC_HIPRIOCOMTASKEXEC.name() + " WHERE comport = ");
        sqlBuilder.addLong(comPort.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    @Override
    public List<ComTaskExecution> findTimedOutComTasksByComPort(ComPort comPort) {
        Set<ComTaskExecution> timedOutComTasks = new HashSet<>();
        List<OutboundComPortPool> containingPools = deviceDataModelService.engineConfigurationService().findContainingComPortPoolsForComPort((OutboundComPort) comPort);
        containingPools.forEach(pool -> timedOutComTasks.addAll(findTimedOutComTasksByPool(pool)));

        return timedOutComTasks.stream().collect(Collectors.toList());
    }

    private List<ComTaskExecution> findTimedOutComTasksByPool(OutboundComPortPool comPortPool) {
        int timeOutSeconds = comPortPool.getTaskExecutionTimeout().getSeconds();
        Instant lastValidInstant = deviceDataModelService.clock().instant().minusSeconds(timeOutSeconds);
        Condition condition =
                where("connectionTask." + ConnectionTaskFields.OBSOLETE_DATE.fieldName()).isNull()
                        .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull())
                        .and(where(ComTaskExecutionFields.COMPORT.fieldName()).isNotNull())
                        .and(where("connectionTask." + ConnectionTaskFields.COM_PORT_POOL.fieldName()).isEqualTo(comPortPool))
                        .and(where("connectionTask." + ConnectionTaskFields.LAST_COMMUNICATION_START.fieldName()).isLessThan(lastValidInstant));
        LOGGER.warning("Looking for busy comtasks started before " + lastValidInstant + " on pool " + comPortPool);
        return deviceDataModelService.dataModel().query(ComTaskExecution.class, ConnectionTask.class).select(condition);
    }

    private ServerComTaskExecution refreshComTaskExecution(ComTaskExecution comTaskExecution) {
        ServerComTaskExecution freshComTaskExecution = (ServerComTaskExecution) deviceDataModelService.dataModel().mapper(ComTaskExecution.class).getUnique("id", comTaskExecution.getId()).get();
        Optional<PriorityComTaskExecutionLink> priorityComTaskExecutionLink = priorityComTaskService.findByComTaskExecution(comTaskExecution);
        if (priorityComTaskExecutionLink.isPresent()) {
            return new PriorityComTaskExecutionImpl(freshComTaskExecution, (ServerPriorityComTaskExecutionLink) priorityComTaskExecutionLink.get());
        }
        return freshComTaskExecution;
    }

    private long toSeconds(Instant time) {
        return time.toEpochMilli() / DateTimeConstants.MILLIS_PER_SECOND;
    }

    @Override
    public List<ComTaskExecution> findComTaskExecutionsByFilter(ComTaskExecutionFilterSpecification filter, int pageStart, int pageSize) {
        ComTaskExecutionFilterSqlBuilder sqlBuilder = new ComTaskExecutionFilterSqlBuilder(filter, this.deviceDataModelService.clock(), this.deviceFromDeviceGroupQueryExecutor());
        DataMapper<ComTaskExecution> dataMapper = this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class);
        return this.fetchComTaskExecutions(dataMapper, sqlBuilder.build(dataMapper, pageStart + 1, pageSize)); // SQL is 1-based
    }

    private List<ComTaskExecution> fetchComTaskExecutions(DataMapper<ComTaskExecution> dataMapper, SqlBuilder sqlBuilder) {
        try (Fetcher<ComTaskExecution> fetcher = dataMapper.fetcher(sqlBuilder)) {
            Iterator<ComTaskExecution> comTaskExecutionIterator = fetcher.iterator();
            List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
            while (comTaskExecutionIterator.hasNext()) {
                comTaskExecutions.add(comTaskExecutionIterator.next());
            }
            return comTaskExecutions;
        }
    }

    /**
     * Returns a QueryExecutor that supports building a subquery to match
     * that the ConnectionTask's device is in a EndDeviceGroup.
     *
     * @return The QueryExecutor
     */
    private QueryExecutor<Device> deviceFromDeviceGroupQueryExecutor() {
        return this.deviceDataModelService.dataModel().query(Device.class, DeviceConfiguration.class, DeviceType.class);
    }

    @Override
    public List<ComTaskExecution> findComTaskExecutionsWithDefaultConnectionTask(Device device) {
        Condition query = where(ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName()).isEqualTo(true)
                .and(where(ComTaskExecutionFields.DEVICE.fieldName()).isEqualTo(device))
                .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class).select(query);
    }

    @Override
    public List<ComTaskExecution> findComTaskExecutionsWithConnectionFunction(Device device, ConnectionFunction connectionFunction) {
        Condition query = where(ComTaskExecutionFields.CONNECTION_FUNCTION.fieldName()).isEqualTo(connectionFunction.getId())
                .and(where(ComTaskExecutionFields.DEVICE.fieldName()).isEqualTo(device))
                .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class).select(query);
    }

    @Override
    public void removePreferredConnectionTask(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask) {
        this.deviceDataModelService.executeUpdate(this.removePreferredConnectionTaskSqlBuilder(comTask, deviceConfiguration, partialConnectionTask));
    }

    private SqlBuilder removePreferredConnectionTaskSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set connectionTask = null");
        sqlBuilder.append(" where comtask = ?");  // Match the ComTask
        return addConditionsToSqlBuilder(comTask, deviceConfiguration, previousPartialConnectionTask, sqlBuilder);
    }

    private SqlBuilder addConditionsToSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask, SqlBuilder sqlBuilder) {
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and connectionTask = ");
        sqlBuilder.append("(select id from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append("  where device = ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(".device");
        sqlBuilder.append("    and partialconnectiontask =");   // Match the connection task
        sqlBuilder.addLong(previousPartialConnectionTask.getId());
        sqlBuilder.append("    and obsolete_date is null)");
        return addConditionOnDeviceByConfiguration(deviceConfiguration, sqlBuilder);
    }

    private SqlBuilder addConditionOnDeviceByConfiguration(DeviceConfiguration deviceConfiguration, SqlBuilder sqlBuilder) {
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    @Override
    public void switchFromDefaultConnectionTaskToPreferredConnectionTask(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask) {
        this.deviceDataModelService.executeUpdate(this.switchFromDefaultConnectionTaskToPreferredConnectionTaskSqlBuilder(comTask, deviceConfiguration, partialConnectionTask));
    }

    private SqlBuilder switchFromDefaultConnectionTaskToPreferredConnectionTaskSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask) {
        SqlBuilder sqlBuilder = initSwitchToConnectionTaskSqlBuilder(comTask, partialConnectionTask,
                " set useDefaultConnectionTask = 0, connectionTask = ", " where comtask =");
        sqlBuilder.append(" and useDefaultConnectionTask = 1"); // Update only if the ComTaskExecution is also using the default connection (~ so config not overwritten on device level)
        return addConditionOnDeviceByConfiguration(deviceConfiguration, sqlBuilder);
    }

    private void addConditionOnConnectionTask(PartialConnectionTask partialConnectionTask, SqlBuilder sqlBuilder) {
        sqlBuilder.append("(select id from DDC_CONNECTIONTASK");
        sqlBuilder.append(" where device = exec.device");
        sqlBuilder.append("   and partialconnectiontask =");  //Match the connection task against the same device
        sqlBuilder.addLong(partialConnectionTask.getId());
        sqlBuilder.append("   and obsolete_date is null)");
    }

    @Override
    public void switchFromDefaultConnectionTaskToConnectionFunction(ComTask comTask, DeviceConfiguration deviceConfiguration, ConnectionFunction connectionFunction) {
        this.deviceDataModelService.executeUpdate(this.switchOnConnectionFunctionSqlBuilder(comTask, deviceConfiguration, connectionFunction, true));
    }

    @Override
    public void switchOnDefault(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.deviceDataModelService.executeUpdate(this.switchOnDefaultSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder switchOnDefaultSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        SqlBuilder sqlBuilder = switchToDefaultConnectionTaskSqlBuilder();
        sqlBuilder.addLong(comTask.getId());
        return addConditionOnDeviceByConfiguration(deviceConfiguration, sqlBuilder);
    }

    private SqlBuilder switchToDefaultConnectionTaskSqlBuilder() {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set useDefaultConnectionTask = 1, connectionTask = ");
        sqlBuilder.append("(select id from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" where device = ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(".device");
        sqlBuilder.append("    and isdefault = 1");  // Match the default connection task against the same device
        sqlBuilder.append("    and obsolete_date is null)");
        sqlBuilder.append(" where comtask =");  // Match the ComTask

        return sqlBuilder;
    }

    @Override
    public void switchOnConnectionFunction(ComTask comTask, DeviceConfiguration deviceConfiguration, ConnectionFunction connectionFunction) {
        this.deviceDataModelService.executeUpdate(this.switchOnConnectionFunctionSqlBuilder(comTask, deviceConfiguration, connectionFunction, false));
    }

    private SqlBuilder switchOnConnectionFunctionSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration,
                                                            ConnectionFunction connectionFunction, boolean addCondOnDefaultConnTask) {
        SqlBuilder sqlBuilder = switchToConnectionFunctionSqlBuilder(comTask, connectionFunction);
        if (addCondOnDefaultConnTask) {
            sqlBuilder.append(" and exec.useDefaultConnectionTask = 1"); // Update only if the ComTaskExecution is also using the default connection (~ so config not overwritten on device level)
        }
        sqlBuilder.append(" and exec.device in (select id from DDC_DEVICE where deviceConfigId = ");    // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    private SqlBuilder switchToConnectionFunctionSqlBuilder(ComTask comTask, ConnectionFunction connectionFunction) {
        SqlBuilder sqlBuilder = new SqlBuilder("update DDC_COMTASKEXEC exec");
        sqlBuilder.append(" set useDefaultConnectionTask = 0, connectionFunction = ");
        sqlBuilder.addLong(connectionFunction.getId());
        sqlBuilder.append(", connectionTask = ");
        sqlBuilder.append("(select ct.id from DDC_CONNECTIONTASK ct, DTC_PARTIALCONNECTIONTASK pct"); // Find the connection task with the given connection function
        sqlBuilder.append(" where ct.partialconnectiontask = pct.id");
        sqlBuilder.append("   and ct.device = exec.device");
        sqlBuilder.append("   and pct.connectionfunction = ");
        sqlBuilder.addLong(connectionFunction.getId());
        sqlBuilder.append("   and ct.obsolete_date is null)");
        sqlBuilder.append(" where exec.comtask = "); // Match the ComTask
        sqlBuilder.addLong(comTask.getId());
        return sqlBuilder;
    }

    @Override
    public void switchOnPreferredConnectionTask(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask) {
        this.deviceDataModelService.executeUpdate(this.switchOnPreferredConnectionTaskSqlBuilder(comTask, deviceConfiguration, partialConnectionTask));
    }

    private SqlBuilder switchOnPreferredConnectionTaskSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask) {
        SqlBuilder sqlBuilder = initSwitchToConnectionTaskSqlBuilder(comTask, partialConnectionTask,
                " set useDefaultConnectionTask = 0, connectionFunction = 0, connectionTask = ", " where exec.comtask = ");
        sqlBuilder.append(" and exec.device in (select id from DDC_DEVICE where deviceConfigId = ");    // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    private SqlBuilder initSwitchToConnectionTaskSqlBuilder(ComTask comTask, PartialConnectionTask partialConnectionTask, String specificValues, String condition) {
        SqlBuilder sqlBuilder = new SqlBuilder("update DDC_COMTASKEXEC exec");
        sqlBuilder.append(specificValues);
        addConditionOnConnectionTask(partialConnectionTask, sqlBuilder);
        sqlBuilder.append(condition); // Match the ComTask
        sqlBuilder.addLong(comTask.getId());
        return sqlBuilder;
    }

    @Override
    public void switchOffDefault(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.deviceDataModelService.executeUpdate(this.switchOffDefaultSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder switchOffDefaultSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set useDefaultConnectionTask = 0");
        sqlBuilder.append(" where comtask =");  // Match the ComTask
        sqlBuilder.addLong(comTask.getId());
        return addConditionOnDeviceByConfiguration(deviceConfiguration, sqlBuilder);
    }

    @Override
    public void switchOffConnectionFunction(ComTask comTask, DeviceConfiguration deviceConfiguration, ConnectionFunction connectionFunction) {
        this.deviceDataModelService.executeUpdate(this.switchOffConnectionFunctionSqlBuilder(comTask, deviceConfiguration, connectionFunction));
    }

    private SqlBuilder switchOffConnectionFunctionSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, ConnectionFunction connectionFunction) {
        SqlBuilder sqlBuilder = new SqlBuilder("update DDC_COMTASKEXEC exec");
        sqlBuilder.append(" set connectionFunction = 0");
        return addConditionOnComTaskConnectionFunctionAndDevice(comTask, deviceConfiguration, connectionFunction, sqlBuilder);
    }

    private SqlBuilder addConditionOnComTaskConnectionFunctionAndDevice(ComTask comTask, DeviceConfiguration deviceConfiguration, ConnectionFunction connectionFunction, SqlBuilder sqlBuilder) {
        sqlBuilder.append(" where exec.comtask = "); // Match the ComTask
        sqlBuilder.addLong(comTask.getId());
        return addConditionOnConnectionFunctionAndDevice(deviceConfiguration, connectionFunction, sqlBuilder);
    }

    private SqlBuilder addConditionOnConnectionFunctionAndDevice(DeviceConfiguration deviceConfiguration, ConnectionFunction connectionFunction, SqlBuilder sqlBuilder) {
        sqlBuilder.append(" and exec.connectionFunction = ");   // Update only if the ComTaskExecution is also using the connection function (~ so config not overwritten on device level)
        sqlBuilder.addLong(connectionFunction.getId());
        sqlBuilder.append(" and exec.device in (select id from DDC_DEVICE where deviceConfigId = ");    // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    @Override
    public void switchFromPreferredConnectionTaskToDefault(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask) {
        this.deviceDataModelService.executeUpdate(this.switchFromPreferredConnectionTaskToDefaultSqlBuilder(comTask, deviceConfiguration, previousPartialConnectionTask));
    }

    private SqlBuilder switchFromPreferredConnectionTaskToDefaultSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask) {
        SqlBuilder sqlBuilder = switchToDefaultConnectionTaskSqlBuilder();
        return addConditionsToSqlBuilder(comTask, deviceConfiguration, previousPartialConnectionTask, sqlBuilder);
    }

    @Override
    public void switchFromPreferredConnectionTaskToConnectionFunction(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask, ConnectionFunction connectionFunction) {
        this.deviceDataModelService.executeUpdate(this.switchFromPreferredConnectionTaskToConnectionFunctionSqlBuilder(comTask, deviceConfiguration, partialConnectionTask, connectionFunction));
    }

    private SqlBuilder switchFromPreferredConnectionTaskToConnectionFunctionSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask, ConnectionFunction connectionFunction) {
        SqlBuilder sqlBuilder = switchToConnectionFunctionSqlBuilder(comTask, connectionFunction);
        sqlBuilder.append("   and connectionTask = "); // Update only if the ComTaskExecution is also using a specified connectionTask (~ so config not overwritten on device level)
        addConditionOnConnectionTask(partialConnectionTask, sqlBuilder);
        sqlBuilder.append(" and exec.device in (select id from DDC_DEVICE where deviceConfigId = ");    // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    @Override
    public void switchFromConnectionFunctionToDefault(ComTask comTask, DeviceConfiguration deviceConfiguration, ConnectionFunction connectionFunction) {
        this.deviceDataModelService.executeUpdate(this.switchFromConnectionFunctionToDefaultSqlBUilder(comTask, deviceConfiguration, connectionFunction));
    }

    private SqlBuilder switchFromConnectionFunctionToDefaultSqlBUilder(ComTask comTask, DeviceConfiguration deviceConfiguration, ConnectionFunction connectionFunction) {
        SqlBuilder sqlBuilder = new SqlBuilder("update DDC_COMTASKEXEC exec");
        sqlBuilder.append(" set useDefaultConnectionTask = 1, connectionFunction = 0, connectionTask = ");
        sqlBuilder.append("(select id from DDC_CONNECTIONTASK");
        sqlBuilder.append(" where device = exec.device");
        sqlBuilder.append("   and isdefault = 1");  // Match the default connection task against the same device
        sqlBuilder.append("   and obsolete_date is null)");
        return addConditionOnComTaskConnectionFunctionAndDevice(comTask, deviceConfiguration, connectionFunction, sqlBuilder);
    }

    @Override
    public void switchFromConnectionFunctionToPreferredConnectionTask(ComTask comTask, DeviceConfiguration deviceConfiguration, ConnectionFunction connectionFunction, PartialConnectionTask partialConnectionTask) {
        this.deviceDataModelService.executeUpdate(this.switchFromConnectionFunctionToPreferredConnectionTaskSqlBuilder(comTask, deviceConfiguration, connectionFunction, partialConnectionTask));
    }

    private SqlBuilder switchFromConnectionFunctionToPreferredConnectionTaskSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, ConnectionFunction connectionFunction, PartialConnectionTask partialConnectionTask) {
        SqlBuilder sqlBuilder = initSwitchToConnectionTaskSqlBuilder(comTask, partialConnectionTask, " set useDefaultConnectionTask = 0, connectionFunction = 0, connectionTask = ", " where exec.comtask = ");
        return addConditionOnConnectionFunctionAndDevice(deviceConfiguration, connectionFunction, sqlBuilder);
    }

    @Override
    public void preferredConnectionTaskChanged(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask, PartialConnectionTask newPartialConnectionTask) {
        this.deviceDataModelService.executeUpdate(this.preferredConnectionTaskChangedSqlBuilder(comTask, deviceConfiguration, previousPartialConnectionTask, newPartialConnectionTask));
    }

    private SqlBuilder preferredConnectionTaskChangedSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask, PartialConnectionTask newPartialConnectionTask) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set connectionTask = ");
        sqlBuilder.append("   (select id from " + TableSpecs.DDC_CONNECTIONTASK.name() + "");
        sqlBuilder.append("     where device = ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(".device");
        sqlBuilder.append("       and partialconnectiontask =");  //Match the connection task against the same device
        sqlBuilder.addLong(newPartialConnectionTask.getId());
        sqlBuilder.append("       and obsolete_date is null)");
        // Avoid comTaskExecutions that use the default connection
        sqlBuilder.append(" where useDefaultConnectionTask = 0");
        sqlBuilder.append("   and comtask =");  // Match the ComTask
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and connectionTask = ");
        sqlBuilder.append("     (select id from " + TableSpecs.DDC_CONNECTIONTASK.name() + "");
        sqlBuilder.append("       where device = ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(".device");
        sqlBuilder.append("         and partialconnectiontask =");   // Match the previous connection task
        sqlBuilder.addLong(previousPartialConnectionTask.getId());
        sqlBuilder.append("         and obsolete_date is null)");
        return addConditionOnDeviceByConfiguration(deviceConfiguration, sqlBuilder);
    }

    @Override
    public void preferredConnectionFunctionChanged(ComTask comTask, DeviceConfiguration deviceConfiguration, ConnectionFunction oldConnectionFunction, ConnectionFunction newConnectionFunction) {
        this.deviceDataModelService.executeUpdate(this.preferredConnectionFunctionChangedSqlBuilder(comTask, deviceConfiguration, oldConnectionFunction, newConnectionFunction));
    }

    private SqlBuilder preferredConnectionFunctionChangedSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, ConnectionFunction oldConnectionFunction, ConnectionFunction newConnectionFunction) {
        SqlBuilder sqlBuilder = switchToConnectionFunctionSqlBuilder(comTask, newConnectionFunction);
        return addConditionOnConnectionFunctionAndDevice(deviceConfiguration, oldConnectionFunction, sqlBuilder);
    }

    @Override
    public boolean hasComTaskExecutions(ComTaskEnablement comTaskEnablement) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("select count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" cte");
        sqlBuilder.append(" inner join ddc_device device on cte.device = device.id");
        sqlBuilder.append(" inner join dtc_comtaskenablement ctn on ctn.devicecomconfig = device.deviceconfigid");
        sqlBuilder.append(" where (device.deviceconfigid = ");
        sqlBuilder.addLong(comTaskEnablement.getDeviceConfiguration().getId());
        sqlBuilder.append(" and ((cte.discriminator = ");
        sqlBuilder.addObject(String.valueOf(ComTaskExecutionImpl.ComTaskExecType.MANUALLY_SCHEDULED_COM_TASK_EXECUTION_DISCRIMINATOR.ordinal()));
        sqlBuilder.append("    and cte.comtask = ctn.comtask and ctn.id =");
        sqlBuilder.addLong(comTaskEnablement.getId());
        sqlBuilder.append(") or (cte.discriminator = ");
        sqlBuilder.addObject(String.valueOf(ComTaskExecutionImpl.ComTaskExecType.SHARED_SCHEDULE_COM_TASK_EXECUTION_DISCRIMINATOR.ordinal()));
        sqlBuilder.append("and cte.comschedule in (select comschedule from sch_comtaskincomschedule where comtask = ");
        sqlBuilder.addLong(comTaskEnablement.getComTask().getId());
        sqlBuilder.append(")))) and cte.obsolete_date is null");
        try (Connection connection = this.deviceDataModelService.dataModel().getConnection(true);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count != 0;
                }
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        return false;
    }

    @Override
    public boolean hasComTaskExecutions(ComSchedule comSchedule) {
        Condition condition = where(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).isEqualTo(comSchedule).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        List<ComTaskExecution> comTaskExecutions = this.deviceDataModelService.dataModel().query(ComTaskExecution.class).select(condition, new Order[0], false, new String[0], 1, 1);
        return !comTaskExecutions.isEmpty();
    }

    @Override
    public Optional<ScheduledComTaskExecutionIdRange> getScheduledComTaskExecutionIdRange(long comScheduleId) {
        try (Connection connection = this.deviceDataModelService.dataModel().getConnection(true);
             PreparedStatement preparedStatement = connection.prepareStatement(this.getMinMaxComTaskExecutionIdStatement());) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.first();  // There is always at least one row since we are counting
                long minId = resultSet.getLong(0);
                if (resultSet.wasNull()) {
                    return Optional.empty();    // There were not ComTaskExecutions
                } else {
                    long maxId = resultSet.getLong(1);
                    return Optional.of(new ScheduledComTaskExecutionIdRange(comScheduleId, minId, maxId));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private String getMinMaxComTaskExecutionIdStatement() {
        return "SELECT MIN(id), MAX(id) FROM " + TableSpecs.DDC_COMTASKEXEC.name() + " WHERE comschedule = ? AND obsolete_date IS NULL";
    }

    @Override
    public void obsoleteComTaskExecutionsInRange(ScheduledComTaskExecutionIdRange idRange) {
        try (Connection connection = this.deviceDataModelService.dataModel().getConnection(true);
             PreparedStatement preparedStatement = connection.prepareStatement(this.getObsoleteComTaskExecutionInRangeStatement())) {
            preparedStatement.setDate(1, this.nowAsSqlDate());
            preparedStatement.setLong(2, idRange.comScheduleId);
            preparedStatement.setLong(3, idRange.minId);
            preparedStatement.setLong(4, idRange.maxId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private java.sql.Date nowAsSqlDate() {
        return new java.sql.Date(this.deviceDataModelService.clock().instant().toEpochMilli());
    }

    private String getObsoleteComTaskExecutionInRangeStatement() {
        return "UPDATE " + TableSpecs.DDC_COMTASKEXEC.name() + " SET OBSOLETE_DATE = ? WHERE comschedule = ? AND id BETWEEN ? AND ?";
    }

    @Override
    public void preferredPriorityChanged(ComTask comTask, DeviceConfiguration deviceConfiguration, int previousPreferredPriority, int newPreferredPriority) {
        this.deviceDataModelService.executeUpdate(this.preferredPriorityChangedSqlBuilder(comTask, deviceConfiguration, previousPreferredPriority, newPreferredPriority));
    }

    private SqlBuilder preferredPriorityChangedSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, int previousPreferredPriority, int newPreferredPriority) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set priority = ?");
        sqlBuilder.addInt(newPreferredPriority);
        sqlBuilder.append("   and priority = ?");  // Match the previous priority
        sqlBuilder.addInt(previousPreferredPriority);
        sqlBuilder.append("   and comtask = ?");  // Match the ComTask
        sqlBuilder.addLong(comTask.getId());
        return addConditionOnDeviceByConfiguration(deviceConfiguration, sqlBuilder);
    }

    @Override
    public void suspendAll(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.deviceDataModelService.executeUpdate(this.suspendAllSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder suspendAllSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        return suspendResumeSqlBuilder(comTask, deviceConfiguration, "nextExecutionTimestamp = null, onHold = 1");
    }

    private SqlBuilder suspendResumeSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, String specificValues) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set " + specificValues);
        sqlBuilder.append("   where device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        sqlBuilder.append("   and comtask =");
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and comport is null");    // exclude tasks that are currently executing
        return sqlBuilder;
    }

    @Override
    public void resumeAll(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.deviceDataModelService.executeUpdate(this.resumeAllSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder resumeAllSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        return suspendResumeSqlBuilder(comTask, deviceConfiguration, "onHold = 0, nextExecutionTimestamp = plannedNextExecutionTimestamp");
    }

    @Override
    public Optional<ComTaskExecution> findComTaskExecution(long id) {
        return this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class).getUnique("id", id);
    }

    @Override
    public Optional<ComTaskExecution> findAndLockComTaskExecutionByIdAndVersion(long id, long version) {
        return this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class).lockObjectIfVersion(version, id);
    }

    @Override
    public List<ComTaskExecution> findComTaskExecutionsByDevice(Device device) {
        Condition condition = where(ComTaskExecutionFields.DEVICE.name()).isEqualTo(device.getId()).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class).select(condition);
    }

    @Override
    public List<ComTaskExecution> findAllComTaskExecutionsIncludingObsoleteForDevice(Device device) {
        return this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class).find(ComTaskExecutionFields.DEVICE.fieldName(), device);
    }

    @Override
    public ComTaskExecution attemptLockComTaskExecution(ComTaskExecution comTaskExecution, ComPort comPort) {
        Optional<ComTaskExecution> lockResult = this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class).lockNoWait(comTaskExecution.getId());
        if (lockResult.isPresent()) {
            ComTaskExecution lockedComTaskExecution = lockResult.get();
            if (lockedComTaskExecution.getExecutingComPort() == null) {
                getServerComTaskExecution(lockedComTaskExecution).setLockedComPort(comPort);
                return lockedComTaskExecution;
            } else {
                // No database lock but business lock is already set
                return null;
            }
        } else {
            // ComTaskExecution no longer exists, attempt to lock fails
            return null;
        }
    }

    @Override
    public boolean attemptUnlockComTaskExecution(ComTaskExecution comTaskExecution) {
        Optional<ComTaskExecution> lockResult = deviceDataModelService.dataModel().mapper(ComTaskExecution.class).lockNoWait(comTaskExecution.getId());
        if (lockResult.isPresent()) {
            //getServerComTaskExecution(lockResult.get()).setLockedComPort(null);
            unlockComTaskExecution(comTaskExecution);
            return true;
        }

        return false;
    }

    @Override
    public void unlockComTaskExecution(ComTaskExecution comTaskExecution) {
        try {
            getServerComTaskExecution(comTaskExecution).setLockedComPort(null);
        } catch(OptimisticLockException e) {
            refreshComTaskExecution(comTaskExecution).setLockedComPort(null);
        }
    }

    @Override
    public Finder<ComTaskExecution> findComTaskExecutionsByConnectionTask(ConnectionTask<?, ?> connectionTask) {
        return DefaultFinder.of(ComTaskExecution.class, where(ComTaskExecutionFields.CONNECTIONTASK.fieldName()).isEqualTo(connectionTask), this.deviceDataModelService.dataModel())
                .sorted("executionStart", false);
    }

    @Override
    public List<ComTaskExecution> findComTasksByDefaultConnectionTask(Device device) {
        return this.deviceDataModelService.dataModel()
                .query(ComTaskExecution.class)
                .select(where(ComTaskExecutionFields.DEVICE.fieldName()).isEqualTo(device)
                        .and(where(ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName()).isEqualTo(true))
                        .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull()));
    }

    @Override
    public Map<ConnectionFunction, List<ComTaskExecution>> findComTasksUsingConnectionFunction(Device device) {
        List<ComTaskExecution> allComTaskExecutionsWithConnectionFunction = this.deviceDataModelService.dataModel()
                .query(ComTaskExecution.class)
                .select(where(ComTaskExecutionFields.DEVICE.fieldName()).isEqualTo(device)
                        .and(where(ComTaskExecutionFields.CONNECTION_FUNCTION.fieldName()).isNotEqualAndNotBothNull(0))
                        .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull()));

        Map<ConnectionFunction, List<ComTaskExecution>> map = new HashMap<>();
        allComTaskExecutionsWithConnectionFunction.forEach(cte -> {
            ConnectionFunction connectionFunction = cte.getConnectionFunction().get();
            List<ComTaskExecution> comTaskExecutions;
            if (map.containsKey(connectionFunction)) {
                comTaskExecutions = map.get(connectionFunction);
                comTaskExecutions.add(cte);
            } else {
                comTaskExecutions = new ArrayList(Arrays.asList(cte));  // Should be mutable list
            }
            map.put(connectionFunction, comTaskExecutions);
        });
        return map;
    }

    @Override
    public List<ComTaskExecution> findComTaskExecutionsByComSchedule(ComSchedule comSchedule) {
        return this.deviceDataModelService.dataModel().query(ComTaskExecution.class)
                .select(where(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).isEqualTo(comSchedule)
                        .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull()));
    }

    @Override
    public List<ComTaskExecution> findComTaskExecutionsByComScheduleWithinRange(ComSchedule comSchedule, long minId, long maxId) {
        return this.deviceDataModelService.dataModel().query(ComTaskExecution.class)
                .select(where(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).isEqualTo(comSchedule)
                        .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull())
                        .and(where(ComTaskExecutionFields.ID.fieldName()).between(minId).and(maxId)));
    }

    private SqlBuilder initComTaskExecFinderSqlBuilder(DataMapper<ComTaskExecution> mapper) {
        SqlBuilder sqlBuilder = new SqlBuilder("SELECT ");
        sqlBuilder.append("t.ID, t.VERSIONCOUNT, t.CREATETIME, t.MODTIME, " +
                "t.USERNAME, t.DISCRIMINATOR, t.DEVICE, t.COMTASK, t.COMSCHEDULE, t.NEXTEXECUTIONSPECS, " +
                "t.LASTEXECUTIONTIMESTAMP, t.NEXTEXECUTIONTIMESTAMP, t.COMPORT, t.OBSOLETE_DATE, t.PRIORITY, " +
                "t.USEDEFAULTCONNECTIONTASK, t.CURRENTRETRYCOUNT, t.PLANNEDNEXTEXECUTIONTIMESTAMP, t.EXECUTIONPRIORITY, " +
                "t.EXECUTIONSTART, t.LASTSUCCESSFULCOMPLETION, t.LASTEXECUTIONFAILED, t.ONHOLD, t.CONNECTIONTASK, " +
                "t.IGNORENEXTEXECSPECS, t.CONNECTIONFUNCTION, t.LASTSESSION, t.LASTSESS_HIGHESTPRIOCOMPLCODE, " +
                "t.LASTSESS_SUCCESSINDICATOR");
        sqlBuilder.append(" FROM (");
        sqlBuilder.append(mapper.builderWithAdditionalColumns("cte", "row_number() over (ORDER BY cte.nextexecutiontimestamp, cte.priority, cte.connectiontask) as rn").toString());
        sqlBuilder.append(" LEFT JOIN ");
        sqlBuilder.append(TableSpecs.DDC_HIPRIOCOMTASKEXEC.name());
        sqlBuilder.append(" hpcte on cte.ID = hpcte.COMTASKEXECUTION");
        sqlBuilder.append(", ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct");
        sqlBuilder.append(" WHERE hpcte.COMTASKEXECUTION is null");
        sqlBuilder.append("   and ct.status = 0");
        sqlBuilder.append("   and ct.comport is null");
        sqlBuilder.append("   and ct.obsolete_date is null");
        return sqlBuilder;
    }

    @Override
    public List<ComTaskExecution> getPendingComTaskExecutionsListFor(OutboundComPort comPort, int factor) {
        Instant nowInSeconds = this.deviceDataModelService.clock().instant();
        List<PriorityComTaskExecutionLink> pendingPrioComTasks = getPendingPrioComTaskExecutions(nowInSeconds);
        List<ComTaskExecution> pendingComTasks = getPendingComTaskExecutions(comPort, nowInSeconds, factor);

        return filterOutPendingPrioComTasks(pendingPrioComTasks, pendingComTasks);
    }


    @Override
    public List<ComTaskExecution> getPendingComTaskExecutionsListFor(ComServer comServer, List<OutboundComPortPool> comPortPools, Duration delta, long limit, long skip) {
        Instant timeInSeconds = Instant.now().plus(delta);
        List<PriorityComTaskExecutionLink> pendingPrioComTasks = getPendingPrioComTaskExecutions(timeInSeconds);
        List<ComTaskExecution> pendingComTasks = getPendingComTaskExecutions(comServer, comPortPools, timeInSeconds, limit, skip);
        return filterOutPendingPrioComTasks(pendingPrioComTasks, pendingComTasks);
    }

    private List<PriorityComTaskExecutionLink> getPendingPrioComTaskExecutions(Instant nowInSeconds) {
        return deviceDataModelService.dataModel().stream(PriorityComTaskExecutionLink.class)
                .filter(where(PriorityComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).isLessThanOrEqual(nowInSeconds)
                        .and(where(PriorityComTaskExecutionFields.COMPORT.fieldName()).isNull()))
                .select();
    }

    private List<ComTaskExecution> getPendingComTaskExecutions(OutboundComPort comPort, Instant nowInSeconds, int factor) {
        long msSinceMidnight = nowInSeconds.atZone(ZoneId.systemDefault()).toLocalTime().toSecondOfDay() * 1000;
        List<OutboundComPortPool> comPortPools =
                this.deviceDataModelService
                        .engineConfigurationService()
                        .findContainingComPortPoolsForComPort(comPort)
                        .stream()
                        .filter(ComPortPool::isActive)
                        .collect(Collectors.toList());
        String connectionTask = ComTaskExecutionFields.CONNECTIONTASK.fieldName() + ".";
        try(QueryStream<ComTaskExecution> comTasks = getFilteredPendingComTaskExecutions(nowInSeconds, msSinceMidnight, comPortPools, connectionTask);) {
            if (factor > 0) {
                comTasks.limit(comPort.getNumberOfSimultaneousConnections() * factor);
                // one comport is starting at row 1, the other at limit + 1
                if (!comTaskExecutionBalancing.isAscending(comPortPools, comPort)) {
                    comTasks.skip(comPort.getNumberOfSimultaneousConnections() * factor);
                }
            }
            initCommunicationParameters();
            return sortComTaskExecutions(connectionTask, comTasks);
        }
    }

    private void initCommunicationParameters() {
        isTrueMinimizedOn = configPropertiesService.getPropertyValue("COMMUNICATION", ConfigProperties.TRUE_MINIMIZED.value()).map(v -> v.equals("1")).orElse(false);
        isRandomizationOn = configPropertiesService.getPropertyValue("COMMUNICATION", ConfigProperties.RANDOMIZATION.value()).map(v -> v.equals("1")).orElse(false);
    }

    private List<ComTaskExecution> sortComTaskExecutions(String connectionTask, QueryStream<ComTaskExecution> comTasks) {
        long start = System.currentTimeMillis();
        List<ComTaskExecution> comTaskExecutions = comTasks.sorted(Order.ascending(connectionTask + ConnectionTaskFields.NEXT_EXECUTION_TIMESTAMP.fieldName()),
                getOrderForPlannedComTaskExecutionsList(connectionTask))
                .select();
        long queryDuration = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        comTaskExecutions.sort((cte1, cte2) -> {
            int result = cte1.getNextExecutionTimestamp().compareTo(cte2.getNextExecutionTimestamp());
            if (result != 0)
                return result;

            if (isRandomizationOn) {
                if (cte1.getConnectionTaskId() % 100 < cte2.getConnectionTaskId() % 100)
                    return -1;
                if (cte1.getConnectionTaskId() % 100 > cte2.getConnectionTaskId() % 100)
                    return 1;
            }

            if (isTrueMinimizedOn) {
                if (cte1.getConnectionTaskId() < cte2.getConnectionTaskId())
                    return -1;
                if (cte1.getConnectionTaskId() > cte2.getConnectionTaskId())
                    return 1;
            }

            return cte1.getPlannedPriority() - cte2.getPlannedPriority();
        });
        long sortDuration = System.currentTimeMillis() - start;
        LOGGER.warning("perf - pendingQuery: " + queryDuration + " ms; sort: " + sortDuration + " ms");
        return comTaskExecutions;
    }

    private List<ComTaskExecution> getPendingComTaskExecutions(ComServer comServer, List<OutboundComPortPool> comPortPools, Instant timeInSeconds, long limit, long skip) {
        long msSinceMidnight = timeInSeconds.atZone(ZoneId.systemDefault()).toLocalTime().toSecondOfDay() * 1000;
        String connectionTask = ComTaskExecutionFields.CONNECTIONTASK.fieldName() + ".";
        QueryStream<ComTaskExecution> comTasks = getFilteredPendingComTaskExecutions(timeInSeconds, msSinceMidnight, comPortPools, connectionTask);
        if (limit > 0) {
            comTasks.limit(limit);
            if (skip >= 0) {
                comTasks.skip(skip);
            }
        }
        List<ComServer> comServers = this.deviceDataModelService
                .engineConfigurationService().findAllComServers().sorted("id", true).find();
        initCommunicationParameters();
        return comTasks.sorted(Order.ascending(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()),
                getOrderForPlannedComTaskExecutionsList(connectionTask))
                .select();
    }

    private List<ComTaskExecution> getPlannedComTaskExecutions(OutboundComPort comPort, Instant nowInSeconds) {
        long msSinceMidnight = nowInSeconds.atZone(ZoneId.systemDefault()).toLocalTime().toSecondOfDay() * 1000;
        List<OutboundComPortPool> comPortPools =
                this.deviceDataModelService
                        .engineConfigurationService()
                        .findContainingComPortPoolsForComPort(comPort)
                        .stream()
                        .filter(ComPortPool::isActive)
                        .collect(Collectors.toList());
        String connectionTask = ComTaskExecutionFields.CONNECTIONTASK.fieldName() + ".";
        List<OutboundComPort> comPorts = this.deviceDataModelService
                .engineConfigurationService().findAllOutboundComPorts();
        initCommunicationParameters();
        return getFilteredPendingComTaskExecutions(nowInSeconds, msSinceMidnight, comPortPools, connectionTask)
                .limit(comPort.getNumberOfSimultaneousConnections() * 2)
                .sorted(Order.ascending(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()),
                        getOrderForPlannedComTaskExecutionsList(connectionTask))
                .select();
    }

    private QueryStream<ComTaskExecution> getFilteredPendingComTaskExecutions(Instant nowInSeconds, long msSinceMidnight, List<OutboundComPortPool> comPortPools, String connectionTask) {
        return deviceDataModelService.dataModel().stream(ComTaskExecution.class)
                .join(ConnectionTask.class)
                .filter(where(connectionTask + ConnectionTaskFields.STATUS.fieldName()).isEqualTo(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
                        .and(where(connectionTask + ConnectionTaskFields.COM_PORT.fieldName()).isNull())
                        .and(where(connectionTask + ConnectionTaskFields.OBSOLETE_DATE.fieldName()).isNull())
                        .and(where(connectionTask + ConnectionTaskFields.COM_PORT_POOL.fieldName()).in(comPortPools))
                        .and(where(ComTaskExecutionFields.COMPORT.fieldName()).isNull())
                        .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull())
                        .and(where(connectionTask + ConnectionTaskFields.NEXT_EXECUTION_TIMESTAMP.fieldName()).isLessThanOrEqual(nowInSeconds))
                        .and(where(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).isLessThanOrEqual(nowInSeconds))
                        .and(
                                where(ComTaskExecutionFields.ONHOLD.fieldName()).isNull()
                                        .or
                                (where(ComTaskExecutionFields.ONHOLD.fieldName()).isEqualTo(false)))
                        .and(where(connectionTask + "comWindow.start.millis").isNull().or(where(connectionTask + "comWindow.start.millis").isLessThanOrEqual(msSinceMidnight)))
                        .and(where(connectionTask + "comWindow.end.millis").isNull().or(where(connectionTask + "comWindow.end.millis").isLessThanOrEqual(msSinceMidnight)))
                );
    }

    private Order[] getOrderForPlannedComTaskExecutionsList(String connectionTask) {
        List<Order> orderList = new ArrayList<>(3);

        if (isRandomizationOn) {
            // the ORM can't resolve the table alias from a field inside a function - providing it hardcoded (ct)
            orderList.add(Order.ascending("mod(ct." + ConnectionTaskFields.ID.fieldName() + ",100)"));
        }
        if (isTrueMinimizedOn) {
            orderList.add(Order.ascending(connectionTask + ConnectionTaskFields.ID.fieldName()));
        }
        return orderList.toArray(new Order[orderList.size()]);
    }

    private List<ComTaskExecution> filterOutPendingPrioComTasks(List<PriorityComTaskExecutionLink> pendingPrioComTasks, List<ComTaskExecution> pendingComTasks) {
        List<Long> prioIds = pendingPrioComTasks.stream().map(pct -> pct.getComTaskExecution().getId()).collect(Collectors.toList());
        pendingComTasks.removeIf(cte -> prioIds.contains(cte.getId()));

        return pendingComTasks;
    }

    @Override
    public Fetcher<ComTaskExecution> getPlannedComTaskExecutionsFor(OutboundComPort comPort) {
        List<OutboundComPortPool> comPortPools =
                this.deviceDataModelService
                        .engineConfigurationService()
                        .findContainingComPortPoolsForComPort(comPort)
                        .stream()
                        .filter(ComPortPool::isActive)
                        .collect(Collectors.toList());
        if (!comPortPools.isEmpty()) {
            long nowInSeconds = this.toSeconds(this.deviceDataModelService.clock().instant());
            DataMapper<ComTaskExecution> mapper = this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class);
            SqlBuilder sqlBuilder = initComTaskExecFinderSqlBuilder(mapper);
            sqlBuilder.append("   and cte.obsolete_date is null");
            sqlBuilder.append("   and cte.connectiontask = ct.id");
            sqlBuilder.append("   and cte.nextexecutiontimestamp <=");
            sqlBuilder.addLong(nowInSeconds);
            sqlBuilder.append("   and cte.comport is null");
            sqlBuilder.append("   and ct.nextExecutionTimestamp <=");
            sqlBuilder.addLong(nowInSeconds);
            sqlBuilder.append("   and ct.comportpool in (");
            int count = 1;
            for (ComPortPool comPortPool : comPortPools) {
                sqlBuilder.addLong(comPortPool.getId());
                if (count < comPortPools.size()) {
                    sqlBuilder.append(", ");
                }
                count++;
            }
            sqlBuilder.append(") ");
            long msSinceMidnight = LocalTime.now(ZoneId.systemDefault()).toSecondOfDay() * 1000;
            sqlBuilder.append(" and NVL(ct.comwindowstart, 0) <= ");
            sqlBuilder.addLong(msSinceMidnight);
            sqlBuilder.append(" and (NVL(ct.comwindowend, 99999000) > "); // max possible value of milisecondsSinceMidnight is 86399000
            sqlBuilder.addLong(msSinceMidnight);
            sqlBuilder.append(" or ct.comwindowend = 0) ");
            sqlBuilder.append(") t");
            sqlBuilder.append(" WHERE rn <= ");
            sqlBuilder.addLong(1300L);
            sqlBuilder.append(getOrderForPlannedComTaskExecutions());
            return mapper.fetcher(sqlBuilder);
        } else {
            return new NoComTaskExecutions();
        }
    }

    private String getOrderForPlannedComTaskExecutions() {
        String orderClause = "";
        boolean isTrueMinimizedOn = configPropertiesService.getPropertyValue("COMMUNICATION", ConfigProperties.TRUE_MINIMIZED.value()).map(v -> v.equals("1")).orElse(false);
        boolean isRandomizationOn = configPropertiesService.getPropertyValue("COMMUNICATION", ConfigProperties.RANDOMIZATION.value()).map(v -> v.equals("1")).orElse(false);

        if (!isTrueMinimizedOn && !isRandomizationOn) {
            orderClause = " order by t.nextexecutiontimestamp, t.priority, t.connectiontask";
        } else if (isTrueMinimizedOn && !isRandomizationOn) {
            orderClause = " order by t.nextexecutiontimestamp, t.connectiontask, t.priority";
        } else if (!isTrueMinimizedOn && isRandomizationOn) {
            orderClause = " order by t.nextexecutiontimestamp, mod(t.connectiontask, 100), t.priority, t.connectiontask";
        } else if (isTrueMinimizedOn && isRandomizationOn) {
            orderClause = " order by t.nextexecutiontimestamp, mod(t.connectiontask, 100), t.connectiontask, t.priority";
        }
        return orderClause;
    }

    public Fetcher<ComTaskExecution> findComTaskExecutionsForDevicesByComTask(List<Long> deviceIds, List<Long> comTaskIds) {
        DataMapper<ComTaskExecution> mapper = this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class);
        SqlBuilder sqlBuilder = mapper.builder("cte");
        sqlBuilder.append(" where cte.obsolete_date is null");
        sqlBuilder.append("   and cte.onhold = 0");
        sqlBuilder.append("   and cte.lastsuccessfulcompletion is not null");
        sqlBuilder.append("   and cte.lastexecutiontimestamp is not null");
        sqlBuilder.append("   and cte.lastexecutiontimestamp > cte.lastsuccessfulcompletion");
        sqlBuilder.append("   and cte.lastexecutionfailed = 1");
        sqlBuilder.append("   and cte.currentretrycount = 0");
        addConditionOnIdList(deviceIds, "cte.device", sqlBuilder);
        addConditionOnIdList(comTaskIds, "cte.comtask", sqlBuilder);

        return mapper.fetcher(sqlBuilder);
    }

    private void addConditionOnIdList(List<Long> deviceIds, String column, SqlBuilder sqlBuilder) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return;
        }
        sqlBuilder.append("   and ");
        INClauseBuilder.build(deviceIds, column, sqlBuilder);
    }

    @Override
    public List<ComTaskExecution> getPlannedComTaskExecutionsFor(InboundComPort comPort, Device device) {
        if (comPort.isActive()) {
            InboundComPortPool inboundComPortPool = comPort.getComPortPool();
            if (!inboundComPortPool.isActive()) {
                return Collections.emptyList();
            }
            Instant now = deviceDataModelService.clock().instant();
            Condition condition =
                    where("connectionTask." + ConnectionTaskFields.COM_PORT.fieldName()).isNull()
                            .and(where("connectionTask." + ConnectionTaskFields.OBSOLETE_DATE.fieldName()).isNull())
                            .and(where("connectionTask." + ConnectionTaskFields.DEVICE.name()).isEqualTo(device.getId()))
                            .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull())
                            .and(where(ComTaskExecutionFields.IGNORENEXTEXECUTIONSPECSFORINBOUND.fieldName()).isEqualTo(true)
                                    .or(where(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).isLessThanOrEqual(now)))
                            .and(where(ComTaskExecutionFields.COMPORT.fieldName()).isNull())
                            .and(where("connectionTask." + ConnectionTaskFields.COM_PORT_POOL.fieldName()).isEqualTo(inboundComPortPool));
            return deviceDataModelService.dataModel().query(ComTaskExecution.class, ConnectionTask.class).select(condition,
                    Order.ascending(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()),
                    Order.ascending(ComTaskExecutionFields.PLANNED_PRIORITY.fieldName()),
                    Order.ascending(ComTaskExecutionFields.CONNECTIONTASK.fieldName()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isComTaskStillPending(long comTaskExecutionId) {
        Instant now = deviceDataModelService.clock().instant();
        Condition condition = where(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).isLessThanOrEqual(now)
                .and(where("id").isEqualTo(comTaskExecutionId))
                .and(where(ComTaskExecutionFields.COMPORT.fieldName()).isNull());
        return !deviceDataModelService.dataModel().query(ComTaskExecution.class, ConnectionTask.class).select(condition).isEmpty();
    }

    @Override
    public boolean areComTasksStillPending(Collection<Long> comTaskExecutionIds) {
        Instant now = deviceDataModelService.clock().instant();
        Condition condition = where(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).isLessThanOrEqual(now)
                .and(ListOperator.IN.contains("id", new ArrayList<>(comTaskExecutionIds)))
                .and(where("connectionTask." + ConnectionTaskFields.COM_PORT.fieldName()).isNull());
        return !deviceDataModelService.dataModel().query(ComTaskExecution.class, ConnectionTask.class).select(condition).isEmpty();
    }

    @Override
    public Optional<ComTaskExecutionSession> findLastSessionFor(ComTaskExecution comTaskExecution) {
        return comTaskExecution.getLastSession();
    }

    private ServerComTaskExecution getServerComTaskExecution(ComTaskExecution comTaskExecution) {
        return (ServerComTaskExecution) comTaskExecution;
    }

    @Override
    public void executionCompletedFor(ComTaskExecution comTaskExecution) {
        try {
            getServerComTaskExecution(comTaskExecution).executionCompleted();
        } catch(OptimisticLockException e) {
            refreshComTaskExecution(comTaskExecution).executionCompleted();
        }
    }

    @Override
    public void executionFailedFor(ComTaskExecution comTaskExecution) {
        try {
            getServerComTaskExecution(comTaskExecution).executionFailed();
        } catch(OptimisticLockException e) {
            refreshComTaskExecution(comTaskExecution).executionFailed();
        }
    }

    @Override
    public void executionStartedFor(ComTaskExecution comTaskExecution, ComPort comPort) {
        try {
            getServerComTaskExecution(comTaskExecution).executionStarted(comPort);
        } catch(OptimisticLockException e) {
            refreshComTaskExecution(comTaskExecution).executionStarted(comPort);
        }
    }

    @Override
    public void executionRescheduled(ComTaskExecution comTaskExecution, Instant rescheduleDate) {
        try {
            getServerComTaskExecution(comTaskExecution).executionRescheduled(rescheduleDate);
        } catch(OptimisticLockException e) {
            refreshComTaskExecution(comTaskExecution).executionRescheduled(rescheduleDate);
        }
    }

    public void executionRescheduledToComWindow(ComTaskExecution comTaskExecution, Instant comWindowStartDate) {
        try {
            getServerComTaskExecution(comTaskExecution).executionRescheduledToComWindow(comWindowStartDate);
        } catch(OptimisticLockException e) {
            refreshComTaskExecution(comTaskExecution).executionRescheduledToComWindow(comWindowStartDate);
        }
    }

    @Override
    public List<ComTaskExecution> findLockedByComPort(ComPort comPort) {
        Condition condition = where(ComTaskExecutionFields.COMPORT.fieldName()).isEqualTo(comPort);
        return this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class).select(condition);
    }

    @Override
    public Finder<ComTaskExecutionSession> findSessionsByComTaskExecution(ComTaskExecution comTaskExecution) {
        return DefaultFinder.of(ComTaskExecutionSession.class,
                Where.where(ComTaskExecutionSessionImpl.Fields.COM_TASK_EXECUTION.fieldName()).isEqualTo(comTaskExecution),
                this.deviceDataModelService.dataModel()).sorted(ComTaskExecutionSessionImpl.Fields.START_DATE.fieldName(), false);
    }

    @Override
    public Optional<ComTaskExecutionSession> findSession(long sessionId) {
        return this.deviceDataModelService.dataModel().mapper(ComTaskExecutionSession.class).getOptional(sessionId);
    }

    @Override
    public Finder<ComTaskExecutionSession> findSessionsByComTaskExecutionAndComTask(ComTaskExecution comTaskExecution, ComTask comTask) {
        return DefaultFinder.of(ComTaskExecutionSession.class,
                Where.where(ComTaskExecutionSessionImpl.Fields.COM_TASK_EXECUTION.fieldName()).isEqualTo(comTaskExecution).
                        and(Where.where(ComTaskExecutionSessionImpl.Fields.COM_TASK.fieldName()).isEqualTo(comTask)),
                this.deviceDataModelService.dataModel()).sorted(ComTaskExecutionSessionImpl.Fields.START_DATE.fieldName(), false);
    }

    @Override
    public int countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(List<Device> devices, Range<Instant> range, Condition successIndicatorCondition) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(successIndicatorCondition);
        conditions.add(where(ComTaskExecutionSessionImpl.Fields.DEVICE.fieldName()).in(devices));
        if (range.hasLowerBound()) {
            conditions.add(where(ComTaskExecutionSessionImpl.Fields.SESSION.fieldName() + "." + ComSessionImpl.Fields.START_DATE.fieldName()).isGreaterThanOrEqual(range.lowerEndpoint()));
        }
        if (range.hasUpperBound()) {
            conditions.add(where(ComTaskExecutionSessionImpl.Fields.SESSION.fieldName() + "." + ComSessionImpl.Fields.STOP_DATE.fieldName()).isLessThanOrEqual(range.upperEndpoint()));
        }
        Condition execSessionCondition = this.andAll(conditions);
        List<ComTaskExecutionSession> comTaskExecutionSessions = this.deviceDataModelService.dataModel()
                .query(ComTaskExecutionSession.class, ComSession.class, Device.class)
                .select(execSessionCondition);
        Set<Long> uniqueDeviceIds = new HashSet<>();
        for (ComTaskExecutionSession comTaskExecutionSession : comTaskExecutionSessions) {
            uniqueDeviceIds.add(comTaskExecutionSession.getDevice().getId());
        }
        return uniqueDeviceIds.size();
    }

    @Override
    public boolean shouldExecuteWithPriority(ComTaskExecution comTaskExecution) {
        return priorityComTaskService.findByComTaskExecution(comTaskExecution).isPresent();
    }

    private Condition andAll(List<Condition> conditions) {
        Condition superCondition = null;
        for (Condition condition : conditions) {
            if (superCondition == null) {
                superCondition = condition;
            } else {
                superCondition = superCondition.and(condition);
            }
        }
        return superCondition;
    }

    /**
     * Provides an implementation for the Fetcher interface
     * that never returns any {@link ComTaskExecution}.
     */
    private class NoComTaskExecutions implements Fetcher<ComTaskExecution> {
        @Override
        public void close() {
            // Nothing to close because there was nothing to read from.
        }

        @Override
        public Iterator<ComTaskExecution> iterator() {
            return Collections.emptyIterator();
        }

    }

}
