package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.conditions.Subquery;
import com.google.common.collect.Range;

import java.time.Instant;


public interface EnumeratedEndDeviceGroup extends EndDeviceGroup {

    String TYPE_IDENTIFIER = "EEG";

    void endMembership(EndDevice endDevice, Instant when);

    /**
     * Returns a Subquery that gives all amrIds of the EndDevice of this group
     *
     * @return
     */
    Subquery getAmrIdSubQuery();

    interface Entry extends Effectivity {
        EndDevice getEndDevice();
    }

    Entry add(EndDevice endDevice, Range<Instant> range);

    void remove(Entry entry);

}
