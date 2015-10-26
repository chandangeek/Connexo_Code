package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.device.data.tasks.TaskStatus;

import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by bvn on 9/18/14.
 */
public class SummaryInfoFactory {

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

        successfulConnections = new TaskSummaryCounterInfo();
        successfulConnections.count = summaryData.getSuccess();
        Long atLeastOneTaskFailed = summaryData.getSuccessWithFailedTasks();
        successfulConnections.id = asJsonStringList(EnumSet.of(TaskStatus.Waiting));
        successfulConnections.displayName = atLeastOneTaskFailed == null ?
                thesaurus.getFormat(TranslationKeys.SUCCESS).format() : thesaurus.getFormat(TranslationKeys.ALL_TASKS_SUCCESSFUL).format();
        successfulConnections.name = KpiId.Success.name();
        info.counters.add(successfulConnections);

        if (atLeastOneTaskFailed != null) {
            connectionsWithFailingTasks = new TaskSummaryCounterInfo();
            connectionsWithFailingTasks.count = atLeastOneTaskFailed;
            connectionsWithFailingTasks.id = null; // not navigable
            connectionsWithFailingTasks.displayName = thesaurus.getFormat(TranslationKeys.SUCCESS_WITH_FAILED_TASKS).format();
            connectionsWithFailingTasks.name = KpiId.SuccessWithFailedTasks.name();
            info.counters.add(connectionsWithFailingTasks);
        }

        pendingConnections = new TaskSummaryCounterInfo();
        pendingConnections.count = summaryData.getPending();
        pendingConnections.id = asJsonStringList(EnumSet.of(TaskStatus.Pending, TaskStatus.Busy, TaskStatus.Retrying));
        pendingConnections.displayName = thesaurus.getFormat(TranslationKeys.ONGOING).format();
        pendingConnections.name = KpiId.Ongoing.name();
        info.counters.add(pendingConnections);

        failedConnections = new TaskSummaryCounterInfo();
        failedConnections.count = summaryData.getFailed();
        failedConnections.id = asJsonStringList(EnumSet.of(TaskStatus.Failed, TaskStatus.NeverCompleted));
        failedConnections.displayName = thesaurus.getFormat(TaskStatusTranslationKeys.FAILED).format();
        failedConnections.name = KpiId.Failed.name();
        info.counters.add(failedConnections);
        return info;
    }

    private List<String> asJsonStringList(Set<TaskStatus> taskStatuses) {
        return taskStatuses.stream().map(TaskStatus::name).collect(Collectors.<String>toList());
    }

}
