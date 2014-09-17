package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.ComPort;

import java.util.List;
import java.util.stream.Collectors;

/**
 * POJO class holding the values for the {@link EventType#DEVICE_CONNECTION_COMPLETION} event.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-16 (14:41)
 */
public class ConnectionTaskCompletionEventInfo {
    private long comPortId;
    private long comServerId;
    private long deviceId;
    private long connectionTaskId;
    private String successTaskIDs;
    private String failedTaskIDs;
    private String skippedTaskIDs;

    public static ConnectionTaskCompletionEventInfo forFailure(ConnectionTask<?, ?> connectionTask, ComPort comPort, List<ComTaskExecution> plannedComTaskExecutions) {
        ConnectionTaskCompletionEventInfo eventInfo = new ConnectionTaskCompletionEventInfo();
        eventInfo.setComPort(comPort);
        eventInfo.setConnectionTask(connectionTask);
        eventInfo.setSuccessTaskIDs("");
        eventInfo.setFailedTaskIDs("");
        eventInfo.setSkippedTaskIDs(toCommaSeparatedIdList(plannedComTaskExecutions));
        return eventInfo;
    }

    public static ConnectionTaskCompletionEventInfo forCompletion(ConnectionTask<?, ?> connectionTask, ComPort comPort, List<ComTaskExecution> successfulComTaskExecutions, List<ComTaskExecution> failedComTaskExecutions, List<ComTaskExecution> skippedComTaskExecutions) {
        ConnectionTaskCompletionEventInfo eventInfo = new ConnectionTaskCompletionEventInfo();
        eventInfo.setComPort(comPort);
        eventInfo.setConnectionTask(connectionTask);
        eventInfo.setSuccessTaskIDs(toCommaSeparatedIdList(successfulComTaskExecutions));
        eventInfo.setFailedTaskIDs(toCommaSeparatedIdList(failedComTaskExecutions));
        eventInfo.setSkippedTaskIDs(toCommaSeparatedIdList(skippedComTaskExecutions));
        return eventInfo;
    }

    private static String toCommaSeparatedIdList(List<ComTaskExecution> comTaskExecutions) {
        return comTaskExecutions.stream().
                    map(ComTaskExecution::getId).
                    map(String::valueOf).
                    collect(Collectors.joining(","));
    }

    private void setConnectionTask(ConnectionTask<?, ?> connectionTask) {
        this.setDeviceId(connectionTask.getDevice().getId());
        this.setConnectionTaskId(connectionTask.getId());
    }

    private void setComPort(ComPort comPort) {
        this.setComServerId(comPort.getComServer().getId());
        this.setComPortId(comPort.getId());
    }

    public long getComPortId() {
        return comPortId;
    }

    public void setComPortId(long comPortId) {
        this.comPortId = comPortId;
    }

    public long getComServerId() {
        return comServerId;
    }

    public void setComServerId(long comServerId) {
        this.comServerId = comServerId;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public long getConnectionTaskId() {
        return connectionTaskId;
    }

    public void setConnectionTaskId(long connectionTaskId) {
        this.connectionTaskId = connectionTaskId;
    }

    public String getSuccessTaskIDs() {
        return successTaskIDs;
    }

    public void setSuccessTaskIDs(String successTaskIDs) {
        this.successTaskIDs = successTaskIDs;
    }

    public String getFailedTaskIDs() {
        return failedTaskIDs;
    }

    public void setFailedTaskIDs(String failedTaskIDs) {
        this.failedTaskIDs = failedTaskIDs;
    }

    public String getSkippedTaskIDs() {
        return skippedTaskIDs;
    }

    public void setSkippedTaskIDs(String skippedTaskIDs) {
        this.skippedTaskIDs = skippedTaskIDs;
    }

}