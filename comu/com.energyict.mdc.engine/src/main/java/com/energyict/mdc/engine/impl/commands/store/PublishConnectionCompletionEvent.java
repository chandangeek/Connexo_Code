/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.events.EventService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.impl.ConnectionTaskCompletionEventInfo;
import com.energyict.mdc.engine.impl.EventType;
import com.energyict.mdc.engine.impl.core.ComServerDAO;

import java.util.Collections;
import java.util.List;

/**
 * Models a {@link DeviceCommand} that publishes an event that notifies
 * interested parties that a {@link ConnectionTask} completed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-28 (16:38)
 */
public class PublishConnectionCompletionEvent extends PublishConnectionTaskEventDeviceCommand {

    private final List<ComTaskExecution> successfulComTaskExecutions;
    private final List<ComTaskExecution> failedComTaskExecutions;
    private final List<ComTaskExecution> notExecutedComTaskExecutions;

    public PublishConnectionCompletionEvent(
            ConnectionTask connectionTask, ComPort comPort,
            List<ComTaskExecution> successfulComTaskExecutions,
            List<ComTaskExecution> failedComTaskExecutions,
            List<ComTaskExecution> notExecutedComTaskExecutions,
            ServiceProvider serviceProvider) {
        super(connectionTask, comPort, serviceProvider);
        this.successfulComTaskExecutions = Collections.unmodifiableList(successfulComTaskExecutions);
        this.failedComTaskExecutions = Collections.unmodifiableList(failedComTaskExecutions);
        this.notExecutedComTaskExecutions = Collections.unmodifiableList(notExecutedComTaskExecutions);
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO, EventService eventService, ConnectionTask connectionTask, ComPort comPort, ComSession comSession) {
        eventService.postEvent(
                this.topic(),
                ConnectionTaskCompletionEventInfo.forCompletion(
                        connectionTask,
                        comPort,
                        comSession,
                        this.successfulComTaskExecutions,
                        this.failedComTaskExecutions,
                        this.notExecutedComTaskExecutions));
    }

    @Override
    protected String topic() {
        return EventType.DEVICE_CONNECTION_COMPLETION.topic();
    }

    @Override
    public String getDescriptionTitle() {
        return "Publish connection completion event";
    }

}