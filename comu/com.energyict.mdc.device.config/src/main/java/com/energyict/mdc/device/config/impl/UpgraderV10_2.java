package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

class UpgraderV10_2 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    UpgraderV10_2(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 2));

        List<String> sql = new ArrayList<>();
        sql.add("UPDATE DTC_PARTIALCONNECTIONTASK SET simultaneousconnections=1 where simultaneousconnections = 0");
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
    }
}
