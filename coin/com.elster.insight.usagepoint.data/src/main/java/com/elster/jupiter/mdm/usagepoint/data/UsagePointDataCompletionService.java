/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface UsagePointDataCompletionService {
    List<IChannelDataCompletionSummary> getDataCompletionStatistics(UsagePoint usagePoint, MetrologyPurpose metrologyPurpose, Range<Instant> interval, ReadingType readingType);

    Map<ReadingTypeDeliverable, List<IChannelDataCompletionSummary>> getDataCompletionStatistics(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration,
                                                                                                 MetrologyContract contract, Range<Instant> interval);

    IChannelDataCompletionSummary getGeneralUsagePointDataCompletionSummary(Range<Instant> intervalWithData);

    Optional<Instant> getLastChecked(UsagePoint usagePoint, MetrologyPurpose metrologyPurpose);

    Optional<Instant> getLastChecked(UsagePoint usagePoint, MetrologyPurpose metrologyPurpose, ReadingType readingType);
}
