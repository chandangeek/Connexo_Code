package com.elster.jupiter.metering;

import java.time.Instant;
import java.util.Map;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.time.Interval;

public interface ReadingStorer {

    boolean overrules();

    void execute();

	void addReading(Channel channel, BaseReading reading);
	
	void addReading(Channel channel, BaseReading reading, ProcessStatus status);

    Map<Channel, Interval> getScope();
    
    boolean processed(Channel channel, Instant instant);
}
