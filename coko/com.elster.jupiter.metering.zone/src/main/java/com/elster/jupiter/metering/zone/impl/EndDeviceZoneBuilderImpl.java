/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.zone.EndDeviceZone;
import com.elster.jupiter.metering.zone.EndDeviceZoneBuilder;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.orm.DataModel;

public class EndDeviceZoneBuilderImpl implements EndDeviceZoneBuilder {

    private final DataModel dataModel;

    private Zone zone;
    private EndDevice endDevice;

    public EndDeviceZoneBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public EndDeviceZoneBuilderImpl withZone(Zone zone) {
        this.zone = zone;
        return this;
    }

    @Override
    public EndDeviceZoneBuilderImpl withEndDevice(EndDevice endDevice) {
        this.endDevice = endDevice;
        return this;
    }

    @Override
    public EndDeviceZone create() {
        EndDeviceZoneImpl endDeviceZone = EndDeviceZoneImpl.from(dataModel, zone, endDevice);
        endDeviceZone.setZone(zone);
        endDeviceZone.setEndDevice(endDevice);
        endDeviceZone.save();
        return endDeviceZone;
    }
}
