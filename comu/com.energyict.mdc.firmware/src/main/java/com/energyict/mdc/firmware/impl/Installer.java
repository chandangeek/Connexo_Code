package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.exception.ExceptionCatcher;
import com.energyict.mdc.device.data.impl.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;

    Installer(DataModel dataModel, Thesaurus thesaurus, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    void install() {
        ExceptionCatcher.executing(
                this::installDataModel
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
        createEventTypesIfNotExist();
    }

    private void installDataModel() {
        dataModel.install(true, true);
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
