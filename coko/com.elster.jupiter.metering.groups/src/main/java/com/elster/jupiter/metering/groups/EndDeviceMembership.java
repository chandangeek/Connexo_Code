package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.util.time.IntermittentInterval;

public interface EndDeviceMembership {

    IntermittentInterval getIntervals();

    EndDevice getEndDevice();
}
