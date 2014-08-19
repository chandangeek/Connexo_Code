package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionStatusOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.DashboardCounters;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.HeatMap;
import com.energyict.mdc.dashboard.HeatMapRow;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounter;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounters;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
    private static final TaskStatusAdapter taskStatusAdapter = new TaskStatusAdapter();
    @JsonIgnore
    private static final SuccessIndicatorAdapter successIndicatorAdapter = new SuccessIndicatorAdapter();

    public ConnectionSummaryInfo connectionSummary;

    public List<TaskSummaryInfo> overviews;
    public List<BreakdownSummaryInfo> breakdowns;
    public List<HeatMapRowInfo> heatMap;

    public ConnectionOverviewInfo() {
    }

    public <H extends HasName & HasId> ConnectionOverviewInfo(
            ConnectionSummaryData connectionSummaryData,
            ConnectionStatusOverview connectionStatusOverview,
            ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview,
            ComPortPoolBreakdown comPortPoolBreakdown,
            ConnectionTypeBreakdown connectionTypeBreakdown,
            DeviceTypeBreakdown deviceTypeBreakdown,
            HeatMap<H> heatMap,
            BreakdownOption breakdown,
            Thesaurus thesaurus) throws Exception {
        this.thesaurus = thesaurus;

        connectionSummary = ConnectionSummaryInfo.from(connectionSummaryData, thesaurus);

        overviews=new ArrayList<>(2);
        overviews.add(createOverview(thesaurus.getString(MessageSeeds.PER_CURRENT_STATE.getKey(), null), connectionStatusOverview, FilterOption.state, taskStatusAdapter)); // JP-4278
        overviews.add(createOverview(thesaurus.getString(MessageSeeds.PER_LATEST_RESULT.getKey(), null), comSessionSuccessIndicatorOverview, FilterOption.latestResult, successIndicatorAdapter)); // JP-4280

        breakdowns=new ArrayList<>(3);
        breakdowns.add(createBreakdown(thesaurus.getString(MessageSeeds.PER_COMMUNICATION_POOL.getKey(), null), comPortPoolBreakdown, BreakdownOption.comPortPool)); // JP-4281
        breakdowns.add(createBreakdown(thesaurus.getString(MessageSeeds.PER_CONNECTION_TYPE.getKey(), null), connectionTypeBreakdown, BreakdownOption.connectionType)); // JP-4283
        breakdowns.add(createBreakdown(thesaurus.getString(MessageSeeds.PER_DEVICE_TYPE.getKey(), null), deviceTypeBreakdown, BreakdownOption.deviceType)); // JP-4284

        sortAllOverviews();
        sortAllBreakdowns();

        this.heatMap=new ArrayList<>();
        createHeatMap(heatMap, breakdown, thesaurus);

    }

    private <H extends HasName & HasId> void createHeatMap(HeatMap<H> heatMap, BreakdownOption breakdown, Thesaurus thesaurus) throws Exception {
        for (HeatMapRow<H> row : heatMap) {
            HeatMapRowInfo heatMapRowInfo = new HeatMapRowInfo();
            heatMapRowInfo.displayValue = row.getTarget().getName(); // CPP name, device type name, ...
            heatMapRowInfo.id = row.getTarget().getId(); // ID of the object
            heatMapRowInfo.alias = breakdown; // Type of object
            heatMapRowInfo.data = new ArrayList<>();
            for (ComSessionSuccessIndicatorOverview counters : row) {
                for (Counter<ComSession.SuccessIndicator> successIndicatorCounter : counters) {
                    TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
                    taskCounterInfo.id=successIndicatorAdapter.marshal(successIndicatorCounter.getCountTarget());
                    taskCounterInfo.displayName=thesaurus.getString(successIndicatorAdapter.marshal(successIndicatorCounter.getCountTarget()), null);
                    taskCounterInfo.count=successIndicatorCounter.getCount();
                    heatMapRowInfo.data.add(taskCounterInfo);
                }
                TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
                taskCounterInfo.id= MessageSeeds.AT_LEAST_ONE_FAILED.getKey();
                taskCounterInfo.displayName=thesaurus.getString(MessageSeeds.AT_LEAST_ONE_FAILED.getKey(), null);
                taskCounterInfo.count=counters.getAtLeastOneTaskFailedCount();
                heatMapRowInfo.data.add(taskCounterInfo);
            }
            this.heatMap.add(heatMapRowInfo);
        }
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
            taskCounterInfo.displayName=thesaurus.getString(adapter.marshal(taskStatusCounter.getCountTarget()), null);
            taskCounterInfo.count=taskStatusCounter.getCount();
            info.counters.add(taskCounterInfo);
        }

        return info;
    }
}

class HeatMapRowInfo {
    public String displayValue;
    public BreakdownOption alias;
    public Long id;
    public List<TaskCounterInfo> data;
}

class TaskSummaryInfo {
    public String displayName;
    public FilterOption alias;
    public List<TaskCounterInfo> counters;
}

class BreakdownSummaryInfo {
    public String displayName;
    public BreakdownOption alias;
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

enum FilterOption {
    state,
    latestResult
}

enum BreakdownOption {
    connectionType,
    deviceType,
    comPortPool
}
