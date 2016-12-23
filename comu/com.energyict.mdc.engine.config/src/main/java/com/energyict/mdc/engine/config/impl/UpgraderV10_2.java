package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_2 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    UpgraderV10_2(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 2));
        List<String> sql = new ArrayList<>();
        sql.add("UPDATE MDC_COMSERVER SET \"SERVERNAME\" = NAME");
        sql.add("ALTER TABLE MDC_COMSERVER MODIFY \"SERVERNAME\" NOT NULL");
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
    }
}
