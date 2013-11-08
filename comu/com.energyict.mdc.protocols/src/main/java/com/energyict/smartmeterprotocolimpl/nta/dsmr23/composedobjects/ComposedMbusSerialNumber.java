package com.energyict.smartmeterprotocolimpl.nta.dsmr23.composedobjects;

import com.energyict.dlms.DLMSAttribute;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 8:34:09
 */
public class ComposedMbusSerialNumber {

    private final DLMSAttribute manufacturerId;
    private final DLMSAttribute identificationNumber;
    private final DLMSAttribute version;
    private final DLMSAttribute deviceType;


    public ComposedMbusSerialNumber(final DLMSAttribute manufacturerId, final DLMSAttribute identificationNumber, final DLMSAttribute version, final DLMSAttribute deviceType) {
        this.manufacturerId = manufacturerId;
        this.identificationNumber = identificationNumber;
        this.version = version;
        this.deviceType = deviceType;
    }

    public DLMSAttribute getManufacturerId() {
        return manufacturerId;
    }

    public DLMSAttribute getIdentificationNumber() {
        return identificationNumber;
    }

    public DLMSAttribute getVersion() {
        return version;
    }

    public DLMSAttribute getDeviceType() {
        return deviceType;
    }
}
