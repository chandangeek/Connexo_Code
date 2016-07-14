package com.elster.jupiter.mdm.usagepoint.data;


import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.config.EffectiveMetrologyContractOnUsagePoint;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Map;

@ProviderType
public interface UsagePointDataService {
    String COMPONENT_NAME = "UDC";

    ChannelDataValidationSummary getValidationSummary(Channel channel, Range<Instant> interval);

    Map<ReadingTypeDeliverable, ChannelDataValidationSummary> getValidationSummary(EffectiveMetrologyContractOnUsagePoint contract, Range<Instant> interval);
}
