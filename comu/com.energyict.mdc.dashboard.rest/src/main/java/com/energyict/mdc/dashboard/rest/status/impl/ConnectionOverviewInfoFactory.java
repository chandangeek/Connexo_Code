package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Created by bvn on 9/18/14.
 */
public class ConnectionOverviewInfoFactory {
    @JsonIgnore
    private static final TaskStatusAdapter taskStatusAdapter = new TaskStatusAdapter();
    @JsonIgnore
    private static final ComSessionSuccessIndicatorAdapter COM_SESSION_SUCCESS_INDICATOR_ADAPTER = new ComSessionSuccessIndicatorAdapter();


    private final BreakdownFactory breakdownFactory;
    private final OverviewFactory overviewFactory;
    private final SummaryInfoFactory summaryInfoFactory;
    private final Thesaurus thesaurus;
    private final DashboardService dashboardService;
    private final ExceptionFactory exceptionFactory;
    private final DataCollectionKpiService dataCollectionKpiService;

    @Inject
    public ConnectionOverviewInfoFactory(BreakdownFactory breakdownFactory, OverviewFactory overviewFactory, SummaryInfoFactory summaryInfoFactory, Thesaurus thesaurus, DashboardService dashboardService, ExceptionFactory exceptionFactory, DataCollectionKpiService dataCollectionKpiService) {
        this.breakdownFactory = breakdownFactory;
        this.overviewFactory = overviewFactory;
        this.summaryInfoFactory = summaryInfoFactory;
        this.thesaurus = thesaurus;
        this.dashboardService = dashboardService;
        this.exceptionFactory = exceptionFactory;
        this.dataCollectionKpiService = dataCollectionKpiService;
    }

    public ConnectionOverviewInfo asInfo(QueryEndDeviceGroup endDeviceGroup) {
        TaskStatusOverview taskStatusOverview = dashboardService.getConnectionTaskStatusOverview(endDeviceGroup);
        ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview = dashboardService.getComSessionSuccessIndicatorOverview();
        ComPortPoolBreakdown comPortPoolBreakdown = dashboardService.getComPortPoolBreakdown(endDeviceGroup);
        ConnectionTypeBreakdown connectionTypeBreakdown = dashboardService.getConnectionTypeBreakdown(endDeviceGroup);
        DeviceTypeBreakdown deviceTypeBreakdown = dashboardService.getConnectionTasksDeviceTypeBreakdown(endDeviceGroup);
        SummaryData summaryData = new SummaryData(taskStatusOverview, comSessionSuccessIndicatorOverview.getAtLeastOneTaskFailedCount());
        DataCollectionKpi dataCollectionKpi = dataCollectionKpiService.findDataCollectionKpi(0).get();
        return getConnectionOverviewInfo(taskStatusOverview, comSessionSuccessIndicatorOverview, comPortPoolBreakdown, connectionTypeBreakdown, deviceTypeBreakdown, summaryData, dataCollectionKpi, endDeviceGroup);
    }

    public ConnectionOverviewInfo asInfo() {
        TaskStatusOverview taskStatusOverview = dashboardService.getConnectionTaskStatusOverview();
        ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview = dashboardService.getComSessionSuccessIndicatorOverview();
        ComPortPoolBreakdown comPortPoolBreakdown = dashboardService.getComPortPoolBreakdown();
        ConnectionTypeBreakdown connectionTypeBreakdown = dashboardService.getConnectionTypeBreakdown();
        DeviceTypeBreakdown deviceTypeBreakdown = dashboardService.getConnectionTasksDeviceTypeBreakdown();
        SummaryData summaryData = new SummaryData(taskStatusOverview, comSessionSuccessIndicatorOverview.getAtLeastOneTaskFailedCount());
        return getConnectionOverviewInfo(taskStatusOverview, comSessionSuccessIndicatorOverview, comPortPoolBreakdown, connectionTypeBreakdown, deviceTypeBreakdown, summaryData);
    }

    private ConnectionOverviewInfo getConnectionOverviewInfo(TaskStatusOverview taskStatusOverview, ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview, ComPortPoolBreakdown comPortPoolBreakdown, ConnectionTypeBreakdown connectionTypeBreakdown, DeviceTypeBreakdown deviceTypeBreakdown, SummaryData summaryData) {
        ConnectionOverviewInfo info = new ConnectionOverviewInfo();
        info.connectionSummary = summaryInfoFactory.from(summaryData);

        info.overviews=new ArrayList<>(2);
        info.overviews.add(overviewFactory.createOverview(thesaurus.getString(MessageSeeds.PER_CURRENT_STATE.getKey(), MessageSeeds.PER_CURRENT_STATE.getDefaultFormat()), taskStatusOverview, FilterOption.currentStates, taskStatusAdapter)); // JP-4278
        TaskSummaryInfo perLatestResultOverview = overviewFactory.createOverview(thesaurus.getString(MessageSeeds.PER_LATEST_RESULT.getKey(), MessageSeeds.PER_LATEST_RESULT.getDefaultFormat()), comSessionSuccessIndicatorOverview, FilterOption.latestResults, COM_SESSION_SUCCESS_INDICATOR_ADAPTER);
        addAtLeastOntTaskFailedCounter(comSessionSuccessIndicatorOverview, perLatestResultOverview); // JP-5868
        info.overviews.add(perLatestResultOverview); // JP-4280
        overviewFactory.sortAllOverviews(info.overviews);

        info.breakdowns=new ArrayList<>(3);
        info.breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_COMMUNICATION_POOL.getKey(), MessageSeeds.PER_COMMUNICATION_POOL.getDefaultFormat()), comPortPoolBreakdown, FilterOption.comPortPools)); // JP-4281
        info.breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_CONNECTION_TYPE.getKey(), MessageSeeds.PER_CONNECTION_TYPE.getDefaultFormat()), connectionTypeBreakdown, FilterOption.connectionTypes)); // JP-4283
        info.breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_DEVICE_TYPE.getKey(), MessageSeeds.PER_DEVICE_TYPE.getDefaultFormat()), deviceTypeBreakdown, FilterOption.deviceTypes)); // JP-4284
        breakdownFactory.sortAllBreakdowns(info.breakdowns);

        return info;
    }

    private ConnectionOverviewInfo getConnectionOverviewInfo(TaskStatusOverview taskStatusOverview, ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview, ComPortPoolBreakdown comPortPoolBreakdown, ConnectionTypeBreakdown connectionTypeBreakdown, DeviceTypeBreakdown deviceTypeBreakdown, SummaryData summaryData, DataCollectionKpi dataCollectionKpi, QueryEndDeviceGroup endDeviceGroup) {
        ConnectionOverviewInfo info = getConnectionOverviewInfo(taskStatusOverview,comSessionSuccessIndicatorOverview,comPortPoolBreakdown,connectionTypeBreakdown,deviceTypeBreakdown,summaryData);
        info.deviceGroup = new IdWithNameInfo(endDeviceGroup.getId(), endDeviceGroup.getName());
        if (dataCollectionKpi.calculatesConnectionSetupKpi()) {
            info.kpi = new ArrayList<>();
            KpiScoreInfo success = new KpiScoreInfo(MessageSeeds.SUCCESS.getKey());
            KpiScoreInfo ongoing = new KpiScoreInfo(MessageSeeds.ONGOING.getKey());
            KpiScoreInfo failed = new KpiScoreInfo(MessageSeeds.FAILED.getKey());
            KpiScoreInfo target = new KpiScoreInfo(MessageSeeds.TARGET.getKey());
            info.kpi.add(success);
            info.kpi.add(ongoing);
            info.kpi.add(failed);
            info.kpi.add(target);

            List<DataCollectionKpiScore> kpiScores = dataCollectionKpi.getConnectionSetupKpiScores(getIntervalByPeriod(dataCollectionKpi.connectionSetupKpiCalculationIntervalLength().get()));
            for (DataCollectionKpiScore kpiScore : kpiScores) {
                success.addKpi(kpiScore.getTimestamp(), kpiScore.getSuccess());
                ongoing.addKpi(kpiScore.getTimestamp(), kpiScore.getOngoing());
                failed.addKpi(kpiScore.getTimestamp(), kpiScore.getFailed());
                target.addKpi(kpiScore.getTimestamp(), kpiScore.getTarget());
            }
        }
        return info;
    }

    private Interval getIntervalByPeriod(TemporalAmount temporalAmount) {
        LocalDate startDay=null;
        if (temporalAmount.getUnits().contains(ChronoUnit.SECONDS)) {
            if (temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofMinutes(5).getSeconds()
                    || temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofMinutes(15).getSeconds()
                    || temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofMinutes(30).getSeconds()) {
                startDay = LocalDate.now();
            } else if (temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofHours(1).getSeconds()) {
                startDay = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
            } else if (temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofDays(1).getSeconds()) {
                startDay = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            }
        } else {
            if (temporalAmount.get(ChronoUnit.DAYS) == 1) {
                startDay = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            } else if (temporalAmount.get(ChronoUnit.MONTHS) == 1) {
                startDay = LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
            }
        }
        if (startDay==null) {
                throw exceptionFactory.newException(MessageSeeds.UNSUPPORTED_KPI_PERIOD);
            }

        return Interval.startAt(Date.from(startDay.atStartOfDay().toInstant(ZoneOffset.UTC)));
    }

    private void addAtLeastOntTaskFailedCounter(ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview, TaskSummaryInfo perLatestResultOverview) {
        TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
        taskCounterInfo.id = null; // Not filterable
        taskCounterInfo.displayName = thesaurus.getString(MessageSeeds.SOME_TASKS_FAILED.getKey(), MessageSeeds.SOME_TASKS_FAILED.getDefaultFormat());
        taskCounterInfo.count = comSessionSuccessIndicatorOverview.getAtLeastOneTaskFailedCount();
        perLatestResultOverview.total += taskCounterInfo.count;
        perLatestResultOverview.counters.add(taskCounterInfo);
    }

}


