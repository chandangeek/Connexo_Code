/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.stream.Stream;

public class UpgraderV10_8_1 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    public UpgraderV10_8_1(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8, 1));
        try (Connection connection = dataModel.getConnection(true);
             Statement statement = connection.createStatement()) {
            String table = "PR1_IP_OUT_WKUP_CT";
            if (dataModel.doesTableExist(table)) {
                ImmutableMap<String, Boolean> statements = new ImmutableMap.Builder<String, Boolean>()
                        .put("ALTER TABLE PR1_IP_OUT_WKUP_CT DROP COLUMN STARTTIME CASCADE CONSTRAINTS", dataModel.doesColumnExist(table, "STARTTIME"))
                        .put("ALTER TABLE PR1_IP_OUT_WKUP_CT DROP COLUMN ENDTIME CASCADE CONSTRAINTS", dataModel.doesColumnExist(table, "ENDTIME"))
                        .build();
                statements.entrySet().stream().filter(Map.Entry::getValue)
                        .forEach(e -> execute(statement, e.getKey()));
            }
            if (dataModel.doesTableExist("PR1_IP_OUT_WKUP_CTJRNL")) {
                Stream.of("DROP TABLE PR1_IP_OUT_WKUP_CTJRNL")
                        .forEach(e -> execute(statement, e));
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }
}
