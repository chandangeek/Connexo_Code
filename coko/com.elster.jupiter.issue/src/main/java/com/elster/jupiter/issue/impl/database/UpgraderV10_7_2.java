/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.issue.impl.database;

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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private static final String START_PROCESS_WEBSERVICE_PROPERTY_NAME = "StartProcessWebServiceIssueAction.startprocess";
    private static final String START_PROCESSS_SERVICE_CALL_PROPERTY_NAME = "servicecall.issue.action.process";


    @Inject
    UpgraderV10_7_2(DataModel dataModel, IssueActionService issueActionService) {
        this.dataModel = dataModel;
        this.issueActionService = issueActionService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        try (Connection connection = this.dataModel.getConnection(true)) {
            Optional<IssueActionType> validActionType = getIsuActionType(START_PROCESS_ACTION);
            Optional<IssueActionType> webServiceIssueActionType = getIsuActionType(START_PROCESS_WEBSERVICE_ISSUE_ACTION);
            Optional<IssueActionType> serviceCallIssueActionType = getIsuActionType(START_PROCESS_SERVICE_CALL_ISSUE_ACTION);
            if (!webServiceIssueActionType.isPresent() && !serviceCallIssueActionType.isPresent()) {
                //nothing to do
                return;
            }
            Map<Long, RuleContent> ruleIdAndContentMap = getIssueActionIdAndRuleId(connection, validActionType.get(), webServiceIssueActionType.get(), serviceCallIssueActionType.get());
            for (Map.Entry<Long, RuleContent> ruleIdAndContent : ruleIdAndContentMap.entrySet()) {
                for (List<PhaseContent> phaseContent : ruleIdAndContent.getValue().values()) {
                    if (phaseContent.size() > 1) {
                        deleteIssueAction(connection, validActionType.get().getId(), phaseContent);
                    } else {
                        updateIssueAction(connection, validActionType.get().getId(), phaseContent);
                    }
                }
            }
            webServiceIssueActionType.get().delete();
            serviceCallIssueActionType.get().delete();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void deleteIssueAction(Connection connection, Long actionTypeIdToKeep, List<PhaseContent> contentList) throws SQLException {
        for (PhaseContent content : contentList) {
            if (content.actionTypeId != actionTypeIdToKeep) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(String.format("delete from ISU_CREATIONRULEACTION where id = %s", content.actionId));
                } catch (SQLException e) {
                    throw new UnderlyingSQLFailedException(e);
                }
            }
        }
    }

    private void updateIssueAction(Connection connection, Long actionTypeIdToKeep, List<PhaseContent> contentList) throws SQLException {
        for (PhaseContent content : contentList) {
            if (content.actionTypeId != actionTypeIdToKeep) {
                try (Statement actionStatement = connection.createStatement();
                     Statement actionPropsStatement = connection.createStatement()) {
                    actionStatement.execute(String.format("update ISU_CREATIONRULEACTION SET ACTIONTYPE = %s where id = %s", actionTypeIdToKeep, content.actionId));
                    actionPropsStatement.execute(String.format("update ISU_CREATIONRULEACTIONPROPS SET NAME = '%s' where CREATIONRULEACTION = %s and ( NAME = '%s' or NAME = '%s')", START_PROCESS_PROPERTY_NAME, content.actionId, START_PROCESS_WEBSERVICE_PROPERTY_NAME, START_PROCESSS_SERVICE_CALL_PROPERTY_NAME));
                } catch (SQLException e) {
                    throw new UnderlyingSQLFailedException(e);
                }
            }
        }
    }

    private Map<Long, RuleContent> getIssueActionIdAndRuleId(Connection connection, IssueActionType... issueActionTypes) throws SQLException {
        String query = "select ID, RULE, PHASE, ACTIONTYPE from ISU_CREATIONRULEACTION where ACTIONTYPE in " + Arrays.stream(issueActionTypes)
                .map(IssueActionType::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(", ", "(", ")"));
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
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
    }

    private Optional<IssueActionType> getIsuActionType(String className) {
        List<IssueActionType> issueActionTypes = issueActionService.getActionTypeQuery().select(where("className").isEqualTo(className));
        switch (issueActionTypes.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(issueActionTypes.get(0));
            default:
                throw new IllegalStateException("Values of ISU_ACTIONTYPE.class_name should be unique");

        }
    }

    private class RuleContent extends HashMap<Long, List<PhaseContent>> {

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
