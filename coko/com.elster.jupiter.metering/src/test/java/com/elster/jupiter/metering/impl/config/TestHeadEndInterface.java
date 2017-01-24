package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ami.CommandFactory;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.EndDeviceCapabilities;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.servicecall.ServiceCall;

import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TestHeadEndInterface implements HeadEndInterface{
    private List<ReadingType> supportedReadingTypes;

    public TestHeadEndInterface(ReadingType... supported) {
        this.supportedReadingTypes = Arrays.stream(supported).collect(Collectors.toList());
    }

    @Override
    public Optional<URL> getURLForEndDevice(EndDevice endDevice) {
        return Optional.empty();
    }

    @Override
    public EndDeviceCapabilities getCapabilities(EndDevice endDevice) {
        return new EndDeviceCapabilities(this.supportedReadingTypes, Collections.emptyList());
    }

    @Override
    public CommandFactory getCommandFactory() {
        return null;
    }

    @Override
    public CompletionOptions scheduleMeterRead(Meter meter, List<ReadingType> readingTypes, Instant instant) {
        return null;
    }

    @Override
    public CompletionOptions scheduleMeterRead(Meter meter, List<ReadingType> readingTypes, Instant instant, ServiceCall serviceCall) {
        return null;
    }

    @Override
    public CompletionOptions readMeter(Meter meter, List<ReadingType> readingTypes) {
        return null;
    }

    @Override
    public CompletionOptions readMeter(Meter meter, List<ReadingType> readingTypes, ServiceCall serviceCall) {
        return null;
    }

    @Override
    public CompletionOptions sendCommand(EndDeviceCommand endDeviceCommand, Instant releaseDate) {
        return null;
    }

    @Override
    public CompletionOptions sendCommand(EndDeviceCommand endDeviceCommand, Instant releaseDate, ServiceCall parentServiceCall) {
        return null;
    }

    @Override
    public String getAmrSystem() {
        return KnownAmrSystem.MDC.getName();
    }
}
