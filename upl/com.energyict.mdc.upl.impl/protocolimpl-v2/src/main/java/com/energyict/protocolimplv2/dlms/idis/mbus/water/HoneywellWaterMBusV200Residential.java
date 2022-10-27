package com.energyict.protocolimplv2.dlms.idis.mbus.water;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.common.AbstractMbusSlaveDevice;

public class HoneywellWaterMBusV200Residential extends AbstractMbusSlaveDevice {

    public HoneywellWaterMBusV200Residential(NlsService nlsService, PropertySpecService propertySpecService, Converter converter) {
        super(nlsService, propertySpecService, converter);
    }

    @Override
    public String getProtocolDescription() {
        return "Honeywell water Mbus V200 residential";
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-10-21 $";
    }
}