/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.issue.datavalidation.impl;

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
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

public class IssueDataValidationUtil {
    public static List<CreationRule> getIssueCreationRules(IssueService issueService) {
        IssueType dataValidationType = issueService.findIssueType(IssueTypes.DATA_VALIDATION.getName()).orElse(null);

        List<IssueReason> dataValidationReasons = issueService.query(IssueReason.class)
                .select(where("issueType").isEqualTo(dataValidationType))
                .stream()
                .collect(Collectors.toList());

        Query<CreationRule> query = issueService.getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        Condition conditionIssue = where("reason").in(dataValidationReasons);

        return query.select(conditionIssue, Order.ascending("name"));



    }
}
