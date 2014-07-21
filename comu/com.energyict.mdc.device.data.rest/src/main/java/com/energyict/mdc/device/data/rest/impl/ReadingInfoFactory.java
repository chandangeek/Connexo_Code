package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.*;

import java.util.ArrayList;
import java.util.List;

public class ReadingInfoFactory {
    public static ReadingInfo asInfo(Reading reading, Register register) {
        if(NumericalReading.class.isAssignableFrom(reading.getClass())) {
            return new NumericalReadingInfo((NumericalReading)reading, register);
        } else if(EventReading.class.isAssignableFrom(reading.getClass())) {
            return new EventReadingInfo((EventReading)reading, register);
        } else if(TextReading.class.isAssignableFrom(reading.getClass())) {
            return new TextReadingInfo((TextReading)reading, register);
        }

        throw new IllegalArgumentException("Unsupported reading type: " + reading.getClass().getSimpleName());
    }

    public static List<ReadingInfo> asInfoList(List<Reading> readings, Register register) {
        List<ReadingInfo> readingInfos = new ArrayList<>(readings.size());
        for(Reading reading : readings) {
            readingInfos.add(ReadingInfoFactory.asInfo(reading, register));
        }

        return readingInfos;
    }
}
