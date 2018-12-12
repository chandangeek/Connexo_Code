package com.energyict.mdc.device.alarms.impl;

import com.elster.jupiter.issue.share.entity.CreationRule;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.time.RelativePeriodUsageInfo;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.RelativePeriodUsageProvider;

import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;
import com.energyict.mdc.device.alarms.impl.templates.BasicDeviceAlarmRuleTemplate;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.stream.Collectors;


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
        return DeviceAlarmUtil.getAlarmCreationRules(issueService)
                .stream()
                .filter(rule -> this.matchesRelativePeriod(rule, relativePeriodId))
                .map(this::createRelativePeriodUsageInfo)
                .collect(Collectors.toList());
    }

    @Override
    public String getType() {
        return TranslationKeys.ALARM_RELATIVE_PERIOD_CATEGORY.getKey();
    }

    private boolean matchesRelativePeriod(CreationRule rule, long relativePeriodId) {
        BasicDeviceAlarmRuleTemplate.RelativePeriodWithCountInfo info = (BasicDeviceAlarmRuleTemplate.RelativePeriodWithCountInfo) rule.getProperties().get(BasicDeviceAlarmRuleTemplate.THRESHOLD);

        return info != null && info.getRelativePeriodId() == relativePeriodId;
    }

    private RelativePeriodUsageInfo createRelativePeriodUsageInfo(CreationRule rule) {
        return new RelativePeriodUsageInfo(
                rule.getName(),
                timeService.findRelativePeriodCategoryDisplayName(this.getType()),
                APPLICATION_NAME,
                null);
    }



}