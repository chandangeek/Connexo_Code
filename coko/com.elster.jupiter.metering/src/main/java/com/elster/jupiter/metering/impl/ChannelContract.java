package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;

public interface ChannelContract extends Channel {

	Object[] toArray(BaseReading reading, ReadingType readingType, ProcessStatus status);
    void validateValues(BaseReading reading, Object[] values);
	TimeSeries getTimeSeries();

}
