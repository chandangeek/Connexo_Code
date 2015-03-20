package com.energyict.mdc.issue.datacollection.rest.response;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceShortInfo;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;

public class DataCollectionIssueInfoFactory {

    private final DeviceService deviceService;

    @Inject
    public DataCollectionIssueInfoFactory(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public DataCollectionIssueInfo<?> asInfo(IssueDataCollection issue, Class<? extends DeviceInfo> deviceInfoClass) {
        DataCollectionIssueInfo<?> info = new DataCollectionIssueInfo<>(issue, deviceInfoClass);

        if (issue.getDevice() == null || !issue.getDevice().getAmrSystem().is(KnownAmrSystem.MDC)) {
            return info;
        }
        Optional<Device> device = deviceService.findDeviceById(Long.parseLong(issue.getDevice().getAmrId()));
        Optional<ComSession> comSession = issue.getComSession();
        Optional<ConnectionTask> connectionTask = issue.getConnectionTask();

        if (device.isPresent() && comSession.isPresent() && connectionTask.isPresent()) {
            info.deviceMRID = device.get().getmRID();
            Optional<ComTaskExecution> comTaskExecution = issue.getCommunicationTask();
            if (comTaskExecution.isPresent()) {
                //communication issue
                info.comTaskId = getComTask(comTaskExecution.get());
                info.comTaskSessionId = getComTaskExecutionSession(comSession.get(), comTaskExecution.get());
            } else {
                //connection issue
                info.connectionTaskId = connectionTask.get().getId();
                info.comSessionId = comSession.get().getId();
            }
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
}
