package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.ComScheduleBreakdown;
import com.energyict.mdc.dashboard.ComTaskBreakdown;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.DashboardCounters;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounter;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounters;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * This JSON representation holds the entire communication overview
 * @link http://confluence.eict.vpdc/display/JUP/Communications
 */
public class CommunicationOverviewInfo {

    public static final Comparator<TaskCounterInfo> TASK_COUNTER_INFO_COMPARATOR = new Comparator<TaskCounterInfo>() {
        @Override
        public int compare(TaskCounterInfo o1, TaskCounterInfo o2) {
            return -Long.valueOf(o1.count).compareTo(o2.count);
        }
    };

    public static final Comparator<TaskBreakdownInfo> TASK_BREAKDOWN_INFO_COMPARATOR = new Comparator<TaskBreakdownInfo>() {
        @Override
        public int compare(TaskBreakdownInfo o1, TaskBreakdownInfo o2) {
            return -Long.valueOf(o1.failedCount).compareTo(o2.failedCount);
        }
    };

    @JsonIgnore
    private Thesaurus thesaurus;
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
            Thesaurus thesaurus) throws Exception {
        this.thesaurus = thesaurus;


        overviews=new ArrayList<>(2);
        overviews.add(createOverview(thesaurus.getString(MessageSeeds.PER_CURRENT_STATE.getKey(), MessageSeeds.PER_CURRENT_STATE.getDefaultFormat()), taskStatusOverview, FilterOption.state, taskStatusAdapter));
        overviews.add(createOverview(thesaurus.getString(MessageSeeds.PER_LATEST_RESULT.getKey(), MessageSeeds.PER_LATEST_RESULT.getDefaultFormat()), comSessionSuccessIndicatorOverview, FilterOption.latestResult, completionCodeAdapter));

        breakdowns=new ArrayList<>(3);
        breakdowns.add(createBreakdown(thesaurus.getString(MessageSeeds.PER_COMMUNICATION_SCHEDULE.getKey(), MessageSeeds.PER_COMMUNICATION_SCHEDULE.getDefaultFormat()), comScheduleBreakdown, BreakdownOption.comSchedule));
        breakdowns.add(createBreakdown(thesaurus.getString(MessageSeeds.PER_COMMUNICATION_TASK.getKey(), MessageSeeds.PER_COMMUNICATION_TASK.getDefaultFormat()), comTaskBreakdown, BreakdownOption.comTask));
        breakdowns.add(createBreakdown(thesaurus.getString(MessageSeeds.PER_DEVICE_TYPE.getKey(), MessageSeeds.PER_DEVICE_TYPE.getDefaultFormat()), deviceTypeBreakdown, BreakdownOption.deviceType));
        sortAllOverviews();
    }


    private void sortAllOverviews() {
        for (TaskSummaryInfo overview : overviews) {
            Collections.sort(overview.counters, TASK_COUNTER_INFO_COMPARATOR);
        }
    }

    private <C extends HasName & HasId> BreakdownSummaryInfo createBreakdown(String name, TaskStatusBreakdownCounters<C> breakdownCounters, BreakdownOption alias) {
        BreakdownSummaryInfo info = new BreakdownSummaryInfo();
        info.displayName=name;
        info.alias=alias;
        info.total=breakdownCounters.getTotalCount();
        info.totalSuccessCount=breakdownCounters.getTotalSuccessCount();
        info.totalPendingCount=breakdownCounters.getTotalPendingCount();
        info.totalFailedCount =breakdownCounters.getTotalFailedCount();
        info.counters=new ArrayList<>();
        for (TaskStatusBreakdownCounter<C> counter : breakdownCounters) {
            TaskBreakdownInfo taskBreakdownInfo = new TaskBreakdownInfo();
            taskBreakdownInfo.id=counter.getCountTarget().getId();
            taskBreakdownInfo.displayName=counter.getCountTarget().getName();
            taskBreakdownInfo.successCount=counter.getSuccessCount();
            taskBreakdownInfo.pendingCount=counter.getPendingCount();
            taskBreakdownInfo.failedCount=counter.getFailedCount();
            info.counters.add(taskBreakdownInfo);
        }

        return info;
    }

    private <C> TaskSummaryInfo createOverview(String overviewBreakdownName, DashboardCounters<C> dashboardCounters, FilterOption alias, MapBasedXmlAdapter<C> adapter) throws Exception {
        TaskSummaryInfo info = new TaskSummaryInfo();
        info.displayName =overviewBreakdownName;
        info.alias=alias;
        info.counters=new ArrayList<>();
        for (Counter<C> taskStatusCounter : dashboardCounters) {
            TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
            taskCounterInfo.id=adapter.marshal(taskStatusCounter.getCountTarget());
            taskCounterInfo.displayName=thesaurus.getString(adapter.marshal(taskStatusCounter.getCountTarget()), adapter.marshal(taskStatusCounter.getCountTarget()));
            taskCounterInfo.count=taskStatusCounter.getCount();
            info.counters.add(taskCounterInfo);
        }

        return info;
    }
}

