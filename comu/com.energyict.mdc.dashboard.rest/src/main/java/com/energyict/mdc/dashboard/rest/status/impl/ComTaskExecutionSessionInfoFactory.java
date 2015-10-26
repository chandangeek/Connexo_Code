package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by bvn on 9/1/14.
 */
public class ComTaskExecutionSessionInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public ComTaskExecutionSessionInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public List<ComTaskExecutionSessionInfo> from(List<ComTaskExecutionSession> comTaskExecutionSessions) {
        List<ComTaskExecutionSessionInfo> comTaskExecutionSessionInfos = new ArrayList<>(comTaskExecutionSessions.size());
        for (ComTaskExecutionSession comTaskExecutionSession : comTaskExecutionSessions) {
            comTaskExecutionSessionInfos.add(this.from(comTaskExecutionSession));
        }
        return comTaskExecutionSessionInfos;
    }

    public ComTaskExecutionSessionInfo from(ComTaskExecutionSession comTaskExecutionSession) {
        ComTaskExecutionSessionInfo info = new ComTaskExecutionSessionInfo();
        ComTaskExecution comTaskExecution = comTaskExecutionSession.getComTaskExecution();
       /* info.comTasks = new ArrayList<>(comTaskExecution.getComTasks().size());
        for (ComTask comTask : comTaskExecution.getComTasks()) {
            info.comTasks.add(new IdWithNameInfo(comTask));
        }*/
        ComTask comTask = comTaskExecutionSession.getComTask();
        info.comTask = new IdWithNameInfo(comTask.getId(), comTask.getName());
        if(comTaskExecution.usesSharedSchedule()){
            info.name = ((ScheduledComTaskExecution)comTaskExecution).getComSchedule().getName();
        } else {
            info.name = comTaskExecution.getComTasks().stream().map(ComTask::getName).collect(Collectors.joining(" + "));
        }
        info.id = comTaskExecutionSession.getId();
        Device device = comTaskExecutionSession.getDevice();
        info.device = new IdWithNameInfo(device.getmRID(), device.getName());
        info.deviceConfiguration = new DeviceConfigurationIdInfo(device.getDeviceConfiguration());
        info.deviceType = new IdWithNameInfo(device.getDeviceType());
        if (comTaskExecution instanceof ScheduledComTaskExecution) {
            ComSchedule comSchedule = ((ScheduledComTaskExecution) comTaskExecution).getComSchedule();
            info.comScheduleName = comSchedule.getName();
            if (comSchedule.getTemporalExpression() != null) {
                info.comScheduleFrequency = TemporalExpressionInfo.from(comSchedule.getTemporalExpression());
            }
        } else {
            if (comTaskExecutionSession instanceof ManuallyScheduledComTaskExecution) {
                Optional<NextExecutionSpecs> nextExecutionSpecs = comTaskExecution.getNextExecutionSpecs();
                info.comScheduleName = thesaurus.getFormat(TranslationKeys.INDIVIDUAL).format();
                if (nextExecutionSpecs.isPresent()) {
                    info.comScheduleFrequency = TemporalExpressionInfo.from(nextExecutionSpecs.get().getTemporalExpression());
                }

            }
        }
        info.urgency = comTaskExecution.getExecutionPriority();
        TaskStatusTranslationKeys taskStatusTranslationKey = TaskStatusTranslationKeys.from(comTaskExecution.getStatus());
        info.currentState = new TaskStatusInfo(taskStatusTranslationKey.getKey(), thesaurus.getFormat(taskStatusTranslationKey).format());
        CompletionCode completionCode = comTaskExecutionSession.getHighestPriorityCompletionCode();
        info.result = new CompletionCodeInfo(completionCode.name(), CompletionCodeTranslationKeys.translationFor(completionCode, thesaurus));
        info.startTime = comTaskExecutionSession.getStartDate();
        info.stopTime = comTaskExecutionSession.getStopDate();
        info.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        info.alwaysExecuteOnInbound = comTaskExecution.isIgnoreNextExecutionSpecsForInbound();
        return info;
    }

}