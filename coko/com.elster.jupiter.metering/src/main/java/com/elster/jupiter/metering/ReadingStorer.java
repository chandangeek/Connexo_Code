package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.time.Interval;

import java.time.Instant;
import java.util.Map;

/*
 * ReadingStorer only stores the Readings, not the associated Reading Qualities.
 * Use meter.store(MeterReading meterReading) to store ReadingQualities created by the meter
 * 
 */

public interface ReadingStorer {

    boolean overrules();

    void execute();

    void addReading(Channel channel, BaseReading reading);

    void addReading(Channel channel, BaseReading reading, ProcessStatus status);

    Map<Channel, Interval> getScope();
<<<<<<< Upstream, based on origin/master

=======
    
    /*
     * 
     * Indicates if a reading for the given was actually processed by the system.
     * Note that the implementation may check for duplicated readings, and as 
     * such may return false for an instant that had a corresponding addReading(channel, reading)
     * Note also that some readings may contain derived values based on the previous reading,
     * so the implementation may return true for an instant without a corresponding addReading(channel, reading) call
     * 
     */
>>>>>>> 12ed53c reading quality
    boolean processed(Channel channel, Instant instant);
}
