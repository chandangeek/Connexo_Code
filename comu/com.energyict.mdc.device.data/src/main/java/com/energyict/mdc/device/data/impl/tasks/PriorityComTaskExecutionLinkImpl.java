/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.common.tasks.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.CreateEventType;
import com.energyict.mdc.device.data.impl.DeleteEventType;
import com.energyict.mdc.device.data.impl.PersistentIdObject;
import com.energyict.mdc.device.data.impl.UpdateEventType;
import com.energyict.mdc.device.data.impl.constraintvalidators.ComTasksMustBeEnabledByDeviceConfiguration;
import com.energyict.mdc.device.data.impl.constraintvalidators.ManuallyScheduledNextExecSpecRequired;
import com.energyict.mdc.device.data.impl.constraintvalidators.SaveScheduled;
import com.energyict.mdc.device.data.impl.constraintvalidators.SharedScheduleComScheduleRequired;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.logging.Logger;

@ComTasksMustBeEnabledByDeviceConfiguration(groups = {Save.Create.class})
@ManuallyScheduledNextExecSpecRequired(groups = {SaveScheduled.class})
@SharedScheduleComScheduleRequired(groups = {Save.Create.class, Save.Update.class})
@ComTaskMustBeFirmwareManagement(groups = {Save.Create.class, Save.Update.class})
@ConsumableConnectionFunctionMustBeSupportedByTheDeviceProtocol(groups = {Save.Create.class, Save.Update.class})
public class PriorityComTaskExecutionLinkImpl extends PersistentIdObject<PriorityComTaskExecutionLink> implements ServerPriorityComTaskExecutionLink {

    private static final Logger LOGGER = Logger.getLogger(PriorityComTaskExecutionLinkImpl.class.getName());
    private final CommunicationTaskService communicationTaskService;
    private long id;
    private long comTaskExecutionId;
    private Reference<ComTaskExecution> comTaskExecution = ValueReference.absent();
    private Instant nextExecutionTimestamp;
    private Instant modDate;
    private Clock clock;
    private int priority;
    private int comPortId;
    private ScheduledConnectionTask connectionTask;
    private Reference<ComPort> comPort = ValueReference.absent();

    @Inject
    public PriorityComTaskExecutionLinkImpl(DataModel dataModel, EventService eventService, CommunicationTaskService communicationTaskService,
                                            Clock clock, Thesaurus thesaurus) {
        super(PriorityComTaskExecutionLink.class, dataModel, eventService, thesaurus);
        this.communicationTaskService = communicationTaskService;
        this.clock = clock;
    }

    public PriorityComTaskExecutionLink init(ResultSet resultSet, ScheduledConnectionTask connectionTask) throws SQLException {
        id = resultSet.getLong(1);
        comTaskExecutionId = resultSet.getLong(2);
        // TODO high-prio: check if nextExecutionTimestamp shouldn't be in seconds instead of millis!
        nextExecutionTimestamp = Instant.ofEpochMilli(resultSet.getLong(3));
        priority = resultSet.getInt(4);
        comPortId = resultSet.getInt(5);
        this.connectionTask = connectionTask;
        comTaskExecution.set(communicationTaskService.findComTaskExecution(comTaskExecutionId).get());

        return this;
    }

    public PriorityComTaskExecutionLink init(ComTaskExecution comTaskExecution) {
        this.comTaskExecution.set(comTaskExecution);
        comTaskExecutionId = comTaskExecution.getId();
        priority = comTaskExecution.getExecutionPriority();
        nextExecutionTimestamp = clock.instant();

        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public ComTaskExecution getComTaskExecution() {
        return new PriorityComTaskExecutionImpl(comTaskExecution.orElseGet(() ->
                communicationTaskService.findComTaskExecution(comTaskExecutionId).get()), this);
    }

    @Override
    public ConnectionTask getConnectionTask() {
        return connectionTask;
    }

    @Override
    public Instant getNextExecutionTimestamp() {
        return nextExecutionTimestamp;
    }

    @Override
    public boolean isExecuting() {
        return comPortId != 0;
    }

    public ComPort getExecutingComPort() {
        return comPort.orNull();
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    protected CreateEventType createEventType() {
        return null;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return null;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return null;
    }

    @Override
    public void notifyCreated() {
        LOGGER.info("Created " + this.getClass().getName() + " with comTaskExecutionId=" + comTaskExecutionId + " nextExecutionTimestamp=" + nextExecutionTimestamp + " comPortId=" + comPortId);
    }

    @Override
    public void notifyUpdated() {}

    @Override
    public void notifyDeleted() {
        LOGGER.info("Deleted " + this.getClass().getName() + " with comTaskExecutionId=" + comTaskExecutionId + " nextExecutionTimestamp=" + nextExecutionTimestamp + " comPortId=" + comPortId);
    }

    @Override
    protected void doDelete() {
        getDataModel().remove(this);
    }

    @Override
    protected void validateDelete() {

    }

    @Override
    public boolean attemptLock(ComPort comPort) {
        setExecutingComPort(comPort);
        return true;
    }

    @Override
    public void unlock() {
        comPort = ValueReference.absent();
        update(ComTaskExecutionFields.COMPORT.fieldName());
    }

    @Override
    public void injectConnectionTask(OutboundConnectionTask connectionTask) {
        if (getComTaskExecution() != null) {
            ((ServerComTaskExecution) getComTaskExecution()).injectConnectionTask(connectionTask);
        }
    }

    @Override
    public void setLockedComPort(ComPort comPort) {
        setExecutingComPort(comPort);
        update(ComTaskExecutionFields.COMPORT.fieldName());
    }

    @Override
    public void executionRescheduled(Instant nextExecutionTimestamp) {
        setExecutingComPort(null);
        this.nextExecutionTimestamp = nextExecutionTimestamp;
        update(ComTaskExecutionFields.COMPORT.fieldName(), ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName());
    }

    private void setExecutingComPort(ComPort comPort) {
        this.comPort.set(comPort);
    }
}
