/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.database;


import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_3 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    UpgraderV10_3(DataModel dataModel) {
        this.dataModel = dataModel;
    }


    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 3));
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
                "ALTER TABLE ISU_ISSUE_HISTORY DROP COLUMN ASSIGNEE_TYPE",
                "ALTER TABLE ISU_ISSUE_OPEN DROP COLUMN ASSIGNEE_TYPE",
                "UPDATE ISU_ISSUE_HISTORY SET CREATEDATETIME = CREATETIME",
                "UPDATE ISU_ISSUE_OPEN SET CREATEDATETIME = CREATETIME",
                //WHERE REASON_ID IN (SELECT KEY FROM ISU_REASON WHERE ISSUE_TYPE IN ('datacollection','datavalidation'))
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
