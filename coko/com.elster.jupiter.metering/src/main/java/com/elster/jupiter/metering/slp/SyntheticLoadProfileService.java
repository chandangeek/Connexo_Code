/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.slp;

import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface SyntheticLoadProfileService {
    String COMPONENTNAME = "SLP";

    SyntheticLoadProfileBuilder newSyntheticLoadProfile(String name, Period duration, Instant startTime, ReadingType readingType);

    List<SyntheticLoadProfile> findSyntheticLoadProfiles();

    Optional<SyntheticLoadProfile> findSyntheticLoadProfile(long id);

    Optional<SyntheticLoadProfile> findSyntheticLoadProfile(String name);
}
