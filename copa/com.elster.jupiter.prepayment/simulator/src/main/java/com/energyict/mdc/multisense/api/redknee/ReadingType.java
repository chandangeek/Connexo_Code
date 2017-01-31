/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.redknee;

/**
 * Created by bvn on 9/24/15.
 */
public class ReadingType {
    private final int measuringPeriod;
    private final String readingTypeString;

    public ReadingType(String readingTypeString) {
        this.readingTypeString = readingTypeString;
        String[] split = readingTypeString.split("\\.");
        if (!(split[0].equals("0"))) {
            throw new IllegalArgumentException("Only reading types with macro period 0 are supported");
        }
        int measuringPeriod = Integer.parseInt(split[2]);
        if (measuringPeriod==100 || measuringPeriod==101) {
            int argument = Integer.parseInt(split[9]);
            int denominator = Integer.parseInt(split[10]);
        }
        this.measuringPeriod = TimeAttribute.get(measuringPeriod).getMinutes();
    }

    public int getMeasuringPeriod() {
        return measuringPeriod;
    }

    @Override
    public String toString() {
        return readingTypeString;
    }
}
