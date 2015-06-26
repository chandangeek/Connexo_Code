package com.energyict.mdc.issue.datavalidation.impl.event;

import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.impl.MessageSeeds;
import com.google.inject.Inject;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class SuspectDeletedEvent extends DataValidationEvent {
    
    private Instant readingTimestamp;

    @Inject
    public SuspectDeletedEvent(Thesaurus thesaurus, MeteringService meteringService, DeviceService deviceService, IssueDataValidationService issueDataValidationService, IssueService issueService) {
        super(thesaurus, meteringService, deviceService, issueDataValidationService, issueService);
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataValidation) {
            OpenIssueDataValidation dataValidationIssue = (OpenIssueDataValidation) issue;
            Optional<Channel> channel = findChannel();
            Optional<BaseReadingRecord> reading = channel.get().getReading(readingTimestamp);
            if (reading.isPresent()) {
                dataValidationIssue.removeNotEstimatedBlock(channel.get(), reading.get().getReadingType(), readingTimestamp);
            }
        }
    }

    @Override
    void init(Map<?, ?> jsonPayload) {
        try {
            this.readingTimestamp = Instant.ofEpochMilli((Long) jsonPayload.get("readingTimestamp"));
            this.channelId = ((Number) jsonPayload.get("channelId")).longValue();
        } catch (Exception e) {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.UNABLE_TO_CREATE_EVENT, jsonPayload.toString());
        }
    }
}
