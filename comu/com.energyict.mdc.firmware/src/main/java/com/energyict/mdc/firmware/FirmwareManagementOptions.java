/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import aQute.bnd.annotation.ProviderType;

import java.util.EnumSet;
import java.util.Set;

@ProviderType
public interface FirmwareManagementOptions {
    void setOptions(Set<ProtocolSupportedFirmwareOptions> allowedOptions);

    void activateFirmwareCheck(FirmwareCheckManagementOption checkManagementOption, Set<FirmwareStatus> firmwareStatuses);

    Set<ProtocolSupportedFirmwareOptions> getOptions();

    void save();

    void delete();

    long getVersion();

    boolean isActivated(FirmwareCheckManagementOption checkManagementOption);

    EnumSet<FirmwareStatus> getTargetFirmwareStatuses(FirmwareCheckManagementOption checkManagementOption);
}
