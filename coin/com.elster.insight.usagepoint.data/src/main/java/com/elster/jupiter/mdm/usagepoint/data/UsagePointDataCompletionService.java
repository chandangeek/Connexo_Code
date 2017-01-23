package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@ProviderType
public interface UsagePointDataCompletionService {
    List<IChannelDataCompletionSummary> getDataCompletionStatistics(Channel channel, Range<Instant> interval);

    Map<ReadingTypeDeliverable, List<IChannelDataCompletionSummary>> getDataCompletionStatistics(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration,
                                                                                                 MetrologyContract contract, Range<Instant> interval);

    IChannelDataCompletionSummary getGeneralUsagePointDataCompletionSummary(Range<Instant> intervalWithData);
}
