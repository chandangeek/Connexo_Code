package com.elster.jupiter.mdm.usagepoint.data;


import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Map;

@ProviderType
public interface UsagePointDataService {
    String COMPONENT_NAME = "UDC";
    String BULK_ITEMIZER_QUEUE_DESTINATION = "ItemizeBulkUsagePoint";
    String BULK_HANDLING_QUEUE_DESTINATION = "HandleBulkUsagePoint";
    String BULK_ITEMIZER_QUEUE_SUBSCRIBER = "usagepoint.bulk.itemizer";
    String BULK_HANDLING_QUEUE_SUBSCRIBER = "usagepoint.bulk.handler";

    ChannelDataValidationSummary getValidationSummary(Channel channel, Range<Instant> interval);

    Map<ReadingTypeDeliverable, ChannelDataValidationSummary> getValidationSummary(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration,
                                                                                   MetrologyContract contract, Range<Instant> interval);
}
