package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.DashboardCounters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by bvn on 8/29/14.
 */
public class OverviewFactory {

    private final Thesaurus thesaurus;

    public static final Comparator<TaskCounterInfo> TASK_COUNTER_INFO_COMPARATOR = new Comparator<TaskCounterInfo>() {
        @Override
        public int compare(TaskCounterInfo o1, TaskCounterInfo o2) {
            return -Long.valueOf(o1.count).compareTo(o2.count);
        }
    };


    @Inject
    public OverviewFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public <C> TaskSummaryInfo createOverview(String overviewBreakdownName, DashboardCounters<C> dashboardCounters, FilterOption alias, MapBasedXmlAdapter<C> adapter) throws Exception {
        TaskSummaryInfo info = new TaskSummaryInfo();
        info.displayName = overviewBreakdownName;
        info.alias = alias;
        info.counters = new ArrayList<>();
        for (Counter<C> taskStatusCounter : dashboardCounters) {
            TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
            taskCounterInfo.id = adapter.marshal(taskStatusCounter.getCountTarget());
            taskCounterInfo.displayName = thesaurus.getString(adapter.marshal(taskStatusCounter.getCountTarget()), adapter.marshal(taskStatusCounter.getCountTarget()));
            taskCounterInfo.count = taskStatusCounter.getCount();
            info.total += taskCounterInfo.count;
            info.counters.add(taskCounterInfo);
        }

        return info;
    }

    public void sortAllOverviews(List<TaskSummaryInfo> overviews) {
        for (TaskSummaryInfo overview : overviews) {
            Collections.sort(overview.counters, TASK_COUNTER_INFO_COMPARATOR);
        }
    }
}
