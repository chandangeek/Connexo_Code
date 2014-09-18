package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.ComScheduleBreakdown;
import com.energyict.mdc.dashboard.ComTaskBreakdown;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * This JSON representation holds the entire communication overview
 * @link http://confluence.eict.vpdc/display/JUP/Communications
 */
public class CommunicationOverviewInfo {

    @JsonIgnore
    private static final TaskStatusAdapter taskStatusAdapter = new TaskStatusAdapter();
    @JsonIgnore
    private static final CompletionCodeAdapter completionCodeAdapter = new CompletionCodeAdapter();

    public SummaryInfo communicationSummary;
    public List<TaskSummaryInfo> overviews;
    public List<BreakdownSummaryInfo> breakdowns;

    public CommunicationOverviewInfo() {
    }

    public CommunicationOverviewInfo(
            SummaryData summaryData,
            TaskStatusOverview taskStatusOverview,
            ComCommandCompletionCodeOverview comSessionSuccessIndicatorOverview,
            ComScheduleBreakdown comScheduleBreakdown,
            ComTaskBreakdown comTaskBreakdown,
            DeviceTypeBreakdown deviceTypeBreakdown,
            BreakdownFactory breakdownFactory,
            OverviewFactory overviewFactory,
            Thesaurus thesaurus) {

        this.communicationSummary= SummaryInfo.from(summaryData, thesaurus);

        this.overviews=new ArrayList<>(2);
        this.overviews.add(overviewFactory.createOverview(thesaurus.getString(MessageSeeds.PER_CURRENT_STATE.getKey(), MessageSeeds.PER_CURRENT_STATE.getDefaultFormat()), taskStatusOverview, FilterOption.currentStates, taskStatusAdapter));
        this.overviews.add(overviewFactory.createOverview(thesaurus.getString(MessageSeeds.PER_LATEST_RESULT.getKey(), MessageSeeds.PER_LATEST_RESULT.getDefaultFormat()), comSessionSuccessIndicatorOverview, FilterOption.latestResults, completionCodeAdapter));
        overviewFactory.sortAllOverviews(this.overviews);

        this.breakdowns=new ArrayList<>(3);
        this.breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_COMMUNICATION_SCHEDULE.getKey(), MessageSeeds.PER_COMMUNICATION_SCHEDULE.getDefaultFormat()), comScheduleBreakdown, FilterOption.comSchedules));
        this.breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_COMMUNICATION_TASK.getKey(), MessageSeeds.PER_COMMUNICATION_TASK.getDefaultFormat()), comTaskBreakdown, FilterOption.comTasks));
        this.breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_DEVICE_TYPE.getKey(), MessageSeeds.PER_DEVICE_TYPE.getDefaultFormat()), deviceTypeBreakdown, FilterOption.deviceTypes));
        breakdownFactory.sortAllBreakdowns(this.breakdowns);
    }


}

