/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;

import java.time.ZoneId;

@ProviderType
public interface ChannelBuilder {

    ChannelBuilder channelsContainer(ChannelsContainer channelsContainer);

    ChannelBuilder readingTypes(ReadingType main, ReadingType... readingTypes);

    ChannelBuilder offset(long offset);

    Channel build();

    ChannelBuilder zoneId(ZoneId zoneId);
}
