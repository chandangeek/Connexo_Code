/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.servicecall.ServiceCall;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HeadEndInterface {

    Optional<URL> getURLForEndDevice(EndDevice endDevice);

    EndDeviceCapabilities getCapabilities(EndDevice endDevice);

    CommandFactory getCommandFactory();

    CompletionOptions scheduleMeterRead(Meter meter, List<ReadingType> readingTypes, Instant instant);

    CompletionOptions scheduleMeterRead(Meter meter, List<ReadingType> readingTypes, Instant instant, ServiceCall serviceCall);

    CompletionOptions readMeter(Meter meter, List<ReadingType> readingTypes);

    CompletionOptions readMeter(Meter meter, List<ReadingType> readingTypes, ServiceCall serviceCall);

    CompletionOptions sendCommand(EndDeviceCommand endDeviceCommand, Instant releaseDate);

    CompletionOptions sendCommand(EndDeviceCommand endDeviceCommand, Instant releaseDate, ServiceCall parentServiceCall);

    String getAmrSystem();
}
