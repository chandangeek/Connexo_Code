package com.energyict.mdc.issue.datavalidation.impl.event;

import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.impl.MessageSeeds;
import com.google.inject.Inject;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class CannotEstimateDataEvent extends DataValidationEvent {
    
    private Instant startTime;
    private Instant endTime;
    private String readingType;
    
    @Inject
    public CannotEstimateDataEvent(Thesaurus thesaurus, MeteringService meteringService, DeviceService deviceService, IssueDataValidationService issueDataValidationService, IssueService issueService) {
        super(thesaurus, meteringService, deviceService, issueDataValidationService, issueService);
    }
    
    @Override
    public void init(Map<?, ?> jsonPayload) {
        try {
            this.startTime = Instant.ofEpochMilli(((Number) jsonPayload.get("startTime")).longValue());
            this.endTime = Instant.ofEpochMilli(((Number) jsonPayload.get("endTime")).longValue());
            this.channelId = ((Number) jsonPayload.get("channelId")).longValue();
            this.readingType = (String) jsonPayload.get("readingType");
        } catch (Exception e) {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.UNABLE_TO_CREATE_EVENT, jsonPayload.toString());
        }
    }
    
    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataValidation) {
            OpenIssueDataValidation dataValidationIssue = (OpenIssueDataValidation) issue;
            Optional<Channel> channel = findChannel();
            Optional<ReadingType> readingType = findReadingType();
            dataValidationIssue.addNotEstimatedBlock(channel.get(), readingType.get(), startTime, endTime);
        }
    }
    
    private Optional<ReadingType> findReadingType() {
        return getMeteringService().getReadingType(readingType);
    }
}
