package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bvn on 7/31/14.
 */
public class SummaryData {
    private long success;
    private Long allTasksSuccessful;
    private Long atLeastOneTaskFailed;
    private long pending;
    private long failed;
    private long total;

    public SummaryData(TaskStatusOverview taskStatusOverview) {
        Map<TaskStatus, Long> counts = getTaskStatusCountsAsMap(taskStatusOverview);

        failed=counts.get(TaskStatus.Failed)+counts.get(TaskStatus.NeverCompleted);
        pending=counts.get(TaskStatus.Pending)+counts.get(TaskStatus.Busy)+counts.get(TaskStatus.Retrying);
        total=counts.get(TaskStatus.Retrying)+counts.get(TaskStatus.Busy)+counts.get(TaskStatus.Pending)+counts.get(TaskStatus.Failed)+counts.get(TaskStatus.NeverCompleted)+counts.get(TaskStatus.Waiting);
        success=counts.get(TaskStatus.Waiting);
    }

    public SummaryData(TaskStatusOverview taskStatusOverview, long atLeastOneTaskFailed) {
        this(taskStatusOverview);
        this.atLeastOneTaskFailed =atLeastOneTaskFailed;
        allTasksSuccessful=success - this.atLeastOneTaskFailed;
    }

    private Map<TaskStatus, Long> getTaskStatusCountsAsMap(TaskStatusOverview taskStatusOverview) {
        Map<TaskStatus, Long> counts = new HashMap<>();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            counts.put(taskStatus, 0L);
        }

        for (Counter<TaskStatus> taskStatusCounter : taskStatusOverview) {
            long taskStatusCount = taskStatusCounter.getCount();
            counts.put(taskStatusCounter.getCountTarget(), taskStatusCount);
            total += taskStatusCount;
        }
        return counts;
    }

    public long getSuccess() {
        return success;
    }

    public Long getAllTasksSuccessful() {
        return allTasksSuccessful;
    }

    public Long getAtLeastOneTaskFailed() {
        return atLeastOneTaskFailed;
    }

    public long getPending() {
        return pending;
    }

    public long getFailed() {
        return failed;
    }

    public long getTotal() {
        return total;
    }
}
