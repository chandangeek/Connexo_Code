package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.inbound.LoadProfileIdentifierType;

import javax.xml.bind.annotation.XmlElement;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8/1/14
 * Time: 2:59 PM
 */
public class LoadProfileIdentifierForAlreadyKnownLoadProfile implements LoadProfileIdentifier {

    private final LoadProfile loadProfile;

    public LoadProfileIdentifierForAlreadyKnownLoadProfile(LoadProfile loadProfile) {
        this.loadProfile = loadProfile;
    }

    @Override
    public BaseLoadProfile<?> findLoadProfile() {
        return this.loadProfile;
    }

    @Override
    public LoadProfileIdentifierType getLoadProfileIdentifierType() {
        return LoadProfileIdentifierType.ActualLoadProfile;
    }

    @Override
    public List<Object> getIdentifier() {
        return Arrays.asList(loadProfile);
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {

    }
}
