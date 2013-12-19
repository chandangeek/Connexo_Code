package com.elster.jupiter.metering.impl.test;

import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntervalBlockImpl implements IntervalBlock {

    private final String mrid;
    private List<IntervalReading> intervals;

    public IntervalBlockImpl(String mrid) {
        this.mrid = mrid;
    }

    @Override
    public List<IntervalReading> getIntervals() {
        if(intervals != null && intervals.size() > 0){
            return intervals;
        }
        return Collections.emptyList();
    }

    @Override
    public String getReadingTypeCode() {
        return this.mrid;
    }

    public void addIntervalReading(final IntervalReading intervalReading){
        if (this.intervals == null) {
            this.intervals = new ArrayList<>();
        }
        this.intervals.add(intervalReading);
    }
}
