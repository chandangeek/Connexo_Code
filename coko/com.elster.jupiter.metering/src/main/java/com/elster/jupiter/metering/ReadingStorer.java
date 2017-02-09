/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.readings.BaseReading;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Map;

/*
 * ReadingStorer only stores the Readings, not the associated Reading Qualities.
 * Use meter.store(MeterReading meterReading) to store ReadingQualities created by the meter
 * 
 */

public interface ReadingStorer {

    boolean overrules();

    /**
     * Stores the readings.
     *
     * @param system {@link QualityCodeSystem} that handles storage.
     */
    void execute(QualityCodeSystem system);

    void addReading(CimChannel channel, BaseReading reading);

    void addReading(CimChannel channel, BaseReading reading, ProcessStatus status);

    Map<CimChannel, Range<Instant>> getScope();
    
    /*
     * 
     * Indicates if a reading for the given was actually processed by the system.
     * Note that the implementation may check for duplicated readings, and as 
     * such may return false for an instant that had a corresponding addReading(channel, reading)
     * Note also that some readings may contain derived values based on the previous reading,
     * so the implementation may return true for an instant without a corresponding addReading(channel, reading) call
     * 
     */

    boolean processed(Channel channel, Instant instant);

    StorerProcess getStorerProcess();
}
