package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.impl.InstallerV10_4_3Impl;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.logging.Logger;

public class UpgraderV10_6 implements Upgrader {
    private final DataModel dataModel;
    private final EventService eventService;
    private final InstallerV10_4_3Impl installerV10_4_3;
    private final Logger logger;

    @Inject
    public UpgraderV10_6(DataModel dataModel, EventService eventService, InstallerV10_4_3Impl installerV10_4_3) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.installerV10_4_3 = installerV10_4_3;
        this.logger = Logger.getLogger(this.getClass().getName());
    }


    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 6));
        EventType.METER_LINKED.install(eventService);
        EventType.METER_UNLINKED.install(eventService);

        installerV10_4_3.install(dataModelUpgrader, logger);
    }
}
