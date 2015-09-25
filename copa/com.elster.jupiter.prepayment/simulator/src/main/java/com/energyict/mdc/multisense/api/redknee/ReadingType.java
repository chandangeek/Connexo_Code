package com.energyict.mdc.multisense.api.redknee;

/**
 * Created by bvn on 9/24/15.
 */
public class ReadingType {
    private final int measuringPeriod;

    public ReadingType(String readingTypeString) {
        String[] split = readingTypeString.split("\\.");
        if (!(split[0].equals("0"))) {
            throw new IllegalArgumentException("Only reading types with macro period 0 are supported");
        }
        measuringPeriod = TimeAttribute.get(Integer.parseInt(split[2])).getMinutes();
    }

    public int getMeasuringPeriod() {
        return measuringPeriod;
    }
}
