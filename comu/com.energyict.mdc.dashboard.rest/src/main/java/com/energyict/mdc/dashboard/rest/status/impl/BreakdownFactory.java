package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounter;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by bvn on 8/29/14.
 */
public class BreakdownFactory {

    public static final Comparator<TaskBreakdownInfo> TASK_BREAKDOWN_INFO_COMPARATOR = new Comparator<TaskBreakdownInfo>() {
        @Override
        public int compare(TaskBreakdownInfo o1, TaskBreakdownInfo o2) {
            return -Long.valueOf(o1.failedCount).compareTo(o2.failedCount);
        }
    };

    public <C extends HasName & HasId> BreakdownSummaryInfo createBreakdown(String name, TaskStatusBreakdownCounters<C> breakdownCounters, BreakdownOption alias) {
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

    public void sortAllBreakdowns(List<BreakdownSummaryInfo> breakdowns) {
        for (BreakdownSummaryInfo breakdown : breakdowns) {
            Collections.sort(breakdown.counters, TASK_BREAKDOWN_INFO_COMPARATOR);
        }
    }



}
