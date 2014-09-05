package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.data.*;

import java.util.ArrayList;
import java.util.List;

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

    public static List<ReadingInfo> asInfoList(List<Reading> readings, RegisterSpec registerSpec, boolean isValidationStatusActive, List<DataValidationStatus> dataValidationStatuses) {
        List<ReadingInfo> readingInfos = new ArrayList<>(readings.size());
        DataValidationStatus[] dataValidationStatusesArray = new DataValidationStatus[readings.size()];
        dataValidationStatusesArray = dataValidationStatuses.toArray(dataValidationStatusesArray);
        int count = 0;
        for(Reading reading : readings) {
            DataValidationStatus dataValidationStatus = dataValidationStatusesArray[count++];
            readingInfos.add(ReadingInfoFactory.asInfo(reading, registerSpec, isValidationStatusActive, dataValidationStatus));
        }

        return readingInfos;
    }
}
