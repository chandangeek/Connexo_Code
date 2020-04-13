package com.energyict.mdc.issue.datacollection.impl;

import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.time.RelativePeriodUsageInfo;
import com.elster.jupiter.time.RelativePeriodUsageProvider;
import com.elster.jupiter.time.TimeService;
import com.energyict.mdc.issue.datacollection.impl.i18n.TranslationKeys;
import com.energyict.mdc.issue.datacollection.impl.templates.BasicDataCollectionRuleTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.stream.Collectors;

@Component(service = RelativePeriodUsageProvider.class)
public class IssueDataCollectionUsageProvider implements RelativePeriodUsageProvider{

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

        return IssueDataCollectionUtil.getIssueCreationRules(issueService)
                .stream()
                .filter(rule -> this.matchesRelativePeriod(rule, relativePeriodId))
                .map(this::createRelativePeriodUsageInfo)
                .collect(Collectors.toList());
    }

    private boolean matchesRelativePeriod(CreationRule rule, long relativePeriodId) {
        BasicDataCollectionRuleTemplate.RelativePeriodWithCountInfo info = (BasicDataCollectionRuleTemplate.RelativePeriodWithCountInfo) rule.getProperties().get(BasicDataCollectionRuleTemplate.THRESHOLD);
        return info != null && info.getRelativePeriodId() == relativePeriodId;
    }

    private RelativePeriodUsageInfo createRelativePeriodUsageInfo(CreationRule rule) {
        return new RelativePeriodUsageInfo(
                rule.getName(),
                timeService.findRelativePeriodCategoryDisplayName(this.getType()),
                APPLICATION_NAME,
                null);
    }

   @Override
    public String getType() {
        return TranslationKeys.ISSUE_RELATIVE_PERIOD_CATEGORY.getKey();
    }
}
