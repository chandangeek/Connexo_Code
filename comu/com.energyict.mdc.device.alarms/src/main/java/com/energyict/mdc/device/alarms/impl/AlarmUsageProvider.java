package com.energyict.mdc.device.alarms.impl;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.CreationRule;

import com.elster.jupiter.issue.share.entity.CreationRuleProperty;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.time.RelativePeriodUsageInfo;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.RelativePeriodUsageProvider;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;
import com.energyict.mdc.device.alarms.impl.templates.BasicDeviceAlarmRuleTemplate;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import static com.elster.jupiter.util.conditions.Where.where;

@Component(service = RelativePeriodUsageProvider.class)
public class AlarmUsageProvider implements RelativePeriodUsageProvider {

    private static final String APPLICATION_NAME="MultiSense";

    private volatile IssueService issueService;
    private volatile TimeService timeService;


    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Override
    public List<RelativePeriodUsageInfo> getUsageReferences(long relativePeriodId) {
        return getAlarmCreationRules().stream()
                .filter(rule -> this.matchesRelativePeriod(rule, relativePeriodId))
                .map(this::createRelativePeriodUsageInfo)
                .collect(Collectors.toList());
    }

    @Override
    public String getType() {
        return TranslationKeys.ALARM_RELATIVE_PERIOD_CATEGORY.getKey();
    }


    private boolean matchesRelativePeriod(CreationRule rule, long relativePeriodId) {
        return rule.getCreationRuleProperties()
                .stream()
                .filter(property -> BasicDeviceAlarmRuleTemplate.THRESHOLD.equals(property.getName()))
                .map(CreationRuleProperty::getValue)
                .map(BasicDeviceAlarmRuleTemplate.RelativePeriodWithCountInfo.class::cast)
                .anyMatch(relativePeriod -> relativePeriod.getRelativePeriodId() == relativePeriodId);
    }

    private RelativePeriodUsageInfo createRelativePeriodUsageInfo(CreationRule rule) {
        return new RelativePeriodUsageInfo(
                rule.getName(),
                timeService.findRelativePeriodCategoryDisplayName(this.getType()),
                APPLICATION_NAME,
                null);
    }


    private List<CreationRule> getAlarmCreationRules() {
        IssueType alarmType = issueService.findIssueType("devicealarm").orElse(null);
        List<IssueReason> alarmReasons = new ArrayList<>(issueService.query(IssueReason.class)
                .select(where("issueType").isEqualTo(alarmType)));

        Query<CreationRule> query = issueService.getIssueCreationService().getCreationRuleQuery(IssueReason.class, IssueType.class);
        Condition conditionIssue = where("reason").in(alarmReasons);
        return query.select(conditionIssue, Order.ascending("name"));
    }

}