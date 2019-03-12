/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_6 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    UpgraderV10_6(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 6));
        List<String> sql = new ArrayList<>();
        sql.add("ALTER TABLE DTC_SECACCTYPES_ON_DEVICETYPE ADD DEFAULTKEY varchar2(4000 char)");
        sql.add("ALTER TABLE DTC_SECACCTYPESONDEVTYPE_JRNL ADD DEFAULTKEY varchar2(4000 char)");
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
    }
}
