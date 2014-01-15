package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.BusinessObject;

/**
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 11:17
 */
public interface DeviceCapabilityMapping extends BusinessObject {

    public String getDeviceProtocolJavaClassName();

    public int getDeviceProtocolCapabilities();
}
