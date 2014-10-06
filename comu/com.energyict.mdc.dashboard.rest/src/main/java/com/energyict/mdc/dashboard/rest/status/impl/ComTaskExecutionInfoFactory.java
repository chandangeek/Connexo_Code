package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.nls.Thesaurus;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by bvn on 9/1/14.
 */
public class ComTaskExecutionInfoFactory {

    private static final Comparator<ComTaskExecution> COM_TASK_EXECUTION_COMPARATOR = new ComTaskExecutionComparator();

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
        info.name= Joiner.on(" + ").join(info.comTasks);
        Device device = comTaskExecution.getDevice();
        info.device=new IdWithNameInfo(device.getmRID(), device.getName());
        info.deviceConfiguration=new IdWithNameInfo(device.getDeviceConfiguration());
        info.deviceType=new IdWithNameInfo(device.getDeviceType());
        if (comTaskExecution instanceof ScheduledComTaskExecution) {
            ComSchedule comSchedule = ((ScheduledComTaskExecution) comTaskExecution).getComSchedule();
            info.comScheduleName=comSchedule.getName();
            if (comSchedule.getTemporalExpression()!=null) {
                info.comScheduleFrequency = TemporalExpressionInfo.from(comSchedule.getTemporalExpression());
            }
        } else {
            if (comTaskExecution instanceof ManuallyScheduledComTaskExecution) {
                Optional<NextExecutionSpecs> nextExecutionSpecs = comTaskExecution.getNextExecutionSpecs();
                info.comScheduleName=thesaurus.getString(MessageSeeds.INDIVIDUAL.getKey(), MessageSeeds.INDIVIDUAL.getKey());
                if (nextExecutionSpecs.isPresent()) {
                    info.comScheduleFrequency = TemporalExpressionInfo.from(nextExecutionSpecs.get().getTemporalExpression());
                }

            }
        }
        info.urgency = comTaskExecution.getExecutionPriority();
        info.currentState=new TaskStatusInfo(comTaskExecution.getStatus(), thesaurus);
        info.latestResult=comTaskExecutionSession.isPresent()?CompletionCodeInfo.from(comTaskExecutionSession.get().getHighestPriorityCompletionCode(), thesaurus):null;
        info.startTime=comTaskExecution.getLastExecutionStartTimestamp();
        info.successfulFinishTime=comTaskExecution.getLastSuccessfulCompletionTimestamp();
        info.nextCommunication=comTaskExecution.getNextExecutionTimestamp();
        info.alwaysExecuteOnInbound = comTaskExecution.isIgnoreNextExecutionSpecsForInbound();

        return info;
    }

    public ComTaskExecutionInfo from(ComTaskExecution comTaskExecution, Optional<ComTaskExecutionSession> comTaskExecutionSession, ConnectionTask<?,?> connectionTask) throws Exception {
        ComTaskExecutionInfo comTaskExecutionInfo = this.from(comTaskExecution, comTaskExecutionSession);
        comTaskExecutionInfo.connectionTask = connectionTaskInfoFactory.get().from(connectionTask, connectionTask.getLastComSession(), Collections.<ComTaskExecution>emptyList());
        return comTaskExecutionInfo;
    }

    public List<ComTaskExecutionInfo> from(List<ComTaskExecution> comTaskExecutions) throws Exception {
        List<ComTaskExecutionInfo> comTaskExecutionInfos = new ArrayList<>(comTaskExecutions.size());
        Collections.sort(comTaskExecutions, COM_TASK_EXECUTION_COMPARATOR);
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            comTaskExecutionInfos.add(this.from(comTaskExecution, communicationTaskService.findLastSessionFor(comTaskExecution)));
        }
        return comTaskExecutionInfos;
    }

}
