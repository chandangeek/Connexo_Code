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
import com.elster.jupiter.util.HasId;
import com.elster.insight.issue.datavalidation.DataValidationIssueFilter;
import com.elster.insight.issue.datavalidation.IssueDataValidation;
import com.elster.insight.issue.datavalidation.IssueDataValidationService;

import com.google.inject.Inject;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;

public abstract class DataValidationEvent implements IssueEvent {

    protected Long channelId;
    protected String readingType;
    protected Long metrologyConfigId;

    private final Thesaurus thesaurus;
    private final MeteringService meteringService;
    private final IssueDataValidationService issueDataValidationService;
    private final IssueService issueService;
    private final Clock clock;

    @Inject
    public DataValidationEvent(Thesaurus thesaurus, MeteringService meteringService, IssueDataValidationService issueDataValidationService, IssueService issueService, Clock clock) {
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
        this.issueDataValidationService = issueDataValidationService;
        this.issueService = issueService;
        this.clock = clock;
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

    public long getMetrologyConfigId() {
        if (metrologyConfigId == null) {
            metrologyConfigId = getUsagePoint().getEffectiveMetrologyConfiguration(clock.instant()).map(HasId::getId).orElse(-1L);
        }
        return metrologyConfigId;
    }

    @Override
    public Optional<? extends OpenIssue> findExistingIssue() {
        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        getEndDevice().ifPresent(filter::setDevice);
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        filter.addStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
        Optional<? extends IssueDataValidation> foundIssue = issueDataValidationService.findAllDataValidationIssues(filter)
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

    private UsagePoint getUsagePoint() {
        return findMeter().get().getUsagePoint(clock.instant()).orElse(null);
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }
}
