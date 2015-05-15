package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;
    private final TaskService taskService;

    Installer(DataModel dataModel, EventService eventService, MessageService messageService, TaskService taskService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.messageService = messageService;
        this.taskService = taskService;
    }

    void install() {
        ExceptionCatcher.executing(
                this::installDataModel,
                this::createDestinationAndSubscriber
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
        createEventTypesIfNotExist();
    }

    private void installDataModel() {
        dataModel.install(true, true);
    }

    private void createDestinationAndSubscriber() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(FirmwareCampaignsHandlerFactory.FIRMWARE_CAMPAIGNS_DESTINATION);
        if (!destinationSpec.isPresent()) {
            DestinationSpec spec = queueTableSpec.createDestinationSpec(FirmwareCampaignsHandlerFactory.FIRMWARE_CAMPAIGNS_DESTINATION, 60);
            spec.save();
            spec.activate();
            spec.subscribe(FirmwareCampaignsHandlerFactory.FIRMWARE_CAMPAIGNS_SUBSCRIBER);
            destinationSpec = Optional.of(spec);
        }

        Optional<RecurrentTask> recurrentTask = taskService.getRecurrentTask(FirmwareCampaignsHandlerFactory.FIRMWARE_CAMPAIGNS_TASK);
        if (!recurrentTask.isPresent()) {
            RecurrentTaskBuilder builder = taskService.newBuilder()
                    .setName(FirmwareCampaignsHandlerFactory.FIRMWARE_CAMPAIGNS_TASK)
                    .setScheduleExpression(FirmwareCampaignsHandlerFactory.FIRMWARE_CAMPAIGNS_SCHEDULE_EXPRESSION)
                    .setDestination(destinationSpec.get())
                    .setPayLoad(FirmwareCampaignsHandlerFactory.class.getName());
            builder.scheduleImmediately();
            builder.build().save();
        }
    }

    private void createEventTypesIfNotExist() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.createIfNotExists(this.eventService);
            } catch (Exception e) {
                this.logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

}
