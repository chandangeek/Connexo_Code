package com.elster.jupiter.metering.groups;

import java.time.Instant;

import com.elster.jupiter.metering.EndDevice;
import com.google.common.collect.RangeSet;

public interface EndDeviceMembership {

    RangeSet<Instant> getRanges();

    EndDevice getEndDevice();
}
