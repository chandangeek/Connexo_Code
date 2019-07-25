/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import aQute.bnd.annotation.ProviderType;

import java.util.EnumSet;
import java.util.Set;

@ProviderType
public interface FirmwareCampaignManagementOptions {

    void activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption checkManagementOption, Set<FirmwareStatus> firmwareStatuses);

    void deactivate(FirmwareCheckManagementOption checkManagementOption);

    void save();

    void delete();

    //long getVersion();

    boolean isActivated(FirmwareCheckManagementOption checkManagementOption);

    EnumSet<FirmwareStatus> getStatuses(FirmwareCheckManagementOption checkManagementOption);
}
