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

    // multipliers
    void setMultiplier(MultiplierType type, BigDecimal value);

    void removeMultiplier(MultiplierType type);

    Map<MultiplierType, BigDecimal> getMultipliers();

    Optional<BigDecimal> getMultiplier(MultiplierType type);

    ChannelsContainer getChannelsContainer();
}
