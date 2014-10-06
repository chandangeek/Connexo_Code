package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.ComScheduleBreakdown;
import com.energyict.mdc.dashboard.ComTaskBreakdown;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.data.rest.CompletionCodeAdapter;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import java.util.ArrayList;
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

    @Inject
    public CommunicationOverviewInfoFactory(BreakdownFactory breakdownFactory, OverviewFactory overviewFactory, SummaryInfoFactory summaryInfoFactory, Thesaurus thesaurus) {
        this.breakdownFactory = breakdownFactory;
        this.overviewFactory = overviewFactory;
        this.summaryInfoFactory = summaryInfoFactory;
        this.thesaurus = thesaurus;
    }

    public CommunicationOverviewInfo from(
            SummaryData summaryData,
            TaskStatusOverview taskStatusOverview,
            ComCommandCompletionCodeOverview comSessionSuccessIndicatorOverview,
            ComScheduleBreakdown comScheduleBreakdown,
            ComTaskBreakdown comTaskBreakdown,
            DeviceTypeBreakdown deviceTypeBreakdown) {
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


}

