package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.usagepoint.data.ChannelDataCompletionSummaryFlag;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataCompletionSummaryType;
import com.elster.jupiter.mdm.usagepoint.data.IChannelDataCompletionSummary;
import com.elster.jupiter.mdm.usagepoint.data.IChannelDataCompletionSummaryFlag;
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

    ChannelDataValidationSummaryInfo from(ReadingTypeDeliverable deliverable, List<IChannelDataCompletionSummary> summary) {
        IChannelDataCompletionSummary generalSummary = summary.stream().filter(sum -> sum.getType() == ChannelDataCompletionSummaryType.GENERAL).findFirst().orElse(null);
        IChannelDataCompletionSummary editedSummary = summary.stream().filter(sum -> sum.getType() == ChannelDataCompletionSummaryType.EDITED).findFirst().orElse(null);
        IChannelDataCompletionSummary validSummary = summary.stream().filter(sum -> sum.getType() == ChannelDataCompletionSummaryType.VALID).findFirst().orElse(null);

        ChannelDataValidationSummaryInfo channelDataValidationGeneralSummaryInfo = new ChannelDataValidationSummaryInfo(deliverable.getId(),
                deliverable.getName(),
                generalSummary.getSum(),
                generalSummary.getValues().entrySet().stream()
                        .map(flagEntry -> {
                            IChannelDataCompletionSummaryFlag flag = flagEntry.getKey();
                            if (flag == ChannelDataCompletionSummaryFlag.VALID && validSummary != null) {
                                return getFlagInfo(ChannelDataCompletionSummaryType.VALID, validSummary);
                            }
                            return new ChannelDataValidationSummaryFlagInfo(flag.getKey(),
                                    flag.getDisplayName(thesaurus),
                                    flagEntry.getValue());
                        })
                        .collect(Collectors.toList()));
        if (editedSummary != null) {
            channelDataValidationGeneralSummaryInfo.statistics.add(getFlagInfo(ChannelDataCompletionSummaryType.EDITED, editedSummary));
        }
        return channelDataValidationGeneralSummaryInfo;
    }

    private ChannelDataValidationSummaryFlagInfo getFlagInfo(ChannelDataCompletionSummaryType channelDataCompletionSummaryType, IChannelDataCompletionSummary summary) {
        return new ChannelDataValidationSummaryFlagInfo(channelDataCompletionSummaryType.getKey(),
                channelDataCompletionSummaryType.getDefaultFormat(),
                summary.getSum(),
                summary.getValues().entrySet().stream()
                        .map(flagEntry -> {
                            IChannelDataCompletionSummaryFlag flag = flagEntry.getKey();
                            return new ChannelDataValidationSummaryFlagInfo(flag.getKey(),
                                    flag.getDisplayName(thesaurus),
                                    flagEntry.getValue());
                        })
                        .collect(Collectors.toList()));
    }

}
