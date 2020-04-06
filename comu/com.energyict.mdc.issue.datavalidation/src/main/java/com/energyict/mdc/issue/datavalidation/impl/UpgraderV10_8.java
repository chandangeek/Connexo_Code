/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Condition;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class UpgraderV10_8 implements Upgrader {

    private final DataModel dataModel;
    private final IssueService issueService;

    @Inject
    UpgraderV10_8(final DataModel dataModel, final IssueService issueService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8));
        updateValidationCreationRules();
    }

    private void updateValidationCreationRules() {
        issueService.findIssueType("datavalidation").ifPresent(validationType -> {
            List<IssueReason> dataValidationReasons = new ArrayList<>(issueService.query(IssueReason.class)
                    .select(where("issueType").isEqualTo(validationType)));

            Query<CreationRule> query = issueService.getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
            Condition conditionIssue = where("reason").in(dataValidationReasons);
            List<CreationRule> creationRules = query.select(conditionIssue);
            creationRules.forEach(this::updateContent);
        });
    }

    private void updateContent(final CreationRule creationRule) {
        final IssueCreationService.CreationRuleUpdater creationRuleUpdater = creationRule.startUpdate();
        creationRuleUpdater.complete();
    }
}
