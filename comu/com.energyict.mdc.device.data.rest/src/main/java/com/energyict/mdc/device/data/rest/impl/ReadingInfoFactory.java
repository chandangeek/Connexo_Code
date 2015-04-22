package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.FlagsReading;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.TextReading;

import java.util.List;
import java.util.stream.Collectors;

public class ReadingInfoFactory {

    public static ReadingInfo asInfo(Reading reading, RegisterSpec registerSpec) {
        return ReadingInfoFactory.asInfo(reading, registerSpec, false);
    }

    public static ReadingInfo asInfo(Reading reading, RegisterSpec registerSpec, boolean isValidationStatusActive) {
        if (reading instanceof BillingReading) {
            return new BillingReadingInfo((BillingReading) reading, (NumericalRegisterSpec) registerSpec, isValidationStatusActive);
        }
        else if (reading instanceof NumericalReading) {
            return new NumericalReadingInfo((NumericalReading) reading, (NumericalRegisterSpec) registerSpec, isValidationStatusActive);
        }
        else if (reading instanceof TextReading) {
            return new TextReadingInfo((TextReading) reading, (TextualRegisterSpec) registerSpec);
        }
        else if (reading instanceof FlagsReading) {
            return new FlagsReadingInfo((FlagsReading) reading, (NumericalRegisterSpec) registerSpec);
        }

        throw new IllegalArgumentException("Unsupported reading type: " + reading.getClass().getSimpleName());
    }

    public static List<ReadingInfo> asInfoList(List<? extends Reading> readings, RegisterSpec registerSpec, boolean isValidationStatusActive) {
        return readings
                .stream()
                .map(r -> ReadingInfoFactory.asInfo(r, registerSpec, isValidationStatusActive))
                .collect(Collectors.toList());
    }

}