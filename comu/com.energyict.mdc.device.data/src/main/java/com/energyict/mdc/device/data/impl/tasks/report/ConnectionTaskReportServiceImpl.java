/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.DeviceStateSqlBuilder;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskStatus;
import com.energyict.mdc.device.data.tasks.ConnectionTaskBreakdowns;
import com.energyict.mdc.device.data.tasks.ConnectionTaskReportService;

import org.joda.time.DateTimeConstants;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link ConnectionTaskReportService} interface.
 * Implementation note: no need for @Component annotation as this
 * component is dynamically registered as part of the activation of the
 * {@link com.energyict.mdc.device.data.impl.DeviceDataModelServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (09:01)
 */
@LiteralSql
public class ConnectionTaskReportServiceImpl implements ConnectionTaskReportService {
    private static final Logger LOGGER = Logger.getLogger(ConnectionTaskReportServiceImpl.class.getName());// just for time measurement

    private final DeviceDataModelService deviceDataModelService;
    private final MeteringService meteringService;

    @Inject
    public ConnectionTaskReportServiceImpl(DeviceDataModelService deviceDataModelService, MeteringService meteringService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
        this.meteringService = meteringService;
    }

    private long toSeconds(Instant time) {
        return time.toEpochMilli() / DateTimeConstants.MILLIS_PER_SECOND;
    }

    @Override
    public Map<TaskStatus, Long> getConnectionTaskStatusCount() {
        return this.doGetConnectionTaskStatusCount(null);
    }

    @Override
    public Map<TaskStatus, Long> getConnectionTaskStatusCount(EndDeviceGroup deviceGroup) {
        return this.doGetConnectionTaskStatusCount(deviceGroup);
    }

    private Map<TaskStatus, Long> doGetConnectionTaskStatusCount(EndDeviceGroup deviceGroup) {
        ConnectionTaskStatusCountSqlBuilder sqlBuilder =
                new ConnectionTaskStatusCountSqlBuilder(
                        this.taskStatusesForCounting(TaskStatus.withoutPrio()),
                        deviceGroup,
                        this);
        return this.addMissingTaskStatusCounters(this.deviceDataModelService.fetchTaskStatusCounters(sqlBuilder));
    }

    private Set<ServerConnectionTaskStatus> taskStatusesForCounting(Set<TaskStatus> taskStatuses) {
        return EnumSet.copyOf(
                taskStatuses
                        .stream()
                        .map(ServerConnectionTaskStatus::forTaskStatus)
                        .collect(Collectors.toList()));
    }

    @Override
    public Map<ComPortPool, Map<TaskStatus, Long>> getComPortPoolBreakdown(Set<TaskStatus> taskStatuses) {
        return this.doGetComPortPoolBreakdown(taskStatuses, null);
    }

    @Override
    public Map<ComPortPool, Map<TaskStatus, Long>> getComPortPoolBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup) {
        return this.doGetComPortPoolBreakdown(taskStatuses, deviceGroup);
    }

    private Map<ComPortPool, Map<TaskStatus, Long>> doGetComPortPoolBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup) {
        ConnectionTaskBreakdownSqlBuilder sqlBuilder =
                new ConnectionTaskComPortPoolBreakdownSqlBuilder(
                        this.taskStatusesForCounting(taskStatuses),
                        deviceGroup,
                        this);
        return this.injectComPortPoolsAndAddMissing(this.deviceDataModelService.fetchTaskStatusBreakdown(sqlBuilder));
    }

    private Map<ComPortPool, Map<TaskStatus, Long>> injectComPortPoolsAndAddMissing(Map<Long, Map<TaskStatus, Long>> statusBreakdown) {
        Map<Long, ComPortPool> comPortPools =
                this.deviceDataModelService.engineConfigurationService()
                    .findAllComPortPools()
                    .stream()
                    .collect(Collectors.toMap(ComPortPool::getId, Function.identity()));
        return this.injectBreakDownsAndAddMissing(statusBreakdown, comPortPools);
    }

    @Override
    public Map<DeviceType, Map<TaskStatus, Long>> getDeviceTypeBreakdown(Set<TaskStatus> taskStatuses) {
        return this.doGetDeviceTypeBreakdown(taskStatuses, null);
    }

    @Override
    public Map<DeviceType, Map<TaskStatus, Long>> getDeviceTypeBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup) {
        return this.doGetDeviceTypeBreakdown(taskStatuses, deviceGroup);
    }

    private Map<DeviceType, Map<TaskStatus, Long>> doGetDeviceTypeBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup) {
        ConnectionTaskBreakdownSqlBuilder sqlBuilder =
                new ConnectionTaskDeviceTypeBreakdownSqlBuilder(
                        this.taskStatusesForCounting(taskStatuses),
                        deviceGroup,
                        this);
        return this.injectDeviceTypesAndAddMissing(this.deviceDataModelService.fetchTaskStatusBreakdown(sqlBuilder));
    }

    private Map<DeviceType, Map<TaskStatus, Long>> injectDeviceTypesAndAddMissing(Map<Long, Map<TaskStatus, Long>> statusBreakdown) {
        Map<Long, DeviceType> deviceTypes = this.deviceDataModelService.deviceConfigurationService().findAllDeviceTypes().stream().collect(Collectors.toMap(DeviceType::getId, Function.identity()));
        return this.injectBreakDownsAndAddMissing(statusBreakdown, deviceTypes);
    }

    @Override
    public Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> getConnectionTypeBreakdown(Set<TaskStatus> taskStatuses) {
        return this.doGetConnectionTypeBreakdown(taskStatuses, null);
    }

    @Override
    public Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> getConnectionTypeBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup) {
        return this.doGetConnectionTypeBreakdown(taskStatuses, deviceGroup);
    }

    private Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> doGetConnectionTypeBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup) {
        ConnectionTaskTypeBreakdownSqlBuilder sqlBuilder =
                new ConnectionTaskTypeBreakdownSqlBuilder(
                        this.taskStatusesForCounting(taskStatuses),
                        deviceGroup,
                        this);
        return this.injectConnectionTypesAndAddMissing(this.deviceDataModelService.fetchTaskStatusBreakdown(sqlBuilder));
    }

    private <BDT> Map<BDT, Map<TaskStatus, Long>> injectBreakDownsAndAddMissing(Map<Long, Map<TaskStatus, Long>> breakdown, Map<Long, BDT> allBreakDowns) {
        Map<BDT, Map<TaskStatus, Long>> breakDownByDeviceType = this.emptyBreakdown(allBreakDowns);
        for (Long breakDownId : breakdown.keySet()) {
            breakDownByDeviceType.put(allBreakDowns.get(breakDownId), breakdown.get(breakDownId));
        }
        return breakDownByDeviceType;
    }

    private <BDT> Map<BDT, Map<TaskStatus, Long>> emptyBreakdown(Map<Long, BDT> breakDowns) {
        Map<BDT, Map<TaskStatus, Long>> emptyBreakdown = new HashMap<>();
        for (BDT breakDown : breakDowns.values()) {
            Map<TaskStatus, Long> emptyCounters = new HashMap<>();
            this.addMissingTaskStatusCounters(emptyCounters);
            emptyBreakdown.put(breakDown, emptyCounters);
        }
        return emptyBreakdown;
    }

    private Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> injectConnectionTypesAndAddMissing(Map<Long, Map<TaskStatus, Long>> statusBreakdown) {
        Map<Long, ConnectionTypePluggableClass> connectionTypePluggableClasses = this.deviceDataModelService.protocolPluggableService().findAllConnectionTypePluggableClasses().stream().collect(Collectors
                .toMap(ConnectionTypePluggableClass::getId, Function.identity()));
        return this.injectBreakDownsAndAddMissing(statusBreakdown, connectionTypePluggableClasses);
    }

    private Map<TaskStatus, Long> addMissingTaskStatusCounters(Map<TaskStatus, Long> counters) {
        return this.deviceDataModelService.addMissingTaskStatusCounters(counters);
    }

    @Override
    public long countConnectionTasksLastComSessionsWithAtLeastOneFailedTask() {
        return this.countConnectionTasksLastComSessionsWithAtLeastOneFailedTask(false, null);
    }

    @Override
    public long countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTask() {
        return this.countConnectionTasksLastComSessionsWithAtLeastOneFailedTask(true, null);
    }

    @Override
    public long countConnectionTasksLastComSessionsWithAtLeastOneFailedTask(EndDeviceGroup deviceGroup) {
        return this.countConnectionTasksLastComSessionsWithAtLeastOneFailedTask(false, deviceGroup);
    }

    @Override
    public long countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTask(EndDeviceGroup deviceGroup) {
        return this.countConnectionTasksLastComSessionsWithAtLeastOneFailedTask(true, deviceGroup);
    }

    private long countConnectionTasksLastComSessionsWithAtLeastOneFailedTask(boolean waitingOnly, EndDeviceGroup deviceGroup) {
        SqlBuilder sqlBuilder = new SqlBuilder("SELECT NVL(SUM(\"COUNT\"), 0) ");
        sqlBuilder.append("FROM DASHBOARD_CTLCSWITHATLSTONEFT ");
        sqlBuilder.append(" where 1=1 ");
        if(deviceGroup != null) {
            sqlBuilder.append(" AND MRID = '");
            sqlBuilder.append(deviceGroup.getMRID());
            sqlBuilder.append("'");
        }
        try (Connection connection = this.deviceDataModelService.dataModel().getConnection(true);
             PreparedStatement stmnt = sqlBuilder.prepare(connection)) {
            try (ResultSet resultSet = stmnt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        return 0;
    }

    private void appendConnectionTypeHeatMapComTaskExecutionSessionConditions(boolean atLeastOneFailingComTask, SqlBuilder sqlBuilder) {
        if (atLeastOneFailingComTask) {
            sqlBuilder.append(" and");
        } else {
            sqlBuilder.append(" and not");
        }
        sqlBuilder.append(" exists (select * from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXECSESSION.name());
        sqlBuilder.append(" ctes where ctes.COMSESSION = ct.lastSession and ctes.SUCCESSINDICATOR > 0)");
    }

    @Override
    public Map<ComSession.SuccessIndicator, Long> getConnectionTaskLastComSessionSuccessIndicatorCount() {
        SqlBuilder sqlBuilder = new SqlBuilder("SELECT LASTSESSIONSUCCESSINDICATOR, SUM(\"COUNT\") ");
        sqlBuilder.append("FROM DASHBOARD_CTLCSSUCINDCOUNT ");
        sqlBuilder.append("GROUP BY LASTSESSIONSUCCESSINDICATOR");
        return this.addMissingSuccessIndicatorCounters(this.fetchSuccessIndicatorCounters(sqlBuilder));
    }

    @Override
    public Map<ComSession.SuccessIndicator, Long> getConnectionTaskLastComSessionSuccessIndicatorCount(EndDeviceGroup deviceGroup) {
        SqlBuilder sqlBuilder = new SqlBuilder("SELECT LASTSESSIONSUCCESSINDICATOR, SUM(\"COUNT\") ");
        sqlBuilder.append("FROM DASHBOARD_CTLCSSUCINDCOUNT ");
        sqlBuilder.append("WHERE 1=1 ");
        if(deviceGroup != null) {
            sqlBuilder.append(" AND MRID = '");
            sqlBuilder.append(deviceGroup.getMRID());
            sqlBuilder.append("'");
        }
        sqlBuilder.append(" GROUP BY LASTSESSIONSUCCESSINDICATOR");
        return this.addMissingSuccessIndicatorCounters(this.fetchSuccessIndicatorCounters(sqlBuilder));
    }

    private Map<ComSession.SuccessIndicator, Long> fetchSuccessIndicatorCounters(SqlBuilder builder) {
        Map<ComSession.SuccessIndicator, Long> counters = new HashMap<>();
        try (Connection connection = this.deviceDataModelService.dataModel().getConnection(true);
             PreparedStatement stmnt = builder.prepare(connection)) {
            this.fetchSuccessIndicatorCounters(stmnt, counters);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        return counters;
    }

    private void fetchSuccessIndicatorCounters(PreparedStatement statement, Map<ComSession.SuccessIndicator, Long> counters) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int successIndicatorOrdinal = resultSet.getInt(1);
                long counter = resultSet.getLong(2);
                counters.put(ComSession.SuccessIndicator.fromOrdinal(successIndicatorOrdinal), counter);
            }
        }
    }

    private Map<ComSession.SuccessIndicator, Long> addMissingSuccessIndicatorCounters(Map<ComSession.SuccessIndicator, Long> counters) {
        for (ComSession.SuccessIndicator missing : this.successIndicatorComplement(counters.keySet())) {
            counters.put(missing, 0L);
        }
        return counters;
    }

    private EnumSet<ComSession.SuccessIndicator> successIndicatorComplement(Set<ComSession.SuccessIndicator> successIndicators) {
        if (successIndicators.isEmpty()) {
            return EnumSet.allOf(ComSession.SuccessIndicator.class);
        } else {
            return EnumSet.complementOf(EnumSet.copyOf(successIndicators));
        }
    }

    @Override
    public Map<ConnectionTypePluggableClass, List<Long>> getConnectionTypeHeatMap() {
        return this.doGetConnectionTypeHeatMap(null);
    }

    @Override
    public Map<ConnectionTypePluggableClass, List<Long>> getConnectionTypeHeatMap(EndDeviceGroup deviceGroup) {
        return this.doGetConnectionTypeHeatMap(deviceGroup);
    }

    private Map<ConnectionTypePluggableClass, List<Long>> doGetConnectionTypeHeatMap(EndDeviceGroup deviceGroup) {
        return this.fetchConnectionTypeHeatMapCounters(
                this.connectionTypeHeatMapSqlBuilder(
                        deviceGroup,
                        this.connectionTypeHeapMapFailureIndicators(),
                        "connectiontypepluggableclass"));
    }

    private Map<ConnectionTypePluggableClass, List<Long>> fetchConnectionTypeHeatMapCounters(SqlBuilder sqlBuilder) {
        try (Connection connection = this.deviceDataModelService.dataModel().getConnection(true);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            return this.fetchConnectionTypeHeatMapCounters(statement);
        }
        catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private Map<ConnectionTypePluggableClass, List<Long>> fetchConnectionTypeHeatMapCounters(PreparedStatement statement) throws SQLException {
        Map<Long, ConnectionTypePluggableClass> connectionTypePluggableClasses =
                this.deviceDataModelService.protocolPluggableService().findAllConnectionTypePluggableClasses().
                        stream().
                        collect(Collectors.toMap(ConnectionTypePluggableClass::getId, Function.identity()));
        Map<ConnectionTypePluggableClass, List<Long>> heatMap =
                connectionTypePluggableClasses.values().stream().collect(
                        Collectors.toMap(
                                Function.identity(),
                                this::missingSuccessIndicatorCounters));
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                long connectionTypePluggableClassId = resultSet.getLong(1);
                heatMap.put(connectionTypePluggableClasses.get(connectionTypePluggableClassId), this.toConnectionTypeHeatMapCounters(resultSet));
            }
        }
        return heatMap;
    }

    private List<Long> toConnectionTypeHeatMapCounters(ResultSet resultSet) throws SQLException {
        int columnIndex = 2;    // id of pluggable class is at 1 and we have already read that
        long completeSuccess = resultSet.getLong(columnIndex);
        columnIndex++;
        long atLeastOneFailure = resultSet.getLong(columnIndex);
        columnIndex++;
        long failureSetupError = resultSet.getLong(columnIndex + this.connectionTypeHeapMapFailureIndicators().indexOf(ComSession.SuccessIndicator.SetupError));
        long failureBroken = resultSet.getLong(columnIndex + this.connectionTypeHeapMapFailureIndicators().indexOf(ComSession.SuccessIndicator.Broken));
        long failureInterrupted = resultSet.getLong(columnIndex + this.connectionTypeHeapMapFailureIndicators().indexOf(ComSession.SuccessIndicator.Interrupted));
        long failureNotExecuted = resultSet.getLong(columnIndex + this.connectionTypeHeapMapFailureIndicators().indexOf(ComSession.SuccessIndicator.Not_Executed));
        return Arrays.asList(atLeastOneFailure, completeSuccess, failureSetupError, failureBroken, failureInterrupted, failureNotExecuted);
    }

    private List<Long> missingSuccessIndicatorCounters(ConnectionTypePluggableClass connectionTypePluggableClass) {
        return this.orderSuccessIndicatorCounters(null, null);
    }

    private List<Long> missingSuccessIndicatorCounters(ComPortPool comPortPool) {
        return this.orderSuccessIndicatorCounters(null, null);
    }

    private List<Long> orderSuccessIndicatorCounters(Map<ComSession.SuccessIndicator, Long> successIndicatorCounters, Map<ComSession.SuccessIndicator, Long> failingTaskCounters) {
        List<Long> counters = new ArrayList<>(ComSession.SuccessIndicator.values().length + 1);
        addSuccessIndicatorCounter(counters, failingTaskCounters, ComSession.SuccessIndicator.Success);
        addSuccessIndicatorCounter(counters, successIndicatorCounters, ComSession.SuccessIndicator.Success);
        addSuccessIndicatorCounter(counters, successIndicatorCounters, ComSession.SuccessIndicator.SetupError);
        addSuccessIndicatorCounter(counters, successIndicatorCounters, ComSession.SuccessIndicator.Broken);
        addSuccessIndicatorCounter(counters, successIndicatorCounters, ComSession.SuccessIndicator.Interrupted);
        addSuccessIndicatorCounter(counters, successIndicatorCounters, ComSession.SuccessIndicator.Not_Executed);
        return counters;
    }

    private void addSuccessIndicatorCounter(List<Long> targetCounters, Map<ComSession.SuccessIndicator, Long> sourceCounters, ComSession.SuccessIndicator successIndicator) {
        if (sourceCounters != null) {
            targetCounters.add(sourceCounters.get(successIndicator));
        } else {
            targetCounters.add(0L);
        }
    }

    @Override
    public Map<DeviceType, List<Long>> getConnectionsDeviceTypeHeatMap() {
        return this.doGetConnectionsDeviceTypeHeatMap(null);
    }

    @Override
    public Map<DeviceType, List<Long>> getConnectionsDeviceTypeHeatMap(EndDeviceGroup deviceGroup) {
        return this.doGetConnectionsDeviceTypeHeatMap(deviceGroup);
    }

    private Map<DeviceType, List<Long>> doGetConnectionsDeviceTypeHeatMap(EndDeviceGroup deviceGroup) {
        return this.fetchDeviceTypeHeatMapCounters(
                this.connectionTypeHeatMapSqlBuilder(
                        deviceGroup,
                        this.connectionTypeHeapMapFailureIndicators(),
                        "ct", "devicetype"
                ));
    }

    private Map<DeviceType, List<Long>> fetchDeviceTypeHeatMapCounters(SqlBuilder sqlBuilder) {
        try (Connection connection = this.deviceDataModelService.dataModel().getConnection(true);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            return this.fetchDeviceTypeHeatMapCounters(statement);
        }
        catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private Map<DeviceType, List<Long>> fetchDeviceTypeHeatMapCounters(PreparedStatement statement) throws SQLException {
        Map<Long, DeviceType> deviceTypes =
                this.deviceDataModelService
                        .deviceConfigurationService()
                        .findAllDeviceTypes()
                        .find()
                        .stream()
                        .collect(Collectors.toMap(
                                DeviceType::getId,
                                Function.identity()));
        Map<DeviceType, List<Long>> heatMap =
                deviceTypes.values().stream().collect(
                        Collectors.toMap(
                                Function.identity(),
                                this::missingCompletionCodeCounters));
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                long deviceTypeId = resultSet.getLong(1);
                heatMap.put(deviceTypes.get(deviceTypeId), this.toConnectionTypeHeatMapCounters(resultSet));
            }
        }
        return heatMap;
    }

    private List<Long> missingCompletionCodeCounters(DeviceType deviceType) {
        return Stream.of(CompletionCode.values()).map(code -> 0L).collect(Collectors.toList());
    }

    @Override
    public Map<ComPortPool, List<Long>> getConnectionsComPortPoolHeatMap() {
        return this.doGetConnectionsComPortPoolHeatMap(null);
    }

    @Override
    public Map<ComPortPool, List<Long>> getConnectionsComPortPoolHeatMap(EndDeviceGroup deviceGroup) {
        return this.doGetConnectionsComPortPoolHeatMap(deviceGroup);
    }

    private Map<ComPortPool, List<Long>> doGetConnectionsComPortPoolHeatMap(EndDeviceGroup deviceGroup) {
        return this.fetchComPortPoolHeatMapCounters(
                this.connectionTypeHeatMapSqlBuilder(
                        deviceGroup,
                        this.connectionTypeHeapMapFailureIndicators(),
                        "comportpool"));
    }

    private Map<ComPortPool, List<Long>> fetchComPortPoolHeatMapCounters(SqlBuilder sqlBuilder) {
        try (Connection connection = this.deviceDataModelService.dataModel().getConnection(true);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            return this.fetchComPortPoolHeatMapCounters(statement);
        }
        catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private Map<ComPortPool, List<Long>> fetchComPortPoolHeatMapCounters(PreparedStatement statement) throws SQLException {
        Map<Long, ComPortPool> comPortPools =
                this.deviceDataModelService.engineConfigurationService().findAllComPortPools().
                        stream().
                        collect(Collectors.toMap(
                                ComPortPool::getId,
                                Function.identity()));
        Map<ComPortPool, List<Long>> heatMap =
                comPortPools.values().stream().collect(Collectors.toMap(
                        Function.identity(),
                        this::missingSuccessIndicatorCounters));
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                long comPortPoolId = resultSet.getLong(1);
                heatMap.put(comPortPools.get(comPortPoolId), this.toConnectionTypeHeatMapCounters(resultSet));
            }
        }
        return heatMap;
    }

    private Optional<AmrSystem> getMdcAmrSystem() {
        return this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
    }

    private SqlBuilder connectionTypeHeatMapSqlBuilder(
            final EndDeviceGroup deviceGroup,
            final List<ComSession.SuccessIndicator> failureIndicators,
            final String groupByFieldName) {
        return this.connectionTypeHeatMapSqlBuilder(deviceGroup, failureIndicators, "ct", groupByFieldName);
    }

    private SqlBuilder connectionTypeHeatMapSqlBuilder(
            final EndDeviceGroup deviceGroup,
            final List<ComSession.SuccessIndicator> failureIndicators,
            final String groupByEntityAliasName,
            final String groupByFieldName) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("select ");
        sqlBuilder.append(groupByFieldName);
        sqlBuilder.append(", NVL(SUM(COMPLETESUCCES),0) ");
        sqlBuilder.append(", NVL(SUM(ATLEASTONEFAILURE),0) ");
        sqlBuilder.append(", NVL(SUM(FAILURESETUPERROR),0) ");
        sqlBuilder.append(", NVL(SUM(FAILUREBROKEN),0) ");
        sqlBuilder.append(", NVL(SUM(FAILUREINTERRUPTED),0) ");
        sqlBuilder.append(", NVL(SUM(FAILURENOT_EXECUTE),0) ");
        sqlBuilder.append("FROM DASHBOARD_CONTYPEHEATMAP ");
        sqlBuilder.append(" where 1=1 ");
        if(deviceGroup != null){
            sqlBuilder.append(" AND MRID ='");
            sqlBuilder.append(deviceGroup.getMRID());
            sqlBuilder.append("' ");
        }

        sqlBuilder.append(" group by " + groupByFieldName);
        return sqlBuilder;
    }

    private String connectionTypeHeatMapFailureIndicatorCaseClause(ComSession.SuccessIndicator indicator) {
        return "CASE WHEN ct.lastSessionSuccessIndicator = " + indicator.ordinal() + " THEN 1 ELSE 0 END " + this.connectionTypeHeatMapFailureIndicatorCaseClauseNameFor(indicator);
    }

    private String connectionTypeHeatMapFailureIndicatorCaseClauseNameFor(ComSession.SuccessIndicator indicator) {
        return "failure" + indicator.name();
    }

    private List<ComSession.SuccessIndicator> connectionTypeHeapMapFailureIndicators() {
        return Arrays.asList(ComSession.SuccessIndicator.SetupError,
                ComSession.SuccessIndicator.Broken,
                ComSession.SuccessIndicator.Interrupted,
                ComSession.SuccessIndicator.Not_Executed
        );
    }

    Clock clock() {
        return this.deviceDataModelService.clock();
    }

    void appendDeviceGroupConditions(EndDeviceGroup deviceGroup, SqlBuilder sqlBuilder, String connectionTaskAliasName) {
        if (deviceGroup != null) {
            sqlBuilder.append(" and ");
            sqlBuilder.append(connectionTaskAliasName);
            sqlBuilder.append(".device in (");
            if (deviceGroup instanceof QueryEndDeviceGroup) {
                sqlBuilder.add(((QueryEndDeviceGroup) deviceGroup).toFragment());
            }
            else {
                sqlBuilder.add(((EnumeratedEndDeviceGroup) deviceGroup).getAmrIdSubQuery(getMdcAmrSystem().get()).toFragment());
            }
            sqlBuilder.append(")");
        }
    }

    @Override
    public ConnectionTaskBreakdowns getConnectionTaskBreakdowns() {
        return this.getConnectionTaskBreakdowns(() -> ConnectionTaskBreakdownSqlExecutor.systemWide(this.deviceDataModelService.dataModel()));
    }

    @Override
    public ConnectionTaskBreakdowns getConnectionTaskBreakdowns(EndDeviceGroup deviceGroup) {
        return this.getConnectionTaskBreakdowns(() -> ConnectionTaskBreakdownSqlExecutor.forGroup(deviceGroup, this.getMdcAmrSystem().get(), this.deviceDataModelService.dataModel()));
    }

    private ConnectionTaskBreakdowns getConnectionTaskBreakdowns(Supplier<ConnectionTaskBreakdownSqlExecutor> sqlExecutorSupplier) {
        ConnectionTaskBreakdownsImpl breakdowns = this.deviceDataModelService.dataModel().getInstance(ConnectionTaskBreakdownsImpl.class);
        sqlExecutorSupplier.get().breakdowns().forEach(breakdowns::add);
        return breakdowns;
    }

}