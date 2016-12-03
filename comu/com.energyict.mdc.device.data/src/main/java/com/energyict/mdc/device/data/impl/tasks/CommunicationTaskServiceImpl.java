package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.ScheduledComTaskExecutionIdRange;
import com.energyict.mdc.device.data.impl.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComTaskExecutionSessionImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFields;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import com.google.common.collect.Range;
import org.joda.time.DateTimeConstants;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

    @Inject
    public CommunicationTaskServiceImpl(DeviceDataModelService deviceDataModelService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public void releaseInterruptedComTasks(ComServer comServer) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_COMTASKEXEC.name() + " SET comport = NULL, executionStart = null WHERE comport in (select id from mdc_comport where COMSERVERID = ");
        sqlBuilder.addLong(comServer.getId());
        sqlBuilder.append(")");
        this.deviceDataModelService.executeUpdate(sqlBuilder);
    }

    @Override
    public TimeDuration releaseTimedOutComTasks(ComServer comServer) {
        int waitTime = -1;
        List<ComPortPool> containingComPortPoolsForComServer = this.deviceDataModelService.engineConfigurationService().findContainingComPortPoolsForComServer(comServer);
        for (ComPortPool comPortPool : containingComPortPoolsForComServer) {
            this.releaseTimedOutComTasks((OutboundComPortPool) comPortPool);
            waitTime = this.minimumWaitTime(waitTime, ((OutboundComPortPool) comPortPool).getTaskExecutionTimeout().getSeconds());
        }
        if (waitTime <= 0) {
            return new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
        } else {
            return new TimeDuration(waitTime, TimeDuration.TimeUnit.SECONDS);
        }
    }

    private ServerComTaskExecution refreshComTaskExecution(ComTaskExecution comTaskExecution) {
        return (ServerComTaskExecution) this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class).getUnique("id", comTaskExecution.getId()).get();
    }

    private int minimumWaitTime(int currentWaitTime, int comPortPoolTaskExecutionTimeout) {
        if (currentWaitTime < 0) {
            return comPortPoolTaskExecutionTimeout;
        } else {
            return Math.min(currentWaitTime, comPortPoolTaskExecutionTimeout);
        }
    }

    private void releaseTimedOutComTasks(OutboundComPortPool outboundComPortPool) {
        long now = this.toSeconds(this.deviceDataModelService.clock().instant());
        int timeOutSeconds = outboundComPortPool.getTaskExecutionTimeout().getSeconds();
        this.deviceDataModelService.executeUpdate(this.releaseTimedOutComTaskExecutionsSqlBuilder(outboundComPortPool, now, timeOutSeconds));
    }

    private SqlBuilder releaseTimedOutComTaskExecutionsSqlBuilder(OutboundComPortPool outboundComPortPool, long now, int timeOutSeconds) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append("   set comport = null, executionStart = null");
        sqlBuilder.append(" where id in (");
        TimedOutTasksSqlBuilder.appendTimedOutComTaskExecutionSql(sqlBuilder, outboundComPortPool, now, timeOutSeconds);
        sqlBuilder.append(")");
        return sqlBuilder;
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
    public void removePreferredConnectionTask(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask) {
        this.deviceDataModelService.executeUpdate(this.removePreferredConnectionTaskSqlBuilder(comTask, deviceConfiguration, partialConnectionTask));
    }

    private SqlBuilder removePreferredConnectionTaskSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set connectionTask = null");
        sqlBuilder.append(" where comtask = ?");  // Match the ComTask
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
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set useDefaultConnectionTask = 0, connectionTask = ");
        sqlBuilder.append("select id from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" where device = ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(".device");
        sqlBuilder.append("   and partialconnectiontask =");  //Match the connection task against the same device
        sqlBuilder.addLong(partialConnectionTask.getId());
        sqlBuilder.append("   and obsolete_date is null)");
        sqlBuilder.append(" where comtask =");  // Match the ComTask
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    @Override
    public void switchOnDefault(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.deviceDataModelService.executeUpdate(this.switchOnDefaultSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder switchOnDefaultSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
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
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
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
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    @Override
    public void switchFromPreferredConnectionTaskToDefault(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask) {
        this.deviceDataModelService.executeUpdate(this.switchFromPreferredConnectionTaskToDefaultSqlBuilder(comTask, deviceConfiguration, previousPartialConnectionTask));
    }

    private SqlBuilder switchFromPreferredConnectionTaskToDefaultSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask) {
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
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and connectionTask = ");
        sqlBuilder.append("(select id from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" where device = ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(".device");
        sqlBuilder.append("    and partialconnectiontask =");   // Match the connection task
        sqlBuilder.addLong(previousPartialConnectionTask.getId());
        sqlBuilder.append("    and obsolete_date is null)");
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
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
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
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
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    @Override
    public void suspendAll(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.deviceDataModelService.executeUpdate(this.suspendAllSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder suspendAllSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set nextExecutionTimestamp = null, onHold = 1");
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
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set onHold = 0, nextExecutionTimestamp = plannedNextExecutionTimestamp");
        sqlBuilder.append("   where device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        sqlBuilder.append("   and comtask =");
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and onHold = 1");      // Only tasks that were suspended
        return sqlBuilder;
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
    public void unlockComTaskExecution(ComTaskExecution comTaskExecution) {
        //Avoid OptimisticLockException
        refreshComTaskExecution(comTaskExecution).setLockedComPort(null);
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
            com.elster.jupiter.util.sql.SqlBuilder sqlBuilder = mapper.builder("cte", "FIRST_ROWS(1) LEADING(cte) USE_NL(ct)");
            sqlBuilder.append(", ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(" ct");
            sqlBuilder.append(" where ct.status = 0");
            sqlBuilder.append("   and ct.comserver is null");
            sqlBuilder.append("   and ct.obsolete_date is null");
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
            sqlBuilder.append(") order by cte.nextexecutiontimestamp, cte.priority, cte.connectiontask");
            return mapper.fetcher(sqlBuilder);
        } else {
            return new NoComTaskExecutions();
        }
    }

    @Override
    public List<ComTaskExecution> getPlannedComTaskExecutionsFor(InboundComPort comPort, Device device) {
        if (comPort.isActive()) {
            InboundComPortPool inboundComPortPool = comPort.getComPortPool();
            if (!inboundComPortPool.isActive()) {
                return Collections.emptyList();
            }
            Instant now = this.deviceDataModelService.clock().instant();
            Condition condition =
                    where("connectionTask.comServer").isNull()
                            .and(where("connectionTask.obsoleteDate").isNull())
                            .and(where("connectionTask." + ConnectionTaskFields.DEVICE.name()).isEqualTo(device.getId()))
                            .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull())
                            .and(where(ComTaskExecutionFields.IGNORENEXTEXECUTIONSPECSFORINBOUND.fieldName()).isEqualTo(true)
                                    .or(where(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).isLessThanOrEqual(now)))
                            .and(where(ComTaskExecutionFields.COMPORT.fieldName()).isNull())
                            .and(where("connectionTask.comPortPool").isEqualTo(inboundComPortPool));
            return this.deviceDataModelService.dataModel().query(ComTaskExecution.class, ConnectionTask.class).select(condition,
                    Order.ascending(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()),
                    Order.ascending(ComTaskExecutionFields.PLANNED_PRIORITY.fieldName()),
                    Order.ascending(ComTaskExecutionFields.CONNECTIONTASK.fieldName()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isComTaskStillPending(long comTaskExecutionId) {
        Instant now = this.deviceDataModelService.clock().instant();
        Condition condition = where(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).isLessThanOrEqual(now)
                .and(where("id").isEqualTo(comTaskExecutionId))
                .and(where("comPort").isNull());
        return !this.deviceDataModelService.dataModel().query(ComTaskExecution.class, ConnectionTask.class).select(condition).isEmpty();
    }

    @Override
    public boolean areComTasksStillPending(Collection<Long> comTaskExecutionIds) {
        Instant now = this.deviceDataModelService.clock().instant();
        Condition condition = where(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).isLessThanOrEqual(now)
                .and(ListOperator.IN.contains("id", new ArrayList<>(comTaskExecutionIds)))
                .and(where("connectionTask.comServer").isNull());
        return !this.deviceDataModelService.dataModel().query(ComTaskExecution.class, ConnectionTask.class).select(condition).isEmpty();
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
        //Avoid OptimisticLockException
        refreshComTaskExecution(comTaskExecution).executionCompleted();
    }

    @Override
    public void executionFailedFor(ComTaskExecution comTaskExecution) {
        //Avoid OptimisticLockException
        refreshComTaskExecution(comTaskExecution).executionFailed();
    }

    @Override
    public void executionStartedFor(ComTaskExecution comTaskExecution, ComPort comPort) {
        //Avoid OptimisticLockException
        refreshComTaskExecution(comTaskExecution).executionStarted(comPort);
    }

    @Override
    public void executionRescheduled(ComTaskExecution comTaskExecution, Instant rescheduleDate) {
        refreshComTaskExecution(comTaskExecution).executionRescheduled(rescheduleDate);
    }

    @Override
    public List<ComTaskExecution> findComTaskExecutionsWhichAreExecuting(ComPort comPort) {
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
