/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

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

    Optional<LoadProfile> findAndLockLoadProfileByIdAndVersion(long id, long version);
}