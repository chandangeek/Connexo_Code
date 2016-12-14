package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifierType;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import javax.xml.bind.annotation.XmlElement;

/**
 * Copyrights EnergyICT
 * Date: 2/23/15
 * Time: 3:11 PM
 */
public class DeviceIdentifierByLoadProfile implements DeviceIdentifier {

    private final LoadProfileIdentifier loadProfileIdentifier;

    public DeviceIdentifierByLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        this.loadProfileIdentifier = loadProfileIdentifier;
    }

    @Override
    public Device findDevice() {
        com.energyict.mdc.device.data.LoadProfile loadProfile = (com.energyict.mdc.device.data.LoadProfile) this.loadProfileIdentifier.getLoadProfile();    //Downcast to Connexo LoadProfile
        return loadProfile.getDevice();
    }

    @Override
    public String getIdentifier() {
        return loadProfileIdentifier.toString();
    }

    @Override
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.Other;
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceIdentifierByLoadProfile)) {
            return false;
        }

        DeviceIdentifierByLoadProfile that = (DeviceIdentifierByLoadProfile) o;

        return loadProfileIdentifier.equals(that.loadProfileIdentifier);

    }

    @Override
    public int hashCode() {
        return loadProfileIdentifier.hashCode();
    }

    @Override
    public String toString() {
        return "device having load profile identified by '" + this.loadProfileIdentifier + "'";
    }
}
