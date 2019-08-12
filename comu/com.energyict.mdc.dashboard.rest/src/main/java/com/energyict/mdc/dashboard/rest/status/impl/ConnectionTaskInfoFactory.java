/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo;
import com.energyict.mdc.device.data.rest.SuccessIndicatorInfo;
import com.energyict.mdc.device.data.rest.TaskStatusInfo;

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
            info.duration = new TimeDurationInfo(Duration.ofMillis(info.endDateTime.toEpochMilli() - info.startDateTime.toEpochMilli()).getSeconds());   // JP-6022
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
            info.connectionStrategyInfo=new DeviceConnectionTaskInfo.ConnectionStrategyInfo();
            info.connectionStrategyInfo.connectionStrategy = scheduledConnectionTask.getConnectionStrategy().name();
            info.connectionStrategyInfo.localizedValue = ConnectionStrategyTranslationKeys.translationFor(scheduledConnectionTask.getConnectionStrategy(), thesaurus);
            ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = scheduledConnectionTask.getProtocolDialectConfigurationProperties();
            if (protocolDialectConfigurationProperties != null) {
                info.protocolDialect = protocolDialectConfigurationProperties.getDeviceProtocolDialectName();
                info.protocolDialectDisplayName = protocolDialectConfigurationProperties.getDeviceProtocolDialect().getDeviceProtocolDialectDisplayName();
            }
            ComWindow communicationWindow = scheduledConnectionTask.getCommunicationWindow();
            if (communicationWindow!=null &&
                    (communicationWindow.getStart().getMillis()!=0 || communicationWindow.getEnd().getMillis()!=0)) {
                info.window = communicationWindow.getStart() + " - " + communicationWindow.getEnd();
            } else {
                info.window = thesaurus.getFormat(TranslationKeys.NO_RESTRICTIONS).format();
            }
            info.nextExecution=scheduledConnectionTask.getNextExecutionTimestamp();
        }
        Device device = connectionTask.getDevice();
        info.device = new IdWithNameInfo(device.getId(), device.getName());
        info.deviceType = new IdWithNameInfo(device.getDeviceType());
        info.deviceConfiguration = new DeviceConfigurationIdInfo(device.getDeviceConfiguration());
        if (connectionTask.isDefault()) {
            info.connectionMethod.name += " (" + thesaurus.getFormat(TranslationKeys.DEFAULT).format() + ")";
        } else if (connectionTask.getPartialConnectionTask().getConnectionFunction().isPresent()) {
            String connectionFunction = connectionTask.getPartialConnectionTask().getConnectionFunction().get().getConnectionFunctionDisplayName();
            info.connectionMethod.name += " (" + thesaurus.getFormat(TranslationKeys.CONNECTION_FUNCTION).format(connectionFunction)  + ")";
        }
        info.connectionFunctionInfo = connectionTask.getPartialConnectionTask().getConnectionFunction().isPresent()
                       ? new ConnectionFunctionInfo(connectionTask.getPartialConnectionTask().getConnectionFunction().get())
                       : deviceProtocolSupportsConnectionFunctions(device.getDeviceType()) ? getNoConnectionFunctionSpecifiedConnectionFunctionInfo(thesaurus) : null;
        info.version = connectionTask.getVersion();
        return info;
    }

    private boolean deviceProtocolSupportsConnectionFunctions(DeviceType deviceType) {
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClassOptional = deviceType.getDeviceProtocolPluggableClass();
        return deviceProtocolPluggableClassOptional.isPresent() && !deviceProtocolPluggableClassOptional.get().getProvidedConnectionFunctions().isEmpty();
    }

    private ConnectionFunctionInfo getNoConnectionFunctionSpecifiedConnectionFunctionInfo(Thesaurus thesaurus) {
        return new ConnectionFunctionInfo(new ConnectionFunction() {
            @Override
            public String getConnectionFunctionDisplayName() {
                return thesaurus.getString(TranslationKeys.NONE.getKey(), TranslationKeys.NONE.getDefaultFormat());
            }

            @Override
            public String getConnectionFunctionName() {
                return TranslationKeys.NONE.getKey();
            }

            @Override
            public long getId() {
                return -1;
            }
        });
    }
}