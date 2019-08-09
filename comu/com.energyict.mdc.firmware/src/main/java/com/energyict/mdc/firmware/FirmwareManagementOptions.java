/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import aQute.bnd.annotation.ProviderType;

import java.util.EnumSet;
import java.util.Set;

@ProviderType
public interface FirmwareManagementOptions extends FirmwareCheckManagementOptions {
    void setOptions(Set<ProtocolSupportedFirmwareOptions> allowedOptions);

    @Override
    void activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption checkManagementOption, Set<FirmwareStatus> firmwareStatuses);

    @Override
    void deactivate(FirmwareCheckManagementOption checkManagementOption);

    Set<ProtocolSupportedFirmwareOptions> getOptions();

    @Override
    void save();

    @Override
    void delete();

    long getVersion();

    @Override
    boolean isActivated(FirmwareCheckManagementOption checkManagementOption);

    @Override
    EnumSet<FirmwareStatus> getStatuses(FirmwareCheckManagementOption checkManagementOption);
}
