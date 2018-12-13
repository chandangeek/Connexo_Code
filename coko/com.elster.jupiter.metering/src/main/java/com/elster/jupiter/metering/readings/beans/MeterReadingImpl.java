/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings.beans;

import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MeterReadingImpl implements MeterReading {

    private List<EndDeviceEvent> endDeviceEvents = new ArrayList<>();
    private List<Reading> readings = new ArrayList<>();
    private List<IntervalBlock> intervalBlocks = new ArrayList<>();

    private MeterReadingImpl() {
    }

    public static MeterReadingImpl newInstance() {
    	return new MeterReadingImpl();
    }

    public static MeterReadingImpl of(Reading reading) {
    	MeterReadingImpl result = newInstance();
    	result.addReading(reading);
    	return result;
    }

    @Override
    public List<Reading> getReadings() {
        if (this.readings != null && !this.readings.isEmpty()) {
            return Collections.unmodifiableList(this.readings);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<IntervalBlock> getIntervalBlocks() {
        if (this.intervalBlocks != null && !this.intervalBlocks.isEmpty()) {
            return Collections.unmodifiableList(this.intervalBlocks);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<EndDeviceEvent> getEvents() {
        if (this.endDeviceEvents != null && !this.endDeviceEvents.isEmpty()) {
            return Collections.unmodifiableList(this.endDeviceEvents);
        } else {
            return Collections.emptyList();
        }
    }

    public void addAllIntervalBlocks(List<? extends IntervalBlock> intervalBlocks){
        this.intervalBlocks.addAll(intervalBlocks);

    }

    public void addAllEndDeviceEvents(List<? extends EndDeviceEvent> endDeviceEvents) {
        this.endDeviceEvents.addAll(endDeviceEvents);
    }

    public void addAllReadings(List<? extends Reading> readings){
        this.readings.addAll(readings);
    }

    public void addReading(final Reading reading){
        this.readings.add(reading);
    }

    public void addIntervalBlock(IntervalBlock intervalBlock){
        this.intervalBlocks.add(intervalBlock);
    }

    public void addEndDeviceEvent(EndDeviceEvent endDeviceEvent) {
        this.endDeviceEvents.add(endDeviceEvent);
    }
}