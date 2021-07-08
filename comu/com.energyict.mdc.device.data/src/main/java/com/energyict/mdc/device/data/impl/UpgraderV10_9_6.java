/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.logging.Logger;

public class UpgraderV10_9_6 implements Upgrader {

    private final InstallerV10_8_1Impl installerV10_8_1;

    @Inject
    UpgraderV10_9_6(InstallerV10_8_1Impl installerV10_8_1) {
        this.installerV10_8_1 = installerV10_8_1;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        installerV10_8_1.createOrUpdateDashBordProcedures(Logger.getAnonymousLogger());
    }
}
