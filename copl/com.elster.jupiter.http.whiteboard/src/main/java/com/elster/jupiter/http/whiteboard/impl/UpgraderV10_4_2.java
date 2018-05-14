/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;

import java.sql.Statement;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_4_2 implements Upgrader {

    private final DataModel dataModel;
    private final Installer installerV10_4_2;

    @Inject
    UpgraderV10_4_2(DataModel dataModel, Installer installerV10_4_2) {
        this.dataModel = dataModel;
        this.installerV10_4_2 = installerV10_4_2;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 4));
        installerV10_4_2.createAdditionalEventTypes();
    }

}

