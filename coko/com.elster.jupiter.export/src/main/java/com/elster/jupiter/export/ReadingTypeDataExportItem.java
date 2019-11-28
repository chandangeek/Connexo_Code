/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
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

    Optional<Instant> getLastExportedDate();

    Optional<Instant> getLastExportedPeriodEnd();

    boolean isActive();

    Optional<? extends DataExportOccurrence> getLastOccurrence();

    String getDescription();

    IdentifiedObject getDomainObject();

    void setLastRun(Instant lastRun);

    void setLastExportedDate(Instant lastExportedDate);

    void setLastExportedPeriodEnd(Instant lastExportedPeriodEnd);

    void update();

    void activate();

    void deactivate();

    void clearCachedReadingContainer();
}
