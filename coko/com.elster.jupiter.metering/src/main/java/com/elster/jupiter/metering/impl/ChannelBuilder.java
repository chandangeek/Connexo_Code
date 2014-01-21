package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;

public interface ChannelBuilder {

    ChannelBuilder meterActivation(MeterActivation meterActivation);

    ChannelBuilder readingTypes(ReadingTypeImpl main, ReadingTypeImpl... readingTypes);

    Channel build();
}
