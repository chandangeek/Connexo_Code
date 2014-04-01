package com.energyict.mdc.device.data;

import com.energyict.mdc.common.ApplicationComponent;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.EarliestNextExecutionTimeStampAndPriority;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.device.data.Device;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Defines the behavior of an {@link ApplicationComponent}
 * that is capable of finding {@link ComTaskExecution}s.
 * <p>
 * Todo (JP-1125): this interface can and must be removed as soon as ComTaskExecution is moved to the mdc.device.data bundle
 * </p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (14:10)
 */
public interface ComTaskExecutionFactory {

    public List<ComTaskExecution> findComTaskExecutionsByConnectionTask(ConnectionTask connectionTask);

    public List<ComTaskExecution> findComTaskExecutionsForDefaultOutboundConnectionTask(Device device);

    public List<ComTaskExecution> findComTaskExecutionsByTopology(Device device);

    public List<ComTaskExecution> findByConnectionTask(ConnectionTask connectionTask);

    public List<ComTaskExecution> findAllByConnectionTask(ConnectionTask connectionTask);

    public EarliestNextExecutionTimeStampAndPriority getEarliestNextExecutionTimeStampAndPriority(ScheduledConnectionTask connectionTask);

    public List<ComTaskExecution> findRetryingComTaskExecutionsForConnectionTask(ScheduledConnectionTask connectionTask);

    public List<ComTaskExecution> findCurrentlyExecutingByConnectionTask(ScheduledConnectionTask task);

    public void synchronizeNextExecutionAndPriorityToMinimizeConnections(ScheduledConnectionTask connectionTask, Date when, int priority);

}