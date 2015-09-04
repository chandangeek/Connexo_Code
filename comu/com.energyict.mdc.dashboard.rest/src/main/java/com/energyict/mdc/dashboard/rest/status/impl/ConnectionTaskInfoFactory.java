package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo;
import com.energyict.mdc.device.data.rest.SuccessIndicatorInfo;
import com.energyict.mdc.device.data.rest.TaskStatusInfo;
import com.energyict.mdc.device.data.rest.impl.DefaultTranslationKey;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;

import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.time.Duration;
import java.time.temporal.ChronoField;
import java.util.Optional;

public class ConnectionTaskInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public ConnectionTaskInfoFactory(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    protected ConnectionTaskInfo from(ConnectionTask<?, ?> connectionTask, Optional<ComSession> lastComSessionOptional) {
        ConnectionTaskInfo info = new ConnectionTaskInfo();
        info.id=connectionTask.getId();
        info.latestStatus=new DeviceConnectionTaskInfo.LatestStatusInfo();
        info.latestStatus.id = connectionTask.getSuccessIndicator().name();
        info.latestStatus.displayValue = ConnectionTaskSuccessIndicatorTranslationKeys.translationFor(connectionTask.getSuccessIndicator(), thesaurus);
        if (lastComSessionOptional.isPresent()) {
            ComSession comSession = lastComSessionOptional.get();
            ComSession.SuccessIndicator successIndicator = comSession.getSuccessIndicator();
            info.latestResult = new SuccessIndicatorInfo(successIndicator.name(), ComSessionSuccessIndicatorTranslationKeys.translationFor(successIndicator, thesaurus));
            if (connectionTask instanceof OutboundConnectionTask<?>) {
                info.latestResult.retries=((OutboundConnectionTask<?>)connectionTask).getCurrentTryCount();
            }
            info.taskCount = new DeviceConnectionTaskInfo.ComTaskCountInfo();
            info.taskCount.numberOfSuccessfulTasks = comSession.getNumberOfSuccessFulTasks();
            info.taskCount.numberOfFailedTasks = comSession.getNumberOfFailedTasks();
            info.taskCount.numberOfIncompleteTasks = comSession.getNumberOfPlannedButNotExecutedTasks();
            info.startDateTime = comSession.getStartDate().with(ChronoField.MILLI_OF_SECOND, 0);
            info.endDateTime = comSession.getStopDate().with(ChronoField.MILLI_OF_SECOND, 0);
            info.duration=new TimeDurationInfo(Duration.ofMillis(info.endDateTime.toEpochMilli() - info.startDateTime.toEpochMilli()).getSeconds());   // JP-6022
            info.comPort = new IdWithNameInfo(comSession.getComPort());
            info.comServer = new IdWithNameInfo(comSession.getComPort().getComServer());
            info.comSessionId = comSession.getId();
        }
        info.comPortPool = new IdWithNameInfo(connectionTask.getComPortPool());
        info.direction=thesaurus.getString(connectionTask.getConnectionType().getDirection().name(),connectionTask.getConnectionType().getDirection().name());
        info.connectionType = connectionTask.getPluggableClass().getName();
        info.connectionMethod = new DeviceConnectionTaskInfo.ConnectionMethodInfo();
        info.connectionMethod.id = connectionTask.getPartialConnectionTask().getId();
        info.connectionMethod.name = connectionTask.getPartialConnectionTask().getName();
        info.connectionMethod.status = connectionTask.getStatus();
        info.connectionMethod.isDefault = connectionTask.isDefault();
        if (connectionTask instanceof ScheduledConnectionTask) {
            ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) connectionTask;
            if (scheduledConnectionTask.getTaskStatus()!=null) {
                TaskStatusTranslationKeys taskStatusTranslationKey = TaskStatusTranslationKeys.from(scheduledConnectionTask.getTaskStatus());
                info.currentState = new TaskStatusInfo(taskStatusTranslationKey.getKey(), thesaurus.getFormat(taskStatusTranslationKey).format());
            }
            info.connectionStrategy=new DeviceConnectionTaskInfo.ConnectionStrategyInfo();
            info.connectionStrategy.id = scheduledConnectionTask.getConnectionStrategy().name();
            info.connectionStrategy.displayValue = ConnectionStrategyTranslationKeys.translationFor(scheduledConnectionTask.getConnectionStrategy(), thesaurus);
            ComWindow communicationWindow = scheduledConnectionTask.getCommunicationWindow();
            if (communicationWindow!=null &&
                    (communicationWindow.getStart().getMillis()!=0 || communicationWindow.getEnd().getMillis()!=0)) {
                info.window = communicationWindow.getStart() + " - " + communicationWindow.getEnd();
            } else {
                info.window = thesaurus.getFormat(DefaultTranslationKey.NO_RESTRICTIONS).format();
            }
            info.nextExecution=scheduledConnectionTask.getNextExecutionTimestamp();
        }
        Device device = connectionTask.getDevice();
        info.device = new IdWithNameInfo(device.getmRID(), device.getName());
        info.deviceType = new IdWithNameInfo(device.getDeviceType());
        info.deviceConfiguration = new DeviceConfigurationIdInfo(device.getDeviceConfiguration());
        if (connectionTask.isDefault()) {
            info.connectionMethod.name += " (" + thesaurus.getFormat(TranslationKeys.DEFAULT).format() + ")";
        }
        return info;
    }

}
