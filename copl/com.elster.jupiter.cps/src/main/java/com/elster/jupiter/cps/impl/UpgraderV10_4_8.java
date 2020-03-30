/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static com.elster.jupiter.util.streams.Currying.perform;

public class UpgraderV10_4_8 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    public UpgraderV10_4_8(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        try (Connection connection = dataModel.getConnection(true);
             Statement statement = connection.createStatement();) {
            alterTableColumnNameSQL()
                    .forEach(perform(this::executeStatement).on(statement));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 8));
    }

    private List<String> alterTableColumnNameSQL() {
        return Arrays.asList(
                "ALTER TABLE PR1_SIOSERIAL_CONNTASK RENAME COLUMN PHONENUMBER TO PHONE_NUMBER",
                "ALTER TABLE PR1_SIOSERIAL_CONNTASKJRNL RENAME COLUMN PHONENUMBER TO PHONE_NUMBER"
        );
    }

    private void executeStatement(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }
}
