package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummary;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummaryFlag;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummaryType;
import com.elster.jupiter.mdm.usagepoint.data.IChannelDataValidationSummaryFlag;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class ChannelDataValidationSummaryInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public ChannelDataValidationSummaryInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    ChannelDataValidationSummaryInfo from(ReadingTypeDeliverable deliverable, List<ChannelDataValidationSummary> summary) {
        ChannelDataValidationSummary generalSummary = summary.stream().filter(sum -> sum.getType() == ChannelDataValidationSummaryType.GENERAL).findFirst().orElse(null);
        ChannelDataValidationSummary editedSummary = summary.stream().filter(sum -> sum.getType() == ChannelDataValidationSummaryType.EDITED).findFirst().orElse(null);
        ChannelDataValidationSummary validSummary = summary.stream().filter(sum -> sum.getType() == ChannelDataValidationSummaryType.VALID).findFirst().orElse(null);

        ChannelDataValidationSummaryInfo channelDataValidationGeneralSummaryInfo = new ChannelDataValidationSummaryInfo(deliverable.getId(),
                deliverable.getName(),
                generalSummary.getSum(),
                generalSummary.getValues().entrySet().stream()
                        .map(flagEntry -> {
                            IChannelDataValidationSummaryFlag flag = flagEntry.getKey();
                            if (flag == ChannelDataValidationSummaryFlag.VALID && validSummary != null) {
                                return getFlagInfo(ChannelDataValidationSummaryType.VALID, validSummary);
                            }
                            return new ChannelDataValidationSummaryFlagInfo(flag.getKey(),
                                    flag.getDisplayName(thesaurus),
                                    flagEntry.getValue());
                        })
                        .collect(Collectors.toList()));
        if (editedSummary != null) {
            channelDataValidationGeneralSummaryInfo.statistics.add(getFlagInfo(ChannelDataValidationSummaryType.EDITED, editedSummary));
        }
        return channelDataValidationGeneralSummaryInfo;
    }

    private ChannelDataValidationSummaryFlagInfo getFlagInfo(ChannelDataValidationSummaryType channelDataValidationSummaryType, ChannelDataValidationSummary summary) {
        return new ChannelDataValidationSummaryFlagInfo(channelDataValidationSummaryType.getKey(),
                channelDataValidationSummaryType.getDefaultFormat(),
                summary.getSum(),
                summary.getValues().entrySet().stream()
                        .map(flagEntry -> {
                            IChannelDataValidationSummaryFlag flag = flagEntry.getKey();
                            return new ChannelDataValidationSummaryFlagInfo(flag.getKey(),
                                    flag.getDisplayName(thesaurus),
                                    flagEntry.getValue());
                        })
                        .collect(Collectors.toList()));
    }

}
