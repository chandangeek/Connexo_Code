package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by bvn on 5/3/16.
 */
public class Installer implements FullInstaller {
    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    public Installer(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            if (!this.eventService.getEventType(eventType.topic()).isPresent()) {
                this.eventService.buildEventTypeWithTopic(eventType.topic())
                        .name(eventType.name())
                        .component(WebServicesService.COMPONENT_NAME)
                        .category("Crud")
                        .scope("System").create();
                //                            .shouldPublish();
            }
        }
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry("Create event types", this::createEventTypes, logger);
    }
}
