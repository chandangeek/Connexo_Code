/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.Statement;

class UpgraderV10_5 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    public UpgraderV10_5(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 5));

        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                ImmutableList.of(
                        "UPDATE MSG_DESTINATIONSPEC SET QUEUE_TYPE_NAME = NAME",
                        "UPDATE MSG_DESTINATIONSPEC SET IS_EXTRA_QUEUE_ENABLED = 'Y' WHERE NAME = 'EstimationTask'",
                        "UPDATE MSG_DESTINATIONSPEC SET IS_EXTRA_QUEUE_ENABLED = 'Y' WHERE NAME = 'DataValidation'",
                        "UPDATE MSG_DESTINATIONSPEC SET IS_EXTRA_QUEUE_ENABLED = 'Y' WHERE NAME = 'DataExport'"
                ).forEach(command -> execute(statement, command));
            }
        });
    }

}
