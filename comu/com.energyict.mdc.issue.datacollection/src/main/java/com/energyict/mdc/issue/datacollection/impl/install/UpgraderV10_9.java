/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.install;

import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.issue.datacollection.impl.templates.EventAggregationRuleTemplate;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_9 implements Upgrader {

    private final DataModel dataModel;

    private final IssueService issueService;

    @Inject
    public UpgraderV10_9(DataModel dataModel, IssueService issueService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 9));
        issueService.getIssueCreationService().getCreationRuleQuery()
                .select(Where.where("template").isEqualTo(EventAggregationRuleTemplate.NAME))
                .forEach(CreationRule::update);
    }
}
