package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.EndDevice;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.RangeSet;

import java.time.Instant;

@ProviderType
public interface EndDeviceMembership {

    RangeSet<Instant> getRanges();

    EndDevice getEndDevice();

}