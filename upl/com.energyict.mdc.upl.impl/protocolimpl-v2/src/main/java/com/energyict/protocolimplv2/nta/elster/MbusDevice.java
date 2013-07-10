package com.energyict.protocolimplv2.nta.elster;

import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.common.TempDeviceMessageSupport;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaMbusDevice;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaProtocol;

/**
 * @author: sva
 * @since: 2/11/12 (11:26)
 */
public class MbusDevice extends AbstractNtaMbusDevice {

    private DeviceMessageSupport messageProtocol;

    public MbusDevice() {
        super();
    }

    public MbusDevice(final AbstractNtaProtocol meterProtocol, final String serialNumber, final int physicalAddress) {
        super(meterProtocol, serialNumber, physicalAddress);
    }

    @Override
    public DeviceMessageSupport getMessageProtocol() {
        if (messageProtocol == null) {
            // messageProtocol = Dsmr23MbusMessaging(); ToDo
            messageProtocol = new TempDeviceMessageSupport();
        }
        return messageProtocol;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }
}
