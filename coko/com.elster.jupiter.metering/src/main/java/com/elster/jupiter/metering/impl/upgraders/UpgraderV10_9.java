/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.impl.EndDeviceControlTypeInstallerUtil;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.logging.Logger;

public class UpgraderV10_9 implements Upgrader {

    private final ServerMeteringService meteringService;
    private final Logger logger;

    @Inject
    UpgraderV10_9(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        new EndDeviceControlTypeInstallerUtil(meteringService).createEndDeviceControlTypes(logger);
    }
}
