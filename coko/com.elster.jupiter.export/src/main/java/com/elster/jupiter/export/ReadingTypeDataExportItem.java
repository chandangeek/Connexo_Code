/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

@ProviderType
public interface ReadingTypeDataExportItem {

    ReadingDataSelectorConfig getSelector();

    ReadingContainer getReadingContainer();

    ReadingType getReadingType();

    Optional<Instant> getLastRun();

    Optional<Range<Instant>> getLastExportPeriod();

    Optional<Instant> getLastExportedDate();

    boolean isActive();

    Optional<? extends DataExportOccurrence> getLastOccurrence();

    String getDescription();

    IdentifiedObject getDomainObject();
}

