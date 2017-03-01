/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.slp;

import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
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
    private ReadingType readingType;
    private Instant startTime;
    private Period duration;

    public SyntheticLoadProfileBuilderImpl(SyntheticLoadProfileServiceImpl syntheticLoadProfileService, MeteringService meteringService, String name) {
        this.syntheticLoadProfileService = syntheticLoadProfileService;
        this.meteringService = meteringService;
        this.name = name;
    }

    @Override
    public SyntheticLoadProfileBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public SyntheticLoadProfileBuilder withReadingType(ReadingType readingType) {
        if(!readingType.isRegular()){
            throw new IllegalArgumentException("Synthetic load profiles does not support irregular reading types");
        }
        this.readingType = readingType;
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

    @Override
    public SyntheticLoadProfile build() {
        SyntheticLoadProfileImpl syntheticLoadProfile = syntheticLoadProfileService.getDataModel().getInstance(SyntheticLoadProfileImpl.class).initialize(name);
        syntheticLoadProfile.setDescription(description);
        syntheticLoadProfile.setReadingType(readingType);
        syntheticLoadProfile.setStartTime(startTime);
        syntheticLoadProfile.setDuration(duration);

        Vault vault = syntheticLoadProfileService.getVault();
        RecordSpec recordSpec = syntheticLoadProfileService.getRecordSpec();
        TimeSeries timeSeries = vault.createRegularTimeSeries(recordSpec, timeZone, Duration.from(readingType.getIntervalLength().get()), 0);
        syntheticLoadProfile.setTimeSeries(timeSeries);

        syntheticLoadProfile.save();
        return syntheticLoadProfile;
    }
}