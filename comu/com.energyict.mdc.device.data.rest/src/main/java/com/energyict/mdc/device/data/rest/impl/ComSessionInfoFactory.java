/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;
import com.energyict.mdc.device.data.rest.SuccessIndicatorInfo;

import javax.inject.Inject;
import java.time.Duration;
import java.time.temporal.ChronoField;
import java.util.EnumSet;
import java.util.stream.Collectors;

/**
 * Created by bvn on 10/3/14.
 */
public class ComSessionInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public ComSessionInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ComSessionInfo from(ComSession comSession, JournalEntryInfoFactory journalEntryInfoFactory) {
        return from(comSession, journalEntryInfoFactory, false);
    }

    public ComSessionInfo from(ComSession comSession, JournalEntryInfoFactory journalEntryInfoFactory, boolean connectionLogsOnly) {
        ComSessionInfo info = new ComSessionInfo();
        ConnectionTask<?,?> connectionTask = comSession.getConnectionTask();
        PartialConnectionTask partialConnectionTask = connectionTask.getPartialConnectionTask();
        info.id = comSession.getId();
        info.device = new IdWithNameInfo(comSession.getConnectionTask().getDevice().getId(), comSession.getConnectionTask().getDevice().getName());
        info.deviceType = new IdWithNameInfo(comSession.getConnectionTask().getDevice().getDeviceType());
        info.deviceConfiguration = new DeviceConfigurationIdInfo(comSession.getConnectionTask().getDevice().getDeviceConfiguration());
        info.connectionMethod = new IdWithNameInfo(connectionTask);
        info.isDefault = connectionTask.isDefault();
        info.startedOn = comSession.getStartDate().with(ChronoField.MILLI_OF_SECOND, 0);
        info.finishedOn = comSession.getStopDate().with(ChronoField.MILLI_OF_SECOND, 0);
        info.durationInSeconds = Duration.ofMillis(info.finishedOn.toEpochMilli() - info.startedOn.toEpochMilli()).getSeconds(); // JP-6022
        if (comSession.getComPort()!=null) {
            info.comPort = comSession.getComPort().getName();
            info.comServer = new IdWithNameInfo(comSession.getComPort().getComServer());
        }
        String direction = partialConnectionTask.getConnectionType().getDirection().name();
        info.direction = thesaurus.getString(direction, direction);
        info.connectionType = partialConnectionTask.getPluggableClass().getName();
        ComSession.SuccessIndicator successIndicator = comSession.getSuccessIndicator();
        info.status = getStatus(comSession, successIndicator);

        info.result = new SuccessIndicatorInfo(successIndicator.name(), ComSessionSuccessIndicatorTranslationKeys.translationFor(successIndicator, thesaurus));
        if (!successIndicator.equals(ComSession.SuccessIndicator.Success) && connectionTask instanceof OutboundConnectionTask) {
            info.result.retries = ((OutboundConnectionTask)connectionTask).getCurrentTryCount();
        }
        info.comTaskCount = new ComTaskCountInfo();
        info.comTaskCount.numberOfSuccessfulTasks = comSession.getNumberOfSuccessFulTasks();
        info.comTaskCount.numberOfFailedTasks = comSession.getNumberOfFailedTasks();
        info.comTaskCount.numberOfIncompleteTasks = comSession.getNumberOfPlannedButNotExecutedTasks();
        info.errors = connectionLogsOnly
            ? comSession.getJournalEntries(EnumSet.of(ComServer.LogLevel.ERROR)).stream().map(journalEntryInfoFactory::asInfo).collect(Collectors.toList())
            : comSession.getAllLogs(EnumSet.of(ComServer.LogLevel.ERROR), 0, Integer.MAX_VALUE).stream().map(journalEntryInfoFactory::asInfo).collect(Collectors.toList());
        info.warnings = connectionLogsOnly
            ? comSession.getJournalEntries(EnumSet.of(ComServer.LogLevel.WARN)).stream().map(journalEntryInfoFactory::asInfo).collect(Collectors.toList())
            : comSession.getAllLogs(EnumSet.of(ComServer.LogLevel.WARN), 0, Integer.MAX_VALUE).stream().map(journalEntryInfoFactory::asInfo).collect(Collectors.toList());
        return info;
    }

    private String getStatus(ComSession comSession, ComSession.SuccessIndicator successIndicator) {
        if (successIndicator.equals(ComSession.SuccessIndicator.Success)
                && comSession.getComTaskExecutionSessions().stream().allMatch(comTaskExecutionSession -> comTaskExecutionSession.getSuccessIndicator().equals(ComTaskExecutionSession.SuccessIndicator.Success))) {
            return thesaurus.getFormat(ComSessionSuccessIndicatorTranslationKeys.SUCCESS).format();
        }
        if (successIndicator.equals(ComSession.SuccessIndicator.Not_Executed)) {
            return thesaurus.getFormat(CompletionCodeTranslationKeys.NOT_EXECUTED).format();
        }
        return thesaurus.getFormat(DefaultTranslationKey.FAILURE).format();
    }

}