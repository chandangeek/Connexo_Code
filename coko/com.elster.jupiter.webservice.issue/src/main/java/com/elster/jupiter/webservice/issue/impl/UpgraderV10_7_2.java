/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.webservice.issue.impl;

import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@LiteralSql
public class UpgraderV10_7_2 implements Upgrader {

    private final DataModel dataModel;
    private final IssueActionService issueActionService;
    private static final String START_PROCESS_WEBSERVICE_ISSUE_ACTION = "com.elster.jupiter.webservice.issue.impl.actions.StartProcessWebServiceIssueAction";
    private static final String START_PROCESS_SERVICE_CALL_ISSUE_ACTION = "com.elster.jupiter.issue.servicecall.impl.action.StartProcessAction";
    private static final String START_PROCESS_ACTION = "com.elster.jupiter.issue.impl.actions.ProcessAction";
    private static final String START_PROCESS_PROPERTY_NAME = "ProcessAction.processesCombobox";

    @Inject
    UpgraderV10_7_2(DataModel dataModel, IssueActionService issueActionService) {
        this.dataModel = dataModel;
        this.issueActionService = issueActionService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        try (Connection connection = this.dataModel.getConnection(true)) {
            IssueActionType validActionType = getIsuActionType(START_PROCESS_ACTION);
            IssueActionType webServiceIssueActionType = getIsuActionType(START_PROCESS_WEBSERVICE_ISSUE_ACTION);
            IssueActionType serviceCallIssueActionType = getIsuActionType(START_PROCESS_SERVICE_CALL_ISSUE_ACTION);
            Map<Long, RuleContent> ruleIdAndContentMap = getIssueActionIdAndRuleId(connection, validActionType, webServiceIssueActionType, serviceCallIssueActionType);
            for (Map.Entry<Long, RuleContent> ruleIdAndContent : ruleIdAndContentMap.entrySet()) {
                for (List<PhaseContent> phaseContent : ruleIdAndContent.getValue().values()) {
                    if (phaseContent.size() > 1) {
                        deleteIssueAction(connection, validActionType.getId(), phaseContent);
                    } else {
                        updateIssueAction(connection, validActionType.getId(), phaseContent);
                    }
                }
            }
            webServiceIssueActionType.delete();
            serviceCallIssueActionType.delete();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void deleteIssueAction(Connection connection, Long actionTypeIdToKeep, List<PhaseContent> contentList) throws SQLException {
        for (PhaseContent content : contentList) {
            if (content.actionTypeId != actionTypeIdToKeep) {
                connection.createStatement().execute(String.format("delete from ISU_CREATIONRULEACTION where id = %s", content.actionId));
            }
        }
    }

    private void updateIssueAction(Connection connection, Long actionTypeIdToKeep, List<PhaseContent> contentList) throws SQLException {
        for (PhaseContent content : contentList) {
            if (content.actionTypeId != actionTypeIdToKeep) {
                connection.createStatement().execute(String.format("update ISU_CREATIONRULEACTION SET ACTIONTYPE = %s where id = %s", actionTypeIdToKeep, content.actionId));
                connection.createStatement().execute(String.format("update ISU_CREATIONRULEACTIONPROPS SET NAME = %s where CREATIONRULEACTION = %s", START_PROCESS_PROPERTY_NAME, content.actionId));
            }
        }
    }

    private Map<Long, RuleContent> getIssueActionIdAndRuleId(Connection connection, IssueActionType ...issueActionTypes) throws SQLException {
        String query = "select ID, RULE, PHASE, ACTIONTYPE from ISU_CREATIONRULEACTION where ACTIONTYPE in " + Arrays.stream(issueActionTypes).map(IssueActionType::getId).map(String::valueOf).collect(Collectors.joining(", ", "(", ")"));
        ResultSet rs = connection.createStatement().executeQuery(query);
        Map<Long, RuleContent> rulesContents = new HashMap<>();
        while (rs.next()) {
            long ruleID = rs.getLong(2);
            RuleContent ruleContent = rulesContents.get(ruleID);
            if (ruleContent == null) {
                rulesContents.put(ruleID, new RuleContent(rs.getLong(3), rs.getLong(4), rs.getLong(1)));
            } else {
                ruleContent.computeIfAbsent(rs.getLong(3), Long -> new ArrayList<>()).add(new PhaseContent(rs.getLong(4), rs.getLong(1)));
            }
        }
        return rulesContents;
    }

    private IssueActionType getIsuActionType(String className) {
        List<IssueActionType> issueActionTypes = issueActionService.getActionTypeQuery().select(where("className").isEqualTo(className));
        if (issueActionTypes.size() == 1) {
            return issueActionTypes.get(0);
        } else {
            throw new IllegalStateException("Values of ISU_ACTIONTYPE.class_name should be unique");
        }
    }

    private class RuleContent extends HashMap <Long, List<PhaseContent>> {

        RuleContent(long phase, long actionTypeId, long actionId) {
            computeIfAbsent(phase, Long -> new ArrayList<>()).add(new PhaseContent(actionTypeId, actionId));
        }
    }

    private class PhaseContent {

        long actionTypeId;
        long actionId;

        PhaseContent(long actionTypeId, long actionId) {
            this.actionId = actionId;
            this.actionTypeId = actionTypeId;
        }
    }
}
