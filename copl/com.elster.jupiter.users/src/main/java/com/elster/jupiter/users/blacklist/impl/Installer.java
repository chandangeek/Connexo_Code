/*
 *
 *  * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 *
 *
 */

package com.elster.jupiter.users.blacklist.impl;

/**
 * Insert your comments here.
 *
 * @author E492165 (M R)
 * @since 1/3/2020 (17:00)
 */
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

class Installer implements FullInstaller {
    private final DataModel dataModel;

    @Inject
    Installer(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7, 1));
    }
}