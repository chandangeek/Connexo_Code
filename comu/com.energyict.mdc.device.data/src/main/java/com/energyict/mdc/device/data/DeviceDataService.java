package com.energyict.mdc.device.data;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.impl.InfoType;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.google.common.base.Optional;
import java.util.Collection;
import java.util.Date;
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
     * Tests if there are {@link Device} that were created
     * from the specified {@link DeviceConfiguration}.
     *
     * @param deviceConfiguration The DeviceConfiguration
     * @return <code>true</code> iff there is at least one Device created from the DeviceConfiguration
     */
    public boolean hasDevices(DeviceConfiguration deviceConfiguration);

    /**
     * Creates a new {@link InboundConnectionTask} with the minimal required properties
     * against the specified Device.
     * Note that the new InboundConnectionTask is not persisted yet as you may want
     * to complete the non required properties before saving it for the first time.
     *
     * @param device The Device
     * @param partialInboundConnectionTask The PartialInboundConnectionTask
     * @param inboundComPortPool The InboundComPortPool
     * @param status
     * @return The new InboundConnectionTask
     */
    public InboundConnectionTask newInboundConnectionTask(Device device, PartialInboundConnectionTask partialInboundConnectionTask, InboundComPortPool inboundComPortPool, ConnectionTask.ConnectionTaskLifecycleStatus status);

    /**
     * Creates a new {@link ScheduledConnectionTask}, that uses the {@link ConnectionStrategy#AS_SOON_AS_POSSIBLE} strategy,
     * with the minimal required properties against the specified Device.
     * Note that the new ScheduledConnectionTask is not persisted yet as you may want
     * to complete the non required properties before saving it for the first time.
     *
     * @param device The Device
     * @param partialConnectionTask The PartialScheduledConnectionTask
     * @param comPortPool The OutboundComPortPool
     * @param status
     * @return The new ScheduledConnectionTask
     */
    public ScheduledConnectionTask newAsapConnectionTask(Device device, PartialScheduledConnectionTask partialConnectionTask, OutboundComPortPool comPortPool, ConnectionTask.ConnectionTaskLifecycleStatus status);

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
     * @param status
     * @return The new ScheduledConnectionTask
     */
    public ScheduledConnectionTask newMinimizeConnectionTask(Device device, PartialScheduledConnectionTask partialConnectionTask, OutboundComPortPool comPortPool, TemporalExpression nextExecutionSpecs, ConnectionTask.ConnectionTaskLifecycleStatus status);

    public ConnectionInitiationTask newConnectionInitiationTask(Device device, PartialConnectionInitiationTask partialConnectionTask, OutboundComPortPool comPortPool, ConnectionTask.ConnectionTaskLifecycleStatus status);

    public Optional<ConnectionTask> findConnectionTask (long id);

    public Optional<OutboundConnectionTask> findOutboundConnectionTask (long id);

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

    void setOrUpdateDefaultConnectionTaskOnComTaskInDeviceTopology(Device device, ConnectionTask connectionTask);

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

    public TimeDuration releaseTimedOutComTasks(ComServer comServer);

    /**
     * Cleans up any marker flags on {@link ComTaskExecution}s that were not properly
     * cleaned because the {@link ComServer} they were running
     * on was actually forcefully shutdown, i.e. not allowing it to
     * shut down running processing and cleanup when done.
     * Leaving the marker flags, prohibits the ComServer from
     * picking up the tasks again.
     * This is intended to be called at startup time.
     *
     * @param comServer The ComServer that is currently starting up.
     */
    public void releaseInterruptedComTasks(ComServer comServer);

    /**
     * Creates a new Device based on the given name and DeviceConfiguration
     *
     * @param deviceConfiguration the deviceConfiguration which models the device
     * @param name                the name which should be used for the device
     * @param mRID
     * @return the newly created Device
     */
    public Device newDevice(DeviceConfiguration deviceConfiguration, String name, String mRID);

    public Device getPrototypeDeviceFor(DeviceConfiguration deviceConfiguration);

    /**
     * Finds the Device based on his unique ID
     *
     * @param id the unique ID of the device
     * @return the requested Device or null if none was found
     */
    public Device findDeviceById(long id);

    /**
     * Finds the Device based on his unique External name
     *
     * @param mrId the unique Identifier of the device
     * @return the requested Device or null if none was found
     */
    public Device findByUniqueMrid(String mrId);

    /**
     * Finds the devices which are physically connected to the given Device
     *
     * @param device the 'master' device
     * @return a list of physically connected devices to the given device
     */
    public List<Device> findPhysicalConnectedDevicesFor(Device device);

    /**
     * Finds the devices which are linked to the given device for communication purposes.
     *
     * @param device the device that arranges the communication
     * @return a list of devices which use the given device for communication purposes
     */
    public List<Device> findCommunicationReferencingDevicesFor(Device device);

    /**
     * Finds the devices which are linked on the specified timestamp
     * to the specified device for communication purposes.
     *
     * @param device the device that arranges the communication
     * @param timestamp The timestamp on which the devices are linked for communication purposes
     * @return a list of devices which use the given device for communication purposes
     */
    public List<Device> findCommunicationReferencingDevicesFor(Device device, Date timestamp);

    /**
     * Finds the devices which are linked on the specified timestamp
     * to the specified device for communication purposes.
     *
     * @param device the device that arranges the communication
     * @param interval The interval during which the devices are linked for communication purposes
     * @return a list of devices which use the given device for communication purposes
     */
    public List<CommunicationTopologyEntry> findCommunicationReferencingDevicesFor(Device device, Interval interval);

    /**
     * Finds the LoadProfile based on the given unique ID
     *
     * @param id the unique ID of the loadProfile
     * @return the requested LoadProfile or null
     */
    public LoadProfile findLoadProfileById(long id);

    /**
     * Finds the Devices (multiple are possible) based on the given serialNumber
     *
     * @param serialNumber the serialNumber of the device
     * @return a list of Devices which have the given serialNumber
     */
    public List<Device> findDevicesBySerialNumber(String serialNumber);

    /**
     * Finds all the devices in the system
     *
     * @return a list of all devices in the system
     */
    public List<Device> findAllDevices();

    /**
     * Finds all the devices in the system with the specific condition
     *
     * @return a list of all devices with the specific condition in the system
     */
    public Finder<Device> findAllDevices(Condition condition);

    /**
     * Finds all the devices which use the given TimeZone
     *
     * @param timeZone the timeZone
     * @return a list of Devices which use the given TimeZone
     */
    public List<Device> findDevicesByTimeZone(TimeZone timeZone);

    /**
     * Creates a new InfoType object based on the given name
     *
     * @param name the name for the InfoType object
     * @return the newly created infoType object
     */
    public InfoType newInfoType(String name);

    /**
     * Finds the infoType which has the given name
     *
     * @param name the name of the InfoType to find
     * @return the requested InfoType or null if none exists with that name
     */
    public InfoType findInfoType(String name);

    /**
     * Finds the infoType with the given unique ID
     *
     * @param infoTypeId the unique ID of the InfoType
     * @return the requested InfoType or null if none exists with that ID
     */
    public InfoType findInfoTypeById(long infoTypeId);

    /**
     * Finds the LogBook with the given unique ID
     *
     * @param id the unique ID of the LogBook
     * @return the requested LogBook or null if none exists with that ID
     */
    public LogBook findLogBookById(long id);

    /**
     * Finds all the LogBooks for the given Device
     *
     * @param device the device
     * @return a list of LogBooks which exist for the given Device
     */
    public List<LogBook> findLogBooksByDevice(Device device);

    public Date getPlannedDate(ComSchedule comSchedule);

    /**
     * Finds the ComTaskExecution with the given ID
     *
     * @param id the unique ID of the ComTaskExecution
     * @return the requested ComTaskExecution
     */
    ComTaskExecution findComTaskExecution(long id);

    /**
     * Finds all ComTaskExecutions for the given Device which aren't made obsolete yet
     *
     * @param device the device
     * @return the currently active ComTaskExecutions for this device
     */
    List<ComTaskExecution> findComTaskExecutionsByDevice(Device device);

    /**
     * Finds all the ComTaskExecutions for the given Device, including the ones that have been made obsolete
     *
     * @param device the device
     * @return all ComTaskExecutions which have ever been created for this Device
     */
    List<ComTaskExecution> findAllComTaskExecutionsIncludingObsoleteForDevice(Device device);

    /**
     * Attempts to lock the ComTaskExecution that is about to be executed
     * by the specified ComPort and returns the locked ComTaskExecution
     * when the lock succeeds and <code>null</code> when the lock fails.
     * Note that this MUST run in an existing transactional context.
     *
     * @param comTaskExecution The ComTaskExecution
     * @param comPort The ComPort that is about to execute the ComTaskExecution
     * @return <code>true</code> iff the lock succeeds
     */
    ComTaskExecution attemptLockComTaskExecution(ComTaskExecution comTaskExecution, ComPort comPort);

    /**
     * Removes the business lock on the specified ComTaskExecution,
     * making it available for other ComPorts to execute the ComTaskExecution.
     *
     * @param comTaskExecution The ComTaskExecution
     */
    void unlockComTaskExecution(ComTaskExecution comTaskExecution);

    /**
     * Finds all the ComTaskExecutions which are linked to the given ConnectionTask
     * (and are not obsolete)
     *
     * @param connectionTask the given ConnectionTask
     * @return all the ComTaskExecutions (which are not obsolete) for the given ConnectionTask
     */
    List<ComTaskExecution> findComTaskExecutionsByConnectionTask(ConnectionTask<?,?> connectionTask);

    /**
     * Finds all the ComTaskExecutions which are linked to the given ComSchedule (MasterSchedule)
     * (and are not obsolete)
     *
     * @param comSchedule the given comSchedule
     * @return all the ComTaskExecutions (which are not obsolete) for the given ConnectionTask
     */
    List<ComTaskExecution> findComTaskExecutionsByComSchedule(ComSchedule comSchedule);
    List<ComTaskExecution> findComTaskExecutionsByComScheduleWithinRange(ComSchedule comSchedule, long minId, long maxId);

    List<ComTaskExecution> findComTasksByDefaultConnectionTask(Device device);

    /**
     * Find all ComTasks that can be added to the ComSchedule, i.e. all ComTasks that have a ComTaskEnablement for all
     * devices linked to the ComSchedule.
     */
    List<ComTask> findAvailableComTasksForComSchedule(ComSchedule comSchedule);

    /**
     * Returns true if the ComSchedule has been linked to a device
     */
    public boolean isLinkedToDevices(ComSchedule comSchedule);

    Fetcher<ComTaskExecution> getPlannedComTaskExecutionsFor(ComPort comPort);

    List<ComTaskExecution> getPlannedComTaskExecutionsFor(InboundComPort comPort, Device device);

    boolean areComTasksStillPending(Collection<Long> comTaskExecutionIds);

}