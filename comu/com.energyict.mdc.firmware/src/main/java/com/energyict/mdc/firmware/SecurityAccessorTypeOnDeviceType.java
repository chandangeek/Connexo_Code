/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.device.config.DeviceType;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SecurityAccessorTypeOnDeviceType {
    DeviceType getDeviceType();
    SecurityAccessorType getSecurityAccessorType();
    void delete();
    void save();
    void update();
    SecurityAccessorTypeOnDeviceType init(DeviceType deviceType, SecurityAccessorType securityAccessorType);
}