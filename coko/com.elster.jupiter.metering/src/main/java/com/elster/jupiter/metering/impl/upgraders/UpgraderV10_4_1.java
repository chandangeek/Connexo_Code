/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.Statement;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_4_1 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    public UpgraderV10_4_1(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                ImmutableList.of(
                        "ALTER TABLE MTR_ENDDEVICESTATUS ADD (ORIGINATOR VARCHAR2(80 CHAR))",
                        "UPDATE MTR_ENDDEVICESTATUS SET ORIGINATOR = USERNAME"
                ).forEach(command -> execute(statement, command));
            }
        });

        dataModelUpgrader.upgrade(this.dataModel, version(10, 4, 1));
    }
}
