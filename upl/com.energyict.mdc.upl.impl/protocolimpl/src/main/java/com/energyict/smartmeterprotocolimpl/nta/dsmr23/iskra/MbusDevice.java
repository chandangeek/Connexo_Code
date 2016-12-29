package com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra;

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessaging;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 12:04:42
 */
public class MbusDevice extends AbstractNtaMbusDevice {

    public MbusDevice(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public MbusDevice(AbstractSmartNtaProtocol meterProtocol, PropertySpecService propertySpecService, String serialNumber, int physicalAddress) {
        super(meterProtocol, propertySpecService, serialNumber, physicalAddress);
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new Dsmr23MbusMessaging();
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public void setProperties(TypedProperties properties) {
    }

}