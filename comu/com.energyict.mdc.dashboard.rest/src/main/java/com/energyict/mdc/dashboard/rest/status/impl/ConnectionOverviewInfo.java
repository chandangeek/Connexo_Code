package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.dashboard.ConnectionStatusOverview;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.DashboardCounters;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounter;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounters;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * This JSON representation holds the entire connection overview
 * @link http://confluence.eict.vpdc/display/JUP/Connections
 * Created by bvn on 7/29/14.
 */
public class ConnectionOverviewInfo {

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
    private final static TaskStatusAdapter taskStatusAdapter = new TaskStatusAdapter();
    @JsonIgnore
    private final static CompletionCodeAdapter completionCodeAdapter = new CompletionCodeAdapter();

    public ConnectionSummaryInfo connectionSummary;

    public List<TaskSummaryInfo> overviews;
    public List<BreakdownSummaryInfo> breakdowns;

    public ConnectionOverviewInfo() {
    }

    public ConnectionOverviewInfo(DashboardService dashboardService, Thesaurus thesaurus) throws Exception {
        this.thesaurus = thesaurus;
        overviews=new ArrayList<>(2);
        ConnectionStatusOverview connectionStatusOverview = dashboardService.getConnectionStatusOverview();
        overviews.add(createOverview(thesaurus.getString(MessageSeeds.PER_CURRENT_STATE.getKey(), null), connectionStatusOverview, FilterOption.state, taskStatusAdapter)); // JP-4278
        overviews.add(createOverview(thesaurus.getString(MessageSeeds.PER_LATEST_RESULT.getKey(), null), dashboardService.getComTaskCompletionOverview(), FilterOption.latestResult, completionCodeAdapter)); // JP-4280

        breakdowns=new ArrayList<>(3);
        breakdowns.add(createBreakdown(thesaurus.getString(MessageSeeds.PER_COMMUNICATION_POOL.getKey(), null), dashboardService.getComPortPoolBreakdown(), FilterOption.comPortPool)); // JP-4281
        breakdowns.add(createBreakdown(thesaurus.getString(MessageSeeds.PER_CONNECTION_TYPE.getKey(), null), dashboardService.getConnectionTypeBreakdown(), FilterOption.connectionType)); // JP-4283
        breakdowns.add(createBreakdown(thesaurus.getString(MessageSeeds.PER_CONNECTION_TYPE.getKey(), null), dashboardService.getDeviceTypeBreakdown(), FilterOption.deviceType)); // JP-4284

        sortAllOverviews();
        sortAllBreakdowns();

        completeConnectionSummary(connectionStatusOverview);

    }

    private void completeConnectionSummary(ConnectionStatusOverview connectionStatusOverview) {
        Map<TaskStatus, Long> counts = getTaskStatusCountsAsMap(connectionStatusOverview);

        connectionSummary = new ConnectionSummaryInfo();
        connectionSummary.failedConnections=counts.get(TaskStatus.Failed)+counts.get(TaskStatus.NeverCompleted);
        connectionSummary.pendingConnections=counts.get(TaskStatus.Pending)+counts.get(TaskStatus.Busy)+counts.get(TaskStatus.Retrying);
    }

    private Map<TaskStatus, Long> getTaskStatusCountsAsMap(ConnectionStatusOverview connectionStatusOverview) {
        Map<TaskStatus, Long> counts = new HashMap<>();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            counts.put(taskStatus, 0L);
        }

        for (Counter<TaskStatus> taskStatusCounter : connectionStatusOverview) {
            counts.put(taskStatusCounter.getCountTarget(), taskStatusCounter.getCount());
        }
        return counts;
    }

    private void sortAllBreakdowns() {
        for (BreakdownSummaryInfo breakdown : breakdowns) {
            Collections.sort(breakdown.counters, TASK_BREAKDOWN_INFO_COMPARATOR);
        }
    }

    private void sortAllOverviews() {
        for (TaskSummaryInfo overview : overviews) {
            Collections.sort(overview.counters, TASK_COUNTER_INFO_COMPARATOR);
        }
    }

    private <C extends HasName & HasId> BreakdownSummaryInfo createBreakdown(String name, TaskStatusBreakdownCounters<C> breakdownCounters, FilterOption alias) {
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
            taskCounterInfo.displayName=thesaurus.getString(adapter.marshal(taskStatusCounter.getCountTarget()), null);
            taskCounterInfo.count=taskStatusCounter.getCount();
            info.counters.add(taskCounterInfo);
        }

        return info;
    }
}

class TaskSummaryInfo {
    public String displayName;
    public FilterOption alias;
    public List<TaskCounterInfo> counters;
}

class BreakdownSummaryInfo {
    public String displayName;
    public FilterOption alias;
    public long total;
    public long totalSuccessCount;
    public long totalPendingCount;
    public long totalFailedCount;
    public List<TaskBreakdownInfo> counters;
}

class TaskBreakdownInfo {
    public String displayName;
    public long id;
    public long successCount;
    public long failedCount;
    public long pendingCount;
}

class TaskCounterInfo {
    public String id;
    public String displayName;
    public long count;
}

enum FilterOption {
    state,
    comPortPool,
    connectionType,
    deviceType,
    latestResult
}
