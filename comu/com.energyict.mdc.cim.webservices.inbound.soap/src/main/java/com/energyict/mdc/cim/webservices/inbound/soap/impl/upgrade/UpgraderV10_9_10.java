/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

//for compatibility with release/Enexis_10.9.9 branch
public class UpgraderV10_9_10 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_9_10(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9, 10));
    }
}