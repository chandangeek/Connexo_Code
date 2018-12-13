/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.data.LogBook;

/**
 * Provides functionality to manipulate the LogBook in order to perform a valid DeviceConfigChange
*/
public interface ServerLogBookForConfigChange extends LogBook {

    /**
     * Sets AND updates the new logBookSpec
     *
     * @param logBookSpec the new LogBookSpec which should server this LogBook
     */
    void setNewLogBookSpec(LogBookSpec logBookSpec);

}
