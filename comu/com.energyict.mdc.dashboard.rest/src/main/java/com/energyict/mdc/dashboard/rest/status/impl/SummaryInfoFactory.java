package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * Created by bvn on 9/18/14.
 */
public class SummaryInfoFactory {
    private final static TaskStatusAdapter TASK_STATUS_ADAPTER = new TaskStatusAdapter();

    private final Thesaurus thesaurus;

    @Inject
    public SummaryInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public SummaryInfo from(SummaryData summaryData) {
        SummaryInfo info = new SummaryInfo();
        TaskSummaryCounterInfo successfulConnections;
        TaskSummaryCounterInfo pendingConnections;
        TaskSummaryCounterInfo failedConnections;
        TaskSummaryCounterInfo connectionsWithFailingTasks;

        info.total = summaryData.getTotal();

        successfulConnections=new TaskSummaryCounterInfo();
        successfulConnections.count= summaryData.getSuccess();
        successfulConnections.id= asJsonStringList(EnumSet.of(TaskStatus.Waiting));
        successfulConnections.displayName=thesaurus.getString(MessageSeeds.SUCCESS.getKey(),"Success");
        successfulConnections.name=KpiId.Success.name();
        info.counters.add(successfulConnections);

        Long atLeastOneTaskFailed = summaryData.getSuccessWithFailedTasks();
        if (atLeastOneTaskFailed!=null) {
            connectionsWithFailingTasks = new TaskSummaryCounterInfo();
            connectionsWithFailingTasks.count = atLeastOneTaskFailed;
            connectionsWithFailingTasks.id = null; // not navigable
            connectionsWithFailingTasks.displayName = thesaurus.getString(MessageSeeds.SUCCESS_WITH_FAILED_TASKS.getKey(), "Success with failed tasks");
            connectionsWithFailingTasks.name = KpiId.SuccessWithFailedTasks.name();
            info.counters.add(connectionsWithFailingTasks);
        }

        pendingConnections=new TaskSummaryCounterInfo();
        pendingConnections.count= summaryData.getPending();
        pendingConnections.id= asJsonStringList(EnumSet.of(TaskStatus.Pending, TaskStatus.Busy, TaskStatus.Retrying));
        pendingConnections.displayName=thesaurus.getString(MessageSeeds.ONGOING.getKey(), "Ongoing");
        pendingConnections.name=KpiId.Ongoing.name();
        info.counters.add(pendingConnections);

        failedConnections=new TaskSummaryCounterInfo();
        failedConnections.count= summaryData.getFailed();
        failedConnections.id=asJsonStringList(EnumSet.of(TaskStatus.Failed,TaskStatus.NeverCompleted));
        failedConnections.displayName=thesaurus.getString(MessageSeeds.FAILED.getKey(), "Failed");
        failedConnections.name=KpiId.Failed.name();
        info.counters.add(failedConnections);
        return info;
    }

    private List<String> asJsonStringList(Set<TaskStatus> taskStatuses) {
        return taskStatuses.stream().map(TASK_STATUS_ADAPTER::marshal).collect(Collectors.<String>toList());
    }

}
