/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone;

import com.elster.jupiter.metering.EndDevice;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface EndDeviceZoneBuilder {

    EndDeviceZoneBuilder withZone(Zone zone);

    EndDeviceZoneBuilder withEndDevice(EndDevice endDevice);

    EndDeviceZone create();

}
