/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.ChannelsContainer;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-21 (15:21)
 */
public interface SimpleChannelContract extends ChannelContract {
    SimpleChannelContract init(ChannelsContainer channelsContainer, List<IReadingType> readingTypes, Optional<Integer> hourOffset);
    SimpleChannelContract init(ChannelsContainer channelsContainer, ZoneId zoneId, List<IReadingType> readingTypes, Optional<Integer> hourOffset);
}