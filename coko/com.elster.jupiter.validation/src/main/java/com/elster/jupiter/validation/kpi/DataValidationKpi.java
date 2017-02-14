/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.kpi;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

@ProviderType
public interface DataValidationKpi extends HasId {

    Optional<TemporalAmount> dataValidationKpiCalculationIntervalLength();

    void setFrequency(TemporalAmount intervalLength);

    TemporalAmount getFrequency();

    EndDeviceGroup getDeviceGroup();

    void delete();

    Optional<Instant> getLatestCalculation();

    void dropDataValidationKpi();

    boolean isCancelled();

    long getVersion();

}

