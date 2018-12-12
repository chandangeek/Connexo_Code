/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.HasId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MeterActivation extends HasId, Effectivity, ReadingContainer {

    Optional<UsagePoint> getUsagePoint();

    Optional<Meter> getMeter();

    Optional<MeterRole> getMeterRole();

    List<ReadingType> getReadingTypes();

    boolean isCurrent();

    void endAt(Instant end);

    long getVersion();

    Instant getCreateDate();

    Instant getModificationDate();

    Instant getStart();

    Instant getEnd();

    /**
     * @param startTime new start time for this MeterActivation, which must be earlier than the current start time.
     */
    void advanceStartDate(Instant startTime);

    /**
     * Split current meter activation into two, both of them have the same settings (meter, usage point, meter role, multiplier),
     * channels data and qualities after {@code breakTime} will be mowed to the new activation.
     *
     * @param breakTime point in time within {@link #getRange()}, the IllegalArgumentException will be thrown if
     * {@link #getRange()} doesn't contain breakTime.
     * @return new meter activation with range [breakTime; this.getEnd())
     */
    MeterActivation split(Instant breakTime);

    // multipliers
    void setMultiplier(MultiplierType type, BigDecimal value);

    void removeMultiplier(MultiplierType type);

    Map<MultiplierType, BigDecimal> getMultipliers();

    Optional<BigDecimal> getMultiplier(MultiplierType type);

    ChannelsContainer getChannelsContainer();
}
