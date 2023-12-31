/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.orm.JournalEntry;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface Meter extends EndDevice, ReadingContainer {

    String TYPE_IDENTIFIER = "M";

    /**
     * Stores {@link MeterReading}.
     *
     * @param system {@link QualityCodeSystem} that handles storage.
     * @param reading {@link MeterReading} to store.
     */
    void store(QualityCodeSystem system, MeterReading reading);

    /**
     * Stores {@link MeterReading}.
     *
     * @param system {@link QualityCodeSystem} that handles storage.
     * @param reading {@link MeterReading} to store.
     * @param readingDate used to set the reading date in case data is imported from another system.  If data is collected, it can be set to null
     */
    void store(QualityCodeSystem system, MeterReading reading, Instant readingDate);

    List<? extends MeterActivation> getMeterActivations();

    List<? extends MeterActivation> getMeterActivations(Range<Instant> range);

    MeterActivation activate(Instant start);

    MeterActivation activate(Range<Instant> start);

    /**
     * Use the {@link #activate(UsagePoint, MeterRole, Instant)} instead.
     * In fact this method will call the mentioned method with {@link com.elster.jupiter.metering.config.DefaultMeterRole#DEFAULT} meter role.
     */
    @Deprecated
    MeterActivation activate(UsagePoint usagePoint, Instant from);

    MeterActivation activate(UsagePoint usagePoint, MeterRole meterRole, Instant from);

    Optional<? extends MeterActivation> getCurrentMeterActivation();

    Optional<? extends MeterActivation> getMeterActivation(Instant when);

    List<? extends ReadingQualityRecord> getReadingQualities(Range<Instant> range);

    ReadingQualityFetcher findReadingQualities();

    List<JournalEntry<? extends ReadingQualityRecord>> getReadingQualitiesJournal(Range<Instant> range, List<ReadingType> readingTypes, List<Channel> channels);

    MeterConfigurationBuilder startingConfigurationOn(Instant startTime);

    Optional<MeterConfiguration> getConfiguration(Instant time);

    interface MeterConfigurationBuilder {

        MeterConfigurationBuilder endingAt(Instant endTime);

        MeterReadingTypeConfigurationBuilder configureReadingType(ReadingType readingType);

        MeterConfiguration create();
    }

    interface MeterReadingTypeConfigurationBuilder {

        MeterReadingTypeConfigurationBuilder withOverflowValue(BigDecimal value);

        MeterReadingTypeConfigurationBuilder withNumberOfFractionDigits(int digits);

        MeterConfiguration create();

        MeterReadingTypeMultiplierConfigurationBuilder withMultiplierOfType(MultiplierType multiplierOfType);

    }

    interface MeterReadingTypeMultiplierConfigurationBuilder {

        MeterConfigurationBuilder calculating(ReadingType readingType);
    }

}
