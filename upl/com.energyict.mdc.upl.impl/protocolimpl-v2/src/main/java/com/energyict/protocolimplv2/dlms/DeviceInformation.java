package com.energyict.protocolimplv2.dlms;

import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.ManufacturerInformation;

public class DeviceInformation {

    private final DeviceFunction function;
    private final ManufacturerInformation manufacturer;
    private final String version;
    private final String protocolName;


    public DeviceInformation(DeviceFunction function, ManufacturerInformation manufacturer, String version, String protocolName) {
        this.function = function;
        this.manufacturer = manufacturer;
        this.version = version;
        this.protocolName = protocolName;
    }


    public DeviceFunction getFunction() {
        return function;
    }

    public ManufacturerInformation getManufacturer() {
        return manufacturer;
    }

    public String getVersion() {
        return version;
    }

    /**
     *
     * @return name that should be printed in UI for slection
     */
    public String getProtocolDescription() {
        return this.protocolName;
    }
}

