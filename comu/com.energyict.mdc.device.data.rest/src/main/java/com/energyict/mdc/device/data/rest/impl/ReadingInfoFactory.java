package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.data.*;

import java.util.ArrayList;
import java.util.List;

public class ReadingInfoFactory {
    public static ReadingInfo asInfo(Reading reading, RegisterSpec registerSpec) {
        if(NumericalReading.class.isAssignableFrom(reading.getClass())) {
            return new NumericalReadingInfo((NumericalReading)reading, (NumericalRegisterSpec)registerSpec);
        } else if(BillingReading.class.isAssignableFrom(reading.getClass())) {
            return new EventReadingInfo((BillingReading)reading, (NumericalRegisterSpec)registerSpec);
        } else if(TextReading.class.isAssignableFrom(reading.getClass())) {
            return new TextReadingInfo((TextReading)reading, (TextualRegisterSpec)registerSpec);
        }

        throw new IllegalArgumentException("Unsupported reading type: " + reading.getClass().getSimpleName());
    }

    public static List<ReadingInfo> asInfoList(List<Reading> readings, RegisterSpec registerSpec) {
        List<ReadingInfo> readingInfos = new ArrayList<>(readings.size());
        for(Reading reading : readings) {
            readingInfos.add(ReadingInfoFactory.asInfo(reading, registerSpec));
        }

        return readingInfos;
    }
}
