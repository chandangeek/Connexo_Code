/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import java.util.EnumSet;
import java.util.Set;

public interface FirmwareCheckManagementOptions {
    /**
     * Activates firmware check option with a given set of {@link FirmwareStatus FirmwareStatuses}, where applicable.
     * If statuses are applicable, empty set of statuses means deactivation of the check option.
     *
     * @param checkManagementOption The check option to activate.
     * @param firmwareStatuses The set of {@link FirmwareStatus FirmwareStatuses} to activate the provided check option with.
     */
    void activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption checkManagementOption, Set<FirmwareStatus> firmwareStatuses);

    void deactivate(FirmwareCheckManagementOption checkManagementOption);

    void save();

    void delete();

    boolean isActivated(FirmwareCheckManagementOption checkManagementOption);

    EnumSet<FirmwareStatus> getStatuses(FirmwareCheckManagementOption checkManagementOption);
}
