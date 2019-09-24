/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.database;

import com.elster.jupiter.issue.impl.actions.CloseIssueAction;
import com.elster.jupiter.issue.impl.actions.MailIssueAction;
import com.elster.jupiter.issue.impl.actions.ProcessAction;
import com.elster.jupiter.issue.impl.module.TranslationKeys;
import com.elster.jupiter.issue.impl.records.CreationRuleImpl;
import com.elster.jupiter.issue.impl.service.IssueDefaultActionsFactory;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.time.DefaultRelativePeriodDefinition;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;

@LiteralSql
public class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;
    private final IssueService issueService;
    private final UserService userService;
    private final PrivilegesProviderV10_7 privilegesProviderV10_7;
    private final IssueActionService issueActionService;

    @Inject
    UpgraderV10_7(DataModel dataModel, IssueService issueService, UserService userService, PrivilegesProviderV10_7 privilegesProviderV10_7,
                  IssueActionService issueActionService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.userService = userService;
        this.privilegesProviderV10_7 = privilegesProviderV10_7;
        this.issueActionService = issueActionService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 7));
        updateDataCollectionIssueCreationRule();
        this.addManualIssueType();
        this.createActionTypesIfNotPresent();
        addCloseActionType();
        userService.addModulePrivileges(privilegesProviderV10_7);
        updateOpenIssueType();
        updateHistoricalIssueType();
        this.upgradeAllIssues();
    }

    private void addManualIssueType() {
        issueService.createIssueType(IssueService.MANUAL_ISSUE_TYPE, TranslationKeys.MANUAL_ISSUE_TYPE, IssueService.MANUAL_ISSUE_PREFIX);
    }

    private void upgradeAllIssues() {
        execute(dataModel, "CREATE OR REPLACE VIEW ISU_ISSUE_ALL AS SELECT * FROM ISU_ISSUE_OPEN UNION SELECT * FROM ISU_ISSUE_HISTORY");
    }

    private void updateOpenIssueType() {
        execute(dataModel, "UPDATE (SELECT t1.id, t1.TYPE t_type, t2.ISSUE_TYPE r_type FROM ISU_ISSUE_OPEN t1, ISU_REASON t2 WHERE t1.REASON_ID = t2.KEY) SET t_type = r_type");
    }

    private void updateHistoricalIssueType() {
        execute(dataModel, "UPDATE (SELECT t1.ISU_HIST_ISSUE_ID, t1.TYPE t_type, t2.ISSUE_TYPE r_type FROM ISU_ISSUE_HISTORY t1 inner join ISU_REASON t2 on t1.REASON_ID = t2.KEY) SET t_type = r_type");
    }

    private void addCloseActionType() {
        issueActionService.createActionType(IssueDefaultActionsFactory.ID, CloseIssueAction.class.getName(), issueService.findIssueType(IssueService.MANUAL_ISSUE_TYPE).get());
        issueActionService.createActionType(IssueDefaultActionsFactory.ID, ProcessAction.class.getName(), (IssueType) null);
    }

    private void createActionTypesIfNotPresent() {
        IssueType type = null;
        Condition classNameCondition = buildCondition("className", Optional.of(MailIssueAction.class.getName()));
        Condition factoryCondition = buildCondition("factoryId", Optional.of(IssueDefaultActionsFactory.ID));
        if (issueActionService.getActionTypeQuery().select(classNameCondition.and(factoryCondition)).isEmpty()) {
            issueActionService.createActionType(IssueDefaultActionsFactory.ID, MailIssueAction.class.getName(), type, CreationRuleActionPhase.CREATE);
        }
    }

    private void updateDataCollectionIssueCreationRule() {
        final List<CreationRule> creationRules = issueService.getIssueCreationService()
                .getCreationRuleQuery(IssueReason.class, IssueType.class)
                .select(Condition.TRUE)
                .stream()
                .filter(rule -> rule.getTemplateImpl().equals("BasicDataCollectionRuleTemplate"))
                .filter(rule -> !rule.getCreationRuleProperties().stream().anyMatch(creationRuleProperty -> creationRuleProperty.getName().equals("BasicDataCollectionRuleTemplate.threshold")))
                .collect(Collectors.toList());

        creationRules.forEach(creationRule -> updateCreationRule((CreationRuleImpl) creationRule));
    }

    private Condition buildCondition(String field, Optional<?> value) {
        Condition condition = where(field).isNull();
        if (value.isPresent()) {
            condition = condition.or(where(field).isEqualTo(value.get()));
        }
        return condition;
    }

    private void updateCreationRule(final CreationRuleImpl creationRule) {
        execute(dataModel, "INSERT INTO ISU_CREATIONRULEPROPS\n" +
                "VALUES ('BasicDataCollectionRuleTemplate.threshold',\n" +
                "        " + creationRule.getId() + ",\n" +
                "        '1:7',\n" +
                "        1,\n" +
                "        " + Instant.now().toEpochMilli() + ",\n" +
                "        " + Instant.now().toEpochMilli() + ",\n" +
                "        DEFAULT)");
        // final IssueCreationService.CreationRuleUpdater creationRuleUpdater = creationRule.startUpdate();
        // creationRuleUpdater.complete();
    }
}