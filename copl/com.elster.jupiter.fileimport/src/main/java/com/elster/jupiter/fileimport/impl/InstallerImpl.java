package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.logging.Logger;

class InstallerImpl implements FullInstaller {

    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    InstallerImpl(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Install event types for FIM",
                this::installEventTypes,
                logger
        );
    }

    private void installEventTypes() {
        Arrays.stream(EventType.values()).forEach(eventType -> eventType.install(eventService));
    }
}
