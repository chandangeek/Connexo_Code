package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON representation of connection summary
 * JP-4278
 * Created by bvn on 7/29/14.
 */
public class ConnectionSummaryInfo {
    private final static TaskStatusAdapter TASK_STATUS_ADAPTER = new TaskStatusAdapter();

    public long total;
    public FilterOption alias=FilterOption.state;
    public List<? super TaskCounterInfo> counters = new ArrayList<>();

    public static ConnectionSummaryInfo from(ConnectionSummaryData connectionSummaryData, Thesaurus thesaurus) throws Exception {
        ConnectionSummaryInfo info = new ConnectionSummaryInfo();
        SubTaskCounterInfo successfulConnections;
        TaskCounterInfo pendingConnections;
        TaskCounterInfo failedConnections;
        TaskCounterInfo connectionsWithAllTasksSuccessful;
        TaskCounterInfo connectionsWithFailingTasks;

        info.total =connectionSummaryData.getTotal();

        successfulConnections=new SubTaskCounterInfo();
        successfulConnections.count=connectionSummaryData.getSuccess();
        successfulConnections.id="TBD"; // TODO
        successfulConnections.displayName=thesaurus.getString(MessageSeeds.SUCCESS.getKey(),"Success");
        info.counters.add(successfulConnections);

        connectionsWithAllTasksSuccessful=new TaskCounterInfo();
        connectionsWithAllTasksSuccessful.count=connectionSummaryData.getAllTasksSuccessful();
        connectionsWithAllTasksSuccessful.id="TBD"; // TODO
        connectionsWithAllTasksSuccessful.displayName=thesaurus.getString(MessageSeeds.ALL_TASKS_SUCCESSFUL.getKey(),"All tasks successful");
        successfulConnections.counters.add(connectionsWithAllTasksSuccessful);

        connectionsWithFailingTasks=new TaskCounterInfo();
        connectionsWithFailingTasks.count=connectionSummaryData.getAtLeastOneTaskFailed();
        connectionsWithFailingTasks.id="TBD"; // TODO
        connectionsWithFailingTasks.displayName=thesaurus.getString(MessageSeeds.AT_LEAST_ONE_FAILED.getKey(),"At least one task failed");
        successfulConnections.counters.add(connectionsWithFailingTasks);

        pendingConnections=new TaskCounterInfo();
        pendingConnections.count=connectionSummaryData.getPending();
        pendingConnections.id= TASK_STATUS_ADAPTER.marshal(TaskStatus.Pending)+","+TASK_STATUS_ADAPTER.marshal(TaskStatus.Busy)+","+TASK_STATUS_ADAPTER.marshal(TaskStatus.Retrying);
        pendingConnections.displayName=thesaurus.getString(MessageSeeds.PENDING.getKey(), "Pending");
        info.counters.add(pendingConnections);

        failedConnections=new TaskCounterInfo();
        failedConnections.count=connectionSummaryData.getFailed();
        failedConnections.id=TASK_STATUS_ADAPTER.marshal(TaskStatus.Failed)+","+TASK_STATUS_ADAPTER.marshal(TaskStatus.NeverCompleted);
        failedConnections.displayName=thesaurus.getString(MessageSeeds.FAILED.getKey(), "Failed");
        info.counters.add(failedConnections);
        return info;
    }
}

class SubTaskCounterInfo extends TaskCounterInfo {
    public List<TaskCounterInfo> counters = new ArrayList<>();
}
