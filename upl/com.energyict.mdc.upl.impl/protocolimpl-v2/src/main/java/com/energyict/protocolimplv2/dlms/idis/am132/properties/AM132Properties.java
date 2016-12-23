package com.energyict.protocolimplv2.dlms.idis.am132.properties;

import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540Properties;

/**
 * Created by iulian on 12/23/2016.
 */
public class AM132Properties extends AM540Properties{
    @Override
    public boolean useBeaconMirrorDeviceDialect() {
        return false; // always a single connection - i.e. gateway-like
    }

    @Override
    public boolean useBeaconGatewayDeviceDialect() {
        return true; // always a single connection - i.e. gateway-like
    }

    @Override
    protected int getMirrorLogicalDeviceId() {
        return getNodeAddress();
    }

    @Override
    protected int getGatewayLogicalDeviceId() {
        return getNodeAddress();
    }
}
