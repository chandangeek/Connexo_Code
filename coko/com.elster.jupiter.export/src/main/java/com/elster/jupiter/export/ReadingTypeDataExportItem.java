package com.elster.jupiter.export;

import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

@ProviderType
public interface ReadingTypeDataExportItem {
    StandardDataSelector getSelector();

    ReadingContainer getReadingContainer();

    ReadingType getReadingType();

    Optional<Instant> getLastRun();

    Optional<Range<Instant>> getLastExportPeriod();

    Optional<Instant> getLastExportedDate();

    boolean isActive();

    Optional<? extends DataExportOccurrence> getLastOccurrence();

    default String getDescription(Instant momentOfTime) {
        String meterDescription = getReadingContainer()
                .getMeter(momentOfTime)
                .map(meter -> meter.getName() + ':')
                .orElse("");
        return meterDescription + getReadingType().getFullAliasName();
    }
}
