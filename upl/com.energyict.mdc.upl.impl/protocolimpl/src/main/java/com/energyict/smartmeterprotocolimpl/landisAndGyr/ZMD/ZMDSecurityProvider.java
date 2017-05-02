package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;

/**
 * Default implementation of the securityProvider for the ZMD Meters.
 * Provides all the securityKeys, just for LOCAL purpose
 * Functionality is implemented according to the NTA specification
 * <p>
 * Copyrights EnergyICT
 * User: sva
 * Date: 14/12/11
 * Time: 11:36
 */
public class ZMDSecurityProvider extends NTASecurityProvider {
    public ZMDSecurityProvider(TypedProperties properties) {
        super(properties);
    }
}