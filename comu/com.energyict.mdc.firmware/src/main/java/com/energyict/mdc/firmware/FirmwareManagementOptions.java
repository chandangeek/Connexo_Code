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

    /**
     * Activates firmware check option for a given set of {@link FirmwareStatus FirmwareStatuses}.
     * Empty set of statuses means deactivation of the check option.
     *
     * @param checkManagementOption The check option to activate or deactivate.
     * @param firmwareStatuses The set of {@link FirmwareStatus FirmwareStatuses} to activate the provided check option for.
     * Empty set of statuses means deactivation of the check option.
     */
    void activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption checkManagementOption, Set<FirmwareStatus> firmwareStatuses);

    Set<ProtocolSupportedFirmwareOptions> getOptions();

    void save();

    void delete();

    long getVersion();

    boolean isActivated(FirmwareCheckManagementOption checkManagementOption);

    EnumSet<FirmwareStatus> getTargetFirmwareStatuses(FirmwareCheckManagementOption checkManagementOption);
}
