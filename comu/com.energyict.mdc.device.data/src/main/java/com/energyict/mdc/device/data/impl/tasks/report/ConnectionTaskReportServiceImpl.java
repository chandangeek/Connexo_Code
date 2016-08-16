package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.DeviceStateSqlBuilder;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskStatus;
import com.energyict.mdc.device.data.tasks.ConnectionTaskBreakdowns;
import com.energyict.mdc.device.data.tasks.ConnectionTaskReportService;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

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
                        this.taskStatusesForCounting(EnumSet.allOf(TaskStatus.class)),
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
        return this.countConnectionTasksLastComSessionsWithAtLeastOneFailedTask(false);
    }

    @Override
    public long countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTask() {
        return this.countConnectionTasksLastComSessionsWithAtLeastOneFailedTask(true);
    }

    private long countConnectionTasksLastComSessionsWithAtLeastOneFailedTask(boolean waitingOnly) {
        SqlBuilder sqlBuilder = new SqlBuilder("select count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct where ct.obsolete_date is null");
        if (waitingOnly) {
            sqlBuilder.append(" and nextexecutiontimestamp >");
            sqlBuilder.addLong(this.toSeconds(this.deviceDataModelService.clock().instant()));
            sqlBuilder.append(" and ct.comserver is null and ct.status = 0 and ct.currentretrycount = 0 and ct.lastExecutionFailed = 0 and ct.lastsuccessfulcommunicationend is not null");
        } else {
            sqlBuilder.append(" and ct.nextexecutiontimestamp is not null");
        }
        sqlBuilder.append(" and ct.lastSessionSuccessIndicator = 0");
        this.appendConnectionTypeHeatMapComTaskExecutionSessionConditions(true, sqlBuilder);
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
        SqlBuilder sqlBuilder = new SqlBuilder("select ct.lastSessionSuccessIndicator, count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct where ct.nextexecutiontimestamp is not null");
        sqlBuilder.append("      and ct.obsolete_date is null");
        sqlBuilder.append("      and ct.lastsession is not null");
        this.appendRestrictedStatesClause(sqlBuilder, "ct");
        sqlBuilder.append("    group by ct.lastSessionSuccessIndicator");
        return this.addMissingSuccessIndicatorCounters(this.fetchSuccessIndicatorCounters(sqlBuilder));
    }

    @Override
    public Map<ComSession.SuccessIndicator, Long> getConnectionTaskLastComSessionSuccessIndicatorCount(EndDeviceGroup deviceGroup) {
        SqlBuilder sqlBuilder = new SqlBuilder("select ct.lastSessionSuccessIndicator, count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct where ct.nextexecutiontimestamp is not null");
        sqlBuilder.append("      and ct.obsolete_date is null");
        sqlBuilder.append("      and ct.lastsession is not null");
        this.appendDeviceGroupConditions(deviceGroup, sqlBuilder, "ct");
        this.appendRestrictedStatesClause(sqlBuilder, "ct");
        sqlBuilder.append(" group by ct.lastSessionSuccessIndicator");
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
        return Arrays.asList(atLeastOneFailure, completeSuccess, failureSetupError, failureBroken);
    }

    private List<Long> missingSuccessIndicatorCounters(ConnectionTypePluggableClass connectionTypePluggableClass) {
        return this.orderSuccessIndicatorCounters(null, null);
    }

    private List<Long> missingSuccessIndicatorCounters(ComPortPool comPortPool) {
        return this.orderSuccessIndicatorCounters(null, null);
    }

    private List<Long> orderSuccessIndicatorCounters(Map<ComSession.SuccessIndicator, Long> successIndicatorCounters, Map<ComSession.SuccessIndicator, Long> failingTaskCounters) {
        List<Long> counters = new ArrayList<>(ComSession.SuccessIndicator.values().length + 1);
        this.addSuccessIndicatorCounter(counters, failingTaskCounters, ComSession.SuccessIndicator.Success);
        this.addSuccessIndicatorCounter(counters, successIndicatorCounters, ComSession.SuccessIndicator.Success);
        this.addSuccessIndicatorCounter(counters, successIndicatorCounters, ComSession.SuccessIndicator.SetupError);
        this.addSuccessIndicatorCounter(counters, successIndicatorCounters, ComSession.SuccessIndicator.Broken);
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
                        "dev", "devicetype",
                        "DDC_DEVICE dev", "ct.device = dev.id"));
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
        return this.connectionTypeHeatMapSqlBuilder(deviceGroup, failureIndicators, "ct", groupByFieldName, "", "");
    }

    private SqlBuilder connectionTypeHeatMapSqlBuilder(
            final EndDeviceGroup deviceGroup,
            final List<ComSession.SuccessIndicator> failureIndicators,
            final String groupByEntityAliasName,
            final String groupByFieldName,
            final String additionalFromClause,
            final String additionalJoinClause) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("WITH failedTask as (");
        sqlBuilder.append("  select comsession");
        sqlBuilder.append("    from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXECSESSION.name());
        sqlBuilder.append("   where successindicator > 0");
        sqlBuilder.append("   group by comsession)");
        sqlBuilder.append("select ");
        sqlBuilder.append(groupByFieldName);
        sqlBuilder.append(", sum(completeSucces), sum(atLeastOneFailure), ");
        sqlBuilder.append(
                failureIndicators
                        .stream()
                        .map(i -> "sum(" + this.connectionTypeHeatMapFailureIndicatorCaseClauseNameFor(i) + ")")
                        .collect(Collectors.joining(",")));
        sqlBuilder.append("  from (");
        sqlBuilder.append("        select ");
        sqlBuilder.append(groupByEntityAliasName);
        sqlBuilder.append(".");
        sqlBuilder.append(groupByFieldName);
        sqlBuilder.append(", ct.lastSessionSuccessIndicator,");
        sqlBuilder.append("          CASE WHEN ct.lastSessionSuccessIndicator = 0");
        sqlBuilder.append("                AND failedTask.comsession IS NULL");
        sqlBuilder.append("               THEN 1");
        sqlBuilder.append("               ELSE 0");
        sqlBuilder.append("          END completeSucces,");
        sqlBuilder.append("          CASE WHEN ct.lastSessionSuccessIndicator = 0");
        sqlBuilder.append("                AND failedTask.comsession IS NOT NULL");
        sqlBuilder.append("               THEN 1");
        sqlBuilder.append("               ELSE 0");
        sqlBuilder.append("          END atLeastOneFailure,");
        sqlBuilder.append(
                failureIndicators
                        .stream()
                        .map(this::connectionTypeHeatMapFailureIndicatorCaseClause)
                        .collect(Collectors.joining(",")));
        sqlBuilder.append("        from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct, ");
        sqlBuilder.append(additionalFromClause);
        if (!additionalFromClause.isEmpty()) {
            sqlBuilder.append(", ");
        }
        sqlBuilder.append("failedTask where ct.obsolete_date is null and ct.status = 0 and ");
        sqlBuilder.append(additionalJoinClause);
        if (!additionalJoinClause.isEmpty()) {
            sqlBuilder.append(" and ");
        }
        sqlBuilder.append("ct.lastSession = failedTask.comSession(+)");
        this.appendRestrictedStatesClause(sqlBuilder, "ct");
        this.appendDeviceGroupConditions(deviceGroup, sqlBuilder, "ct");
        sqlBuilder.append("       )");
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
        return Arrays.asList(ComSession.SuccessIndicator.SetupError, ComSession.SuccessIndicator.Broken);
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

    void appendRestrictedStatesClause(SqlBuilder sqlBuilder, String connectionTaskAliasName) {
        sqlBuilder.append( " and ");
        DeviceStateSqlBuilder
                .forDefaultExcludedStates(connectionTaskAliasName)
                .appendRestrictedStatesCondition(sqlBuilder, this.deviceDataModelService.clock());
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