package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.impl.EndDeviceControlTypeInstallerUtil;
import com.elster.jupiter.metering.impl.InstallerV10_4_3Impl;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.logging.Logger;

public class UpgraderV10_6 implements Upgrader {
    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    public UpgraderV10_6(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }


    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 6));
        EventType.METER_LINKED.install(eventService);
        EventType.METER_UNLINKED.install(eventService);
    }
}
