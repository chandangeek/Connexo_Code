package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.util.Iterator;
import java.util.List;

/**
* Copyrights EnergyICT
* Date: 09/05/14
* Time: 14:46
*/
public interface ComJobFactory {

    /**
     * Consumes the {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s from the ResultSet
     * and wraps them in the appropriate {@link com.energyict.mdc.engine.impl.core.ComJob}s.
     *
     *
     * @param comTaskExecutions the fetched ComTaskExecutions
     */
    List<ComJob> consume(Iterator<ComTaskExecution> comTaskExecutions);

}