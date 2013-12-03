package com.energyict.protocolimplv2.nta.elster;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaMbusDevice;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MbusMessaging;

import java.util.ArrayList;
import java.util.List;

/**
 *  The MBus device used for the AM100 implementation of the NTA spec
 *
 * @author: sva
 * @since: 2/11/12 (11:26)
 */
public class MbusDevice extends AbstractNtaMbusDevice {

    private Dsmr23MbusMessaging dsmr23MbusMessaging;

    @Override
    public DeviceMessageSupport getDeviceMessageSupport() {
        if (dsmr23MbusMessaging == null) {
            dsmr23MbusMessaging = new Dsmr23MbusMessaging(this);
        }
        return dsmr23MbusMessaging;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AS220/AS1440 AM100 NTA Mbus Slave (protocolimpl V2)";
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return new ArrayList<>(0);
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return new ArrayList<>(0);
    }
}
