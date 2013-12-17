package com.elster.jupiter.metering.events;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.readings.EndDeviceEvent;

public interface EndDeviceEventRecord extends EndDeviceEvent {

    long getProcessingFlags();

    EndDevice getEndDevice();

    EndDeviceEventType getEventType();

    void save();
}
