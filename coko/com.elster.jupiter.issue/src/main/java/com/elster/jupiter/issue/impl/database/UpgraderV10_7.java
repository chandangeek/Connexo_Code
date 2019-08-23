<<<<<<< HEAD
package com.elster.jupiter.issue.impl.database;
import com.elster.jupiter.issue.impl.actions.MailIssueAction;
import com.elster.jupiter.issue.impl.service.IssueDefaultActionsFactory;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Condition;

import javax.inject.Inject;
import java.util.Optional;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;

@LiteralSql
=======
/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.database;

import com.elster.jupiter.issue.impl.actions.CloseIssueAction;
import com.elster.jupiter.issue.impl.module.TranslationKeys;
import com.elster.jupiter.issue.impl.service.IssueDefaultActionsFactory;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

>>>>>>> master
public class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;
    private final IssueService issueService;
<<<<<<< HEAD
=======
    private final UserService userService;
    private final PrivilegesProviderV10_7 privilegesProviderV10_7;
>>>>>>> master
    private final IssueActionService issueActionService;


    @Inject
<<<<<<< HEAD
    UpgraderV10_7(DataModel dataModel, IssueService issueService, IssueActionService issueActionService, MessageService messageService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.issueActionService = issueActionService;

=======
    UpgraderV10_7(DataModel dataModel, IssueService issueService, UserService userService, PrivilegesProviderV10_7 privilegesProviderV10_7,
                  IssueActionService issueActionService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.userService = userService;
        this.privilegesProviderV10_7 = privilegesProviderV10_7;
        this.issueActionService = issueActionService;
>>>>>>> master
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 7));
<<<<<<< HEAD
        this.createActionTypesIfNotPresent();

    }
    private void createActionTypesIfNotPresent() {
        IssueType type = null;
        Condition classNameCondition = buildCondition("className", Optional.of(MailIssueAction.class.getName()));
        Condition factoryCondition = buildCondition("factoryId", Optional.of(IssueDefaultActionsFactory.ID));
        if (issueActionService.getActionTypeQuery().select(classNameCondition.and(factoryCondition)).isEmpty()) {
            issueActionService.createActionType(IssueDefaultActionsFactory.ID, MailIssueAction.class.getName(), type, CreationRuleActionPhase.CREATE);
        }
    }

    private Condition buildCondition(String field, Optional<?> value) {
        Condition condition = where(field).isNull();
        if (value.isPresent()) {
            condition = condition.or(where(field).isEqualTo(value.get()));
        }
        return condition;
=======
        this.addManualIssueType();
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
>>>>>>> master
    }
}
