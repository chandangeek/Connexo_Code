package com.energyict.mdc.device.data;

import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

/**
 * Provides services that relate to {@link LoadProfile}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:04)
 */
@ProviderType
public interface LoadProfileService {

    /**
     * Finds the LoadProfile that is uniquely identified by the specified number.
     *
     * @param id The unique ID of the loadProfile
     * @return The requested LoadProfile
     */
    Optional<LoadProfile> findById(long id);

    /**
     * Finds the LoadProfile with the given {@link LoadProfileIdentifier}.
     *
     * @param identifier The LoadProfileIdentifier
     * @return the LoadProfile
     */
    Optional<LoadProfile> findByIdentifier(LoadProfileIdentifier identifier);

    Optional<LoadProfile> findAndLockLoadProfileByIdAndVersion(long id, long version);

}