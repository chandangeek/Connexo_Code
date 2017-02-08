/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.upload;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.commands.upload.time.NoneIntervalReadingTimeProvider;
import com.elster.jupiter.demo.impl.commands.upload.time.TimeProvider;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.time.Instant;

public class AddNoneIntervalChannelReadingsCommand extends AddChannelReadingsCommand {

    private TimeProvider timeProvider;

    @Inject
    public AddNoneIntervalChannelReadingsCommand(MeteringService meteringService, DeviceService deviceService) {
        super(meteringService, deviceService);
        this.timeProvider = new NoneIntervalReadingTimeProvider();
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
        return this.timeProvider.getTimeForReading(readingType, startDate, controlValue);
    }
}
