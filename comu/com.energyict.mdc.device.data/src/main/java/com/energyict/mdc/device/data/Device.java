package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.tasks.AdHocComTaskExecution;
import com.energyict.mdc.device.data.tasks.AdHocComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.AdHocComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.DeviceMultiplier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 19/12/12
 * Time: 10:35
 */
public interface Device extends BaseDevice<Channel, LoadProfile, Register>, HasId {

    void save();

    void delete();

    /**
     * Gets the name of the Device
     *
     * @return the name of the Device
     */
    String getName();

    void setName(String name);

    /**
     * Returns the receiver's DeviceType
     *
     * @return the receiver's DeviceType
     */
    public DeviceType getDeviceType();

    @Override
    public Device getPhysicalGateway();

    public Device getPhysicalGateway(Date timestamp);

    /**
     * Get the Device which is used for <i>communication</i><br>
     * <i>Note that this can be another device than the physical gateway</i>
     *
     * @return the Device which is used to communicate with the HeadEnd
     */
    public Device getCommunicationGateway();

    /**
     * Get the Device which was used for <i>communication</i> on the specified Date<br>
     * <i>Note that this can be another device than the physical gateway</i>
     *
     * @return the Device which is used to communicate with the HeadEnd
     */
    public Device getCommunicationGateway(Date timestamp);

    /**
     * Set the device which will be used to communicate with the HeadEnd.<br>
     *
     * @param device the communication gateway for this device
     */
    void setCommunicationGateway(Device device);

    /**
     * Remove the current link to the communication gateway
     */
    void clearCommunicationGateway();

    @Override
    List<Device> getPhysicalConnectedDevices();

    /**
     * Gets the list of Devices which are referencing this Device for Communication.
     * This means that for each returned Device, the {@link #getCommunicationGateway()}
     * will return this Device for the current timestamp.
     *
     * @return the list of Devices which are currently linked to this Device for communication
     */
    List<Device> getCommunicationReferencingDevices();

    /**
     * Gets the list of Devices which are referencing this Device for Communication.
     * This means that for each returned Device, the {@link #getCommunicationGateway()}
     * will return this Device for the specified timestamp.
     *
     * @param timestamp The timestamp on which the devices were linked for communication to this Device
     * @return the list of Devices which are currently linked to this Device for communication
     */
    List<Device> getCommunicationReferencingDevices(Date timestamp);

    /**
     * Gets the list of Devices which are, in the end, referencing this Device for Communication.
     * The reference can be direct or indirect. When direct, the {@link #getCommunicationGateway()}
     * will return this Device for the current timestamp. When indirect, the communication gateway
     * for the current timestamp will be another Device that directly or indirectly references
     * this Device for communication.
     * In other words, this will return the complete communication topology for the current timestamp
     * starting from this Device.
     *
     * @return the list of Devices which are currently linked to this Device for communication
     */
    List<Device> getAllCommunicationReferencingDevices();

    /**
     * Gets the list of Devices which are, in the end, referencing this Device for Communication.
     * The reference can be direct or indirect. When direct, the {@link #getCommunicationGateway()}
     * will return this Device for the current timestamp. When indirect, the communication gateway
     * for the current timestamp will be another Device that directly or indirectly references
     * this Device for communication.
     * In other words, this will return the complete communication topology for the current timestamp
     * starting from this Device.
     *
     * @param timestamp The timestamp on which the devices were linked for communication to this Device
     * @return the list of Devices which are currently linked to this Device for communication
     */
    List<Device> getAllCommunicationReferencingDevices(Date timestamp);

    /**
     * Gets the {@link CommunicationTopologyEntry CommunicationTopologies} for this Device
     * during the specified Interval, organized (or sorted) along the timeline.
     *
     * @param interval The Interval during which the devices were linked for communication to this Device
     * @return The CommunicationTopologies
     */
    List<CommunicationTopologyEntry> getAllCommunicationTopologies(Interval interval);

    List<DeviceMessage> getMessages();

    /**
     * returns The released pending messages for this device
     *
     * @return a List of all messages of this device
     */
    List<DeviceMessage> getMessagesByState(DeviceMessageStatus status);


    /**
     * Returns the {@link DeviceProtocolPluggableClass} configured for this device
     *
     * @return the used {@link DeviceProtocolPluggableClass}
     */
    public DeviceProtocolPluggableClass getDeviceProtocolPluggableClass();

    /**
     * Returns the device configuration of a device
     *
     * @return a device configuration
     */
    DeviceConfiguration getDeviceConfiguration();

    /**
     * Returns the receiver's collection TimeZone.
     * This is the timeZone in which interval data is stored
     * in the database. All eiserver protocols and import modules
     * convert between device time and the configured collection TimeZone
     *
     * @return the receiver's collection TimeZone
     */
    TimeZone getTimeZone();

    void setTimeZone(TimeZone timeZone);

    void setSerialNumber(String serialNumber);

    void setYearOfCertification(Date yearOfCertification);

    /**
     * Returns the year of certification of a device
     *
     * @return a certification date
     */
    Date getYearOfCertification();

    /**
     * Returns the receiver's last modification date
     *
     * @return the last modification timestamp.
     */
    Date getModDate();

    LogBook.LogBookUpdater getLogBookUpdaterFor(LogBook logBook);

    LoadProfile.LoadProfileUpdater getLoadProfileUpdaterFor(LoadProfile loadProfile);

    TypedProperties getDeviceProtocolProperties();

    void setProperty(String name, Object value);

    void removeProperty(String name);

    List<ProtocolDialectProperties> getProtocolDialectPropertiesList();

    ProtocolDialectProperties getProtocolDialectProperties(String dialectName);

    void setProtocolDialectProperty(String dialectName, String propertyName, Object value);

    void removeProtocolDialectProperty(String dialectName, String propertyName);

    /**
     * Stores the given MeterReadings
     *
     * @param meterReading the meterReadings which will be stored
     */
    void store(MeterReading meterReading);

    public List<LoadProfileReading> getChannelDataFor(LoadProfile loadProfile, Interval interval);

    List<LoadProfileReading> getChannelDataFor(Channel channel, Interval interval);

    /**
     * Gets a list of all device multipliers that were active for a device
     *
     * @return a list of device multipliers
     */
    List<DeviceMultiplier> getDeviceMultipliers();

    /**
     * Gets the active device multiplier for a certain date.
     *
     * @param date
     * @return a device multiplier
     */
    DeviceMultiplier getDeviceMultiplier(Date date);

    /**
     * Gets the active device multiplier at the moment the method is called
     *
     * @return a device multiplier
     */
    DeviceMultiplier getDeviceMultiplier();

    /**
     * return the Unique mRID of the device
     */
    String getmRID();

    /**
     * Returns the channel with the given name or null.
     *
     * @param name the channel name.
     * @return the channel or null.
     */
    BaseChannel getChannel(String name);

    /**
     * Provides a builder that allows the creation of a ScheduledConnectionTask for the Device
     *
     * @param partialOutboundConnectionTask the partialConnectionTask that will model the actual ScheduledConnectionTask
     * @return the builder
     */
    ScheduledConnectionTaskBuilder getScheduledConnectionTaskBuilder(PartialOutboundConnectionTask partialOutboundConnectionTask);

    /**
     * Provides a builder that allows the creation of an InboundConnectionTask for the Device
     *
     * @param partialInboundConnectionTask the partialConnectionTask that will model the actual InboundConnectionTask
     * @return the builder
     */
    InboundConnectionTaskBuilder getInboundConnectionTaskBuilder(PartialInboundConnectionTask partialInboundConnectionTask);

    /**
     * Provides a builder that allows the creation of a ConnectionInitiationTask for the Device
     *
     * @param partialConnectionInitiationTask the partialConnectionTask that will model the actual ConnectionInitiationTask
     * @return the builder
     */
    ConnectionInitiationTaskBuilder getConnectionInitiationTaskBuilder(PartialConnectionInitiationTask partialConnectionInitiationTask);


    List<ConnectionTask<?, ?>> getConnectionTasks();

    List<ConnectionInitiationTask> getConnectionInitiationTasks();

    List<ScheduledConnectionTask> getScheduledConnectionTasks();

    List<InboundConnectionTask> getInboundConnectionTasks();

    void removeConnectionTask(ConnectionTask<?, ?> connectionTask);

    /**
     * Gets the ComTaskExecutions that are configured against this Device.
     *
     * @return The List of ComTaskExecutions
     */
    public List<ComTaskExecution> getComTaskExecutions();

    public ManuallyScheduledComTaskExecutionUpdater getComTaskExecutionUpdater(ManuallyScheduledComTaskExecution comTaskExecution);

    public ScheduledComTaskExecutionUpdater getComTaskExecutionUpdater(ScheduledComTaskExecution comTaskExecution);

    public AdHocComTaskExecutionUpdater getComTaskExecutionUpdater(AdHocComTaskExecution comTaskExecution);

    void removeComTaskExecution(ComTaskExecution comTaskExecution);

    /**
     * Counts the number of EndDeviceEvents of the specified types
     * that have occurred in the specified {@link Interval}
     * within the topology that starts from this Device.
     *
     * @param eventTypes The List of EndDeviceEventType of interest
     * @param interval The Interval during which the EndDeviceEvents have occurred
     * @return The number of EndDeviceEvents
     */
    public int countNumberOfEndDeviceEvents(List<EndDeviceEventType> eventTypes, Interval interval);

    /**
     * Returns a {@link ScheduledComTaskExecutionBuilder} that will build a
     * {@link ScheduledComTaskExecution} for the {@link ComSchedule} on this Device.
     * This will enable all the current and future {@link com.energyict.mdc.tasks.ComTask}s
     * that are contained in the ComSchedule, for execution on this Device.
     * Note that a ComSchedule can only be added once to a Device.
     * Note also that a ComTask that is contained in a ComSchedule
     * cannot be manually scheduled on that Device.
     *
     * @param comSchedule The ComSchedule
     * @see ManuallyScheduledComTaskExecution
     */
    public ScheduledComTaskExecutionBuilder newScheduledComTaskExecution(ComSchedule comSchedule);

    public AdHocComTaskExecutionBuilder newAdHocComTaskExecution(ComTaskEnablement comTaskEnablement, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties);

    public ManuallyScheduledComTaskExecutionBuilder newManuallyScheduledComTaskExecution(ComTaskEnablement comTaskEnablement, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, TemporalExpression temporalExpression);

    List<SecurityProperty> getSecurityProperties(SecurityPropertySet securityPropertySet);

    List<ProtocolDialectConfigurationProperties> getProtocolDialects();

    /**
     * Removes the {@link ComSchedule} from this Device,
     * removing the scheduled execution of the {@link com.energyict.mdc.tasks.ComTask}s
     * that are contained in the ComSchedule, against this Device.
     *
     * @param comSchedule The ComSchedule
     */
    public void removeComSchedule (ComSchedule comSchedule);

    /**
     * Indicates if there are properties for the device and the passed securityPropertySet.
     *
     * @param securityPropertySet The SecurityPropertySet
     * @return A flag that indicates if this Device has properties for the SecurityPropertySet
     */
    boolean hasSecurityProperties(SecurityPropertySet securityPropertySet);

    /**
     * Builder that support basic value setters for a ScheduledConnectionTask
     */
    interface ScheduledConnectionTaskBuilder {

        ScheduledConnectionTaskBuilder setCommunicationWindow(ComWindow communicationWindow);

        ScheduledConnectionTaskBuilder setComPortPool(OutboundComPortPool comPortPool);

        ScheduledConnectionTaskBuilder setConnectionStrategy(ConnectionStrategy connectionStrategy);

        ScheduledConnectionTaskBuilder setInitiatorTask(ConnectionInitiationTask connectionInitiationTask);

        ScheduledConnectionTaskBuilder setNextExecutionSpecsFrom(TemporalExpression temporalExpression);

        ScheduledConnectionTaskBuilder setProperty(String propertyName, Object value);

        ScheduledConnectionTaskBuilder setSimultaneousConnectionsAllowed(boolean allowSimultaneousConnections);

        ScheduledConnectionTaskBuilder setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus status);

        /**
         * Creates the actual ScheduledConnectionTask with the objects set in this builder
         *
         * @return the newly created ScheduledConnectionTask
         */
        ScheduledConnectionTask add();
    }

    /**
     * Builder that supports basic value setters for an InboundConnectionTask
     */
    interface InboundConnectionTaskBuilder {

        InboundConnectionTaskBuilder setComPortPool(InboundComPortPool comPortPool);

        InboundConnectionTaskBuilder setProperty(String propertyName, Object value);

        InboundConnectionTaskBuilder setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus status);

        /**
         * Creates the actual InboundConnectionTask with the objects set in this builder
         *
         * @return the newly created InboundConnectionTask
         */
        InboundConnectionTask add();
    }

    interface ConnectionInitiationTaskBuilder {

        ConnectionInitiationTaskBuilder setComPortPool(OutboundComPortPool comPortPool);

        ConnectionInitiationTaskBuilder setProperty(String propertyName, Object value);

        ConnectionInitiationTaskBuilder setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus status);


        /**
         * Creates the actual ConnectionInitiationTask with the objects set in this builder
         *
         * @return the newly created ConnectionInitiationTask
         */
        ConnectionInitiationTask add();
    }
}