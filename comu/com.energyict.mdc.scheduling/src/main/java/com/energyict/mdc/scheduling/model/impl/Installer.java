package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.events.EventType;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the Installer for the DeviceConfiguration module
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:42)
 */
public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;

    public Installer(DataModel dataModel, EventService eventService, UserService userService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.userService = userService;
    }

    public void install(boolean executeDdl) {
        try {
            this.dataModel.install(executeDdl, true);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        createEventTypes();
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            install(eventType);
        }
    }

    @TransactionRequired
    void install(EventType eventType) {
        if (!eventService.getEventType(eventType.topic()).isPresent()) {
            EventTypeBuilder eventTypeBuilder = this.eventService.buildEventTypeWithTopic(eventType.topic())
                    .name(eventType.name())
                    .component(SchedulingService.COMPONENT_NAME)
                    .category("Crud")
                    .scope("System");
            eventType.addCustomProperties(eventTypeBuilder);
            eventTypeBuilder.create().save();
        }
    }

}