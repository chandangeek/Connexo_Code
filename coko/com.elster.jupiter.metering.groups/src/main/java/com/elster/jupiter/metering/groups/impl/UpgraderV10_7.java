/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.Statement;

public class UpgraderV10_7 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_7(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                ImmutableList.of(
                        "UPDATE MTG_ED_GROUP SET SEARCHDOMAIN = 'com.energyict.mdc.common.device.data.Device' WHERE SEARCHDOMAIN = 'com.energyict.mdc.device.data.Device'"
                ).forEach(command -> execute(statement, command));
            }
        });

        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7));
    }
}
