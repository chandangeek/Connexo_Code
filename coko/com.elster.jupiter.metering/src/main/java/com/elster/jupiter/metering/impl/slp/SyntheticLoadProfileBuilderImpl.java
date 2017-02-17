/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.slp;

import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileBuilder;
import com.elster.jupiter.util.units.Unit;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.TimeZone;

public class SyntheticLoadProfileBuilderImpl implements SyntheticLoadProfileBuilder {

    private final SyntheticLoadProfileServiceImpl syntheticLoadProfileService;
    private final MeteringService meteringService;

    private final TimeZone timeZone = TimeZone.getTimeZone("UTC");

    private String name;
    private String description;
    private Duration interval;
    private Unit unitOfMeasure;
    private Instant startTime;
    private Period duration;

    public SyntheticLoadProfileBuilderImpl(SyntheticLoadProfileServiceImpl syntheticLoadProfileService, MeteringService meteringService, String name) {
        this.syntheticLoadProfileService = syntheticLoadProfileService;
        this.meteringService = meteringService;
        this.name = name;
    }

    public SyntheticLoadProfileBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public SyntheticLoadProfileBuilder withInterval(Duration interval) {
        this.interval = interval;
        return this;
    }

    public SyntheticLoadProfileBuilder withUnitOfMeasure(Unit unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
        return this;
    }

    public SyntheticLoadProfileBuilder withStartTime(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    public SyntheticLoadProfileBuilder withDuration(Period duration) {
        this.duration = duration;
        return this;
    }

    public SyntheticLoadProfile build() {
        SyntheticLoadProfileImpl syntheticLoadProfile = syntheticLoadProfileService.getDataModel().getInstance(SyntheticLoadProfileImpl.class).initialize(name);
        syntheticLoadProfile.setDescription(description);
        syntheticLoadProfile.setInterval(interval);
        syntheticLoadProfile.setUnitOfMeasure(unitOfMeasure);
        syntheticLoadProfile.setStartTime(startTime);
        syntheticLoadProfile.setDuration(duration);

        Vault vault = syntheticLoadProfileService.getVault();
        RecordSpec recordSpec = syntheticLoadProfileService.getRecordSpec();
        TimeSeries timeSeries = vault.createRegularTimeSeries(recordSpec, timeZone, interval, 0);
        syntheticLoadProfile.setTimeSeries(timeSeries);

        syntheticLoadProfile.save();
        return syntheticLoadProfile;
    }
}