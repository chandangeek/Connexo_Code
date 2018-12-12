/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.events.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.Statement;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_4_1 implements Upgrader {
    private static final Version VERSION = version(10, 4, 1);

    private DataModel dataModel;

    @Inject
    public UpgraderV10_4_1(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                ImmutableList.of(
                        "UPDATE EVT_EVENTPROPERTYTYPE SET accesspath = replace(accesspath, 'createdDateTime.epochSecond', 'createdDateTimeMillis') WHERE  accesspath = 'createdDateTime.epochSecond'")
                        .forEach(command -> execute(statement, command));
            }
        });
    }
}
