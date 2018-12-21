/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.HasAuditInfo;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface EndDeviceZone extends HasAuditInfo {

    long getId();

    Zone getZone();

    EndDevice getEndDevice();

    void setZone(Zone zone);

    void setEndDevice(EndDevice endDevice);

    void save();

    void delete();
}
