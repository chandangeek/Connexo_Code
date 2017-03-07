/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

@ProviderType
public interface DataQualityKpi {

    long getId();

    long getVersion();

    TemporalAmount getFrequency();

    Optional<Instant> getLatestCalculation();

    void delete();

    void makeObsolete();

    Optional<Instant> getObsoleteTime();

}
