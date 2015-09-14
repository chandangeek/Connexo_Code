package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounter;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounters;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.HasName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by bvn on 8/29/14.
 */
public class BreakdownFactory {

    public <C extends HasName & HasId> BreakdownSummaryInfo createBreakdown(Thesaurus thesaurus, TranslationKey translationKey, TaskStatusBreakdownCounters<C> breakdownCounters, FilterOption alias) {
        return this.createBreakdown(
                thesaurus.getFormat(translationKey).format(),
                breakdownCounters,
                alias);
    }

    public <C extends HasName & HasId> BreakdownSummaryInfo createBreakdown(String name, TaskStatusBreakdownCounters<C> breakdownCounters, FilterOption alias) {
        BreakdownSummaryInfo info = new BreakdownSummaryInfo();
        info.alias=alias;
        info.displayName= name;
        info.total= breakdownCounters.getTotalCount();
        info.totalSuccessCount= breakdownCounters.getTotalSuccessCount();
        info.totalPendingCount= breakdownCounters.getTotalPendingCount();
        info.totalFailedCount = breakdownCounters.getTotalFailedCount();
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

    public void sortAllBreakdowns(List<BreakdownSummaryInfo> breakdowns) {
        breakdowns.stream().forEach(breakdown->Collections.sort(breakdown.counters, (o1, o2) -> -Long.valueOf(o1.failedCount).compareTo(o2.failedCount)));
    }

}
