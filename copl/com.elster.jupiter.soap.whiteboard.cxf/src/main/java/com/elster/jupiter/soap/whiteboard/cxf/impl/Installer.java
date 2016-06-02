package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by bvn on 5/3/16.
 */
public class Installer {
    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    public Installer(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    public void install() {
        installOrm();
        createEventTypes();
    }

    protected void installOrm() {
        try {
            this.dataModel.install(true, true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                if (!this.eventService.getEventType(eventType.topic()).isPresent()) {
                    this.eventService.buildEventTypeWithTopic(eventType.topic())
                            .name(eventType.name())
                            .component(WebServicesService.COMPONENT_NAME)
                            .category("Crud")
                            .scope("System").create();
                    //                            .shouldPublish();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

}
