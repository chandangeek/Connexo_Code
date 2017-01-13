package com.elster.jupiter.issue.impl.database;


import com.elster.jupiter.issue.impl.actions.AssignToMeIssueAction;
import com.elster.jupiter.issue.impl.actions.UnassignIssueAction;
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
        String[] sqlStatements = { "DELETE FROM ISU_CREATIONRULEACTION WHERE ACTIONTYPE = (SELECT ID FROM ISU_ACTIONTYPE WHERE CLASS_NAME = 'com.elster.jupiter.issue.impl.actions.AssignIssueAction')",
                "DELETE FROM ISU_ACTIONTYPE WHERE CLASS_NAME = 'com.elster.jupiter.issue.impl.actions.AssignIssueAction'"};
        for (String sqlStatement : sqlStatements) {
            try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }

        IssueActionService issueActionService = this.dataModel.getInstance(IssueActionService.class);
        IssueType issueType = null;
        Condition conditionAssignToMeIssueAction = Operator.EQUALIGNORECASE.compare("className", AssignToMeIssueAction.class.getName());
        Condition conditionUnassignIssueAction = Operator.EQUALIGNORECASE.compare("className", UnassignIssueAction.class.getName());
        if (issueActionService.getActionTypeQuery()
                .select(conditionAssignToMeIssueAction).isEmpty()) {
            issueActionService.createActionType(IssueDefaultActionsFactory.ID, AssignToMeIssueAction.class.getName(), issueType);
        }
        if (issueActionService.getActionTypeQuery()
                .select(conditionUnassignIssueAction).isEmpty()){
            issueActionService.createActionType(IssueDefaultActionsFactory.ID, UnassignIssueAction.class.getName(), issueType);
        }
    }

}
