package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.FlagsReading;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.TextReading;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadingInfoFactory {
    public static ReadingInfo asInfo(Reading reading, RegisterSpec registerSpec) {
        return ReadingInfoFactory.asInfo(reading, registerSpec, false, null);
    }

    public static ReadingInfo asInfo(Reading reading, RegisterSpec registerSpec, boolean isValidationStatusActive, DataValidationStatus dataValidationStatus) {
        if (reading instanceof BillingReading) {
            return new BillingReadingInfo((BillingReading)reading, (NumericalRegisterSpec)registerSpec, isValidationStatusActive, dataValidationStatus);
        } else if(reading instanceof NumericalReading) {
            return new NumericalReadingInfo((NumericalReading)reading, (NumericalRegisterSpec)registerSpec, isValidationStatusActive, dataValidationStatus);
        } else if(reading instanceof TextReading) {
            return new TextReadingInfo((TextReading)reading, (TextualRegisterSpec)registerSpec);
        } else if(reading instanceof FlagsReading) {
            return new FlagsReadingInfo((FlagsReading)reading, (NumericalRegisterSpec)registerSpec);
        }

        throw new IllegalArgumentException("Unsupported reading type: " + reading.getClass().getSimpleName());
    }

    public static List<ReadingInfo> asInfoList(List<? extends Reading> readings, RegisterSpec registerSpec, boolean isValidationStatusActive, List<DataValidationStatus> dataValidationStatuses) {
        List<ReadingInfo> readingInfos = new ArrayList<>(readings.size());
        Map<Instant, DataValidationStatus> statuses = new HashMap<>(dataValidationStatuses.size());
        dataValidationStatuses.stream().forEach(s -> statuses.put(s.getReadingTimestamp(), s));
        for(Reading reading : readings) {
            DataValidationStatus dataValidationStatus = statuses.get(reading.getActualReading().getTimeStamp());
            readingInfos.add(ReadingInfoFactory.asInfo(reading, registerSpec, isValidationStatusActive, dataValidationStatus));
        }

        return readingInfos;
    }
}
