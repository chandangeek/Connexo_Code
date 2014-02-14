package com.elster.jupiter.metering.readings.beans;

import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Our default implementation of a <i>series</i> of {@link IntervalReading IntervalReadings}.
 * The interval of the readings will be defined in the <i>ReadingTypeCode</i>
 *
 * Copyrights EnergyICT
 * Date: 25/11/13
 * Time: 15:17
 */
public class IntervalBlockImpl implements IntervalBlock {

    private final String readingTypeCode;
    private List<IntervalReading> intervals = new ArrayList<>();

    public IntervalBlockImpl(String readingTypeCode) {
        this.readingTypeCode = readingTypeCode;
    }

    @Override
    public List<IntervalReading> getIntervals() {
        if(intervals != null && intervals.size() > 0){
            return Collections.unmodifiableList(this.intervals);
        }
        return Collections.emptyList();
    }

    @Override
    public String getReadingTypeCode() {
        return this.readingTypeCode;
    }

    public void addIntervalReading(final IntervalReading intervalReading){
        this.intervals.add(intervalReading);
    }

    public void addAllIntervalReadings(List<IntervalReading> intervalReadings){
        this.intervals.addAll(intervalReadings);
    }
}
