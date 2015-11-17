package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dashboard.CommunicationTaskOverview;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This JSON representation holds the entire communication overview, including heat map
 * @link http://confluence.eict.vpdc/display/JUP/Communications
 */
public class CommunicationOverviewInfoFactory {

    private final BreakdownFactory breakdownFactory;
    private final OverviewFactory overviewFactory;
    private final SummaryInfoFactory summaryInfoFactory;
    private final Thesaurus thesaurus;
    private final DashboardService dashboardService;
    private final DataCollectionKpiService dataCollectionKpiService;
    private final KpiScoreFactory kpiScoreFactory;

    @Inject
    public CommunicationOverviewInfoFactory(BreakdownFactory breakdownFactory, OverviewFactory overviewFactory,
                                            SummaryInfoFactory summaryInfoFactory, Thesaurus thesaurus,
                                            DashboardService dashboardService, DataCollectionKpiService dataCollectionKpiService,
                                            KpiScoreFactory kpiScoreFactory) {
        this.breakdownFactory = breakdownFactory;
        this.overviewFactory = overviewFactory;
        this.summaryInfoFactory = summaryInfoFactory;
        this.thesaurus = thesaurus;
        this.dashboardService = dashboardService;
        this.dataCollectionKpiService = dataCollectionKpiService;
        this.kpiScoreFactory = kpiScoreFactory;
    }

    public CommunicationOverviewInfo asInfo() {
        CommunicationTaskOverview overview = dashboardService.getCommunicationTaskOverview();

        return getCommunicationOverviewInfo(overview);
    }

    public CommunicationOverviewInfo asInfo(EndDeviceGroup queryEndDeviceGroup) {
        CommunicationTaskOverview overview = dashboardService.getCommunicationTaskOverview(queryEndDeviceGroup);
        CommunicationOverviewInfo info = getCommunicationOverviewInfo(overview);
        addKpiInfo(queryEndDeviceGroup, info);
        info.deviceGroup = new DeviceGroupFilterInfo(queryEndDeviceGroup.getId(), queryEndDeviceGroup.getName());
        return info;
    }

    public CommunicationOverviewInfo asWidgetInfo() {
        CommunicationOverviewInfo info = new CommunicationOverviewInfo();
        TaskStatusOverview taskStatusOverview = dashboardService.getCommunicationTaskStatusOverview();
        SummaryData summaryData = new SummaryData(taskStatusOverview);
        info.communicationSummary= summaryInfoFactory.from(summaryData);
        return info;
    }

    private CommunicationOverviewInfo getCommunicationOverviewInfo(CommunicationTaskOverview overview) {
        CommunicationOverviewInfo info = new CommunicationOverviewInfo();
        info.communicationSummary= summaryInfoFactory.from(new SummaryData(overview.getStatusOverview()));
        info.overviews=new ArrayList<>(2);
        info.overviews.add(
                overviewFactory.createOverview(
                        thesaurus.getFormat(TranslationKeys.PER_CURRENT_STATE).format(),
                        overview.getStatusOverview(),
                        FilterOption.currentStates,
                        TaskStatusTranslationKeys::translationFor));
        info.overviews.add(
                overviewFactory.createOverview(
                        thesaurus.getFormat(TranslationKeys.PER_LATEST_RESULT).format(),
                        overview.getCommunicationTaskCompletionResultOverview(),
                        FilterOption.latestResults,
                        CompletionCodeTranslationKeys::translationFor));
        overviewFactory.sortAllOverviews(info.overviews);

        info.breakdowns=new ArrayList<>(3);
        info.breakdowns.add(
                breakdownFactory.createBreakdown(
                        thesaurus, TranslationKeys.PER_COMMUNICATION_SCHEDULE,
                        overview.getComScheduleBreakdown(),
                        FilterOption.comSchedules));
        info.breakdowns.add(
                breakdownFactory.createBreakdown(
                        thesaurus, TranslationKeys.PER_COMMUNICATION_TASK,
                        overview.getComTaskBreakdown(), FilterOption.comTasks));
        info.breakdowns.add(
                breakdownFactory.createBreakdown(
                        thesaurus, TranslationKeys.PER_DEVICE_TYPE,
                        overview.getDeviceTypeBreakdown(), FilterOption.deviceTypes));
        breakdownFactory.sortAllBreakdowns(info.breakdowns);

        return info;
    }


    private void addKpiInfo(EndDeviceGroup queryEndDeviceGroup, CommunicationOverviewInfo info) {
        Optional<DataCollectionKpi> dataCollectionKpiOptional = dataCollectionKpiService.findDataCollectionKpi(queryEndDeviceGroup);
        if (dataCollectionKpiOptional.isPresent() &&  dataCollectionKpiOptional.get().calculatesComTaskExecutionKpi()) {
            TemporalAmount frequency = dataCollectionKpiOptional.get().comTaskExecutionKpiCalculationIntervalLength().get();
            Range<Instant> intervalByPeriod = kpiScoreFactory.getActualRangeByDisplayRange(dataCollectionKpiOptional.get().getDisplayRange().asTemporalAmount());
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

