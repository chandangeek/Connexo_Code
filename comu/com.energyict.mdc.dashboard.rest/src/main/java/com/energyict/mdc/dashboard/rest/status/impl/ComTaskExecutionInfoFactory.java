/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ComTaskExecutionInfoFactory extends BaseComTaskExecutionInfoFactory<ComTaskExecutionInfo> {

    private final Thesaurus thesaurus;
    private final Provider<ConnectionTaskInfoFactory> connectionTaskInfoFactory;

    @Inject
    public ComTaskExecutionInfoFactory(Thesaurus thesaurus, Provider<ConnectionTaskInfoFactory> connectionTaskInfoFactoryProvider) {
        super(thesaurus);
        this.connectionTaskInfoFactory = connectionTaskInfoFactoryProvider;
        this.thesaurus = thesaurus;
    }

    @Override
    protected Supplier<ComTaskExecutionInfo> getInfoSupplier() {
        return ComTaskExecutionInfo::new;
    }

    @Override
    protected void initExtraFields(ComTaskExecutionInfo info, ComTaskExecution comTaskExecution, Optional<ComTaskExecutionSession> comTaskExecutionSession) {
        info.comTask = new IdWithNameInfo(comTaskExecution.getComTask());
        Device device = comTaskExecution.getDevice();
        Optional<Location> location = device.getLocation();
        info.device = new DeviceInfo(device.getId(),
                device.getName(),
                location.map(location1 -> new IdWithNameInfo(location1.getId(),
                        location1.format()
                                .stream()
                                .flatMap(List::stream)
                                .filter(Objects::nonNull)
                                .collect(Collectors.joining(", "))))
                        .orElse(null));
        info.deviceConfiguration = new DeviceConfigurationIdInfo(device.getDeviceConfiguration());
        info.deviceType = new IdWithNameInfo(device.getDeviceType());
        if (comTaskExecutionSession.isPresent()) {
            info.sessionId = comTaskExecutionSession.get().getId();
        }
        info.alwaysExecuteOnInbound = comTaskExecution.isIgnoreNextExecutionSpecsForInbound();
        comTaskExecution.getConnectionTask().ifPresent(connectionTask -> {
            Optional<ComSession> comSessionOptional = comTaskExecutionSession.map(ComTaskExecutionSession::getComSession);
            info.connectionTask = connectionTaskInfoFactory.get().from(connectionTask, comSessionOptional);
        });
    }

    public ComTaskExecutionInfo getExportInfo(ComTaskExecution comTaskExecution) {
        ComTaskExecutionInfo info = new ComTaskExecutionInfo();
        info.id = comTaskExecution.getId();
        info.version = comTaskExecution.getVersion();
        info.name = comTaskExecution.getComTask().getName();
        TaskStatusTranslationKeys taskStatusTranslationKey = TaskStatusTranslationKeys.from(comTaskExecution.getStatus());
        info.currentState = new TaskStatusInfo(taskStatusTranslationKey.getKey(), thesaurus.getFormat(taskStatusTranslationKey).format());
        comTaskExecution.getComSchedule().ifPresent(cs -> info.comScheduleName = cs.getName());
        info.latestResult =
                comTaskExecution.getLastSession()
                        .map(ComTaskExecutionSession::getHighestPriorityCompletionCode)
                        .map(this::infoFrom)
                        .orElse(null);
        info.startTime = comTaskExecution.getLastExecutionStartTimestamp();
        info.successfulFinishTime = comTaskExecution.getLastSuccessfulCompletionTimestamp();
        info.nextCommunication = comTaskExecution.getNextExecutionTimestamp();
        Device device = comTaskExecution.getDevice();
        info.device = new IdWithNameInfo(device.getId(), device.getName());
        return info;
    }

    private CompletionCodeInfo infoFrom(CompletionCode completionCode) {
        return new CompletionCodeInfo(completionCode.name(), CompletionCodeTranslationKeys.translationFor(completionCode, thesaurus));
    }
}