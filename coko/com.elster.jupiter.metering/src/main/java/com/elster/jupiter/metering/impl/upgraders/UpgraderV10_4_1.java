/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.EndDeviceControlTypeInstallerUtil;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.Statement;
import java.util.logging.Logger;

public class UpgraderV10_4_1 implements Upgrader {
    private final DataModel dataModel;
    private final ServerMeteringService meteringService;
    private final Logger logger;

    @Inject
    public UpgraderV10_4_1(DataModel dataModel, ServerMeteringService meteringService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 1));
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                ImmutableList.of(
                        "UPDATE MTR_ENDDEVICESTATUS SET ORIGINATOR = USERNAME"
                ).forEach(command -> execute(statement, command));
            }
        });
        new EndDeviceControlTypeInstallerUtil(meteringService).createEndDeviceControlTypes(logger);
    }
}
