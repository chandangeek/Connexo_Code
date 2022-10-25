/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.elster.jupiter.pki.SecurityAccessor;
import com.energyict.mdc.common.device.config.DeviceType;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SecurityAccessorOnDeviceType {
    DeviceType getDeviceType();

    SecurityAccessor getSecurityAccessor();

    void delete();

    void save();

    void update();

    SecurityAccessorOnDeviceType init(DeviceType deviceType, SecurityAccessor securityAccessor);
}
