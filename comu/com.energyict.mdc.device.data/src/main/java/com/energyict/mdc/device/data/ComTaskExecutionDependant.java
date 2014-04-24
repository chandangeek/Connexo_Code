package com.energyict.mdc.device.data;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;

/**
 * Provides notification for the deletion of a ComTaskExecution
 *
 * Copyrights EnergyICT
 * Date: 18/04/14
 * Time: 13:41
 */
public interface ComTaskExecutionDependant {

    void comTaskExecutionDeleted(ComTaskExecution comTaskExecution);

}
