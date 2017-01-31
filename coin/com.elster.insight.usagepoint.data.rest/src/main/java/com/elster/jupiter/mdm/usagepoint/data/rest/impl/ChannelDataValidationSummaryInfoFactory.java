/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummary;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummaryFlag;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class ChannelDataValidationSummaryInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public ChannelDataValidationSummaryInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    ChannelDataValidationSummaryInfo from(ReadingTypeDeliverable deliverable,
                                          ChannelDataValidationSummary summary) {
        return new ChannelDataValidationSummaryInfo(deliverable.getId(),
                deliverable.getName(),
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
