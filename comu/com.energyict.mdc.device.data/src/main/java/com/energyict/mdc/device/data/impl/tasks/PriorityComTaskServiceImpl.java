/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.HighPriorityComJob;
import com.energyict.mdc.common.comserver.OutboundCapableComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.common.tasks.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFields;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.PriorityComTaskExecutionFields;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.tasks.NoMoreHighPriorityTasksCanBePickedUpRuntimeException;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

@LiteralSql
public class PriorityComTaskServiceImpl implements PriorityComTaskService {

    private static final Logger LOGGER = Logger.getLogger(PriorityComTaskServiceImpl.class.getName());
    private static final int CONNECTION_TASK_ID_RESULT_SET_INDEX = PriorityComTaskExecutionFields.values().length + 1;
    private static final int CONNECTION_TASK_COMPORT_POOL_RESULT_SET_INDEX = CONNECTION_TASK_ID_RESULT_SET_INDEX + 1;

    private final DeviceDataModelService deviceDataModelService;
    private final EngineConfigurationService engineConfigurationService;
    private final ConnectionTaskService connectionTaskService;
    private final TransactionService transactionService;
    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    public PriorityComTaskServiceImpl(DeviceDataModelService deviceDataModelService, EngineConfigurationService engineConfigurationService,
                                      ConnectionTaskService connectionTaskService, TransactionService transactionService, ThreadPrincipalService threadPrincipalService) {
        this.deviceDataModelService = deviceDataModelService;
        this.engineConfigurationService = engineConfigurationService;
        this.connectionTaskService = connectionTaskService;
        this.transactionService = transactionService;
        this.threadPrincipalService = threadPrincipalService;
    }

    private PriorityComTaskExecutionLink construct(ResultSet resultSet, ScheduledConnectionTask connectionTask) throws SQLException {
        ServerPriorityComTaskExecutionLink highPriorityComTaskExecution = (ServerPriorityComTaskExecutionLink)deviceDataModelService.dataModel().getInstance(PriorityComTaskExecutionLinkImpl.class).init(resultSet, connectionTask);
        highPriorityComTaskExecution.injectConnectionTask(connectionTask);
        return highPriorityComTaskExecution;
    }

    public PriorityComTaskExecutionLink from(ComTaskExecution comTaskExecution) {
        PriorityComTaskExecutionLink priorityComTaskExecutionLink = deviceDataModelService.dataModel().getInstance(PriorityComTaskExecutionLinkImpl.class).init(comTaskExecution);
        deviceDataModelService.dataModel().persist(priorityComTaskExecutionLink);

        return priorityComTaskExecutionLink;
    }

    @Override
    public Optional<PriorityComTaskExecutionLink> find(int id) {
         return deviceDataModelService.dataModel().mapper(PriorityComTaskExecutionLink.class).getUnique(PriorityComTaskExecutionFields.ID.fieldName(), id);
    }

    @Override
    public Optional<PriorityComTaskExecutionLink> findByComTaskExecution(ComTaskExecution comTaskExecution) {
        List<PriorityComTaskExecutionLink> priorityComTaskExecutionLinks = deviceDataModelService.dataModel().mapper(PriorityComTaskExecutionLink.class).find(PriorityComTaskExecutionFields.COMTASKEXECUTION.fieldName(), comTaskExecution);
        if (priorityComTaskExecutionLinks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(priorityComTaskExecutionLinks.get(0));
    }

    @Override
    public List<HighPriorityComJob> findExecutable(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool) {
        return findExecutable(comServer, currentHighPriorityLoadPerComPortPool, Instant.now());
    }

    @Override
    public List<HighPriorityComJob> findExecutable(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool, Instant date) {
        try {
            if (comServer.isActive()) {
                List<MaximumNumberOfTaskForComPortPool> maximumNumberOfTaskForComPortPoolList = getMaximumNumberOfTasksForComPortPools(comServer, currentHighPriorityLoadPerComPortPool);
                if (!maximumNumberOfTaskForComPortPoolList.isEmpty()) {
                    List<OutboundComPort> comPorts = engineConfigurationService.findAllOutboundComPortsByComServer(comServer);
                    LOGGER.info("[high-prio] outbound comports of comserver " + comServer.getName() + ": " + comPorts);
                    try (Connection connection = deviceDataModelService.dataModel().getConnection(false);
                         PreparedStatement preparedStatement = findExecutableSqlBuilder(comServer, maximumNumberOfTaskForComPortPoolList, comPorts, date).prepare(connection);
                         ResultSet resultSet = preparedStatement.executeQuery()) {
                        return new GroupingComJobService(comServer.getName(), maximumNumberOfTaskForComPortPoolList, transactionService, threadPrincipalService).consume(resultSet);
                    }
                }
            }
            return  Collections.emptyList();
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    @Override
    public boolean arePriorityComTasksStillPending(Collection<Long> priorityComTaskExecutionIds) {
        Instant now = deviceDataModelService.clock().instant();
        Condition condition = where(PriorityComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).isLessThanOrEqual(now)
                .and(ListOperator.IN.contains("id", new ArrayList<>(priorityComTaskExecutionIds)))
                .and(where("comPort").isNull());
        return !deviceDataModelService.dataModel().query(PriorityComTaskExecutionLink.class).select(condition).isEmpty();
    }

    @Override
    public PriorityComTaskExecutionLink attemptLockComTaskExecution(PriorityComTaskExecutionLink comTaskExecution, ComPort comPort) {
        Optional<PriorityComTaskExecutionLink> lockResult = deviceDataModelService.dataModel().mapper(PriorityComTaskExecutionLink.class).lockNoWait(comTaskExecution.getId());
        if (lockResult.isPresent()) {
            PriorityComTaskExecutionLink lockedComTaskExecution = lockResult.get();
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

    private ServerPriorityComTaskExecutionLink getServerComTaskExecution(PriorityComTaskExecutionLink lockedComTaskExecution) {
        return (ServerPriorityComTaskExecutionLink) lockedComTaskExecution;
    }

    private List<MaximumNumberOfTaskForComPortPool> getMaximumNumberOfTasksForComPortPools(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool) throws
            SQLException {
        try (Connection connection = deviceDataModelService.dataModel().getConnection(false);
             PreparedStatement preparedStatement = getMaximumNumberOfTasksForComPortPoolsSqlBuilder(comServer).prepare(connection)) {
            List<MaximumNumberOfTaskForComPortPool> maximumNumberOfTaskForComPortPoolList = fetchMaximumNumberOfTasksForComPortPools(preparedStatement, currentHighPriorityLoadPerComPortPool);
            if (LOGGER.isLoggable(Level.FINER)) {
                logMaximumNumberOfTaskForComServerPerComPortPool(maximumNumberOfTaskForComPortPoolList, comServer);
            }
            return maximumNumberOfTaskForComPortPoolList;
        }
    }

    private SqlBuilder getMaximumNumberOfTasksForComPortPoolsSqlBuilder(OutboundCapableComServer comServer) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("with ");
        sqlBuilder.append("  cpCounters as ( ");
        sqlBuilder.append("    select cppm.comPort            as cpid, ");
        sqlBuilder.append("     max(cp.nrOfSimultaneousConns) as nbrOfTasksPerCp, ");
        sqlBuilder.append("     count(*)                      as nbrOfPoolsThePortIsUsedIn ");
        sqlBuilder.append("    from MDC_COMPORTPOOLMEMBER cppm ");
        sqlBuilder.append("    join MDC_COMPORT cp on cppm.comport = cp.id ");
        sqlBuilder.append("    join MDC_COMSERVER cs on cp.comserverId = cs.id ");
        sqlBuilder.append("    where cs.id = ");
        sqlBuilder.addLong(comServer.getId());
        sqlBuilder.append("    and cp.active = 1 and cp.obsolete_date is null ");
        sqlBuilder.append("    group by cppm.comport");
        sqlBuilder.append(") ");
        sqlBuilder.append("select cppm.pool, ceil(sum(cpCounters.nbrOfTasksPerCp / cpCounters.nbrOfPoolsThePortIsUsedIn) * min(cpp.pctHighPrioTasks) / 100) ");
        sqlBuilder.append("from MDC_COMPORTPOOLMEMBER cppm ");
        sqlBuilder.append("join MDC_COMPORT cp on cppm.comport = cp.id     ");
        sqlBuilder.append("join MDC_COMSERVER cs on cp.COMSERVERID = cs.id ");
        sqlBuilder.append("join MDC_COMPORTPOOL cpp on cpp.id = cppm.pool ");
        sqlBuilder.append("join cpCounters on cpCounters.cpid = cp.id ");
        sqlBuilder.append("group by cppm.pool");
        return sqlBuilder;
    }

    private List<MaximumNumberOfTaskForComPortPool> fetchMaximumNumberOfTasksForComPortPools(PreparedStatement statement, Map<Long, Integer> currentHighPriorityLoadPerComPortPool) throws
            SQLException {
        List<MaximumNumberOfTaskForComPortPool> result = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                result.add(constructMaximumNumberOfTaskForComPortPoolFrom(resultSet, currentHighPriorityLoadPerComPortPool));
            }
        }
        return result;
    }

    private MaximumNumberOfTaskForComPortPool constructMaximumNumberOfTaskForComPortPoolFrom(ResultSet resultSet, Map<Long, Integer> currentHighPriorityLoadPerComPortPool) throws SQLException {
        long comPortPoolId = resultSet.getLong(1);
        int numberOfTasks = resultSet.getInt(2);
        if (currentHighPriorityLoadPerComPortPool.containsKey(comPortPoolId)) {
            LOGGER.info("[high-prio] pool " + comPortPoolId + " max nb of high-prio tasks: "
                    + numberOfTasks + ", current load: " + currentHighPriorityLoadPerComPortPool.get(comPortPoolId));
            numberOfTasks = Math.max(0, numberOfTasks - currentHighPriorityLoadPerComPortPool.get(comPortPoolId)); // Subtract the current high priority load from the maximum number
        } else {
            LOGGER.fine("[high-prio] no load info about pool " + comPortPoolId + "!");
        }
        return new MaximumNumberOfTaskForComPortPool(comPortPoolId, numberOfTasks);
    }

    private void logMaximumNumberOfTaskForComServerPerComPortPool(List<MaximumNumberOfTaskForComPortPool> list, OutboundCapableComServer comServer) {
        if (list.isEmpty()) {
            LOGGER.finer(MessageFormat.format("ComServer ''{0}'' is not in any outbound comport pool that allows priority task execution, will skip query to fetch priority tasks", comServer.getName()));
        } else {
            LOGGER.finer(MessageFormat.format("Querying for priority tasks for ComServer ''{0}'' will pick up at most n tasks for outbound comport pool", comServer.getName()));
            for (MaximumNumberOfTaskForComPortPool maximumNumberOfTaskForComPortPool : list) {
                LOGGER.finer("\tcppid\t#max task");
                LOGGER.finer("\t" + maximumNumberOfTaskForComPortPool.getComPortPoolId() + "\t" + maximumNumberOfTaskForComPortPool.getNumberOfTasks());
            }
        }
    }

    private SqlBuilder findExecutableSqlBuilder(OutboundCapableComServer comServer, List<MaximumNumberOfTaskForComPortPool> maximumNumberOfTaskForComPortPoolList,
                                                List<OutboundComPort> comPorts,
                                                Instant now) {
        String alias = "hpcte";
        DataMapper<PriorityComTaskExecutionLink> mapper = deviceDataModelService.dataModel().mapper(PriorityComTaskExecutionLink.class);
        SqlBuilder sqlBuilder = mapper.builderWithAdditionalColumns(alias, "ct.id", "ct." + ConnectionTaskFields.COM_PORT_POOL.fieldName());
        sqlBuilder.append(" join ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" cte on ");
        sqlBuilder.append(alias);
        sqlBuilder.append(".");
        sqlBuilder.append(PriorityComTaskExecutionFields.COMTASKEXECUTION.fieldName());
        sqlBuilder.append(" = cte.id join ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct on cte.");
        sqlBuilder.append(ComTaskExecutionFields.CONNECTIONTASK.fieldName());
        sqlBuilder.append(" = ct.id");
        sqlBuilder.append("  left join ");
        sqlBuilder.append("MDC_COMPORT cp on cp.id"); // can't refer to MDC_COMPORT through TableSpecs, it's in mdc.engine
        sqlBuilder.append(" = cte.");
        sqlBuilder.append(ComTaskExecutionFields.COMPORT.fieldName());
        sqlBuilder.append(" where ");
        sqlBuilder.append(" ct.");
        sqlBuilder.append(ConnectionTaskFields.STATUS.fieldName());
        sqlBuilder.append(" = " + ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE.ordinal());
        sqlBuilder.append(" and (ct.");
        sqlBuilder.append(ConnectionTaskFields.COM_PORT.fieldName());
        sqlBuilder.append(" is null");
        sqlBuilder.append(" or ct.");
        sqlBuilder.append(ConnectionTaskFields.COM_PORT.fieldName());
        sqlBuilder.append(" in (select id from MDC_COMPORT where comserverid = ");
        sqlBuilder.addLong(comServer.getId());
        sqlBuilder.append(")) ");
        sqlBuilder.append("   and ct.obsolete_date is null");
        sqlBuilder.append("   and cte.obsolete_date is null");
        sqlBuilder.append("   and ");
        sqlBuilder.append(alias);
        sqlBuilder.append(".");
        sqlBuilder.append(PriorityComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName());
        sqlBuilder.append(" <= ");
        sqlBuilder.addLong(now.toEpochMilli());
        sqlBuilder.append("   and (cte.");
        sqlBuilder.append(ComTaskExecutionFields.COMPORT.fieldName());
        sqlBuilder.append(" is null");
        if (!comPorts.isEmpty()) {
            sqlBuilder.append(" or cte.");
            sqlBuilder.append(ComTaskExecutionFields.COMPORT.fieldName());
            sqlBuilder.addInClauseForIdList(comPorts);
        }
        sqlBuilder.append(") and ");
        sqlBuilder.append(alias);
        sqlBuilder.append(".");
        sqlBuilder.append(PriorityComTaskExecutionFields.COMPORT.fieldName());
        sqlBuilder.append(" is null");
        if (!maximumNumberOfTaskForComPortPoolList.isEmpty()) {
            sqlBuilder.append("   and ct.");
            sqlBuilder.append(ConnectionTaskFields.COM_PORT_POOL.fieldName());
            sqlBuilder.addInClauseForIdList(maximumNumberOfTaskForComPortPoolList);
        }
        sqlBuilder.append(" order by ");
        sqlBuilder.append("cte.");  // It is important to first order by the connection task, cause all high priority tasks for the same connection should be picked up together
        sqlBuilder.append(ComTaskExecutionFields.CONNECTIONTASK.fieldName());
        sqlBuilder.append(", ");
        sqlBuilder.append(alias);
        sqlBuilder.append(".");
        sqlBuilder.append(PriorityComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName());
        sqlBuilder.append(", ");
        sqlBuilder.append(alias);
        sqlBuilder.append(".");
        sqlBuilder.append(PriorityComTaskExecutionFields.PRIORITY.fieldName());
        return sqlBuilder;
    }

    private interface ComJobService {

        /**
         * Constructs the {@link PriorityComTaskExecutionLink}s from the ResultSet
         * and wraps them in appropriate {@link HighPriorityComJob}s.
         *
         * @param resultSet The ResultSet
         * @return The List of HighPriorityComJobs
         * @throws SQLException Indicates coding problem
         */
        List<HighPriorityComJob> consume(ResultSet resultSet) throws SQLException;
    }

    private class GroupingComJobService implements ComJobService {

        private final String comServer;
        private final TransactionService transactionService;
        private final ThreadPrincipalService threadPrincipalService;
        private final Map<Long, Integer> remainingNumberOfTasksPerPool;
        private List<HighPriorityComJob> jobs = new ArrayList<>();
        private final Map<Integer, ScheduledConnectionTask> connectionTaskCache = new HashMap<>();
        private Map<OutboundConnectionTask, HighPriorityComTaskExecutionGroup> groups = new HashMap<>();
        private int currentConnectionTaskId = 0;

        private GroupingComJobService(String comServer, List<MaximumNumberOfTaskForComPortPool> maximumNumberOfTaskForComPortPoolList,
                                      TransactionService transactionService, ThreadPrincipalService threadPrincipalService) {
            this.comServer = comServer;
            this.transactionService = transactionService;
            this.threadPrincipalService = threadPrincipalService;
            remainingNumberOfTasksPerPool = new HashMap<>();
            for (MaximumNumberOfTaskForComPortPool maximumNumberOfTaskForComPortPool : maximumNumberOfTaskForComPortPoolList) {
                remainingNumberOfTasksPerPool.put(
                        maximumNumberOfTaskForComPortPool.getComPortPoolId(),
                        maximumNumberOfTaskForComPortPool.getNumberOfTasks());
            }
        }

        public int totalRemainingJobs() {
            return remainingNumberOfTasksPerPool.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
        }

        protected int numberOfJobs() {
            return jobs.size();
        }

        @Override
        public List<HighPriorityComJob> consume(ResultSet resultSet) throws SQLException {
            int totalRemainingJobs = totalRemainingJobs();
            while (continueFetching(resultSet)) {
                add(resultSet);
            }

            if (!jobs.isEmpty() && totalRemainingJobs == 0) {
                throw new NoMoreHighPriorityTasksCanBePickedUpRuntimeException();
            }

            return jobs;
        }

        private boolean continueFetching(ResultSet resultSet) throws SQLException {
            int i = 0;
            if (moreResultsAvailable(resultSet)) {
                int connectionTaskId = resultSet.getInt(CONNECTION_TASK_ID_RESULT_SET_INDEX);
                if (currentConnectionTaskId == 0) {
                    // First HighPriorityComTaskExecution
                    currentConnectionTaskId = connectionTaskId;
                    return true;
                } else {
                    /* Continue fetching as long as we are dealing with the
                     * same ConnectionTask and if we are switching to another
                     * ConnectionTask then we only continue fetching
                     * if we need more jobs. */
                    try {
                        boolean needMoreJobs = needMoreJobs();
                        boolean result = currentConnectionTaskId == connectionTaskId || needMoreJobs;

                        return result;
                    } finally {
                        currentConnectionTaskId = connectionTaskId;
                    }
                }
            } else {
                return false;
            }
        }

        private boolean moreResultsAvailable(ResultSet resultSet) throws SQLException {
            return resultSet.next();
        }

        private boolean needMoreJobs() {
            return totalRemainingJobs() > 0;
        }

        private void add(ResultSet resultSet) throws SQLException {
            long comPortPoolId = resultSet.getLong(CONNECTION_TASK_COMPORT_POOL_RESULT_SET_INDEX);
            ScheduledConnectionTask connectionTask = findConnectionTask(resultSet.getInt(CONNECTION_TASK_ID_RESULT_SET_INDEX));
            addToGroup((ServerPriorityComTaskExecutionLink) construct(resultSet, connectionTask), connectionTask, comPortPoolId);
        }

        protected void addToGroup(ServerPriorityComTaskExecutionLink highPriorityComTaskExecution, ScheduledConnectionTask connectionTask, Long comPortPoolId) {
            HighPriorityComTaskExecutionGroup group = groups.get(connectionTask);
            if (group == null) {
                if (needMoreJobsForPool(comPortPoolId)) {
                    group = new HighPriorityComTaskExecutionGroup(connectionTask);
                    groups.put(connectionTask, group);
                    addHighPriorityComJob(group, comPortPoolId);
                } else {
                    if (connectionTask.getComPortPool().getPctHighPrioTasks() == 0) {
                        LOGGER.warning(MessageFormat.format(
                                "Failing HighPriorityComTaskExecution (id={0}) for com server ''{1}'' because comport pool ''{2}'' can''t execute high priority tasks.",
                                highPriorityComTaskExecution.getId(), comServer, connectionTask.getComPortPool().getName()));
                        try {
                            threadPrincipalService.set(() -> "batch executor");
                            try (TransactionContext context = transactionService.getContext()) {
                                ((ServerComTaskExecution) highPriorityComTaskExecution.getComTaskExecution()).executionFailed();
                                context.commit();
                            }
                        } finally {
                            threadPrincipalService.clear();
                        }
                    } else {
                        logIgnoringTask(highPriorityComTaskExecution.getId(), comPortPoolId);
                    }
                    return;
                }
            }
            group.add(highPriorityComTaskExecution);
        }

        private boolean needMoreJobsForPool(long comPortPoolId) {
            Integer remainingNumberOfTaskForPool = remainingNumberOfTasksPerPool.get(comPortPoolId);
            return remainingNumberOfTaskForPool != null && remainingNumberOfTaskForPool > 0;
        }

        private void addHighPriorityComJob(HighPriorityComJob job, Long comPortPoolId) {
            jobs.add(job);
            Integer numberOfTasks = remainingNumberOfTasksPerPool.get(comPortPoolId);
            remainingNumberOfTasksPerPool.put(comPortPoolId, numberOfTasks - 1);
        }

        private void logIgnoringTask(long taskId, long comPortPoolId) {
            LOGGER.info(
                    MessageFormat.format(
                            "Ignoring HighPriorityComTaskExecution (id={0}) for com server ''{1}'' because we already have enough tasks for comport pool {2}",
                            taskId, comServer, comPortPoolId));
        }

        private ScheduledConnectionTask findConnectionTask(int id) {
            ScheduledConnectionTask connectionTask = connectionTaskCache.get(id);
            if (connectionTask == null) {
                connectionTask = connectionTaskService.findScheduledConnectionTask(id).get();
                connectionTaskCache.put(id, connectionTask);
            }
            return connectionTask;
        }
    }

    private class MaximumNumberOfTaskForComPortPool implements HasId {

        private final long comPortPoolId;
        private final int numberOfTasks;

        private MaximumNumberOfTaskForComPortPool(long comPortPoolId, int numberOfTasks) {
            super();
            this.comPortPoolId = comPortPoolId;
            this.numberOfTasks = numberOfTasks;
        }

        private long getComPortPoolId() {
            return comPortPoolId;
        }

        private int getNumberOfTasks() {
            return numberOfTasks;
        }

        @Override
        public long getId() {
            return comPortPoolId;
        }
    }
}
