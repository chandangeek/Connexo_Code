/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.ProtocolDialectProperties;

/**
 * Provides functionality to manipulate the ProtocolDialectProperties in order to perform a valid DeviceConfigChange
 */
public interface ServerProtocolDialectForConfigChange extends ProtocolDialectProperties{

    /**
     * Sets AND updates the new ProtocolDialectProperties
     *
     * @param newProtocolDialectConfigurationProperties the new protocolDialectConfigurationProperties which should serve this ProtocolDialectProperties
     */
    void setNewProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties newProtocolDialectConfigurationProperties);

}
