/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.FirmwareManagementTask;

/**
 * Adds behavior to ComTask that is private
 * to the server side implementation.
 * */
public interface SystemComTask extends ComTask{

    /**
     * Create a FirmwareUpgradeTask
     *
     * @return the newly created FirmwareUpgradeTask
     */
    FirmwareManagementTask createFirmwareUpgradeTask() ;

}