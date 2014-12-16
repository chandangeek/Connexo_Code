package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.associations.Effectivity;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

public interface EndDevicesInDeviceGroup {
    void save();

    interface Entry {
        //EndDevice getEndDevice();
        //EndDeviceGroup getEndDeviceGroup();
        long getEndDeviceId();
        long getEndDeviceGroupId();
    }
}
