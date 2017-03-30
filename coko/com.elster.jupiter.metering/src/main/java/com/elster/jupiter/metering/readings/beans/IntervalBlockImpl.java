/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings.beans;

import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntervalBlockImpl implements IntervalBlock {

    private final String readingTypeCode;
    private List<IntervalReading> intervals = new ArrayList<>();

    private IntervalBlockImpl(String readingTypeCode) {
        this.readingTypeCode = readingTypeCode;
    }

    public static IntervalBlockImpl of(String readingTypeCode) {
    	return new IntervalBlockImpl(readingTypeCode);
    }
    @Override
    public List<IntervalReading> getIntervals() {
        if (intervals != null && !intervals.isEmpty()) {
            return Collections.unmodifiableList(this.intervals);
        }
        return Collections.emptyList();
    }

    @Override
    public String getReadingTypeCode() {
        return this.readingTypeCode;
    }

    public void addIntervalReading(IntervalReading intervalReading){
        this.intervals.add(intervalReading);
    }

    public void addAllIntervalReadings(List<IntervalReading> intervalReadings){
        this.intervals.addAll(intervalReadings);
    }
}
