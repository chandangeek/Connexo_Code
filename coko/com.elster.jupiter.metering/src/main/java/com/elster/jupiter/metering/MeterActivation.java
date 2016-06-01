package com.elster.jupiter.metering;

import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.HasId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MeterActivation extends HasId, Effectivity, ReadingContainer {

    Optional<UsagePoint> getUsagePoint();

    Optional<Meter> getMeter();

    Optional<MeterRole> getMeterRole();

    Channel createChannel(ReadingType main, ReadingType... readingTypes);

    List<Channel> getChannels();

    List<ReadingType> getReadingTypes();

    boolean isCurrent();

    void endAt(Instant end);

    long getVersion();

    Instant getStart();

    Instant getEnd();

    ZoneId getZoneId();

    /**
     * @param startTime new start time for this MeterActivation, which must be earlier than the current start time.
     */
    void advanceStartDate(Instant startTime);

    // multipliers
    void setMultiplier(MultiplierType type, BigDecimal value);

    Optional<BigDecimal> getMultiplier(MultiplierType type);

    void removeMultiplier(MultiplierType type);

    Map<MultiplierType, BigDecimal> getMultipliers();
}
