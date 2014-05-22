package com.energyict.mdc.protocol.api.device.data.identifiers;

import com.energyict.mdc.protocol.api.device.BaseLoadProfile;

import java.io.Serializable;

/**
 * Uniquely identifies a load profile that is stored in a physical device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (17:51)
 */
public interface LoadProfileIdentifier extends Serializable {

    /**
     * Finds the {@link BaseLoadProfile} that is uniquely identified by this LoadProfileIdentifier.
     *
     * @return The LoadProfile
     */
    public BaseLoadProfile findLoadProfile ();

}