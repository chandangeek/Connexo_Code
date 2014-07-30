package com.energyict.mdc.dashboard.rest.status;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.DashboardCounters;
import com.energyict.mdc.dashboard.DashboardService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * This JSON representation holds the entire connection over
 * @link http://confluence.eict.vpdc/display/JUP/Connections
 * Created by bvn on 7/29/14.
 */
public class ConnectionOverviewInfo {

    @JsonIgnore
    private final Thesaurus thesaurus;
    @JsonIgnore
    private final static TaskStatusAdapter taskStatusAdapter = new TaskStatusAdapter();
    @JsonIgnore
    private final static CompletionCodeAdapter completionCodeAdapter = new CompletionCodeAdapter();

    public ConnectionSummaryInfo connectionSummary;

    public List<TaskSummaryInfo> overviews;
    public List<TaskBreakdownInfo> breakdowns;

    public Map<String, Map<String, Integer>> breakdownPerCommunicationPortPool; // JP-4281
    public Map<String, Map<String, Integer>> breakdownPerConnectionType; // JP-4283
    public Map<String, Map<String, Integer>> breakdownPerDeviceType; // JP-4284

    public ConnectionOverviewInfo(DashboardService dashboardService, Thesaurus thesaurus) throws Exception {
        this.thesaurus = thesaurus;
        overviews=new ArrayList<>(2);
        overviews.add(createOverview(thesaurus.getString(MessageSeeds.PER_CURRENT_STATE.getKey(), null), dashboardService.getConnectionStatusOverview(), FilterOption.state, taskStatusAdapter)); // JP-4278
        overviews.add(createOverview(thesaurus.getString(MessageSeeds.PER_LATEST_RESULT.getKey(), null), dashboardService.getComTaskCompletionOverview(), FilterOption.latestResult, completionCodeAdapter)); // JP-4280

        breakdowns=new ArrayList<>(3);
    }

    private <C> TaskSummaryInfo createOverview(String overviewBreakdownName, DashboardCounters<C> dashboardCounters, FilterOption alias, MapBasedXmlAdapter<C> adapter) throws Exception {
        TaskSummaryInfo info = new TaskSummaryInfo();
        info.displayName =overviewBreakdownName;
        info.alias=alias;
        info.counters=new ArrayList<>();
        for (Counter<C> taskStatusCounter : dashboardCounters) {
            info.counters.add(new TaskCounterInfo(
                    thesaurus.getString(adapter.marshal(taskStatusCounter.getCountTarget()), null),
                    adapter.marshal(taskStatusCounter.getCountTarget()),
                    taskStatusCounter.getCount()));
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
    public String name;
    public long total;
    public List<TaskBreakdownInfo> counters;
}

class TaskBreakdownInfo {
    public String name;
    public long id;
    public long successCount;
    public long failureCount;
    public long pendingCount;
}

class TaskCounterInfo {
    public String alias;
    public String displayName;
    public long count;

    TaskCounterInfo(String displayName, String alias, long count) {
        this.displayName = displayName;
        this.alias = alias;
        this.count = count;
    }
}

enum FilterOption {
    state,
    latestResult
}
