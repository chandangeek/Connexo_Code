package com.energyict.mdc.device.config;

import com.energyict.mdc.common.NamedBusinessObject;
import java.util.List;

/**
 * Models a set of {@link ProtocolTask}s which can be scheduled for a Device.
 * Multiple Devices can use the same ComTask.
 *
 * @author gna
 * @since 19/04/12 - 13:52
 */
public interface ComTask extends NamedBusinessObject {

    /**
     * @return true if collected data can be stored, false otherwise
     */
    public boolean storeData();

    /**
     * @return a List of {@link ProtocolTask ProtocolTasks} for this ComTask
     */
    public List<ProtocolTask> getProtocolTasks();

    /**
     * Keeps track of the maximum number of consecutive failures a comTask can have before marking it as failed.
     *
     * @return the maximum number of consecutive failures that a ComTaskExecution using this ComTask can have
     */
    public int getMaxNumberOfTries();

}
