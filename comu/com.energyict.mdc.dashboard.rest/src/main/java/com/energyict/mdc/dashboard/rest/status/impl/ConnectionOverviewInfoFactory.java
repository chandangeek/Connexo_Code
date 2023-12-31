/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionTaskOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by bvn on 9/18/14.
 */
public class ConnectionOverviewInfoFactory {
    private static final Logger LOGGER = Logger.getLogger(ConnectionOverviewInfoFactory.class.getName());// just for time measurement

    private final BreakdownFactory breakdownFactory;
    private final OverviewFactory overviewFactory;
    private final SummaryInfoFactory summaryInfoFactory;
    private final Thesaurus thesaurus;
    private final DashboardService dashboardService;
    private final DataCollectionKpiService dataCollectionKpiService;
    private final KpiScoreFactory kpiScoreFactory;

    @Inject
    public ConnectionOverviewInfoFactory(BreakdownFactory breakdownFactory, OverviewFactory overviewFactory, SummaryInfoFactory summaryInfoFactory,
                                         Thesaurus thesaurus, DashboardService dashboardService,
                                         DataCollectionKpiService dataCollectionKpiService, KpiScoreFactory kpiScoreFactory) {
        this.breakdownFactory = breakdownFactory;
        this.overviewFactory = overviewFactory;
        this.summaryInfoFactory = summaryInfoFactory;
        this.thesaurus = thesaurus;
        this.dashboardService = dashboardService;
        this.dataCollectionKpiService = dataCollectionKpiService;
        this.kpiScoreFactory = kpiScoreFactory;
    }

    public ConnectionOverviewInfo asInfo() {
        ConnectionTaskOverview taskStatusOverview = dashboardService.getConnectionTaskOverview();
        return getConnectionOverviewInfo(taskStatusOverview);
    }

    public ConnectionOverviewInfo asInfo(EndDeviceGroup endDeviceGroup) {
        ConnectionTaskOverview taskStatusOverview = dashboardService.getConnectionTaskOverview(endDeviceGroup);
        ConnectionOverviewInfo info = getConnectionOverviewInfo(taskStatusOverview);
        addKpiInfo(endDeviceGroup, info);
        info.deviceGroup = new DeviceGroupFilterInfo(endDeviceGroup.getId(), endDeviceGroup.getName());
        return info;
    }

    public ConnectionOverviewInfo asWidgetInfo(EndDeviceGroup endDeviceGroup){
        TaskStatusOverview taskStatusOverview = dashboardService.getConnectionTaskStatusOverview(endDeviceGroup);
        ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview = dashboardService.getComSessionSuccessIndicatorOverview(endDeviceGroup);
        SummaryData summaryData = new SummaryData(taskStatusOverview, comSessionSuccessIndicatorOverview.getAtLeastOneTaskFailedCount());
        ConnectionOverviewInfo info = new ConnectionOverviewInfo();
        info.connectionSummary = summaryInfoFactory.from(summaryData);
        addKpiInfo(endDeviceGroup, info);
        info.deviceGroup = new DeviceGroupFilterInfo(endDeviceGroup.getId(), endDeviceGroup.getName());
        return info;
    }

    private void addKpiInfo(EndDeviceGroup endDeviceGroup, ConnectionOverviewInfo info) {
        Optional<DataCollectionKpi> dataCollectionKpiOptional = dataCollectionKpiService.findDataCollectionKpi(endDeviceGroup);
        if (dataCollectionKpiOptional.isPresent() && dataCollectionKpiOptional.get().calculatesConnectionSetupKpi()) {
            TemporalAmount frequency = dataCollectionKpiOptional.get().connectionSetupKpiCalculationIntervalLength().get();
            Range<Instant> rangeByDisplayRange = kpiScoreFactory.getActualRangeByDisplayRange(dataCollectionKpiOptional.get().getDisplayRange().asTemporalAmount());
            List<DataCollectionKpiScore> kpiScores = dataCollectionKpiOptional.get().getConnectionSetupKpiScores(rangeByDisplayRange);
            if (!kpiScores.isEmpty()) {
                BigDecimal currentTarget = kpiScores.get(kpiScores.size() - 1).getTarget();
                if (currentTarget != null) {
                    info.connectionSummary.target = currentTarget.longValue();
                }
            }
            info.kpi = kpiScoreFactory.getKpiAsInfo(frequency, kpiScores, rangeByDisplayRange);
        }
    }

    public ConnectionOverviewInfo asWidgetInfo(){
        ConnectionOverviewInfo info = new ConnectionOverviewInfo();
        TaskStatusOverview taskStatusOverview = dashboardService.getConnectionTaskStatusOverview();
        ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview = dashboardService.getComSessionSuccessIndicatorOverview();
        SummaryData summaryData = new SummaryData(taskStatusOverview, comSessionSuccessIndicatorOverview.getAtLeastOneTaskFailedCount());
        info.connectionSummary = summaryInfoFactory.from(summaryData);
        return info;
    }

    private ConnectionOverviewInfo getConnectionOverviewInfo(ConnectionTaskOverview connectionTaskOverview) {
        ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview = connectionTaskOverview.getComSessionSuccessIndicatorOverview();
        ComPortPoolBreakdown comPortPoolBreakdown = connectionTaskOverview.getComPortPoolBreakdown();
        ConnectionTypeBreakdown connectionTypeBreakdown = connectionTaskOverview.getConnectionTypeBreakdown();
        DeviceTypeBreakdown deviceTypeBreakdown = connectionTaskOverview.getDeviceTypeBreakdown();
        SummaryData summaryData = new SummaryData(connectionTaskOverview.getTaskStatusOverview(), comSessionSuccessIndicatorOverview.getAtLeastOneTaskFailedCount());

        ConnectionOverviewInfo info = new ConnectionOverviewInfo();
        info.connectionSummary = summaryInfoFactory.from(summaryData);

        info.overviews=new ArrayList<>(2);
        info.overviews.add(
                overviewFactory.createOverview(
                        TranslationKeys.PER_CURRENT_STATE,
                        connectionTaskOverview.getTaskStatusOverview(),
                        FilterOption.currentStates,
                        TaskStatusTranslationKeys::translationFor)); // JP-4278
        TaskSummaryInfo perLatestResultOverview =
                overviewFactory.createOverview(
                        TranslationKeys.PER_LATEST_RESULT,
                        comSessionSuccessIndicatorOverview,
                        FilterOption.latestResults,
                        ComSessionSuccessIndicatorTranslationKeys::translationFor);
        addAtLeastOntTaskFailedCounter(comSessionSuccessIndicatorOverview, perLatestResultOverview); // JP-5868
        info.overviews.add(perLatestResultOverview); // JP-4280
        overviewFactory.sortAllOverviews(info.overviews);

        info.breakdowns=new ArrayList<>(3);
        info.breakdowns.add(breakdownFactory.createBreakdown(thesaurus, TranslationKeys.PER_COMMUNICATION_POOL, comPortPoolBreakdown, FilterOption.comPortPools)); // JP-4281
        info.breakdowns.add(breakdownFactory.createBreakdown(thesaurus, TranslationKeys.PER_CONNECTION_TYPE, connectionTypeBreakdown, FilterOption.connectionTypes)); // JP-4283
        info.breakdowns.add(breakdownFactory.createBreakdown(thesaurus, TranslationKeys.PER_DEVICE_TYPE, deviceTypeBreakdown, FilterOption.deviceTypes)); // JP-4284
        breakdownFactory.sortAllBreakdowns(info.breakdowns);

        return info;
    }

    private void addAtLeastOntTaskFailedCounter(ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview, TaskSummaryInfo perLatestResultOverview) {
        TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
        taskCounterInfo.id = null; // Not filterable
        taskCounterInfo.displayName = thesaurus.getFormat(TranslationKeys.SUCCESS_WITH_FAILED_TASKS).format();
        taskCounterInfo.count = comSessionSuccessIndicatorOverview.getAtLeastOneTaskFailedCount();
        perLatestResultOverview.total += taskCounterInfo.count;
        perLatestResultOverview.counters.add(1, taskCounterInfo);
    }

}


