package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.events.EventType;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Represents the Installer for the DeviceConfiguration module
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:42)
 */
class Installer implements FullInstaller {

    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    Installer(DataModel dataModel, EventService eventService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create event types for SCH",
                this::createEventTypes,
                logger
        );


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
            eventTypeBuilder.create();
        }
    }

}