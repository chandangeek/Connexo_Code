/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.Reading;
import com.google.common.collect.Multimap;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class DeviceReadingsData {
    private Multimap<ReadingType, IntervalReading> channelReadingsToStore;
    private Map<ReadingType, Instant> lastReadingPerChannel;
    private List<Reading> registerReadingsToStore;

    public DeviceReadingsData(Multimap<ReadingType, IntervalReading> channelReadingsToStore, Map<ReadingType, Instant> lastReadingPerChannel, List<Reading> registerReadingsToStore) {
        this.channelReadingsToStore = channelReadingsToStore;
        this.lastReadingPerChannel = lastReadingPerChannel;
        this.registerReadingsToStore = registerReadingsToStore;
    }

    public Multimap<ReadingType, IntervalReading> getChannelReadingsToStore() {
        return channelReadingsToStore;
    }


    public Map<ReadingType, Instant> getLastReadingPerChannel() {
        return lastReadingPerChannel;
    }


    public List<Reading> getRegisterReadingsToStore() {
        return registerReadingsToStore;
    }

    public void clear(){
        channelReadingsToStore.clear();
        lastReadingPerChannel.clear();
        registerReadingsToStore.clear();
    }
}
