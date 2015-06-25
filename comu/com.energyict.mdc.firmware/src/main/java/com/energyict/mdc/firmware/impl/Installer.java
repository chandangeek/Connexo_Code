package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.ExceptionCatcher;
import com.energyict.mdc.firmware.security.Privileges;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;
    private final UserService userService;

    Installer(DataModel dataModel, EventService eventService, MessageService messageService, UserService userService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.messageService = messageService;
        this.userService = userService;
    }

    void install() {
        ExceptionCatcher.executing(
                this::installDataModel,
                this::createPrivileges,
                this::createJupiterEventsSubscriber
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
        createEventTypesIfNotExist();
    }

    private void installDataModel() {
        dataModel.install(true, true);
    }

    private void createPrivileges() {
        userService.createResourceWithPrivileges("MDC", "firmware.campaigns", "firmware.campaigns.description", new String[]
                {Privileges.VIEW_FIRMWARE_CAMPAIGN, Privileges.ADMINISTRATE_FIRMWARE_CAMPAIGN});
    }

    private void createJupiterEventsSubscriber() {
        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if(destinationSpec.isPresent()){
            DestinationSpec jupiterEvents = destinationSpec.get();
            Arrays.asList(FirmwareCampaignHandlerFactory.FIRMWARE_CAMPAIGNS_SUBSCRIBER)
                    .stream()
                    .filter(subscriber -> !jupiterEvents.getSubscribers().stream().anyMatch(s -> s.getName().equals(subscriber)))
                    .forEach(jupiterEvents::subscribe);
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