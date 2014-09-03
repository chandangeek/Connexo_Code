package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import java.util.ArrayList;
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

    public ConnectionOverviewInfo() {
    }

    public <H extends HasName & HasId> ConnectionOverviewInfo(
            ConnectionSummaryData connectionSummaryData,
            TaskStatusOverview taskStatusOverview,
            ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview,
            ComPortPoolBreakdown comPortPoolBreakdown,
            ConnectionTypeBreakdown connectionTypeBreakdown,
            DeviceTypeBreakdown deviceTypeBreakdown,
            BreakdownFactory breakdownFactory,
            OverviewFactory overviewFactory,
            Thesaurus thesaurus) throws Exception {
        this.thesaurus = thesaurus;

        connectionSummary = ConnectionSummaryInfo.from(connectionSummaryData, thesaurus);

        overviews=new ArrayList<>(2);
        overviews.add(overviewFactory.createOverview(thesaurus.getString(MessageSeeds.PER_CURRENT_STATE.getKey(), MessageSeeds.PER_CURRENT_STATE.getDefaultFormat()), taskStatusOverview, FilterOption.state, taskStatusAdapter)); // JP-4278
        overviews.add(overviewFactory.createOverview(thesaurus.getString(MessageSeeds.PER_LATEST_RESULT.getKey(), MessageSeeds.PER_LATEST_RESULT.getDefaultFormat()), comSessionSuccessIndicatorOverview, FilterOption.latestResult, successIndicatorAdapter)); // JP-4280
        overviewFactory.sortAllOverviews(overviews);

        breakdowns=new ArrayList<>(3);
        breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_COMMUNICATION_POOL.getKey(), MessageSeeds.PER_COMMUNICATION_POOL.getDefaultFormat()), comPortPoolBreakdown, BreakdownOption.comPortPool)); // JP-4281
        breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_CONNECTION_TYPE.getKey(), MessageSeeds.PER_CONNECTION_TYPE.getDefaultFormat()), connectionTypeBreakdown, BreakdownOption.connectionType)); // JP-4283
        breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_DEVICE_TYPE.getKey(), MessageSeeds.PER_DEVICE_TYPE.getDefaultFormat()), deviceTypeBreakdown, BreakdownOption.deviceType)); // JP-4284
        breakdownFactory.sortAllBreakdowns(breakdowns);
    }
}

class HeatMapRowInfo {
    public String displayValue;
    public BreakdownOption alias;
    public Long id;
    public List<TaskCounterInfo> data;
}

class TaskSummaryInfo {
    public long total;
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
    comPortPool,
    comTask, comSchedule
}
