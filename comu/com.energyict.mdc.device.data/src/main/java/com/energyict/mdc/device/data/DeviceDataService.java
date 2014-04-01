package com.energyict.mdc.device.data;

import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.TemporalExpression;
import com.energyict.mdc.device.data.impl.InfoType;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.google.common.base.Optional;

import java.util.List;
import java.util.TimeZone;

/**
 * Provides services that relate to {@link Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:27)
 */
public interface DeviceDataService {

    public static String COMPONENTNAME = "DDC";

    /**
     * Creates a new {@link InboundConnectionTask} with the minimal required properties
     * against the specified Device.
     * Note that the new InboundConnectionTask is not persisted yet as you may want
     * to complete the non required properties before saving it for the first time.
     *
     * @param device The Device
     * @param partialInboundConnectionTask The PartialInboundConnectionTask
     * @param inboundComPortPool The InboundComPortPool
     * @return The new InboundConnectionTask
     */
    public InboundConnectionTask newInboundConnectionTask (Device device, PartialInboundConnectionTask partialInboundConnectionTask, InboundComPortPool inboundComPortPool);

    /**
     * Creates a new {@link ScheduledConnectionTask}, that uses the {@link ConnectionStrategy#AS_SOON_AS_POSSIBLE} strategy,
     * with the minimal required properties against the specified Device.
     * Note that the new ScheduledConnectionTask is not persisted yet as you may want
     * to complete the non required properties before saving it for the first time.
     *
     * @param device The Device
     * @param partialConnectionTask The PartialScheduledConnectionTask
     * @param comPortPool The OutboundComPortPool
     * @return The new ScheduledConnectionTask
     */
    public ScheduledConnectionTask newAsapConnectionTask(Device device, PartialScheduledConnectionTask partialConnectionTask, OutboundComPortPool comPortPool);

    /**
     * Creates a new {@link ScheduledConnectionTask}, that uses the {@link ConnectionStrategy#MINIMIZE_CONNECTIONS} strategy,
     * with the minimal required properties against the specified Device.
     * Note that the new ScheduledConnectionTask is not persisted yet as you may want
     * to complete the non required properties before saving it for the first time.
     *
     * @param device The Device
     * @param partialConnectionTask The PartialScheduledConnectionTask
     * @param comPortPool The OutboundComPortPool
     * @param nextExecutionSpecs The specification for the next execution of the new ScheduledConnectionTask
     * @return The new ScheduledConnectionTask
     */
    public ScheduledConnectionTask newMinimizeConnectionTask(Device device, PartialScheduledConnectionTask partialConnectionTask, OutboundComPortPool comPortPool, TemporalExpression nextExecutionSpecs);

    public ConnectionInitiationTask newConnectionInitiationTask(Device device, PartialConnectionInitiationTask partialConnectionTask, OutboundComPortPool comPortPool);

    public Optional<ConnectionTask> findConnectionTask (long id);

    public Optional<InboundConnectionTask> findInboundConnectionTask (long id);

    public Optional<ScheduledConnectionTask> findScheduledConnectionTask (long id);

    public Optional<ConnectionInitiationTask> findConnectionInitiationTask (long id);

    /**
     * Finds the {@link ConnectionTask} on the specified Device
     * that uses the specified {@link PartialConnectionTask}.
     * Note that there can be only one such ConnectionTask.
     *
     * @param partialConnectionTask The PartialConnectionTask
     * @param device The Device
     * @return The ConnectionTask or <code>null</code> if there is no such ConnectionTask yet
     */
    public Optional<ConnectionTask> findConnectionTaskForPartialOnDevice(PartialConnectionTask partialConnectionTask, Device device);

    /**
     * Finds the {@link ConnectionTask}s that are configured
     * against the specified Device.
     *
     * @param device the Device
     * @return the List of ConnectionTask
     */
    public List<ConnectionTask> findConnectionTasksByDevice(Device device);

    /**
     * Finds the all {@link ConnectionTask}s, i.e. including the obsolete ones,
     * that are configured against the specified Device.
     *
     * @param device the Device
     * @return the List of ConnectionTask
     * @see ConnectionTask#isObsolete()
     */
    public List<ConnectionTask> findAllConnectionTasksByDevice(Device device);

    /**
     * Finds the {@link InboundConnectionTask}s that are configured
     * against the specified Device.
     *
     * @param device the Device
     * @return the List of InboundConnectionTask
     */
    public List<InboundConnectionTask> findInboundConnectionTasksByDevice(Device device);

    /**
     * Finds the {@link ScheduledConnectionTask}s that are configured
     * against the specified Device.
     *
     * @param device the Device
     * @return the List of ScheduledConnectionTask
     */
    public List<ScheduledConnectionTask> findScheduledConnectionTasksByDevice(Device device);

    /**
     * Finds the default {@link ConnectionTask} for the specified Device.
     * The search will start at the Device but if none is found there,
     * it will continue to the gateway level (if the Device has a gateway of course).
     *
     * @param device The Device for which we need to search the default ConnectionTask
     * @return The default ConnectionTask for the given Device or <code>null</code> if none was found
     */
    public ConnectionTask findDefaultConnectionTaskForDevice(Device device);

    /**
     * Finds all the {@link ConnectionTask}s with the specified {@link TaskStatus}.
     *
     * @param status The TaskStatus
     * @return The ConnectionTasks with the specified TaskStatus
     */
    public List<ConnectionTask> findByStatus(TaskStatus status);

    /**
     * Sets the specified {@link ConnectionTask} as the default for the Device
     * against which the ConnectionTask was created.
     * Note that there can be only 1 default per Device so when there is already
     * another default ConnectionTask marked as the default, the existing
     * ConnectionTask will no longer be the default after this call.
     * This will impact existing {@link ComTaskExecution}s
     * that are linked to the old default as these will now relate to this ConnectionTask.
     *
     * @param connectionTask The ConnectionTask that will become the default
     */
    public void setDefaultConnectionTask(ConnectionTask connectionTask);

    /**
     * Clears the marker flag on the default {@link ConnectionTask} for the specified Device.
     *
     * @param device The Device
     */
    public void clearDefaultConnectionTask (Device device);

    /**
     * Attempts to lock the {@link ConnectionTask} that is about to be executed
     * by the specified {@link ComServer} and returns the locked ConnectionTask
     * when the lock succeeds and <code>null</code> when the lock fails.
     * Note that this MUST run in an existing transactional context.
     *
     * @param connectionTask The ConnectionTask
     * @param comServer The ComServer that is about to execute the ConnectionTask
     * @return <code>true</code> iff the lock succeeds
     */
    public <T extends ConnectionTask> T attemptLockConnectionTask(T connectionTask, ComServer comServer);

    /**
     * Removes the business lock on the specified {@link ConnectionTask},
     * making it available for other {@link ComServer}s to execute the ConnectionTask.
     *
     * @param connectionTask The ConnectionTask
     */
    public void unlockConnectionTask(ConnectionTask connectionTask);

    /**
     * Cleans up any marker flags on {@link ConnectionTask}s that were not properly
     * cleaned because the {@link ComServer} they were running
     * on was actually forcefully shutdown, i.e. not allowing it to
     * shut down running processing and cleanup when done.
     * Leaving the marker flags, prohibits the ComServer from
     * picking up the tasks again.
     * This is intended to be called at startup time.
     *
     * @param comServer The ComServer that is currently starting up.
     */
    public void releaseInterruptedConnectionTasks(ComServer comServer);

    /**
     * Cleans up any marker flags on {@link ConnectionTask}s that are running
     * on {@link com.energyict.mdc.engine.model.OutboundComPort}s of the {@link ComServer}
     * for a period of time that is longer than the task execution timeout specified
     * on the {@link com.energyict.mdc.engine.model.OutboundComPortPool} they are contained in.
     *
     * @param outboundCapableComServer The ComServer
     */
    public void releaseTimedOutConnectionTasks(ComServer outboundCapableComServer);

    public Device newDevice(DeviceConfiguration deviceConfiguration, String name);

    public Device getPrototypeDeviceFor(DeviceConfiguration deviceConfiguration);

    public Device findDeviceById(long id);

    public Device findDeviceByExternalName(String externalName);

    public boolean deviceHasLogBookForLogBookSpec(Device device, LogBookSpec logBookSpec);

    public List<BaseDevice<Channel, LoadProfile, Register>> findPhysicalConnectedDevicesFor(Device device);

    public List<BaseDevice<Channel, LoadProfile, Register>> findCommunicationReferencingDevicesFor(Device device);

    public LoadProfile findLoadProfileById(long id);

    public List<Device> findDevicesBySerialNumber(String serialNumber);

    public List<Device> findAllDevices();

    public List<Device> findDevicesByTimeZone(TimeZone timeZone);

    public InfoType newInfoType(String name);

    public InfoType findInfoType(String name);

    public InfoType findInfoTypeById(long infoTypeId);

    public LogBook findLogBookById(long id);

    public List<LogBook> findLogBooksByDevice(Device device);

}