/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.database;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.elster.jupiter.orm.Version.version;

@LiteralSql
public class UpgraderV10_5 implements Upgrader {

    private final DataModel dataModel;


    @Inject
    UpgraderV10_5(DataModel dataModel) {
        this.dataModel = dataModel;

    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 5));
        this.upgradeOpenIssue();
    }

    private void upgradeOpenIssue() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.upgradeOpenIssue(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeOpenIssue(Connection connection) {
        String[] sqlStatements = {
                "CREATE OR REPLACE VIEW ISU_ISSUE_ALL AS SELECT * FROM ISU_ISSUE_OPEN UNION SELECT * FROM ISU_ISSUE_HISTORY"};
        for (String sqlStatement : sqlStatements) {
            try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }
}
