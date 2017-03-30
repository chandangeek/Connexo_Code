/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.rest.response;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceShortInfo;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComCommandJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionMessageJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.rest.ModuleConstants;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name="issue.data.collection.info.factory", service = { InfoFactory.class }, immediate = true)
public class DataCollectionIssueInfoFactory implements InfoFactory<IssueDataCollection> {

    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile DeviceService deviceService;

    public DataCollectionIssueInfoFactory() {}

    @Inject
    public DataCollectionIssueInfoFactory(DeviceService deviceService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this();
        this.deviceService = deviceService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public DataCollectionIssueInfo<?> asInfo(IssueDataCollection issue, Class<? extends DeviceInfo> deviceInfoClass) {
        DataCollectionIssueInfo<?> info = new DataCollectionIssueInfo<>(issue, deviceInfoClass);


        Optional<ComSession> comSession = issue.getComSession();
        Optional<ConnectionTask> connectionTask = issue.getConnectionTask();

        switch (issue.getReason().getKey()) {
            case ModuleConstants.REASON_CONNECTION_FAILED:
            case ModuleConstants.REASON_CONNECTION_SETUP_FAILED:
                ConnectionFailedIssueInfo<?> connectionFailedIssueInfo = new ConnectionFailedIssueInfo<>(issue, deviceInfoClass);
                addMeterInfo(connectionFailedIssueInfo, issue);
                addConnectionRelatedInfo(connectionFailedIssueInfo, comSession, connectionTask);
                info = connectionFailedIssueInfo;
                break;
            case ModuleConstants.REASON_FAILED_TO_COMMUNICATE:
                CommunicationFailedIssueInfo<?> communicationFailedIssueInfo = new CommunicationFailedIssueInfo<>(issue, deviceInfoClass);
                Optional<ComTaskExecution> comTaskExecution = issue.getCommunicationTask();
                addMeterInfo(communicationFailedIssueInfo, issue);
                addCommunicationRelatedInfo(communicationFailedIssueInfo, comSession, comTaskExecution);
                info = communicationFailedIssueInfo;
                break;
            case ModuleConstants.REASON_UNKNOWN_INBOUND_DEVICE:
                UnknownInboundDeviceIssueInfo<?> unknownInboundDeviceIssueInfo = new UnknownInboundDeviceIssueInfo<>(issue, deviceInfoClass);
                addConnectionAttempts(unknownInboundDeviceIssueInfo, issue);
                unknownInboundDeviceIssueInfo.deviceName = issue.getDeviceMRID();
                info = unknownInboundDeviceIssueInfo;
                break;
            case ModuleConstants.REASON_UNKNOWN_OUTBOUND_DEVICE:
                UnknownOutboundDeviceIssueInfo<?> unknownOutboundDeviceIssueInfo = new UnknownOutboundDeviceIssueInfo<>(issue, deviceInfoClass);
                addConnectionAttempts(unknownOutboundDeviceIssueInfo, issue);
                addMeterInfo(unknownOutboundDeviceIssueInfo, issue);
                unknownOutboundDeviceIssueInfo.slaveDeviceId = issue.getDeviceMRID();
                info = unknownOutboundDeviceIssueInfo;
                break;
        }
        return info;
    }

    public List<DataCollectionIssueInfo<?>> asInfos(List<? extends IssueDataCollection> issues) {
        return issues.stream().map(issue -> this.asInfo(issue, DeviceShortInfo.class)).collect(Collectors.toList());
    }

    private Long getComTask(ComSession comSession, ComTaskExecution comTaskExecution) {
        return comSession.getComTaskExecutionSessions().stream()
                .filter(es -> es.getComTaskExecution().getId() == comTaskExecution.getId())
                .findFirst().map(ComTaskExecutionSession::getComTask)
                .map(HasId::getId).orElse(null);
    }

    private Long getComTaskExecutionSession(ComSession comSession, ComTaskExecution comTaskExecution) {
        return comSession.getComTaskExecutionSessions().stream()
                .filter(es -> es.getComTaskExecution().getId() == comTaskExecution.getId() &&
                             (es.getSuccessIndicator() == ComTaskExecutionSession.SuccessIndicator.Failure || es.getSuccessIndicator() == ComTaskExecutionSession.SuccessIndicator.Interrupted))
                .findFirst()
                .map(HasId::getId).orElse(null);
    }

    @Override
    public Object from(IssueDataCollection issueDataCollection) {
        return asInfo(issueDataCollection, DeviceInfo.class);
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        return new ArrayList<>();
    }

    @Override
    public Class<IssueDataCollection> getDomainClass() {
        return IssueDataCollection.class;
    }

    private void addConnectionAttempts(DataCollectionIssueInfo<?> info, IssueDataCollection issue) {
        info.firstConnectionAttempt = issue.getFirstConnectionAttemptTimestamp();
        info.lastConnectionAttempt = issue.getLastConnectionAttemptTimestamp();
        info.connectionAttemptsNumber = issue.getConnectionAttempt();
    }

    private void addMeterInfo(DataCollectionIssueInfo<?> info, IssueDataCollection issue) {
        if (issue.getDevice() == null || !issue.getDevice().getAmrSystem().is(KnownAmrSystem.MDC)) {
            return;
        }
        Optional<Device> deviceRef = deviceService.findDeviceById(Long.parseLong(issue.getDevice().getAmrId()));
        if (deviceRef.isPresent()) {
            Device device = deviceRef.get();
            info.deviceName = device.getName();
            info.deviceType = new IdWithNameInfo(device.getDeviceType());
            info.deviceConfiguration = new IdWithNameInfo(device.getDeviceConfiguration());
            info.deviceState = new IdWithNameInfo(device.getState().getId(), getStateName(device.getState()));
        }
    }

    private void addConnectionRelatedInfo(ConnectionFailedIssueInfo<?> info, Optional<ComSession> comSessionRef, Optional<ConnectionTask> connectionTaskRef) {
        if (comSessionRef.isPresent() && connectionTaskRef.isPresent()) {
            info.comSessionId = comSessionRef.get().getId();
            info.connectionTaskId = connectionTaskRef.get().getId();
            info.connectionTask = getConnectionTaskInfo(connectionTaskRef.get());
        }
    }

    private void addCommunicationRelatedInfo(CommunicationFailedIssueInfo<?> info, Optional<ComSession> comSessionRef, Optional<ComTaskExecution> comTaskExecutionRef) {
        if (comSessionRef.isPresent() && comTaskExecutionRef.isPresent()) {
            info.comTaskId = getComTask(comSessionRef.get(),comTaskExecutionRef.get());
            info.comTaskSessionId = getComTaskExecutionSession(comSessionRef.get(), comTaskExecutionRef.get());
            info.communicationTask = getCommunicationTaskInfo(comTaskExecutionRef.get());
        }
    }

    private ConnectionTaskIssueInfo getConnectionTaskInfo(ConnectionTask connectionTask) {
        ConnectionTaskIssueInfo info = new ConnectionTaskIssueInfo();
        info.id = connectionTask.getId();
        info.latestAttempt = connectionTask.getLastCommunicationStart();
        info.lastSuccessfulAttempt = connectionTask.getLastSuccessfulCommunicationEnd();
        info.latestStatus = connectionTask.getSuccessIndicator() != null ?
                new IdWithNameInfo(connectionTask.getSuccessIndicator().name(), connectionTask.getSuccessIndicatorDisplayName()) : null;

        Optional<ComSession> comSessionRef = connectionTask.getLastComSession();
        if(comSessionRef.isPresent()) {
            info.latestResult = comSessionRef.get().getSuccessIndicator() != null ?
                    new IdWithNameInfo(comSessionRef.get().getSuccessIndicator().name(), comSessionRef.get().getSuccessIndicatorDisplayName()) : null;
            info.journals = comSessionRef.get().getJournalEntries().stream()
                    .sorted((je1,je2) -> je2.getTimestamp().compareTo(je1.getTimestamp()))
                    .map(this::asComSessionJournalInfo).collect(Collectors.toList());

        }
        info.connectionMethod = new IdWithNameInfo(connectionTask.getPartialConnectionTask());
        info.version = connectionTask.getVersion();
        return info;
    }

    private CommunicationTaskIssueInfo getCommunicationTaskInfo(ComTaskExecution comTaskExecution) {
        CommunicationTaskIssueInfo communicationTaskInfo = new CommunicationTaskIssueInfo();
        communicationTaskInfo.id = comTaskExecution.getId();
        if (comTaskExecution.usesSharedSchedule()) {
            communicationTaskInfo.name = comTaskExecution.getComSchedule().get().getName();
        } else {
            communicationTaskInfo.name = comTaskExecution.getComTask().getName();
        }
        communicationTaskInfo.latestStatus = comTaskExecution.getStatus() != null ?
                new IdWithNameInfo(comTaskExecution.getStatus().name(), comTaskExecution.getStatusDisplayName()) : null;
        Optional<ComTaskExecutionSession> lastComTaskExecutionSessionRef = comTaskExecution.getLastSession();
        if (lastComTaskExecutionSessionRef.isPresent()) {
            communicationTaskInfo.latestResult =  lastComTaskExecutionSessionRef.get().getHighestPriorityCompletionCode() != null ?
                    new IdWithNameInfo(lastComTaskExecutionSessionRef.get().getHighestPriorityCompletionCode().name(),
                    lastComTaskExecutionSessionRef.get().getHighestPriorityCompletionCodeDisplayName()) : null;
            communicationTaskInfo.journals = lastComTaskExecutionSessionRef.get().getComSession()
                    .getCommunicationTaskJournalEntries(EnumSet.allOf(ComServer.LogLevel.class)).stream()
                    .map(this::asComSessionTaskJournalInfo)
                    .collect(Collectors.toList());
        }
        communicationTaskInfo.latestAttempt = comTaskExecution.getLastExecutionStartTimestamp();
        communicationTaskInfo.lastSuccessfulAttempt = comTaskExecution.getLastSuccessfulCompletionTimestamp();
        Optional<ConnectionTask<?,?>> connectionTaskRef = comTaskExecution.getConnectionTask();
        if(connectionTaskRef.isPresent()) {
            communicationTaskInfo.latestConnectionUsed = new IdWithNameInfo(connectionTaskRef.get().getId(), connectionTaskRef.get().getName());
        }

        return communicationTaskInfo;
    }

    private JournalEntryInfo asComSessionJournalInfo(ComSessionJournalEntry comSessionJournalEntry) {
        JournalEntryInfo info = new JournalEntryInfo();
        info.timestamp=comSessionJournalEntry.getTimestamp();
        info.logLevel=comSessionJournalEntry.getLogLevel();
        info.details=comSessionJournalEntry.getMessage();
        return info;
    }

    private JournalEntryInfo asComSessionTaskJournalInfo(ComTaskExecutionJournalEntry comTaskExecutionJournalEntry) {
        JournalEntryInfo info = new JournalEntryInfo();
        info.timestamp=comTaskExecutionJournalEntry.getTimestamp();
        info.logLevel=comTaskExecutionJournalEntry.getLogLevel();
        if (comTaskExecutionJournalEntry instanceof ComTaskExecutionMessageJournalEntry) {
            info.details=((ComTaskExecutionMessageJournalEntry)comTaskExecutionJournalEntry).getMessage();
        } else if (comTaskExecutionJournalEntry instanceof ComCommandJournalEntry) {
            info.details=((ComCommandJournalEntry)comTaskExecutionJournalEntry).getCommandDescription();
        }
        return info;
    }

    private String getStateName(State state) {
        return DefaultState
                .from(state)
                .map(deviceLifeCycleConfigurationService::getDisplayName)
                .orElseGet(state::getName);
    }

}