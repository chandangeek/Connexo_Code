package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.impl.ConnectionTaskCompletionEventInfo;
import com.energyict.mdc.engine.impl.EventType;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;

import com.elster.jupiter.events.EventService;

/**
 * Models a {@link DeviceCommand} that publishes an event
 * that notifies interested parties about the execution of {@link ConnectionTask}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-28 (16:38)
 */
public abstract class PublishConnectionTaskEventDeviceCommand extends DeviceCommandImpl {

    private final EventService eventService;
    private final ConnectionTask connectionTask;
    private final ComPort comPort;
    private CreateComSessionDeviceCommand createComSessionDeviceCommand;

    public PublishConnectionTaskEventDeviceCommand(EventService eventService, ConnectionTask connectionTask, ComPort comPort) {
        super();
        this.eventService = eventService;
        this.connectionTask = connectionTask;
        this.comPort = comPort;
    }

    public void setCreateComSessionDeviceCommand(CreateComSessionDeviceCommand createComSessionDeviceCommand) {
        this.createComSessionDeviceCommand = createComSessionDeviceCommand;
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        this.doExecute(comServerDAO, this.eventService, this.connectionTask, this.comPort, this.createComSessionDeviceCommand.getComSession());
    }

    protected abstract void doExecute(ComServerDAO comServerDAO, EventService eventService, ConnectionTask connectionTask, ComPort comPort, ComSession comSession);

    protected abstract String topic();

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        builder.addListProperty("eventTopic").append(topic());
        builder.addListProperty("connectionTask").append(this.connectionTask.getId());
        builder.addListProperty("comPort").append(this.comPort.getId());
    }

}