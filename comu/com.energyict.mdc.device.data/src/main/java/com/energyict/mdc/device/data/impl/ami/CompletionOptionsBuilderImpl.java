/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.CompletionOptionsBuilder;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.data.Device;

import jersey.repackaged.com.google.common.collect.Lists;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class CompletionOptionsBuilderImpl implements CompletionOptionsBuilder {
    private final MultiSenseHeadEndInterfaceImpl multiSenseHeadEndInterface;
    private final Device device;
    private final Instant instant;
    private List<ReadingType> readingTypes = Lists.newArrayList();
    private ServiceCall serviceCall;

    public CompletionOptionsBuilderImpl(MultiSenseHeadEndInterfaceImpl multiSenseHeadEndInterface, Device device, Instant instant) {
        this.multiSenseHeadEndInterface = multiSenseHeadEndInterface;
        this.device = device;
        this.instant = instant;
    }

    public CompletionOptionsBuilder withReadingTypes(List<ReadingType> readingTypes) {
        if (readingTypes != null) {
            this.readingTypes = readingTypes;
        }
        return this;
    }

    @Override
    public CompletionOptionsBuilder filterReadingTypes(List<ReadingType> readingTypesToFilter) {
        if (readingTypesToFilter != null) {
            this.readingTypes = readingTypesToFilter.stream().filter(this.readingTypes::contains).collect(Collectors.toList());
        }
        return this;
    }

    @Override
    public CompletionOptions build() {
        return this.multiSenseHeadEndInterface.scheduleMeterRead(device, readingTypes, instant, serviceCall);
    }
}
