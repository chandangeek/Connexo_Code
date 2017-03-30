/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;

import java.util.Optional;
import java.util.Set;

public interface ValidationContext {
    Set<QualityCodeSystem> getQualityCodeSystems();

    ValidationContext setQualityCodeSystems(Set<QualityCodeSystem> qualityCodeSystems);

    ChannelsContainer getChannelsContainer();

    Optional<Meter> getMeter();

    Optional<UsagePoint> getUsagePoint();

    Optional<ReadingType> getReadingType();

    Optional<MetrologyContract> getMetrologyContract();
}
