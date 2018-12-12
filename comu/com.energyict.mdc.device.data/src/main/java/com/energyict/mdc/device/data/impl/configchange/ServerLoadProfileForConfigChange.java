/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.LoadProfile;

/**
 * Provides functionality to manipulate the LoadProfile in order to perform a valid DeviceConfigChange
 */
public interface ServerLoadProfileForConfigChange extends LoadProfile{

    /**
     * Sets AND updates the new loadProfileSpec
     *
     * @param loadProfileSpec the new LoadProfileSpec which should server this LoadProfile
     */
    void setNewLoadProfileSpec(LoadProfileSpec loadProfileSpec);
}
