package com.elster.jupiter.issue.impl.database;


import com.elster.jupiter.issue.impl.actions.AssignIssueAction;
import com.elster.jupiter.issue.impl.service.IssueDefaultActionsFactory;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;

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
        this.updateActiontypes();
    }

    private void updateActiontypes() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.updateActiontypes(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void updateActiontypes(Connection connection) {
        String[] sqlStatements = {
                "DELETE FROM ISU_CREATIONRULEACTION WHERE ACTIONTYPE IN (SELECT ID FROM ISU_ACTIONTYPE WHERE CLASS_NAME IN ('com.elster.jupiter.issue.impl.actions.AssignToMeIssueAction', 'com.elster.jupiter.issue.impl.actions.UnassignIssueAction'))",
                "DELETE FROM ISU_ACTIONTYPE WHERE CLASS_NAME IN ('com.elster.jupiter.issue.impl.actions.AssignToMeIssueAction', 'com.elster.jupiter.issue.impl.actions.UnassignIssueAction')"
        };
        for (String sqlStatement : sqlStatements) {
            try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }

        IssueActionService issueActionService = this.dataModel.getInstance(IssueActionService.class);
        IssueType issueType = null;
        Condition conditionAssignIssueAction = Operator.EQUALIGNORECASE.compare("className", AssignIssueAction.class.getName());

        if (issueActionService.getActionTypeQuery()
                .select(conditionAssignIssueAction).isEmpty()) {
            issueActionService.createActionType(IssueDefaultActionsFactory.ID, AssignIssueAction.class.getName(), issueType);
        }
    }

    private void upgradeOpenIssue() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.upgradeOpenIssue(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeOpenIssue(Connection connection) {
        String[] sqlStatements = { "ALTER TABLE ISU_ISSUE_HISTORY DROP COLUMN ASSIGNEE_TYPE",
        "ALTER TABLE ISU_ISSUE_OPEN DROP COLUMN ASSIGNEE_TYPE", "CREATE OR REPLACE VIEW ISU_ISSUE_ALL AS SELECT * FROM ISU_ISSUE_OPEN UNION SELECT * FROM ISU_ISSUE_HISTORY"};
        for (String sqlStatement : sqlStatements) {
            try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }

}
