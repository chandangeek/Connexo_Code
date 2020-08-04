/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.install;

import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.issue.datacollection.impl.templates.BasicDataCollectionRuleTemplate;
import com.energyict.mdc.issue.datacollection.impl.templates.EventAggregationRuleTemplate;
import com.energyict.mdc.issue.datacollection.impl.templates.MeterRegistrationRuleTemplate;

import javax.inject.Inject;
import java.util.Arrays;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_8_1 implements Upgrader {

    private final DataModel dataModel;

    private final IssueCreationService issueCreationService;

    @Inject
    public UpgraderV10_8_1(DataModel dataModel, IssueCreationService issueCreationService) {
        this.dataModel = dataModel;
        this.issueCreationService = issueCreationService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 8, 1));
        issueCreationService.getCreationRuleQuery()
                .select(Where.where("template").in(Arrays.asList(BasicDataCollectionRuleTemplate.NAME, EventAggregationRuleTemplate.NAME, MeterRegistrationRuleTemplate.NAME)))
                .forEach(CreationRule::update);
    }
}
