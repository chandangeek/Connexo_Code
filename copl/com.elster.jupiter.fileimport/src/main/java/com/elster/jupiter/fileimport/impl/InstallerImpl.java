package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import javax.inject.Inject;
import java.util.Arrays;

class InstallerImpl implements FullInstaller {

    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    InstallerImpl(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader) {

        ExceptionCatcher.executing(
                () -> dataModelUpgrader.upgrade(dataModel, Version.latest()),
                this::installEventTypes
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();

    }

    private void installEventTypes() {
        Arrays.stream(EventType.values()).forEach(eventType -> eventType.install(eventService));
    }
}
