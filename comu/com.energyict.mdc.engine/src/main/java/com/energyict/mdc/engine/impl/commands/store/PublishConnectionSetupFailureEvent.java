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
 * Models a {@link DeviceCommand} that publishes an event
 * that notifies interested parties that a {@link ConnectionTask}
 * failed to setup a new connection.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-28 (16:38)
 */
public class PublishConnectionSetupFailureEvent extends PublishConnectionTaskEventDeviceCommand {

    private final List<ComTaskExecution> comTaskExecutions;

    public PublishConnectionSetupFailureEvent(
                ConnectionTask connectionTask, ComPort comPort,
                List<ComTaskExecution> comTaskExecutions,
                ServiceProvider serviceProvider) {
        super(connectionTask, comPort, serviceProvider);
        this.comTaskExecutions = Collections.unmodifiableList(comTaskExecutions);
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO, EventService eventService, ConnectionTask connectionTask, ComPort comPort, ComSession comSession) {
        eventService.postEvent(
                EventType.DEVICE_CONNECTION_FAILURE.topic(),
                ConnectionTaskCompletionEventInfo.forFailure(
                        connectionTask,
                        comPort,
                        comSession,
                        this.comTaskExecutions));
    }

    @Override
    protected String topic() {
        return EventType.DEVICE_CONNECTION_FAILURE.topic();
    }

    @Override
    public String getDescriptionTitle() {
        return "Publish connection setup failure event";
    }

}