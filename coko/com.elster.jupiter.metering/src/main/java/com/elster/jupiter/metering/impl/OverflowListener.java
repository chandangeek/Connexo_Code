package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.readings.BaseReading;

public interface OverflowListener {

    void overflowOccurred(CimChannel cimChannel, BaseReading reading, BaseReading previous);
}
