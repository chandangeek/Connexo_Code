package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import java.util.Arrays;

class InstallerImpl {

    private final DataModel dataModel;
    private final EventService eventService;

    InstallerImpl(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    public void install() {

        ExceptionCatcher.executing(
                this::installDataModel,
                this::installEventTypes
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    private void installDataModel() {
        dataModel.install(true, true);
    }

    private void installEventTypes() {
        Arrays.stream(EventType.values()).forEach(eventType -> eventType.install(eventService));
    }
}
