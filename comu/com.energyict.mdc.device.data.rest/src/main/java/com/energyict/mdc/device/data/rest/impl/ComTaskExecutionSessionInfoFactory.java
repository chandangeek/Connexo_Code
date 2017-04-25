/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by bvn on 10/6/14.
 */
public class ComTaskExecutionSessionInfoFactory {

    private final Thesaurus thesaurus;
    private final JournalEntryInfoFactory journalEntryInfoFactory;

    @Inject
    public ComTaskExecutionSessionInfoFactory(Thesaurus thesaurus, JournalEntryInfoFactory journalEntryInfoFactory) {
        this.thesaurus = thesaurus;
        this.journalEntryInfoFactory = journalEntryInfoFactory;
    }

    public ComTaskExecutionSessionInfo from(ComTaskExecutionSession comTaskExecutionSession) {
        ComTaskExecutionSessionInfo info = new ComTaskExecutionSessionInfo();
        Device device = comTaskExecutionSession.getDevice();
        ComTaskExecution comTaskExecution = comTaskExecutionSession.getComTaskExecution();
        info.comTasks = Collections.singletonList(new IdWithNameInfo(comTaskExecutionSession.getComTask()));
        info.name = comTaskExecutionSession.getComTask().getName();
        info.id = comTaskExecutionSession.getId();
        info.device = new IdWithNameInfo(device.getId(), device.getName());
        info.deviceConfiguration = new DeviceConfigurationIdInfo(device.getDeviceConfiguration());
        info.deviceType = new IdWithNameInfo(device.getDeviceType());
        if (comTaskExecution.usesSharedSchedule()) {
            ComSchedule comSchedule = comTaskExecution.getComSchedule().get();
            info.comScheduleName=comSchedule.getName();
            if (comSchedule.getTemporalExpression()!=null) {
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
        if (comTaskExecutionSession.getHighestPriorityCompletionCode()!=null) {
            info.result = CompletionCodeTranslationKeys.translationFor(comTaskExecutionSession.getHighestPriorityCompletionCode(), thesaurus);
        }
        info.startTime=comTaskExecutionSession.getStartDate();
        info.finishTime =comTaskExecutionSession.getStopDate();
        info.durationInSeconds = info.startTime.until(info.finishTime, ChronoUnit.SECONDS);
        info.alwaysExecuteOnInbound = comTaskExecution.isIgnoreNextExecutionSpecsForInbound();
        info.errors = comTaskExecutionSession.getComTaskExecutionJournalEntries().stream()
                .filter(journalEntry -> journalEntry.getLogLevel().equals(ComServer.LogLevel.ERROR))
                .limit(5)
                .map(journalEntryInfoFactory::asInfo)
                .collect(Collectors.toList());
        info.warnings = comTaskExecutionSession.getComTaskExecutionJournalEntries().stream()
                .filter(journalEntry -> journalEntry.getLogLevel().equals(ComServer.LogLevel.WARN))
                .limit(5 - info.errors.size())
                .map(journalEntryInfoFactory::asInfo)
                .collect(Collectors.toList());
        return info;
    }

}