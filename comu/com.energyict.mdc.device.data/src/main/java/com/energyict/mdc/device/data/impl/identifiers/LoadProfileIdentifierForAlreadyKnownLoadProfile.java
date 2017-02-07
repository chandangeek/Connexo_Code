/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifierType;

import javax.xml.bind.annotation.XmlElement;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

public class LoadProfileIdentifierForAlreadyKnownLoadProfile implements LoadProfileIdentifier<LoadProfile> {

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
    public LoadProfile findLoadProfile() {
        return this.loadProfile;
    }

    @Override
    public LoadProfileIdentifierType getLoadProfileIdentifierType() {
        return LoadProfileIdentifierType.ActualLoadProfile;
    }

    @Override
    public List<Object> getIdentifier() {
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
    public DeviceIdentifier<?> getDeviceIdentifier() {
        return new DeviceIdentifierForAlreadyKnownDeviceByMrID(this.loadProfile.getDevice());
    }

    @Override
    public String toString() {
        return MessageFormat.format("load profile with name ''{0}'' on device with name ''{1}''",
                loadProfile.getLoadProfileSpec().getLoadProfileType().getName(), loadProfile.getDevice().getName());
    }
}
