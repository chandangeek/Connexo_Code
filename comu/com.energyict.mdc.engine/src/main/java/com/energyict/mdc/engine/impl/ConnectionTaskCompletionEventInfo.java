/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * POJO class holding the values for the {@link EventType#DEVICE_CONNECTION_COMPLETION} event.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-16 (14:41)
 */
public class ConnectionTaskCompletionEventInfo {
    private long comSessionId;
    private long comPortId;
    private String comPortName;
    private long comServerId;
    private String comServerName;
    private long deviceIdentifier;
    private long connectionTaskId;
    private String successTaskIDs;
    private String failedTaskIDs;
    private String skippedTaskIDs;

    public static ConnectionTaskCompletionEventInfo forFailure(ConnectionTask<?, ?> connectionTask, ComPort comPort, ComSession comSession, List<ComTaskExecution> plannedComTaskExecutions) {
        ConnectionTaskCompletionEventInfo eventInfo = new ConnectionTaskCompletionEventInfo();
        eventInfo.setComSessionId(comSession.getId());
        eventInfo.setComPort(comPort);
        eventInfo.setConnectionTask(connectionTask);
        eventInfo.setSuccessTaskIDs("");
        eventInfo.setFailedTaskIDs("");
        eventInfo.setSkippedTaskIDs(toCommaSeparatedIdList(plannedComTaskExecutions));
        return eventInfo;
    }

    public static ConnectionTaskCompletionEventInfo forCompletion(ConnectionTask<?, ?> connectionTask, ComPort comPort, ComSession comSession, List<ComTaskExecution> successfulComTaskExecutions, List<ComTaskExecution> failedComTaskExecutions, List<ComTaskExecution> skippedComTaskExecutions) {
        ConnectionTaskCompletionEventInfo eventInfo = new ConnectionTaskCompletionEventInfo();
        eventInfo.setComSessionId(comSession.getId());
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
        this.setDeviceIdentifier(connectionTask.getDevice().getId());
        this.setConnectionTaskId(connectionTask.getId());
    }

    private void setComPort(ComPort comPort) {
        this.setComServer(comPort.getComServer());
        this.setComPortId(comPort.getId());
        this.setComPortName(comPort.getName());
    }

    private void setComServer(ComServer comServer) {
        this.setComServerId(comServer.getId());
        this.setComServerName(comServer.getName());
    }

    public long getComSessionId() {
        return comSessionId;
    }

    public void setComSessionId(long comSessionId) {
        this.comSessionId = comSessionId;
    }

    public long getComPortId() {
        return comPortId;
    }

    public void setComPortId(long comPortId) {
        this.comPortId = comPortId;
    }

    public String getComPortName() {
        return comPortName;
    }

    public void setComPortName(String comPortName) {
        this.comPortName = comPortName;
    }

    public long getComServerId() {
        return comServerId;
    }

    public void setComServerId(long comServerId) {
        this.comServerId = comServerId;
    }

    public String getComServerName() {
        return comServerName;
    }

    public void setComServerName(String comServerName) {
        this.comServerName = comServerName;
    }

    public long getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(long deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
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