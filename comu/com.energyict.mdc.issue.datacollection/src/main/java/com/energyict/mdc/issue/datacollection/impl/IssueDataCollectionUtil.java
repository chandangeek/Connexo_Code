/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.issue.datacollection.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.IssueTypes;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class IssueDataCollectionUtil {

    public static List<CreationRule> getIssueCreationRules(IssueService issueService) {
        IssueType validationType = issueService.findIssueType(IssueTypes.DATA_COLLECTION.getName()).orElse(null);
        List<IssueReason> issueReasons = issueService.query(IssueReason.class)
                .select(where("issueType").isEqualTo(validationType))
                .stream()
                .collect(Collectors.toList());

        Query<CreationRule> query1 = issueService.getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        Condition conditionIssue1 = where("reason").in(issueReasons);
        return query1.select(conditionIssue1, Order.ascending("name"));

    }
}
