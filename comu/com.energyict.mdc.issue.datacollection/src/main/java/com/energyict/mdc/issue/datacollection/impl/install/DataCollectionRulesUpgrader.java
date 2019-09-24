/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.install;

import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.time.DefaultRelativePeriodDefinition;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Condition;
import net.minidev.json.JSONObject;

import javax.inject.Inject;

public class DataCollectionRulesUpgrader implements Upgrader {

    private final IssueService issueService;
    private final TimeService timeService;

    @Inject
    public DataCollectionRulesUpgrader(IssueService issueService, final TimeService timeService) {
        this.issueService = issueService;
        this.timeService = timeService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        updateDataCollectionIssueCreationRule();
    }

    private void updateDataCollectionIssueCreationRule() {
        issueService.getIssueCreationService()
                .getCreationRuleQuery(IssueReason.class, IssueType.class)
                .select(Condition.TRUE)
                .stream()
                .filter(rule -> rule.getTemplateImpl().equals("BasicDataCollectionRuleTemplate"))
                .filter(rule -> rule.getCreationRuleProperties().stream().anyMatch(creationRuleProperty -> creationRuleProperty.getName().equals("BasicDataCollectionRuleTemplate.threshold")))
                .forEach(this::updateCreationRule);
    }

    private void updateCreationRule(final CreationRule creationRule) {
        final IssueCreationService.CreationRuleUpdater creationRuleUpdater = creationRule.startUpdate();
        creationRuleUpdater.complete();
    }

    private HasIdAndName getRelativePeriodWithCount(DefaultRelativePeriodDefinition relativePeriodDefinition) {
        RelativePeriod relativePeriod = timeService.findRelativePeriodByName(relativePeriodDefinition.getPeriodName()).isPresent() ? timeService.findRelativePeriodByName(relativePeriodDefinition.getPeriodName())
                .get() : timeService.getAllRelativePeriod();
        String occurrenceCount = "1";

        return new HasIdAndName() {
            @Override
            public String getId() {
                return occurrenceCount + ":" + relativePeriod.getId();
            }

            @Override
            public String getName() {
                JSONObject jsonId = new JSONObject();
                jsonId.put("occurrenceCount", occurrenceCount);
                jsonId.put("relativePeriod", relativePeriod.getName());
                return jsonId.toString();
            }
        };
    }
}
