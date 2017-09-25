/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.database;

import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.impl.tasks.IssueSnoozeHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_4 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    UpgraderV10_4(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 4));
        this.upgradeOpenIssue();
        this.upgradeSubscriberSpecs();
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

    private void upgradeSubscriberSpecs() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.upgradeSubscriberSpecs(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeSubscriberSpecs(Connection connection) {
        try (PreparedStatement statement = this.upgradeSubscriberSpecsStatement(connection)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private PreparedStatement upgradeSubscriberSpecsStatement(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE MSG_SUBSCRIBERSPEC SET nls_component = ?, nls_layer = ? WHERE name = ?");
        statement.setString(1, IssueServiceImpl.COMPONENT_NAME);
        statement.setString(2, Layer.DOMAIN.name());
        statement.setString(3, IssueSnoozeHandlerFactory.ISSUE_SNOOZE_TASK_SUBSCRIBER);
        return statement;
    }

}
