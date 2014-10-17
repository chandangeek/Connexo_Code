package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.CompletionCodeInfo;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.tasks.ComTask;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by bvn on 9/1/14.
 */
public class ComTaskExecutionInfoFactory {

    private final Thesaurus thesaurus;
    private final CommunicationTaskService communicationTaskService;
    private final Provider<ConnectionTaskInfoFactory> connectionTaskInfoFactory;

    @Inject
    public ComTaskExecutionInfoFactory(Thesaurus thesaurus, CommunicationTaskService communicationTaskService, Provider<ConnectionTaskInfoFactory> connectionTaskInfoFactoryProvider) {
        this.thesaurus = thesaurus;
        this.communicationTaskService = communicationTaskService;
        this.connectionTaskInfoFactory = connectionTaskInfoFactoryProvider;
    }

    public ComTaskExecutionInfo from(ComTaskExecution comTaskExecution, Optional<ComTaskExecutionSession> comTaskExecutionSession) throws Exception {
        ComTaskExecutionInfo info = new ComTaskExecutionInfo();
        info.comTasks = new ArrayList<>(comTaskExecution.getComTasks().size());
        for (ComTask comTask : comTaskExecution.getComTasks()) {
            info.comTasks.add(comTask.getName());
        }
        info.name = Joiner.on(" + ").join(info.comTasks);
        Device device = comTaskExecution.getDevice();
        info.device = new IdWithNameInfo(device.getmRID(), device.getName());
        info.deviceConfiguration = new IdWithNameInfo(device.getDeviceConfiguration());
        info.deviceType = new IdWithNameInfo(device.getDeviceType());
        if (comTaskExecution instanceof ScheduledComTaskExecution) {
            ComSchedule comSchedule = ((ScheduledComTaskExecution) comTaskExecution).getComSchedule();
            info.comScheduleName = comSchedule.getName();
            if (comSchedule.getTemporalExpression() != null) {
                info.comScheduleFrequency = TemporalExpressionInfo.from(comSchedule.getTemporalExpression());
            }
        } else {
            if (comTaskExecution instanceof ManuallyScheduledComTaskExecution) {
                Optional<NextExecutionSpecs> nextExecutionSpecs = comTaskExecution.getNextExecutionSpecs();
                info.comScheduleName = thesaurus.getString(MessageSeeds.INDIVIDUAL.getKey(), MessageSeeds.INDIVIDUAL.getKey());
                if (nextExecutionSpecs.isPresent()) {
                    info.comScheduleFrequency = TemporalExpressionInfo.from(nextExecutionSpecs.get().getTemporalExpression());
                }
            }
        }
        info.urgency = comTaskExecution.getExecutionPriority();
        info.currentState = new TaskStatusInfo(comTaskExecution.getStatus(), thesaurus);
        info.latestResult = comTaskExecutionSession.map(ctes -> CompletionCodeInfo.from(ctes.getHighestPriorityCompletionCode(), thesaurus)).orElse(null);
        info.startTime = comTaskExecution.getLastExecutionStartTimestamp();
        info.successfulFinishTime = comTaskExecution.getLastSuccessfulCompletionTimestamp();
        info.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        info.alwaysExecuteOnInbound = comTaskExecution.isIgnoreNextExecutionSpecsForInbound();
        ConnectionTask<?, ?> connectionTask = comTaskExecution.getConnectionTask();
        if (connectionTask != null) {
            Optional<ComSession> comSessionOptional = comTaskExecutionSession.map(ComTaskExecutionSession::getComSession);
            info.connectionTask = connectionTaskInfoFactory.get().from(connectionTask, comSessionOptional);
        }
        return info;
    }

    public List<ComTaskExecutionInfo> from(List<ComTaskExecution> comTaskExecutions) throws Exception {
        List<ComTaskExecutionInfo> comTaskExecutionInfos = new ArrayList<>(comTaskExecutions.size());
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            comTaskExecutionInfos.add(this.from(comTaskExecution, communicationTaskService.findLastSessionFor(comTaskExecution)));
        }
        return comTaskExecutionInfos;
    }

}
