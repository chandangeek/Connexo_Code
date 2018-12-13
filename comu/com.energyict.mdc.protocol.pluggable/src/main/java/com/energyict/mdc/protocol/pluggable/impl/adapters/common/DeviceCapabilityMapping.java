/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

public interface DeviceCapabilityMapping {

    public String getDeviceProtocolJavaClassName();

    public int getDeviceProtocolCapabilities();

}