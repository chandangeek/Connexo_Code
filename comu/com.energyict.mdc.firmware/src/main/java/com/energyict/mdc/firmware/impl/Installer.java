package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Installer implements FullInstaller {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;
    private final UserService userService;

    @Inject
    Installer(DataModel dataModel, EventService eventService, MessageService messageService, UserService userService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader) {
        ExceptionCatcher.executing(
                () -> dataModelUpgrader.upgrade(dataModel, Version.latest()),
                this::createJupiterEventsSubscriber
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
        createEventTypesIfNotExist();

    }

    private void createJupiterEventsSubscriber() {
        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if (destinationSpec.isPresent()) {
            DestinationSpec jupiterEvents = destinationSpec.get();
            if (!jupiterEvents.getSubscribers().stream().anyMatch(s -> s.getName().equals(FirmwareCampaignHandlerFactory.FIRMWARE_CAMPAIGNS_SUBSCRIBER))) {
                Condition or = Condition.FALSE;
                for (FirmwareCampaignHandler.Handler handler : FirmwareCampaignHandler.Handler.values()) {
                    or = or.or(DestinationSpec.whereCorrelationId().isEqualTo(handler.getTopic()));
                }
                jupiterEvents.subscribe(FirmwareCampaignHandlerFactory.FIRMWARE_CAMPAIGNS_SUBSCRIBER, or);
            }
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