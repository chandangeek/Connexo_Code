package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.sql.Statement;

import static com.elster.jupiter.orm.Version.version;

class UpgraderV10_2_1 implements Upgrader {

    private static final Version VERSION = version(10, 2, 1);

    private final DataModel dataModel;

    @Inject
    UpgraderV10_2_1(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                ImmutableList.of(
                        "ALTER TABLE MTG_QUERY_EDG_CONDITION_VALUE DISABLE CONSTRAINT MTG_FK_QUERY_EDG_VALUE2COND",
                        "UPDATE MTG_QUERY_EDG_CONDITION SET PROPERTY = 'name' where PROPERTY = 'mRID'",
                        "UPDATE MTG_QUERY_EDG_CONDITION_VALUE SET PROPERTY = 'name' where PROPERTY = 'mRID'",
                        "ALTER TABLE MTG_QUERY_EDG_CONDITION_VALUE ENABLE CONSTRAINT MTG_FK_QUERY_EDG_VALUE2COND"
                ).forEach(command -> execute(statement, command));
            }
        });

        dataModelUpgrader.upgrade(this.dataModel, VERSION);
    }
}
