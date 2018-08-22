/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.insight.issue.datavalidation.UsagePointDataValidationIssueFilter;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidationService;

import com.google.inject.Inject;

import java.util.Map;
import java.util.Optional;

public abstract class UsagePointDataValidationEvent implements IssueEvent {

    protected Long channelId;
    protected String readingType;
    protected Long metrologyConfigurationId;

    private final Thesaurus thesaurus;
    private final MeteringService meteringService;
    private final UsagePointIssueDataValidationService usagePointIssueDataValidationService;
    private final IssueService issueService;

    @Inject
    public UsagePointDataValidationEvent(Thesaurus thesaurus, MeteringService meteringService, UsagePointIssueDataValidationService usagePointIssueDataValidationService, IssueService issueService) {
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
        this.usagePointIssueDataValidationService = usagePointIssueDataValidationService;
        this.issueService = issueService;
    }

    abstract void init(Map<?, ?> jsonPayload);

    @Override
    public String getEventType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<EndDevice> getEndDevice() {
        return findMeter().map(EndDevice.class::cast);
    }


    public long getMetrologyConfigurationId() {
        if (metrologyConfigurationId == null) {
            metrologyConfigurationId = getUsagePoint().get()
                    .getCurrentEffectiveMetrologyConfiguration()
                    .get().getMetrologyConfiguration().getId();
        }
        return metrologyConfigurationId;
    }

    @Override
    public Optional<? extends OpenIssue> findExistingIssue() {
        UsagePointDataValidationIssueFilter filter = new UsagePointDataValidationIssueFilter();
        getUsagePoint().ifPresent(filter::setUsagePoint);
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        filter.addStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
        Optional<? extends UsagePointIssueDataValidation> foundIssue = usagePointIssueDataValidationService.findAllDataValidationIssues(filter)
                .find()
                .stream()
                .findFirst();//It is going to be only zero or one open issue per device
        if (foundIssue.isPresent()) {
            return Optional.of((OpenIssue) foundIssue.get());
        }
        return Optional.empty();
    }


    //find Output
    protected Optional<Channel> findChannel() {
        return meteringService.findChannel(channelId);
    }

    protected Optional<ReadingType> findReadingType() {
        return meteringService.getReadingType(readingType);
    }

    private Optional<Meter> findMeter() {
        return findChannel().flatMap(channel -> channel.getChannelsContainer().getMeter());
    }

    @Override
    public Optional<UsagePoint> getUsagePoint() {
        return findChannel().flatMap(channel -> channel.getChannelsContainer().getUsagePoint());
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }
}
