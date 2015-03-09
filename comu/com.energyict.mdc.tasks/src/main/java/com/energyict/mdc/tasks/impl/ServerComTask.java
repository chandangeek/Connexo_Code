package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.FirmwareUpgradeTask;

/**
 * Adds behavior to ComTask that is private
 * to the server side implementation.
 * */
public interface ServerComTask extends ComTask{

    /**
     * Create a FirmwareUpgradeTask
     *
     * @return the newly created FirmwareUpgradeTask
     */
    public FirmwareUpgradeTask createFirmwareUpgradeTask() ;

}
