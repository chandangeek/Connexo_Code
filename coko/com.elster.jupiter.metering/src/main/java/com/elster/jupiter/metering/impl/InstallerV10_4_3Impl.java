package com.elster.jupiter.metering.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

public class InstallerV10_4_3Impl implements FullInstaller {

    private final ServerMeteringService meteringService;

    @Inject
    InstallerV10_4_3Impl(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Create default End Device Event Types",
                () -> InstallerImpl.createEndDeviceEventTypes(meteringService, getClass(), logger),
                logger
        );

    }

}