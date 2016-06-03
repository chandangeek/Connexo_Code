package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ChannelBuilder {

    ChannelBuilder channelsContainer(ChannelsContainer channelsContainer);

    ChannelBuilder readingTypes(ReadingType main, ReadingType... readingTypes);

    Channel build();
}
