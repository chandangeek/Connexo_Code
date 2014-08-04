package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;

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
}
