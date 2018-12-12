/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_4_1 implements Upgrader {

    private final DataModel dataModel;
    private final Installer installerV10_4_1;

    @Inject
    UpgraderV10_4_1(DataModel dataModel, Installer installerV10_4_1) {
        this.dataModel = dataModel;
        this.installerV10_4_1 = installerV10_4_1;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 4));
        installerV10_4_1.createEventTypes();
    }

}
