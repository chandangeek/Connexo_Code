/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Where;

import com.energyict.mdc.issue.datavalidation.impl.template.SuspectCreatedIssueCreationRuleTemplate;

import javax.inject.Inject;

public class UpgraderV10_8_1 implements Upgrader {
    private final DataModel dataModel;
    private final IssueCreationService issueCreationService;

    @Inject
    UpgraderV10_8_1(final DataModel dataModel, final IssueCreationService issueCreationService) {
        this.dataModel = dataModel;
        this.issueCreationService = issueCreationService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8, 1));
        updateValidationCreationRules();
    }

    private void updateValidationCreationRules() {
        issueCreationService.getCreationRuleQuery()
                .select(Where.where("template").isEqualTo(SuspectCreatedIssueCreationRuleTemplate.NAME))
                .forEach(CreationRule::update);
    }
}
