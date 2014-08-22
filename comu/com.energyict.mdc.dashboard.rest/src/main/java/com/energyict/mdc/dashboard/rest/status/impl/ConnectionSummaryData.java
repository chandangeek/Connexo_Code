package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bvn on 7/31/14.
 */
public class ConnectionSummaryData {
    private long success;
    private long allTasksSuccessful;
    private long atLeastOneTaskFailed;
    private long pending;
    private long failed;
    private long total;

    public ConnectionSummaryData(TaskStatusOverview taskStatusOverview) {
        Map<TaskStatus, Long> counts = getTaskStatusCountsAsMap(taskStatusOverview);

        failed=counts.get(TaskStatus.Failed)+counts.get(TaskStatus.NeverCompleted);
        pending=counts.get(TaskStatus.Pending)+counts.get(TaskStatus.Busy)+counts.get(TaskStatus.Retrying);
    }

    private Map<TaskStatus, Long> getTaskStatusCountsAsMap(TaskStatusOverview taskStatusOverview) {
        Map<TaskStatus, Long> counts = new HashMap<>();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            counts.put(taskStatus, 0L);
        }

        for (Counter<TaskStatus> taskStatusCounter : taskStatusOverview) {
            counts.put(taskStatusCounter.getCountTarget(), taskStatusCounter.getCount());
        }
        return counts;
    }

    public long getSuccess() {
        return success;
    }

    public long getAllTasksSuccessful() {
        return allTasksSuccessful;
    }

    public long getAtLeastOneTaskFailed() {
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
