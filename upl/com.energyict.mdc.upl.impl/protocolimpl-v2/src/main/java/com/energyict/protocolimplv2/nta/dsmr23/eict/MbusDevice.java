package com.energyict.protocolimplv2.nta.dsmr23.eict;

import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaMbusDevice;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MbusMessaging;

import java.util.Collections;
import java.util.List;

/**
 * @author sva
 * @since 29/11/13 - 9:59
 */
public class MbusDevice extends AbstractNtaMbusDevice {

    private Dsmr23MbusMessaging dsmr23MbusMessaging;

    public MbusDevice() {
        super();
    }

    @Override
    public DeviceMessageSupport getDeviceMessageSupport() {
        if (dsmr23MbusMessaging == null) {
            dsmr23MbusMessaging = new Dsmr23MbusMessaging(this);
        }
        return dsmr23MbusMessaging;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU KP DLMS (NTA DSMR2.3) Mbus Slave V2";
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-03-30 15:24:34 +0200 (Mon, 30 Mar 2015) $";
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

}