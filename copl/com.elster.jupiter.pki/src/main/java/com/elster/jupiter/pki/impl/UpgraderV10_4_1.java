/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.impl.importers.csr.CSRImporterQueueInstaller;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_4_1 implements Upgrader {
    private final DataModel dataModel;
    private final CSRImporterQueueInstaller csrImporterQueueInstaller;
    private final EventService eventService;

    @Inject
    public UpgraderV10_4_1(DataModel dataModel, CSRImporterQueueInstaller csrImporterQueueInstaller, EventService eventService) {
        this.dataModel = dataModel;
        this.csrImporterQueueInstaller = csrImporterQueueInstaller;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 1));
        csrImporterQueueInstaller.installIfNotPresent();
        installNewEventTypes();
    }

    private void installNewEventTypes() {
        EventType.SECURITY_ACCESSOR_VALIDATE_DELETE.createIfNotExists(eventService);
    }
}
