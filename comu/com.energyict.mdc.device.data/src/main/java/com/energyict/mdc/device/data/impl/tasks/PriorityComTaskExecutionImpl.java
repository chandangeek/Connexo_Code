/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.scheduling.NextExecutionSpecs;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionTrigger;
import com.energyict.mdc.common.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecution;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.common.tasks.ServerComTaskExecution;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;

import javax.xml.bind.annotation.XmlElement;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class PriorityComTaskExecutionImpl implements PriorityComTaskExecution {

    private static final Logger LOGGER = Logger.getLogger(PriorityComTaskExecutionImpl.class.getName());
    private long id;
    private ServerComTaskExecution comTaskExecution;
    private ServerPriorityComTaskExecutionLink comTaskExecutionLink;

    public PriorityComTaskExecutionImpl(ComTaskExecution comTaskExecution, ServerPriorityComTaskExecutionLink comTaskExecutionLink) {
        this.comTaskExecution = (ServerComTaskExecution) comTaskExecution;
        this.comTaskExecutionLink = comTaskExecutionLink;
        id = comTaskExecution.getId();
    }

    @Override
    public boolean usesSharedSchedule() {
        return false;
    }

    @Override
    public boolean isScheduledManually() {
        return false;
    }

    @Override
    public boolean isAdHoc() {
        return true;
    }

    @Override
    public boolean isFirmware() {
        return false;
    }

    @Override
    public Device getDevice() {
        return comTaskExecution.getDevice();
    }

    @Override
    public void setDevice(Device device) {
        comTaskExecution.setDevice(device);
    }

    @Override
    public ComPort getExecutingComPort() {
        return comTaskExecutionLink.getExecutingComPort();
    }

    @Override
    public boolean isExecuting() {
        return comTaskExecution.isExecuting() && comTaskExecutionLink.isExecuting();
    }

    @Override
    public int getExecutionPriority() {
        return comTaskExecution.getExecutionPriority();
    }

    @Override
    public TaskStatus getStatus() {
        return comTaskExecution.getStatus();
    }

    @Override
    public void setStatus(TaskStatus status) {
        comTaskExecution.setStatus(status);
    }

    @Override
    public String getStatusDisplayName() {
        return comTaskExecution.getStatusDisplayName();
    }

    @Override
    public boolean isOnHold() {
        return comTaskExecution.isOnHold();
    }

    @Override
    public Instant getNextExecutionTimestamp() {
        return comTaskExecutionLink.getNextExecutionTimestamp();
    }

    @Override
    public int getMaxNumberOfTries() {
        return comTaskExecution.getMaxNumberOfTries();
    }

    @Override
    public int getCurrentTryCount() {
        return comTaskExecution.getCurrentTryCount();
    }

    @Override
    public boolean usesDefaultConnectionTask() {
        return comTaskExecution.usesDefaultConnectionTask();
    }

    @Override
    public Instant getExecutionStartedTimestamp() {
        return comTaskExecution.getExecutionStartedTimestamp();
    }

    @Override
    public void makeObsolete() {
        comTaskExecution.makeObsolete();
    }

    @Override
    public void connectionTaskRemoved() {
    }

    @Override
    public void injectConnectionTask(OutboundConnectionTask connectionTask) {
        comTaskExecution.injectConnectionTask(connectionTask);
    }

    @Override
    public boolean isObsolete() {
        return comTaskExecution.isObsolete();
    }

    @Override
    public Instant getObsoleteDate() {
        return comTaskExecution.getObsoleteDate();
    }

    @Override
    public Optional<ConnectionTask<?, ?>> getConnectionTask() {
        return comTaskExecution.getConnectionTask();
    }

    @Override
    public boolean usesSameConnectionTaskAs(ComTaskExecution anotherTask) {
        return comTaskExecution.usesSameConnectionTaskAs(anotherTask);
    }

    @Override
    public Optional<ComTaskExecutionSession> getLastSession() {
        return comTaskExecution.getLastSession();
    }

    @Override
    public Instant getLastExecutionStartTimestamp() {
        return comTaskExecution.getLastExecutionStartTimestamp();
    }

    @Override
    public Instant getLastSuccessfulCompletionTimestamp() {
        return comTaskExecution.getLastSuccessfulCompletionTimestamp();
    }

    @Override
    public Optional<NextExecutionSpecs> getNextExecutionSpecs() {
        return comTaskExecution.getNextExecutionSpecs();
    }

    @Override
    public boolean isIgnoreNextExecutionSpecsForInbound() {
        return comTaskExecution.isIgnoreNextExecutionSpecsForInbound();
    }

    @Override
    public Instant getPlannedNextExecutionTimestamp() {
        return comTaskExecution.getPlannedNextExecutionTimestamp();
    }

    @Override
    public int getPlannedPriority() {
        return comTaskExecution.getPlannedPriority();
    }

    @Override
    public void updateNextExecutionTimestamp() {
        comTaskExecution.updateNextExecutionTimestamp();
    }

    @Override
    public void putOnHold() {
        comTaskExecution.putOnHold();
        ((PriorityComTaskExecutionLinkImpl) comTaskExecutionLink).delete();
    }

    @Override
    public void resume() {
        comTaskExecution.resume();
    }

    @Override
    public void scheduleNow() {
    }

    @Override
    public void runNow() {
    }

    @Override
    public void schedule(Instant when) {
    }

    @Override
    public boolean shouldExecuteWithPriority() {
        return true;
    }

    @Override
    public ComTaskExecutionUpdater getUpdater() {
        return null;
    }

    @Override
    public List<ProtocolTask> getProtocolTasks() {
        return comTaskExecution.getProtocolTasks();
    }

    @Override
    public boolean executesComTask(ComTask comTask) {
        return comTaskExecution.executesComTask(comTask);
    }

    @Override
    public boolean isLastExecutionFailed() {
        return comTaskExecution.isLastExecutionFailed();
    }

    @Override
    public long getVersion() {
        return 0;
    }

    @Override
    public List<ComTaskExecutionTrigger> getComTaskExecutionTriggers() {
        return comTaskExecution.isUsingComTaskExecutionTriggers()
                ? comTaskExecution.getComTaskExecutionTriggers() : Collections.emptyList();
    }

    @Override
    public void addNewComTaskExecutionTrigger(Instant triggerTimeStamp) {
        comTaskExecution.addNewComTaskExecutionTrigger(triggerTimeStamp);
    }

    @Override
    public long getConnectionTaskId() {
        return comTaskExecution.getConnectionTaskId();
    }

    @Override
    public long getConnectionFunctionId() {
        return comTaskExecution.getConnectionFunctionId();
    }

    @Override
    public List<ComTask> getComTasks() {
        return comTaskExecution.getComTasks();
    }

    @Override
    public ComTask getComTask() {
        return comTaskExecution.getComTask();
    }

    @Override
    public void setComTask(ComTask comTask) {
        comTaskExecution.setComTask(comTask);
    }

    @Override
    public Optional<ComSchedule> getComSchedule() {
        return Optional.empty();
    }

    @Override
    public Optional<ConnectionFunction> getConnectionFunction() {
        return comTaskExecution.getConnectionFunction();
    }

    @Override
    public void setConnectionFunction(ConnectionFunction connectionFunction) {
        comTaskExecution.setConnectionFunction(connectionFunction);
    }

    public void executionCompleted() {
        comTaskExecution.executionCompleted();
        ((PriorityComTaskExecutionLinkImpl) comTaskExecutionLink).delete();
    }

    @Override
    public void executionFailed() {
        this.executionFailed(false);
    }

    @Override
    public void executionFailed(boolean noRetry) {
        ((ComTaskExecutionImpl) comTaskExecution).executionFailed(noRetry);
        LOGGER.info("[high-prio] device: " + comTaskExecution.getDevice().getName() + "; comTaskExecution status: " + comTaskExecution.getStatus());
        if (TaskStatus.Failed.equals(comTaskExecution.getStatus()) || TaskStatus.NeverCompleted.equals(comTaskExecution.getStatus())) {
            ((PriorityComTaskExecutionLinkImpl) comTaskExecutionLink).delete();
        } else {
            // execution attempt failed, will retry - should not delete yet the high-prio task
            LOGGER.info("[high-prio] execution attempt failed, id=" + comTaskExecutionLink.getId() + "; device: " + comTaskExecution.getDevice()
                    .getName() + "; rescheduled for " + comTaskExecution.getNextExecutionTimestamp());
            comTaskExecutionLink.executionRescheduled(comTaskExecution.getNextExecutionTimestamp());
        }
    }

    public void executionRescheduled(Instant rescheduleDate) {
        ((ComTaskExecutionImpl) comTaskExecution).executionRescheduled(rescheduleDate);
        LOGGER.info("[high-prio] device: " + comTaskExecution.getDevice().getName() + "; comTaskExecution status: " + comTaskExecution.getStatus());
        // depending on the current retry count, the comtaskexecution might be failed
        if (TaskStatus.Failed.equals(comTaskExecution.getStatus()) || TaskStatus.NeverCompleted.equals(comTaskExecution.getStatus())) {
            ((PriorityComTaskExecutionLinkImpl) comTaskExecutionLink).delete();
        } else {
            // execution attempt failed, will retry - should not delete yet the high-prio task
            LOGGER.info("[high-prio] execution attempt failed, id=" + comTaskExecutionLink.getId() + "; device: " + comTaskExecution.getDevice().getName() + "; rescheduled for " + rescheduleDate);
            comTaskExecutionLink.executionRescheduled(rescheduleDate);
        }
    }

    @Override
    public void executionRescheduledToComWindow(Instant comWindowStartDate) {
        ((ComTaskExecutionImpl) comTaskExecution).executionRescheduledToComWindow(comWindowStartDate);
        comTaskExecutionLink.executionRescheduled(comWindowStartDate);
    }

    @Override
    public void executionStarted(ComPort comPort) {
        ((ComTaskExecutionImpl) comTaskExecution).executionStarted(comPort);
    }

    public void unlock() {
        comTaskExecution.unlock();
        comTaskExecutionLink.unlock();
    }

    @Override
    public void setLockedComPort(ComPort comPort) {
        comTaskExecution.setLockedComPort(comPort);
        comTaskExecutionLink.setLockedComPort(comPort);
    }

    @Override
    public long getId() {
        return comTaskExecution.getId();
    }

    @Override
    public boolean isConfiguredToCollectRegisterData() {
        return comTaskExecution.isConfiguredToCollectRegisterData();
    }

    @Override
    public boolean isConfiguredToCollectLoadProfileData() {
        return comTaskExecution.isConfiguredToCollectLoadProfileData();
    }

    @Override
    public boolean isConfiguredToRunBasicChecks() {
        return comTaskExecution.isConfiguredToRunBasicChecks();
    }

    @Override
    public boolean isConfiguredToCheckClock() {
        return comTaskExecution.isConfiguredToCheckClock();
    }

    @Override
    public boolean isConfiguredToCollectEvents() {
        return comTaskExecution.isConfiguredToCollectEvents();
    }

    @Override
    public boolean isConfiguredToSendMessages() {
        return comTaskExecution.isConfiguredToSendMessages();
    }

    @Override
    public boolean isConfiguredToReadStatusInformation() {
        return comTaskExecution.isConfiguredToReadStatusInformation();
    }

    @Override
    public boolean isConfiguredToUpdateTopology() {
        return comTaskExecution.isConfiguredToUpdateTopology();
    }

    @Override
    public void sessionCreated(ComTaskExecutionSession session) {
        comTaskExecution.sessionCreated(session);
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
        //Ignore, only used for JSON
    }

    @Override
    public void removeSchedule() {
        comTaskExecution.removeSchedule();
    }

    @Override
    public boolean isTraced() {
        return comTaskExecution.isTraced();
    }
}
