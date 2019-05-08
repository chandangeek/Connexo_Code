/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.database;

import com.elster.jupiter.issue.impl.module.TranslationKeys;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;
    private final IssueService issueService;

    @Inject
    UpgraderV10_7(DataModel dataModel, IssueService issueService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 7));
        this.addManualIssueType();
        this.upgradeAllIssue();
    }

    private void addManualIssueType() {
        issueService.createIssueType(IssueService.MANUAL_ISSUE_TYPE, TranslationKeys.MANUAL_ISSUE_TYPE, IssueService.COMPONENT_NAME);
    }

    private void upgradeAllIssue() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.upgradeAllIssue(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeAllIssue(Connection connection) {
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
