package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifierType;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

/**
 * Copyrights EnergyICT
 * Date: 2/23/15
 * Time: 3:16 PM
 */
public class DeviceIdentifierByLogBook implements DeviceIdentifier {

    private final LogBookIdentifier logBookIdentifier;

    public DeviceIdentifierByLogBook(LogBookIdentifier logBookIdentifier) {
        this.logBookIdentifier = logBookIdentifier;
    }

    @Override
    public Device findDevice() {
        return ((LogBook) this.logBookIdentifier.getLogBook()).getDevice();
    }

    @Override
    public String getIdentifier() {
        return this.logBookIdentifier.toString();
    }

    @Override
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.Other;
    }

    @Override
    public String getXmlType() {
        return null;
    }

    @Override
    public void setXmlType(String ignore) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceIdentifierByLogBook)) {
            return false;
        }

        DeviceIdentifierByLogBook that = (DeviceIdentifierByLogBook) o;

        return logBookIdentifier.equals(that.logBookIdentifier);

    }

    @Override
    public int hashCode() {
        return logBookIdentifier.hashCode();
    }

    @Override
    public String toString() {
        return "device having logbook identified by '" + this.logBookIdentifier + "'";
    }
}
