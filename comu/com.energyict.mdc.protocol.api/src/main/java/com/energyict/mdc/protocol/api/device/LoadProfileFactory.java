/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device;

import java.util.List;

/**
 * Defines the behavior of a component
 * that is capable of finding {@link BaseLoadProfile}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-08 (16:34)
 */
public interface LoadProfileFactory {

    public List<BaseLoadProfile<BaseChannel>> findLoadProfilesByDevice(BaseDevice<BaseChannel, BaseLoadProfile<BaseChannel>, BaseRegister> device);

    public BaseLoadProfile findLoadProfileById(int loadProfileId);

}