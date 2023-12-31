/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

@ProviderType
public interface ReadingTypeDataExportItem extends HasId {

    ReadingDataSelectorConfig getSelector();

    ReadingContainer getReadingContainer();

    ReadingType getReadingType();

    Optional<Instant> getLastRun();

    Optional<Range<Instant>> getLastExportPeriod();

    Optional<Instant> getLastExportedChangedData();

    Optional<Instant> getLastExportedNewData();

    boolean isExportPostponedForNewData();

    boolean isExportPostponedForChangedData();

    Optional<TimeDuration> getRequestedReadingInterval();

    boolean isActive();

    Optional<? extends DataExportOccurrence> getLastOccurrence();

    String getDescription();

    IdentifiedObject getDomainObject();

    void setLastRun(Instant lastRun);

    void setLastExportedChangedData(Instant lastExportedChangedData);

    void setLastExportedNewData(Instant lastExportedNewData);

    void postponeExportForNewData();

    void postponeExportForChangedData();

    void overrideReadingInterval(TimeDuration readingInterval);

    void update();

    void activate();

    void deactivate();

    void clearCachedReadingContainer();
}
