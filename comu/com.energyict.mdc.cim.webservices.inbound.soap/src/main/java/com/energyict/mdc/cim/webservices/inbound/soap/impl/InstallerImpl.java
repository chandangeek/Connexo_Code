/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

public class InstallerImpl implements FullInstaller {
    private final Installer installer;

    @Inject
    public InstallerImpl(Installer installer) {
        this.installer = installer;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Create service call types",
                installer::createServiceCallTypes,
                logger
        );
    }
}
