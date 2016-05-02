package com.elster.jupiter.license.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.List;

public class Installer implements FullInstaller {

    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    public Installer(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        createEventTypes();
    }

    private void createEventTypes() {
        List<com.elster.jupiter.events.EventType> eventTypesForComponent = eventService.getEventTypesForComponent(LicenseService.COMPONENTNAME);
        for (EventType eventType : EventType.values()) {
            if (!eventTypesForComponent.stream().anyMatch(et -> et.getName().equals(eventType.name()))) {
                eventType.install(eventService);
            }
        }
    }

}
