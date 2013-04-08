package com.energyict.protocolimplv2.ace4000;

import com.energyict.mdc.protocol.DeviceProtocolCapabilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Place holder protocol, requests are handled and parsed in the master protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 4/12/12
 * Time: 13:44
 * Author: khe
 */
public class ACE4000MBus extends ACE4000Outbound {

    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        List<DeviceProtocolCapabilities> capabilities = new ArrayList<DeviceProtocolCapabilities>();
        capabilities.add(DeviceProtocolCapabilities.PROTOCOL_SLAVE);
        return capabilities;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }
}