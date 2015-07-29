package com.energyict.mdc.masterdata;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the fact that a {@link LoadProfileType} uses a {@link com.energyict.mdc.masterdata.ChannelType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-15 (17:55)
 */
@ProviderType
public interface LoadProfileTypeChannelTypeUsage {

    public LoadProfileType getLoadProfileType();

    public ChannelType getChannelType();

}