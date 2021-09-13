/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;

import javax.inject.Inject;

public class V10_8_16SimpleUpgrader implements Upgrader {
    public static final Version VERSION = Version.version(10, 8, 16);
    private final DataModel dataModel;

    @Inject
    public V10_8_16SimpleUpgrader(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
    }
}
