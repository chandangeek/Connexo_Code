/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDeviceStage;
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
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.DeviceStageSqlBuilder;
import com.energyict.mdc.device.data.impl.tasks.ServerComTaskStatus;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskStatus;
import com.energyict.mdc.device.data.impl.tasks.WithClauses;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.CommunicationTaskBreakdowns;
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.scheduling.model.ComSchedule;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link CommunicationTaskReportService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-25 (13:37)
 */
@LiteralSql
public class CommunicationTaskReportServiceImpl implements CommunicationTaskReportService {

    private static final String BUSY_ALIAS_NAME = ServerConnectionTaskStatus.BUSY_TASK_ALIAS_NAME;
    private static final String DEVICE_STAGE_ALIAS_NAME = "enddevices";
    private final DeviceDataModelService deviceDataModelService;
    private final MeteringService meteringService;

    @Inject
    public CommunicationTaskReportServiceImpl(DeviceDataModelService deviceDataModelService, MeteringService meteringService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
        this.meteringService = meteringService;
    }

    @Override
    public Map<TaskStatus, Long> getComTaskExecutionStatusCount() {
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        filter.taskStatuses = EnumSet.allOf(TaskStatus.class);
        filter.deviceGroups = new HashSet<>();
        return this.getComTaskExecutionStatusCount(filter);
    }

    @Override
    public Map<TaskStatus, Long> getComTaskExecutionStatusCount(EndDeviceGroup deviceGroup) {
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        filter.taskStatuses = EnumSet.allOf(TaskStatus.class);
        filter.deviceGroups = this.asSet(deviceGroup);
        return this.getComTaskExecutionStatusCount(filter);
    }

    private Set<EndDeviceGroup> asSet(EndDeviceGroup deviceGroup) {
        return Stream.of(deviceGroup).collect(Collectors.toSet());
    }

    @Override
    public Map<TaskStatus, Long> getComTaskExecutionStatusCount(ComTaskExecutionFilterSpecification filter) {
        ClauseAwareSqlBuilder sqlBuilder = null;
        for (ServerComTaskStatus taskStatus : this.taskStatusesForCounting(filter)) {
            // Check first pass
            if (sqlBuilder == null) {
                sqlBuilder = ClauseAwareSqlBuilder
                                .withExcludedStages(
                                        DEVICE_STAGE_ALIAS_NAME,
                                        EndDeviceStage.fromNames(filter.restrictedDeviceStages),
                                        this.deviceDataModelService.clock().instant());
                WithClauses.COMTASK_EXECUTION_WITH_DEVICE_STATE.appendTo(sqlBuilder, "ctes");
                this.countByFilterAndTaskStatusSqlBuilder(sqlBuilder, filter, taskStatus);
            } else {
                sqlBuilder.unionAll();
                this.countByFilterAndTaskStatusSqlBuilder(sqlBuilder, filter, taskStatus);
            }
        }
        return this.addMissingTaskStatusCounters(this.fetchTaskStatusCounters(sqlBuilder));
    }

    private Set<ServerComTaskStatus> taskStatusesForCounting(ComTaskExecutionFilterSpecification filter) {
        return taskStatusesForCounting(filter.taskStatuses);
    }

    private Set<ServerComTaskStatus> taskStatusesForCounting(Set<TaskStatus> taskStatuses) {
        Set<ServerComTaskStatus> serverTaskStatuses = EnumSet.noneOf(ServerComTaskStatus.class);
        serverTaskStatuses.addAll(taskStatuses.stream().map(ServerComTaskStatus::forTaskStatus).collect(Collectors.toList()));
        return serverTaskStatuses;
    }

    private void countByFilterAndTaskStatusSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, ComTaskExecutionFilterSpecification filter, ServerComTaskStatus taskStatus) {
        ComTaskExecutionFilterMatchCounterSqlBuilder countingFilter =
                new ComTaskExecutionFilterMatchCounterSqlBuilder(
                        taskStatus,
                        filter,
                        this.deviceDataModelService.clock(),
                        this.deviceFromDeviceGroupQueryExecutor());
        countingFilter.appendTo(sqlBuilder);
    }

    private Map<TaskStatus, Long> fetchTaskStatusCounters(ClauseAwareSqlBuilder builder) {
        return this.deviceDataModelService.fetchTaskStatusCounters(builder);
    }

    private Map<TaskStatus, Long> addMissingTaskStatusCounters(Map<TaskStatus, Long> counters) {
        return this.deviceDataModelService.addMissingTaskStatusCounters(counters);
    }

    @Override
    public Map<ComSchedule, Map<TaskStatus, Long>> getCommunicationTasksComScheduleBreakdown(Set<TaskStatus> taskStatuses) {
        return this.getCommunicationTasksComScheduleBreakdown(taskStatuses, Collections.emptySet());
    }

    @Override
    public Map<ComSchedule, Map<TaskStatus, Long>> getCommunicationTasksComScheduleBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup) {
        return this.getCommunicationTasksComScheduleBreakdown(taskStatuses, this.asSet(deviceGroup));
    }

    private Map<ComSchedule, Map<TaskStatus, Long>> getCommunicationTasksComScheduleBreakdown(Set<TaskStatus> taskStatuses, Set<EndDeviceGroup> deviceGroups) {
        ClauseAwareSqlBuilder sqlBuilder = null;
        for (ServerComTaskStatus taskStatus : this.taskStatusesForCounting(taskStatuses)) {
            // Check first pass
            if (sqlBuilder == null) {
                sqlBuilder = WithClauses.BUSY_CONNECTION_TASK.sqlBuilder(BUSY_ALIAS_NAME);
                this.countByComScheduleAndTaskStatusSqlBuilder(sqlBuilder, taskStatus, deviceGroups);
            }
            else {
                sqlBuilder.unionAll();
                this.countByComScheduleAndTaskStatusSqlBuilder(sqlBuilder, taskStatus, deviceGroups);
            }
        }
        return this.injectComSchedulesAndAddMissing(this.deviceDataModelService.fetchTaskStatusBreakdown(sqlBuilder));
    }

    private void countByComScheduleAndTaskStatusSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, ServerComTaskStatus taskStatus, Set<EndDeviceGroup> deviceGroups) {
        ComTaskExecutionFilterSpecification filterSpecification = new ComTaskExecutionFilterSpecification();
        filterSpecification.deviceGroups = deviceGroups;
        ComTaskExecutionComScheduleCounterSqlBuilder countingFilter =
                new ComTaskExecutionComScheduleCounterSqlBuilder(
                        taskStatus,
                        this.deviceDataModelService.clock(),
                        filterSpecification,
                        this.deviceFromDeviceGroupQueryExecutor());
        countingFilter.appendTo(sqlBuilder);
    }

    private Map<ComSchedule, Map<TaskStatus, Long>> injectComSchedulesAndAddMissing(Map<Long, Map<TaskStatus, Long>> breakdown) {
        Map<Long, ComSchedule> comSchedules =
                this.deviceDataModelService.schedulingService().findAllSchedules().stream().
                        collect(Collectors.toMap(ComSchedule::getId, Function.identity()));
        return this.injectBreakDownsAndAddMissing(breakdown, comSchedules);
    }

    @Override
    public Map<DeviceType, Map<TaskStatus, Long>> getCommunicationTasksDeviceTypeBreakdown(Set<TaskStatus> taskStatuses) {
        return this.getCommunicationTasksDeviceTypeBreakdown(taskStatuses, Collections.emptySet());
    }

    @Override
    public Map<DeviceType, Map<TaskStatus, Long>> getCommunicationTasksDeviceTypeBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup) {
        return this.getCommunicationTasksDeviceTypeBreakdown(taskStatuses, this.asSet(deviceGroup));
    }

    private Map<DeviceType, Map<TaskStatus, Long>> getCommunicationTasksDeviceTypeBreakdown(Set<TaskStatus> taskStatuses, Set<EndDeviceGroup> deviceGroups) {
        ComTaskExecutionFilterSpecification filterSpecification = new ComTaskExecutionFilterSpecification();
        filterSpecification.deviceGroups = deviceGroups;
        ClauseAwareSqlBuilder sqlBuilder = null;
        for (ServerComTaskStatus taskStatus : this.taskStatusesForCounting(taskStatuses)) {
            // Check first pass
            if (sqlBuilder == null) {
                sqlBuilder = ClauseAwareSqlBuilder
                        .withExcludedStages(
                                DEVICE_STAGE_ALIAS_NAME,
                                EndDeviceStage.fromNames(filterSpecification.restrictedDeviceStages),
                                this.deviceDataModelService.clock().instant());
                WithClauses.BUSY_CONNECTION_TASK.appendTo(sqlBuilder, BUSY_ALIAS_NAME);
                this.countByDeviceTypeAndTaskStatusSqlBuilder(sqlBuilder, filterSpecification, taskStatus);
            }
            else {
                sqlBuilder.unionAll();
                this.countByDeviceTypeAndTaskStatusSqlBuilder(sqlBuilder, filterSpecification, taskStatus);
            }
        }
        return this.injectDeviceTypesAndAddMissing(this.deviceDataModelService.fetchTaskStatusBreakdown(sqlBuilder));
    }

    private void countByDeviceTypeAndTaskStatusSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, ComTaskExecutionFilterSpecification filterSpecification, ServerComTaskStatus taskStatus) {
        ComTaskExecutionDeviceTypeCounterSqlBuilder countingFilter =
                new ComTaskExecutionDeviceTypeCounterSqlBuilder(
                        taskStatus,
                        this.deviceDataModelService.clock(),
                        filterSpecification,
                        this.deviceFromDeviceGroupQueryExecutor());
        countingFilter.appendTo(sqlBuilder);
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

    private Map<DeviceType, Map<TaskStatus, Long>> injectDeviceTypesAndAddMissing(Map<Long, Map<TaskStatus, Long>> breakdown) {
        Map<Long, DeviceType> deviceTypes =
                this.deviceDataModelService.deviceConfigurationService().findAllDeviceTypes().stream().
                        collect(Collectors.toMap(DeviceType::getId, Function.identity()));
        return this.injectBreakDownsAndAddMissing(breakdown, deviceTypes);
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

    @Override
    public Map<DeviceType, List<Long>> getComTasksDeviceTypeHeatMap() {
        return this.getComTasksDeviceTypeHeatMap(null);
    }

    @Override
    public Map<DeviceType, List<Long>> getComTasksDeviceTypeHeatMap(EndDeviceGroup deviceGroup) {
        SqlBuilder sqlBuilder = new SqlBuilder("WITH ");
        DeviceStageSqlBuilder
                .forDefaultExcludedStages("enddevices")
                .appendRestrictedStagesWithClause(sqlBuilder, this.deviceDataModelService.clock().instant());
        sqlBuilder.append("select dev.DEVICETYPE, cte.lastsess_highestpriocomplcode, count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" cte join ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" dev on cte.device = dev.id join enddevices kd on dev.meterid = kd.id ");
        this.appendDeviceGroupConditions(deviceGroup, sqlBuilder);
        sqlBuilder.append(" where cte.obsolete_date is null");
        sqlBuilder.append("   and cte.lastsession is not null");
        sqlBuilder.append(" group by dev.devicetype, cte.lastsess_highestpriocomplcode");
        Map<Long, Map<CompletionCode, Long>> partialCounters = this.fetchComTaskHeatMapCounters(sqlBuilder);
        return this.buildDeviceTypeHeatMap(partialCounters);
    }

    private Map<DeviceType, List<Long>> buildDeviceTypeHeatMap(Map<Long, Map<CompletionCode, Long>> partialCounters) {
        Map<Long, DeviceType> deviceTypes =
                this.deviceDataModelService
                        .deviceConfigurationService()
                        .findAllDeviceTypes().find()
                        .stream()
                        .collect(Collectors.toMap(
                                DeviceType::getId,
                                Function.identity()));
        Map<DeviceType, List<Long>> heatMap =
                deviceTypes.values().stream().collect(
                        Collectors.toMap(
                                Function.identity(),
                                this::missingCompletionCodeCounters));
        for (Long deviceTypeId : partialCounters.keySet()) {
            DeviceType deviceType = deviceTypes.get(deviceTypeId);
            heatMap.put(deviceType, this.orderedCompletionCodeCounters(partialCounters.get(deviceTypeId)));
        }
        return heatMap;
    }

    private List<Long> missingCompletionCodeCounters(DeviceType deviceType) {
        return Stream.of(CompletionCode.values()).map(code -> 0L).collect(Collectors.toList());
    }

    private List<Long> orderedCompletionCodeCounters(Map<CompletionCode, Long> completionCodeCounters) {
        List<Long> counters = new ArrayList<>(CompletionCode.values().length);
        for (CompletionCode completionCode : CompletionCode.values()) {
            counters.add(completionCodeCounters.get(completionCode));
        }
        return counters;
    }

    private Map<Long, Map<CompletionCode, Long>> fetchComTaskHeatMapCounters(SqlBuilder builder) {
        try (Connection connection = this.deviceDataModelService.dataModel().getConnection(true);
             PreparedStatement stmnt = builder.prepare(connection)) {
            return this.fetchComTaskHeatMapCounters(stmnt);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private Map<Long, Map<CompletionCode, Long>> fetchComTaskHeatMapCounters(PreparedStatement statement) throws SQLException {
        Map<Long, Map<CompletionCode, Long>> counters = new HashMap<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                long businessObjectId = resultSet.getLong(1);
                int completionCodeOrdinal = resultSet.getInt(2);
                long counter = resultSet.getLong(3);
                Map<CompletionCode, Long> successIndicatorCounters = this.getOrPutCompletionCodeCounters(businessObjectId, counters);
                successIndicatorCounters.put(CompletionCode.fromDBValue(completionCodeOrdinal), counter);
            }
        }
        return counters;
    }

    private Map<CompletionCode, Long> getOrPutCompletionCodeCounters(long businessObjectId, Map<Long, Map<CompletionCode, Long>> counters) {
        Map<CompletionCode, Long> completionCodeCounters = counters.get(businessObjectId);
        if (completionCodeCounters == null) {
            completionCodeCounters = new HashMap<>();
            for (CompletionCode missing : EnumSet.allOf(CompletionCode.class)) {
                completionCodeCounters.put(missing, 0L);
            }
            counters.put(businessObjectId, completionCodeCounters);
        }
        return completionCodeCounters;
    }

    @Override
    public Map<CompletionCode, Long> getComTaskLastComSessionHighestPriorityCompletionCodeCount() {
        return this.getComTaskLastComSessionHighestPriorityCompletionCodeCount(Optional.empty());
    }

    @Override
    public Map<CompletionCode, Long> getComTaskLastComSessionHighestPriorityCompletionCodeCount(EndDeviceGroup deviceGroup) {
        return this.getComTaskLastComSessionHighestPriorityCompletionCodeCount(Optional.of(deviceGroup));
    }

    private Map<CompletionCode, Long> getComTaskLastComSessionHighestPriorityCompletionCodeCount(Optional<EndDeviceGroup> deviceGroup) {
        SqlBuilder sqlBuilder = new SqlBuilder("WITH ");
        DeviceStageSqlBuilder
                .forDefaultExcludedStages("cte")
                .appendRestrictedStagesWithClause(sqlBuilder, this.deviceDataModelService.clock().instant());
        sqlBuilder.append("select cte.lastsess_highestpriocomplcode, count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" cte join ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" dev on cte.device = dev.id join enddevices kd on dev.meterid = kd.id");
        sqlBuilder.append("where obsolete_date is null and lastsession is not null");
        deviceGroup.ifPresent(group -> this.appendDeviceGroupConditions(group, sqlBuilder));
        sqlBuilder.append("group by cte.lastsess_highestpriocomplcode");
        return this.addMissingCompletionCodeCounters(this.fetchCompletionCodeCounters(sqlBuilder));
    }

    private void appendDeviceGroupConditions(EndDeviceGroup deviceGroup, SqlBuilder sqlBuilder) {
        if (deviceGroup != null) {
            sqlBuilder.append(" and cte.device in (");
            if (deviceGroup instanceof QueryEndDeviceGroup) {
                sqlBuilder.add(((QueryEndDeviceGroup)deviceGroup).toFragment());
            } else {
                sqlBuilder.add(((EnumeratedEndDeviceGroup) deviceGroup).getAmrIdSubQuery(getMdcAmrSystem().get()).toFragment());
            }
            sqlBuilder.append(")");
        }
    }

    private Map<CompletionCode, Long> fetchCompletionCodeCounters(SqlBuilder builder) {
        Map<CompletionCode, Long> counters = new HashMap<>();
        try (Connection connection = this.deviceDataModelService.dataModel().getConnection(true);
             PreparedStatement stmnt = builder.prepare(connection)) {
            this.fetchCompletionCodeCounters(stmnt, counters);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        return counters;
    }

    private void fetchCompletionCodeCounters(PreparedStatement statement, Map<CompletionCode, Long> counters) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int completionCodeOrdinal = resultSet.getInt(1);
                long counter = resultSet.getLong(2);
                counters.put(CompletionCode.fromDBValue(completionCodeOrdinal), counter);
            }
        }
    }

    private Map<CompletionCode, Long> addMissingCompletionCodeCounters(Map<CompletionCode, Long> counters) {
        for (CompletionCode missing : this.completionCodeComplement(counters.keySet())) {
            counters.put(missing, 0L);
        }
        return counters;
    }

    private EnumSet<CompletionCode> completionCodeComplement(Set<CompletionCode> completionCodes) {
        if (completionCodes.isEmpty()) {
            return EnumSet.allOf(CompletionCode.class);
        } else {
            return EnumSet.complementOf(EnumSet.copyOf(completionCodes));
        }
    }

    private Optional<AmrSystem> getMdcAmrSystem() {
        return this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
    }

    @Override
    public CommunicationTaskBreakdowns getCommunicationTaskBreakdowns() {
        return this.getCommunicationTaskBreakdowns(() -> CommunicationTaskBreakdownSqlExecutor.systemWide(this.deviceDataModelService.dataModel()));
    }

    @Override
    public CommunicationTaskBreakdowns getCommunicationTaskBreakdowns(EndDeviceGroup deviceGroup) {
        return this.getCommunicationTaskBreakdowns(() -> CommunicationTaskBreakdownSqlExecutor.forGroup(deviceGroup, this.getMdcAmrSystem().get(), this.deviceDataModelService.dataModel()));
    }

    private CommunicationTaskBreakdowns getCommunicationTaskBreakdowns(Supplier<CommunicationTaskBreakdownSqlExecutor> sqlExecutorSupplier) {
        CommunicationTaskBreakdownsImpl breakdowns = this.deviceDataModelService.dataModel().getInstance(CommunicationTaskBreakdownsImpl.class);
        sqlExecutorSupplier.get().breakdowns().forEach(breakdowns::add);
        return breakdowns;
    }

}