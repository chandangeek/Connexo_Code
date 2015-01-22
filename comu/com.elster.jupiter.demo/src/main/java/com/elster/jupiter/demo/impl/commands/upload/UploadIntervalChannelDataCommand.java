package com.elster.jupiter.demo.impl.commands.upload;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class UploadIntervalChannelDataCommand extends UploadChannelDataCommand{

    @Inject
    public UploadIntervalChannelDataCommand(MeteringService meteringService, DeviceService deviceService) {
        super(meteringService, deviceService);
    }

    @Override
    protected void validateReadingTypes() {
        super.validateReadingTypes();
        for (ReadingType readingType : getReadingTypes()) {
            if (readingType.getMeasuringPeriod() == TimeAttribute.NOTAPPLICABLE){
                throw new UnableToCreate("You should use the special command for non-interval reading types");
            }
        }
    }

    @Override
    protected Instant getTimeForReading(ReadingType readingType, Instant startDate, String controlValue) {
        if (controlValue.length() != 9){
            throw new UnableToCreate("Incorrect control value for importing data. Should be 000-00:00");
        }
        startDate = startDate.plus(Integer.valueOf(controlValue.substring(0, 3)), ChronoUnit.DAYS);
        startDate = startDate.plus(Integer.valueOf(controlValue.substring(4, 6)), ChronoUnit.HOURS);
        return startDate.plus(Integer.valueOf(controlValue.substring(7, 9)), ChronoUnit.MINUTES);
    }

}
