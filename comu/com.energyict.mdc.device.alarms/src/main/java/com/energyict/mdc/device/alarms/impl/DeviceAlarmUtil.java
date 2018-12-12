package com.energyict.mdc.device.alarms.impl;

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

public class DeviceAlarmUtil {

    public static List<CreationRule> getAlarmCreationRules(IssueService issueService) {
        IssueType alarmType = issueService.findIssueType("devicealarm").orElse(null);
        List<IssueReason> alarmReasons = new ArrayList<>(issueService.query(IssueReason.class)
                .select(where("issueType").isEqualTo(alarmType)));

        Query<CreationRule> query = issueService.getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        Condition conditionIssue = where("reason").in(alarmReasons);
        return query.select(conditionIssue, Order.ascending("name"));
    }
}
