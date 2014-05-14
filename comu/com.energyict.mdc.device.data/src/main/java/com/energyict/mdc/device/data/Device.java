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
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.DeviceMultiplier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.scheduling.TemporalExpression;

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

    /**
     * Get the Device which is used for <i>communication</i><br>
     * <i>Note that this can be another device than the physical gateway</i>
     *
     * @return the Device which is used to communicate with the HeadEnd
     */
    public Device getCommunicationGateway();

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

    /**
     * Gets the list of Devices which are reference to this Device for Communication.
     * This means that for each returned Device, the {@link #getCommunicationGateway()}
     * will return this Device for the current timestamp.
     *
     * @return the list of Devices which are currently linked to this Device for communication
     */
    List<BaseDevice<Channel, LoadProfile, Register>> getCommunicationReferencingDevices();

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

    void removeConnectionTask(ConnectionTask<?, ?> connectionTask);

    /**
     * Gets the ComTaskExecutions that are configured against this Device.
     *
     * @return The List of ComTaskExecutions
     */
    public List<ComTaskExecution> getComTaskExecutions();

    ComTaskExecution.ComTaskExecutionBuilder getComTaskExecutionBuilder(ComTaskEnablement comTaskEnablement);

    ComTaskExecution.ComTaskExecutionUpdater getComTaskExecutionUpdater(ComTaskExecution comTaskExecution);

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

        /**
         * Creates the actual ConnectionInitiationTask with the objects set in this builder
         *
         * @return the newly created ConnectionInitiationTask
         */
        ConnectionInitiationTask add();
    }
}