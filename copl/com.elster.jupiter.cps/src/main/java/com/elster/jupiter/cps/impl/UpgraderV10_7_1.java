/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Stream;


public class UpgraderV10_7_1 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    public UpgraderV10_7_1(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7, 1));
        try (Connection connection = dataModel.getConnection(true);
             Statement statement = connection.createStatement()) {
            if (tableExists(connection, "T01_SAP_C01")) {
                Stream.of("ALTER TABLE T01_SAP_C01 RENAME TO SAP_CS1_CR_SC_CPS",
                        "ALTER TABLE T01_SAP_C01JRNL RENAME TO SAP_CS1_CR_SC_CPSJRNL",
                        "ALTER TABLE SAP_CS1_CR_SC_CPS RENAME CONSTRAINT FK_T01_SAP_C01 TO FK_SAP_CS1_CR_SC_CPS",
                        "ALTER TABLE \"FLYWAYMETA.C01\" RENAME TO \"FLYWAYMETA.CS1\"")
                        .forEach(e -> execute(statement, e));
            }
            if (tableExists(connection, "SAP_C02_MASTER_CR_SC_CPS")) {
                Stream.of("ALTER TABLE SAP_C02_MASTER_CR_SC_CPS RENAME TO SAP_CS2_MASTER_CR_SC_CPS",
                        "ALTER TABLE SAP_C02_MASTER_CR_SC_CPSJRNL RENAME TO SAP_CS2_MASTER_CR_SC_CPSJRNL",
                        "ALTER TABLE SAP_CS2_MASTER_CR_SC_CPS RENAME CONSTRAINT FK_SAP_C02_MASTER_CR_SC_CPS TO FK_SAP_CS2_MASTER_CR_SC_CPS",
                        "ALTER TABLE \"FLYWAYMETA.C02\" RENAME TO \"FLYWAYMETA.CS2\"")
                        .forEach(e -> execute(statement, e));
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private boolean tableExists(Connection connection, String tableName) {
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(), tableName, new String[]{"TABLE"})) {
            return resultSet.next();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }
}
