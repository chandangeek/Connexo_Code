package com.energyict.mdc.issue.datacollection.rest.response;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceShortInfo;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;

import com.energyict.mdc.issue.datacollection.rest.ModuleConstants;
import com.energyict.mdc.issue.datacollection.rest.i18n.TaskStatusTranslationKeys;
import com.energyict.mdc.tasks.ComTask;

public class DataCollectionIssueInfoFactory {

    private final DeviceService deviceService;
    private final Thesaurus thesaurus;
    private final CommunicationTaskService communicationTaskService;

    @Inject
    public DataCollectionIssueInfoFactory(DeviceService deviceService, Thesaurus thesaurus, CommunicationTaskService communicationTaskService) {
        this.deviceService = deviceService;
        this.thesaurus = thesaurus;
        this.communicationTaskService = communicationTaskService;
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
                unknownInboundDeviceIssueInfo.deviceMRID = issue.getDeviceMRID();
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

    private Long getComTask(ComTaskExecution comTaskExecution) {
        if (!comTaskExecution.getComTasks().isEmpty()) {
            return comTaskExecution.getComTasks().get(0).getId();//Get first com task: works ok for manually scheduled comtask execution, but scheduled comtask execution?
        }
        return null;
    }

    private Long getComTaskExecutionSession(ComSession comSession, ComTaskExecution comTaskExecution) {
        return comSession.getComTaskExecutionSessions().stream()
                .filter(es -> es.getComTaskExecution().getId() == comTaskExecution.getId() &&
                             (es.getSuccessIndicator() == ComTaskExecutionSession.SuccessIndicator.Failure || es.getSuccessIndicator() == ComTaskExecutionSession.SuccessIndicator.Interrupted))
                .findFirst()
                .map(es -> es.getId()).orElse(null);
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
            info.deviceMRID = device.getmRID();
            info.deviceType = new IdWithNameInfo(device.getDeviceType());
            info.deviceConfiguration = new IdWithNameInfo(device.getDeviceConfiguration());
            info.deviceState = new IdWithNameInfo(device.getState().getId(), getStateName(thesaurus, device.getState()));
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
            info.comTaskId = getComTask(comTaskExecutionRef.get());
            info.comTaskSessionId = getComTaskExecutionSession(comSessionRef.get(), comTaskExecutionRef.get());
            info.communicationTask = getCommunicationTaskInfo(comTaskExecutionRef.get());
        }
    }

    private ConnectionTaskIssueInfo getConnectionTaskInfo(ConnectionTask connectionTask) {
        ConnectionTaskIssueInfo info = new ConnectionTaskIssueInfo();
        info.id = connectionTask.getId();
        info.latestAttempt = connectionTask.getLastCommunicationStart();
        info.lastSuccessfulAttempt = connectionTask.getLastSuccessfulCommunicationEnd();
        String connectionTaskIndicatorName = connectionTask.getSuccessIndicator().name();
        info.latestStatus = new IdWithNameInfo(connectionTaskIndicatorName,
                thesaurus.getString(connectionTaskIndicatorName, connectionTaskIndicatorName));
                //ConnectionTaskSuccessIndicatorTranslationKeys.translationFor(connectionTask.getSuccessIndicator(), thesaurus));

        Optional<ComSession> comSessionRef = connectionTask.getLastComSession();
        if(comSessionRef.isPresent()) {
            ComSession.SuccessIndicator successIndicator = comSessionRef.get().getSuccessIndicator();
            String thesaurusKey = ComSession.class.getSimpleName() + "." + successIndicator.name();
            info.latestResult = new IdWithNameInfo(successIndicator.name(), thesaurus.getString(thesaurusKey, thesaurusKey));
                    //ComSessionSuccessIndicatorTranslationKeys.translationFor(successIndicator, thesaurus));
            info.journals = comSessionRef.get().getJournalEntries().stream().map(this::asComSessionJournalInfo).collect(Collectors.toList());

        }
        info.connectionMethod = new IdWithNameInfo(connectionTask.getPartialConnectionTask());
        info.version = connectionTask.getVersion();
        return info;
    }

    private CommunicationTaskIssueInfo getCommunicationTaskInfo(ComTaskExecution comTaskExecution) {
        CommunicationTaskIssueInfo communicationTaskInfo = new CommunicationTaskIssueInfo();
        communicationTaskInfo.id = comTaskExecution.getId();
        if (comTaskExecution.usesSharedSchedule()) {
            communicationTaskInfo.name = ((ScheduledComTaskExecution)comTaskExecution).getComSchedule().getName();
        } else {
            communicationTaskInfo.name = comTaskExecution.getComTasks().stream().map(ComTask::getName).collect(Collectors.joining(" + "));
        }
        TaskStatusTranslationKeys taskStatusTranslationKey = TaskStatusTranslationKeys.from(comTaskExecution.getStatus());
        communicationTaskInfo.latestStatus = new IdWithNameInfo(taskStatusTranslationKey.getKey(), thesaurus.getFormat(taskStatusTranslationKey).format());
        Optional<ComTaskExecutionSession> lastComTaskExecutionSessionRef = this.communicationTaskService.findLastSessionFor(comTaskExecution);
        if (lastComTaskExecutionSessionRef.isPresent()) {
            communicationTaskInfo.latestResult = lastComTaskExecutionSessionRef
                    .map(ComTaskExecutionSession::getHighestPriorityCompletionCode)
                    .map(this::getLatestResultAsInfo)
                    .orElse(null);
            communicationTaskInfo.journals = lastComTaskExecutionSessionRef.get().getComTaskExecutionJournalEntries().stream()
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

    private IdWithNameInfo getLatestResultAsInfo(CompletionCode completionCode) {
        String completionCodeName = completionCode.name();
        return new IdWithNameInfo(completionCodeName, thesaurus.getString(completionCodeName, completionCodeName));
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
        info.details=comTaskExecutionJournalEntry.getErrorDescription();
        return info;
    }

    public static String getStateName(Thesaurus thesaurus, State state) {
        Optional<DefaultState> defaultState = DefaultState.from(state);
        if (defaultState.isPresent()) {
            return thesaurus.getStringBeyondComponent(defaultState.get().getKey(), defaultState.get().getKey());
        } else {
            return state.getName();
        }
    }
}
