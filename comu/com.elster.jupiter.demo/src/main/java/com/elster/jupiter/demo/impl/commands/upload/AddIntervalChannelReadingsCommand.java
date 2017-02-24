/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.upload;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.commands.upload.time.IntervalReadingTimeProvider;
import com.elster.jupiter.demo.impl.commands.upload.time.TimeProvider;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.time.Instant;

public class AddIntervalChannelReadingsCommand extends AddChannelReadingsCommand {

    private TimeProvider timeProvider;

    @Inject
    public AddIntervalChannelReadingsCommand(MeteringService meteringService, DeviceService deviceService) {
        super(meteringService, deviceService);
        this.timeProvider = new IntervalReadingTimeProvider();
    }

    @Override
    protected void validateReadingTypes() {
        super.validateReadingTypes();
        for (ReadingType readingType : getReadingTypes()) {
            if (readingType.getMeasuringPeriod() == TimeAttribute.NOTAPPLICABLE && !(readingType.getMacroPeriod().equals(MacroPeriod.DAILY) || readingType.getMacroPeriod().equals(MacroPeriod.MONTHLY))) {
                throw new UnableToCreate("You should use the special command for non-interval reading types");
            }
        }
    }

    @Override
    protected Instant getTimeForReading(ReadingType readingType, Instant startDate, String controlValue) {
        return this.timeProvider.getTimeForReading(readingType, startDate, controlValue);
    }

}
