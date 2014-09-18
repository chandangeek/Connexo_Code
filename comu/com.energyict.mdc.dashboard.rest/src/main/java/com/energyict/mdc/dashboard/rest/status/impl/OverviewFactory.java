package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.DashboardCounters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by bvn on 8/29/14.
 */
public class OverviewFactory {

    private final Thesaurus thesaurus;

    @Inject
    public OverviewFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public <C> TaskSummaryInfo createOverview(String overviewBreakdownName, DashboardCounters<C> dashboardCounters, FilterOption alias, MapBasedXmlAdapter<C> adapter) {
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
        overviews.stream().forEach(overview->Collections.sort(overview.counters, (o1, o2) -> -Long.valueOf(o1.count).compareTo(o2.count)));
    }
}
