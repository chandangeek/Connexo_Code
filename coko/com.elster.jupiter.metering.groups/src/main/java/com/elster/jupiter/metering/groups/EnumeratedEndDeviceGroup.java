package com.elster.jupiter.metering.groups;

import java.time.Instant;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.associations.Effectivity;
import com.google.common.collect.Range;


public interface EnumeratedEndDeviceGroup extends EndDeviceGroup {

    String TYPE_IDENTIFIER = "EEG";

    void endMembership(EndDevice endDevice, Instant when);

    interface Entry extends Effectivity {
        EndDevice getEndDevice();
    }

    Entry add(EndDevice endDevice, Range<Instant> range);

    void remove(Entry entry);

}
