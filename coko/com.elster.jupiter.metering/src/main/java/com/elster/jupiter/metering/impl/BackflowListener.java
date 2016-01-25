package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.readings.BaseReading;

public interface BackflowListener {

    void backflowOccurred(CimChannel cimChannel, BaseReading reading, BaseReading previous);
}
