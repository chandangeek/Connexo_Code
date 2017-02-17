/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.slp;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.units.Unit;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface SyntheticLoadProfile extends HasId, HasName{
    String getDescription();

    Duration getInterval();

    Optional<ReadingType> getReadingType();

    Instant getStartTime();

    Period getDuration();

    void addValues(Map<Instant, BigDecimal> values);

    Optional<BigDecimal> getValue(Instant date);

    Map<Instant, BigDecimal> getValues(Range<Instant> range);

    void delete();
}
