package com.energyict.mdc.issue.devicelifecycle;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class DeviceLifecycleIssueUtil {

    public static List<CreationRule> getIssueCreationRules(IssueService issueService) {
        IssueType issueType = issueService.findIssueType("devicelifecycle").orElse(null);
        List<IssueReason> issueReasons = new ArrayList<>(issueService.query(IssueReason.class)
                .select(where("issueType").isEqualTo(issueType)));

        Query<CreationRule> query = issueService.getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        Condition conditionIssue = where("reason").in(issueReasons);
        return query.select(conditionIssue, Order.ascending("name"));
    }
}
