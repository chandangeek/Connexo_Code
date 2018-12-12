/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.kpi;

import com.elster.jupiter.util.time.TemporalAmountComparator;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Optional;

public enum RegisteredDevicesKpiFrequency {
    FIFTEEN_MINUTES(Duration.of(15, ChronoUnit.MINUTES)),
    FOUR_HOURS(Duration.of(4, ChronoUnit.HOURS)),
    TWELVE_HOURS(Duration.of(12, ChronoUnit.HOURS)),
    ONE_DAY(Period.ofDays(1));

    private final TemporalAmount frequency;

    RegisteredDevicesKpiFrequency(TemporalAmount frequency) {
        this.frequency = frequency;
    }

    public TemporalAmount getFrequency() {
        return frequency;
    }

    public static Optional<RegisteredDevicesKpiFrequency> valueOf(TemporalAmount frequencyToFind) {
        return Arrays.stream(values())
                .filter(freq -> new TemporalAmountComparator().compare(frequencyToFind, freq.getFrequency()) == 0)
                .findAny();
    }
}
