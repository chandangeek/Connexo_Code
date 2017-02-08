/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.rest.BaseComTaskExecutionInfo;
import com.energyict.mdc.device.data.rest.CompletionCodeInfo;
import com.energyict.mdc.device.data.rest.TaskStatusInfo;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.util.Optional;
import java.util.function.Supplier;

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
            info.name = comTaskExecution.getComSchedule().get().getName();
        } else {
            info.name = comTaskExecution.getComTask().getName();
        }

        if (comTaskExecution.usesSharedSchedule()) {
            ComSchedule comSchedule = comTaskExecution.getComSchedule().get();
            info.comScheduleName = comSchedule.getName();
            if (comSchedule.getTemporalExpression() != null) {
                info.comScheduleFrequency = TemporalExpressionInfo.from(comSchedule.getTemporalExpression());
            }
        } else {
            if (comTaskExecution.isScheduledManually()) {
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