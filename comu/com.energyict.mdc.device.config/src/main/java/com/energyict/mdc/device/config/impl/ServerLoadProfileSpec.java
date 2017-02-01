/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;

public interface ServerLoadProfileSpec extends LoadProfileSpec {

    /**
     * adds the specified {@link ChannelSpec}.
     *
     * @param channelSpec The ChannelSpec
     */
    void addChannelSpec(ChannelSpec channelSpec);

    /**
     * Removes the specified {@link ChannelSpec}.
     *
     * @param channelSpec The ChannelSpec
     */
    void removeChannelSpec(ChannelSpec channelSpec);

    /**
     * Clones the current LoadProfileSpec for the given DeviceConfiguration.
     *
     * @param deviceConfiguration the owner of the cloned LoadProfileSpec
     * @return the cloned LoadProfileSpec
     */
    LoadProfileSpec cloneForDeviceConfig(DeviceConfiguration deviceConfiguration);

}