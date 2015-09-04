package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.device.data.rest.impl.DefaultTranslationKey;
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

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class BaseComTaskExecutionInfoFactory <T extends BaseComTaskExecutionInfo>{

    private final Thesaurus thesaurus;

    public BaseComTaskExecutionInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    protected abstract Supplier<T> getInfoSupplier();

    protected abstract void initExtraFields(T info, ComTaskExecution comTaskExecution, Optional<ComTaskExecutionSession> comTaskExecutionSession);

    public final T from(ComTaskExecution comTaskExecution, Optional<ComTaskExecutionSession> comTaskExecutionSession) {
        T info = getInfoSupplier().get();

        info.id = comTaskExecution.getId();
        if (comTaskExecution.usesSharedSchedule()) {
            info.name = ((ScheduledComTaskExecution)comTaskExecution).getComSchedule().getName();
        } else {
            info.name = comTaskExecution.getComTasks().stream().map(ComTask::getName).collect(Collectors.joining(" + "));
        }

        if (comTaskExecution instanceof ScheduledComTaskExecution) {
            ComSchedule comSchedule = ((ScheduledComTaskExecution) comTaskExecution).getComSchedule();
            info.comScheduleName = comSchedule.getName();
            if (comSchedule.getTemporalExpression() != null) {
                info.comScheduleFrequency = TemporalExpressionInfo.from(comSchedule.getTemporalExpression());
            }
        } else {
            if (comTaskExecution instanceof ManuallyScheduledComTaskExecution) {
                Optional<NextExecutionSpecs> nextExecutionSpecs = comTaskExecution.getNextExecutionSpecs();
                info.comScheduleName = thesaurus.getFormat(DefaultTranslationKey.INDIVIDUAL).format();
                if (nextExecutionSpecs.isPresent()) {
                    info.comScheduleFrequency = TemporalExpressionInfo.from(nextExecutionSpecs.get().getTemporalExpression());
                }
            }
        }
        info.urgency = comTaskExecution.getExecutionPriority();
        TaskStatusTranslationKeys taskStatusTranslationKey = TaskStatusTranslationKeys.from(comTaskExecution.getStatus());
        info.currentState = new TaskStatusInfo(taskStatusTranslationKey.getKey(), thesaurus.getFormat(taskStatusTranslationKey).format());
        info.latestResult =
                comTaskExecutionSession
                        .map(ComTaskExecutionSession::getHighestPriorityCompletionCode)
                        .map(this::infoFrom)
                        .orElse(null);
        info.startTime = comTaskExecution.getLastExecutionStartTimestamp();
        info.successfulFinishTime = comTaskExecution.getLastSuccessfulCompletionTimestamp();
        info.nextCommunication = comTaskExecution.getNextExecutionTimestamp();

        initExtraFields(info, comTaskExecution, comTaskExecutionSession);
        return info;
    }

    private CompletionCodeInfo infoFrom(CompletionCode completionCode) {
        return new CompletionCodeInfo(completionCode.name(), CompletionCodeTranslationKeys.translationFor(completionCode, thesaurus));
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

}