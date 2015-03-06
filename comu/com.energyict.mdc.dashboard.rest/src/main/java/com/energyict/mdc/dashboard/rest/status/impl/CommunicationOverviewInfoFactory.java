package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.google.common.collect.Range;

import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.ComScheduleBreakdown;
import com.energyict.mdc.dashboard.ComTaskBreakdown;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.rest.CompletionCodeAdapter;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

/**
 * This JSON representation holds the entire communication overview
 * @link http://confluence.eict.vpdc/display/JUP/Communications
 */
public class CommunicationOverviewInfoFactory {

    private static final TaskStatusAdapter taskStatusAdapter = new TaskStatusAdapter();
    private static final CompletionCodeAdapter completionCodeAdapter = new CompletionCodeAdapter();

    private final BreakdownFactory breakdownFactory;
    private final OverviewFactory overviewFactory;
    private final SummaryInfoFactory summaryInfoFactory;
    private final Thesaurus thesaurus;
    private final DashboardService dashboardService;
    private final DataCollectionKpiService dataCollectionKpiService;
    private final KpiScoreFactory kpiScoreFactory;

    @Inject
    public CommunicationOverviewInfoFactory(BreakdownFactory breakdownFactory, OverviewFactory overviewFactory, SummaryInfoFactory summaryInfoFactory, Thesaurus thesaurus, DashboardService dashboardService, DataCollectionKpiService dataCollectionKpiService, KpiScoreFactory kpiScoreFactory) {
        this.breakdownFactory = breakdownFactory;
        this.overviewFactory = overviewFactory;
        this.summaryInfoFactory = summaryInfoFactory;
        this.thesaurus = thesaurus;
        this.dashboardService = dashboardService;
        this.dataCollectionKpiService = dataCollectionKpiService;
        this.kpiScoreFactory = kpiScoreFactory;
    }

    public CommunicationOverviewInfo asInfo() {
        TaskStatusOverview taskStatusOverview = dashboardService.getCommunicationTaskStatusOverview();
        SummaryData summaryData = new SummaryData(taskStatusOverview);
        ComCommandCompletionCodeOverview comSessionSuccessIndicatorOverview = dashboardService.getCommunicationTaskCompletionResultOverview();
        ComScheduleBreakdown comScheduleBreakdown = dashboardService.getCommunicationTasksComScheduleBreakdown();
        ComTaskBreakdown comTaskBreakdown = dashboardService.getCommunicationTasksBreakdown();
        DeviceTypeBreakdown deviceTypeBreakdown = dashboardService.getCommunicationTasksDeviceTypeBreakdown();

        CommunicationOverviewInfo info = getCommunicationOverviewInfo(taskStatusOverview, summaryData, comSessionSuccessIndicatorOverview, comScheduleBreakdown, comTaskBreakdown, deviceTypeBreakdown);
        return info;
    }

    public CommunicationOverviewInfo asWidgetInfo() {
        CommunicationOverviewInfo info = new CommunicationOverviewInfo();
        TaskStatusOverview taskStatusOverview = dashboardService.getCommunicationTaskStatusOverview();
        SummaryData summaryData = new SummaryData(taskStatusOverview);
        info.communicationSummary= summaryInfoFactory.from(summaryData);
        return info;
    }

    private CommunicationOverviewInfo getCommunicationOverviewInfo(TaskStatusOverview taskStatusOverview, SummaryData summaryData, ComCommandCompletionCodeOverview comSessionSuccessIndicatorOverview, ComScheduleBreakdown comScheduleBreakdown, ComTaskBreakdown comTaskBreakdown, DeviceTypeBreakdown deviceTypeBreakdown) {
        CommunicationOverviewInfo info = new CommunicationOverviewInfo();

        info.communicationSummary= summaryInfoFactory.from(summaryData);

        info.overviews=new ArrayList<>(2);
        info.overviews.add(overviewFactory.createOverview(thesaurus.getString(MessageSeeds.PER_CURRENT_STATE.getKey(), MessageSeeds.PER_CURRENT_STATE.getDefaultFormat()), taskStatusOverview, FilterOption.currentStates, taskStatusAdapter));
        info.overviews.add(overviewFactory.createOverview(thesaurus.getString(MessageSeeds.PER_LATEST_RESULT.getKey(), MessageSeeds.PER_LATEST_RESULT.getDefaultFormat()), comSessionSuccessIndicatorOverview, FilterOption.latestResults, completionCodeAdapter));
        overviewFactory.sortAllOverviews(info.overviews);

        info.breakdowns=new ArrayList<>(3);
        info.breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_COMMUNICATION_SCHEDULE.getKey(), MessageSeeds.PER_COMMUNICATION_SCHEDULE.getDefaultFormat()), comScheduleBreakdown, FilterOption.comSchedules));
        info.breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_COMMUNICATION_TASK.getKey(), MessageSeeds.PER_COMMUNICATION_TASK.getDefaultFormat()), comTaskBreakdown, FilterOption.comTasks));
        info.breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_DEVICE_TYPE.getKey(), MessageSeeds.PER_DEVICE_TYPE.getDefaultFormat()), deviceTypeBreakdown, FilterOption.deviceTypes));
        breakdownFactory.sortAllBreakdowns(info.breakdowns);
        return info;
    }


    public CommunicationOverviewInfo asInfo(EndDeviceGroup queryEndDeviceGroup) {
        TaskStatusOverview taskStatusOverview = dashboardService.getCommunicationTaskStatusOverview(queryEndDeviceGroup);
        SummaryData summaryData = new SummaryData(taskStatusOverview);
        ComCommandCompletionCodeOverview comSessionSuccessIndicatorOverview = dashboardService.getCommunicationTaskCompletionResultOverview(queryEndDeviceGroup);
        ComScheduleBreakdown comScheduleBreakdown = dashboardService.getCommunicationTasksComScheduleBreakdown(queryEndDeviceGroup);
        ComTaskBreakdown comTaskBreakdown = dashboardService.getCommunicationTasksBreakdown(queryEndDeviceGroup);
        DeviceTypeBreakdown deviceTypeBreakdown = dashboardService.getCommunicationTasksDeviceTypeBreakdown(queryEndDeviceGroup);

        CommunicationOverviewInfo info = getCommunicationOverviewInfo(taskStatusOverview, summaryData, comSessionSuccessIndicatorOverview, comScheduleBreakdown, comTaskBreakdown, deviceTypeBreakdown);

        addKpiInfo(queryEndDeviceGroup, info);
        info.deviceGroup = new DeviceGroupFilterInfo(queryEndDeviceGroup.getId(), queryEndDeviceGroup.getName());
        return info;
    }

    private void addKpiInfo(EndDeviceGroup queryEndDeviceGroup, CommunicationOverviewInfo info) {
        Optional<DataCollectionKpi> dataCollectionKpiOptional = dataCollectionKpiService.findDataCollectionKpi(queryEndDeviceGroup);
        if (dataCollectionKpiOptional.isPresent() &&  dataCollectionKpiOptional.get().calculatesComTaskExecutionKpi()) {
            TemporalAmount frequency = dataCollectionKpiOptional.get().comTaskExecutionKpiCalculationIntervalLength().get();
            Range<Instant> intervalByPeriod = kpiScoreFactory.getRangeByDisplayRange(dataCollectionKpiOptional.get().getDisplayRange().asTemporalAmount());
            List<DataCollectionKpiScore> kpiScores = dataCollectionKpiOptional.get().getComTaskExecutionKpiScores(intervalByPeriod);
            if (!kpiScores.isEmpty()) {
                BigDecimal currentTarget = kpiScores.get(kpiScores.size() - 1).getTarget();
                if (currentTarget != null) {
                    info.communicationSummary.target = currentTarget.longValue();
                }
            }
            info.kpi = kpiScoreFactory.getKpiAsInfo(frequency, kpiScores, intervalByPeriod);
        }
    }

    public CommunicationOverviewInfo asWidgetInfo(EndDeviceGroup queryEndDeviceGroup) {
        CommunicationOverviewInfo info = new CommunicationOverviewInfo();
        TaskStatusOverview taskStatusOverview = dashboardService.getCommunicationTaskStatusOverview(queryEndDeviceGroup);
        SummaryData summaryData = new SummaryData(taskStatusOverview);
        info.communicationSummary= summaryInfoFactory.from(summaryData);
        addKpiInfo(queryEndDeviceGroup, info);
        info.deviceGroup = new DeviceGroupFilterInfo(queryEndDeviceGroup.getId(), queryEndDeviceGroup.getName());
        return info;
    }

}

