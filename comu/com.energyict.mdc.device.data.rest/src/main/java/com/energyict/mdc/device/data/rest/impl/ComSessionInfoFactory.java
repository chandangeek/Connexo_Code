package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;
import com.energyict.mdc.device.data.rest.SuccessIndicatorInfo;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;

import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.time.Duration;
import java.time.temporal.ChronoField;

/**
 * Created by bvn on 10/3/14.
 */
public class ComSessionInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public ComSessionInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ComSessionInfo from(ComSession comSession) {
        ComSessionInfo info = new ComSessionInfo();
        ConnectionTask<?,?> connectionTask = comSession.getConnectionTask();
        PartialConnectionTask partialConnectionTask = connectionTask.getPartialConnectionTask();
        info.id = comSession.getId();
        info.device = new IdWithNameInfo(comSession.getConnectionTask().getDevice().getmRID(), comSession.getConnectionTask().getDevice().getName());
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
        info.status = successIndicator.equals(ComSession.SuccessIndicator.Success)
                && comSession.getComTaskExecutionSessions().stream().allMatch(comTaskExecutionSession -> comTaskExecutionSession.getSuccessIndicator().equals(ComTaskExecutionSession.SuccessIndicator.Success))?
                thesaurus.getFormat(ComSessionSuccessIndicatorTranslationKeys.SUCCESS).format():
                thesaurus.getFormat(DefaultTranslationKey.FAILURE).format();

        info.result = new SuccessIndicatorInfo(successIndicator.name(), ComSessionSuccessIndicatorTranslationKeys.translationFor(successIndicator, thesaurus));
        if (!successIndicator.equals(ComSession.SuccessIndicator.Success) && connectionTask instanceof OutboundConnectionTask) {
            info.result.retries = ((OutboundConnectionTask)connectionTask).getCurrentTryCount();
        }
        info.comTaskCount = new ComTaskCountInfo();
        info.comTaskCount.numberOfSuccessfulTasks = comSession.getNumberOfSuccessFulTasks();
        info.comTaskCount.numberOfFailedTasks = comSession.getNumberOfFailedTasks();
        info.comTaskCount.numberOfIncompleteTasks = comSession.getNumberOfPlannedButNotExecutedTasks();
        return info;
    }

}