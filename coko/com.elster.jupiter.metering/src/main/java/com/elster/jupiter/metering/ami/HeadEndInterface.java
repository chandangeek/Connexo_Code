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
    CompletionOptions scheduleMeterRead(Meter meter, List<ReadingType> readingTypes, Instant instant);
    CompletionOptions scheduleMeterRead(Meter meter, List<ReadingType> readingTypes, Instant instant, ServiceCall serviceCall);
    CompletionOptions readMeter(Meter meter, List<ReadingType> readingTypes);
    CompletionOptions readMeter(Meter meter, List<ReadingType> redingTypes, ServiceCall serviceCall);
    CompletionOptions sendCommand(Meter meter, EndDeviceCommand meterCmd, Instant instant);
    CompletionOptions sendCommand(Meter meter, EndDeviceCommand meterCmd, Instant instant, ServiceCall serviceCall);
}
