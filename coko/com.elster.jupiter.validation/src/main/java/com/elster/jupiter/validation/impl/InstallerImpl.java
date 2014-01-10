package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;

public class InstallerImpl {

    private final DataModel dataModel;
    private final EventService eventService;

    public InstallerImpl(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    public void install(boolean executeDdl, boolean updateOrm) {
        dataModel.install(executeDdl, updateOrm);
        createEventTypes();
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(eventService);
        }
    }

}
