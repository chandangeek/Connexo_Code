package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummary;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummaryFlag;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.nls.Thesaurus;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.stream.Collectors;

public class ChannelDataValidationSummaryInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public ChannelDataValidationSummaryInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    ChannelDataValidationSummaryInfo from(ReadingTypeDeliverable deliverable,
                                          ChannelDataValidationSummary summary) {
        Range<Instant> interval = summary.getTargetInterval();
        return new ChannelDataValidationSummaryInfo(deliverable.getId(),
                deliverable.getName(),
                interval.hasLowerBound() ? interval.lowerEndpoint() : null, // should be present normally,
                                                                            // at least channels container start
                interval.hasUpperBound() ? interval.upperEndpoint() : null, // should be present normally,
                                                                            // at most "now"
                summary.getSum(),
                summary.getValues().entrySet().stream()
                        .map(flagEntry -> {
                            ChannelDataValidationSummaryFlag flag = flagEntry.getKey();
                            return new ChannelDataValidationSummaryFlagInfo(flag.getKey(),
                                    flag.getDisplayName(thesaurus),
                                    flagEntry.getValue());
                        })
                        .collect(Collectors.toList()));
    }
}
