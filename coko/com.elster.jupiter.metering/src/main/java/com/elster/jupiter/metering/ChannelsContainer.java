/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.HasId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ChannelsContainer extends ReadingContainer, Effectivity, HasId {

    Channel createChannel(ReadingType main, ReadingType... readingTypes);

    List<Channel> getChannels();

    default Optional<Channel> getChannel(ReadingType readingType) {
        return getChannels().stream().filter(channel -> channel.hasReadingType(readingType)).findFirst();
    }

    // Is this a good idea ?
    Instant getStart();


    Optional<Meter> getMeter();

    Optional<UsagePoint> getUsagePoint();

    Optional<BigDecimal> getMultiplier(MultiplierType type);
}
