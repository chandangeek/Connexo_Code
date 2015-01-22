package com.elster.jupiter.demo.impl.commands.upload;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class AddNoneIntervalChannelReadingsCommand extends AddChannelReadingsCommand {

    @Inject
    public AddNoneIntervalChannelReadingsCommand(MeteringService meteringService, DeviceService deviceService) {
        super(meteringService, deviceService);
    }

    @Override
    protected void validateReadingTypes() {
        super.validateReadingTypes();
        for (ReadingType readingType : getReadingTypes()) {
            if (readingType.getMeasuringPeriod() != TimeAttribute.NOTAPPLICABLE){
                throw new UnableToCreate("You should use the special command for interval reading types");
            }
        }
    }

    @Override
    protected Instant getTimeForReading(ReadingType readingType, Instant startDate, String controlValue) {
        int counter = 0;
        try {
            counter = Integer.parseInt(controlValue);
        } catch (NumberFormatException ex){
            throw new UnableToCreate("Incorrect control value for importing data. Should be simple number");
        }
        ZonedDateTime local = ZonedDateTime.ofInstant(startDate, ZoneId.systemDefault());
        switch (readingType.getMacroPeriod()) {
            case MONTHLY:
                local = local.plus(1 * counter, ChronoUnit.MONTHS);
                break;
            case DAILY:
                local = local.plus(1 * counter, ChronoUnit.DAYS);
                break;
            default:
                throw new UnableToCreate("Unknown measurement period (Only daily and monthly are allowed). ");
        }
        return local.toInstant();
    }
}
