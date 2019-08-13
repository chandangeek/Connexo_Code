/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import aQute.bnd.annotation.ProviderType;

import java.util.Set;

@ProviderType
public interface FirmwareManagementOptions extends FirmwareCheckManagementOptions {
    void setOptions(Set<ProtocolSupportedFirmwareOptions> allowedOptions);

    Set<ProtocolSupportedFirmwareOptions> getOptions();

    long getVersion();
}
