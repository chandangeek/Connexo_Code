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
        execute(dataModel, "UPDATE (SELECT t1.id, t1.TYPE t_type, t2.ISSUE_TYPE r_type FROM ISU_ISSUE_HISTORY t1, ISU_REASON t2 WHERE t1.REASON_ID = t2.KEY) SET t_type = r_type");
    }

    private void addCloseActionType() {
        issueActionService.createActionType(IssueDefaultActionsFactory.ID, CloseIssueAction.class.getName(), issueService.findIssueType(IssueService.MANUAL_ISSUE_TYPE).get());
    }
}
