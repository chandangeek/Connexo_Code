/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import aQute.bnd.annotation.ProviderType;

import java.util.EnumSet;
import java.util.Set;

@ProviderType
public interface FirmwareCampaignManagementOptions extends FirmwareCheckManagementOptions {

    @Override
    void activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption checkManagementOption, Set<FirmwareStatus> firmwareStatuses);

    @Override
    void deactivate(FirmwareCheckManagementOption checkManagementOption);

    @Override
    void save();

    @Override
    void delete();

    @Override
    boolean isActivated(FirmwareCheckManagementOption checkManagementOption);

    @Override
    EnumSet<FirmwareStatus> getStatuses(FirmwareCheckManagementOption checkManagementOption);
}
