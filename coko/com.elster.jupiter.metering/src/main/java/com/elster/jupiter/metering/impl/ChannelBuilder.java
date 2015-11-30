package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;

public interface ChannelBuilder {

    ChannelBuilder meterActivation(MeterActivation meterActivation);

    ChannelBuilder readingTypes(ReadingType main, ReadingType... readingTypes);

    Channel build();
}
