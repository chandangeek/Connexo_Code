/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata;

import com.energyict.mdc.common.masterdata.ChannelType;
import com.energyict.mdc.common.masterdata.LoadProfileType;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the fact that a {@link LoadProfileType} uses a {@link ChannelType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-15 (17:55)
 */
@ProviderType
public interface LoadProfileTypeChannelTypeUsage {

    public LoadProfileType getLoadProfileType();

    public ChannelType getChannelType();

}