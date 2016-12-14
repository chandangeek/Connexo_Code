package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifierType;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlElement;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8/1/14
 * Time: 2:59 PM
 */
public class LoadProfileIdentifierForAlreadyKnownLoadProfile implements LoadProfileIdentifier {

    private final LoadProfile loadProfile;
    private final ObisCode profileObisCode;

    public LoadProfileIdentifierForAlreadyKnownLoadProfile(LoadProfile loadProfile, ObisCode obisCode) {
        this.loadProfile = loadProfile;
        this.profileObisCode = obisCode;
    }

    @Override
    public ObisCode getProfileObisCode() {
        return profileObisCode;
    }

    @Override
    public LoadProfile getLoadProfile() {
        return this.loadProfile;
    }

    @Override
    public LoadProfileIdentifierType getLoadProfileIdentifierType() {
        return LoadProfileIdentifierType.ActualLoadProfile;
    }

    @Override
    public List<Object> getParts() {
        return Collections.singletonList(loadProfile);
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
    public DeviceIdentifier getDeviceIdentifier() {
        return new DeviceIdentifierForAlreadyKnownDeviceByMrID(this.loadProfile.getDevice());
    }

    @Override
    public String toString() {
        return MessageFormat.format("load profile with name ''{0}'' on device having MRID {1}", loadProfile.getLoadProfileSpec().getLoadProfileType().getName(), loadProfile.getId());
    }
}
