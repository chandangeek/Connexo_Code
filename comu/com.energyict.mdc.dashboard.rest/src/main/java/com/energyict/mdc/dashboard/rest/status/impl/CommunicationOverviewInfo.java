package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;
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

//    public ConnectionSummaryInfo connectionSummary;

    public List<TaskSummaryInfo> overviews;
    public List<BreakdownSummaryInfo> breakdowns;

    public CommunicationOverviewInfo() {
    }

    public <H extends HasName & HasId> CommunicationOverviewInfo(
            ConnectionSummaryData connectionSummaryData,
            TaskStatusOverview taskStatusOverview,
            ComCommandCompletionCodeOverview comSessionSuccessIndicatorOverview,
            ComScheduleBreakdown comScheduleBreakdown,
            ComTaskBreakdown comTaskBreakdown,
            DeviceTypeBreakdown deviceTypeBreakdown,
            BreakdownFactory breakdownFactory,
            OverviewFactory overviewFactory,
            Thesaurus thesaurus) throws Exception {


        overviews=new ArrayList<>(2);
        overviews.add(overviewFactory.createOverview(thesaurus.getString(MessageSeeds.PER_CURRENT_STATE.getKey(), MessageSeeds.PER_CURRENT_STATE.getDefaultFormat()), taskStatusOverview, FilterOption.state, taskStatusAdapter));
        overviews.add(overviewFactory.createOverview(thesaurus.getString(MessageSeeds.PER_LATEST_RESULT.getKey(), MessageSeeds.PER_LATEST_RESULT.getDefaultFormat()), comSessionSuccessIndicatorOverview, FilterOption.latestResult, completionCodeAdapter));
        overviewFactory.sortAllOverviews(overviews);

        breakdowns=new ArrayList<>(3);
        breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_COMMUNICATION_SCHEDULE.getKey(), MessageSeeds.PER_COMMUNICATION_SCHEDULE.getDefaultFormat()), comScheduleBreakdown, BreakdownOption.comSchedule));
        breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_COMMUNICATION_TASK.getKey(), MessageSeeds.PER_COMMUNICATION_TASK.getDefaultFormat()), comTaskBreakdown, BreakdownOption.comTask));
        breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_DEVICE_TYPE.getKey(), MessageSeeds.PER_DEVICE_TYPE.getDefaultFormat()), deviceTypeBreakdown, BreakdownOption.deviceType));
        breakdownFactory.sortAllBreakdowns(breakdowns);
    }


}

