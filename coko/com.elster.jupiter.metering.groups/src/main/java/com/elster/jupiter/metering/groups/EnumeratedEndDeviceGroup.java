package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.util.time.Interval;

import java.util.Date;

public interface EnumeratedEndDeviceGroup extends EndDeviceGroup {

    String TYPE_IDENTIFIER = "EEG";

    void endMembership(EndDevice endDevice, Date now);

    interface Entry {
        EndDevice getEndDevice();

        Interval getInterval();
    }

    Entry add(EndDevice endDevice, Interval interval);

    void remove(Entry entry);

}
