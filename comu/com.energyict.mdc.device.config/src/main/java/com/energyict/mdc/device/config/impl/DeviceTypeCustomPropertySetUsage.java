/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.energyict.mdc.device.config.DeviceType;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DeviceTypeCustomPropertySetUsage {

    DeviceType getDeviceType();

    RegisteredCustomPropertySet getRegisteredCustomPropertySet();
}