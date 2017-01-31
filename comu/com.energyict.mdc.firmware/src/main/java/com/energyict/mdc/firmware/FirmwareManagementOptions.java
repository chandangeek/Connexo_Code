/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import java.util.Set;

public interface FirmwareManagementOptions {

    void setOptions(Set<ProtocolSupportedFirmwareOptions> allowedOptions);

    Set<ProtocolSupportedFirmwareOptions> getOptions();

    void save();

    void delete();

    long getVersion();
}
