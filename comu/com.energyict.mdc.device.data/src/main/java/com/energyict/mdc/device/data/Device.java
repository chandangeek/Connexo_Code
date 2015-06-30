package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.DeviceMultiplier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.elster.jupiter.time.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.util.HasName;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 19/12/12
 * Time: 10:35
 */
@ProviderType
public interface Device extends BaseDevice<Channel, LoadProfile, Register>, HasId, HasName {

    void save();

    void delete();

    /**
     * Gets the name of the Device.
     *
     * @return the name of the Device
     */
    String getName();

    void setName(String name);

    /**
     * Gets the receiver's DeviceType.
     *
     * @return the receiver's DeviceType
     */
    public DeviceType getDeviceType();

    List<DeviceMessage<Device>> getMessages();

    /**
     * Gets the released pending messages for this device.
     *
     * @return a List of all messages of this device
     */
    List<DeviceMessage<Device>> getMessagesByState(DeviceMessageStatus status);


    /**
     * Gets the {@link DeviceProtocolPluggableClass} configured for this device.
     *
     * @return the used {@link DeviceProtocolPluggableClass}
     */
    public DeviceProtocolPluggableClass getDeviceProtocolPluggableClass();

    /**
     * Gets the device configuration of a device.
     *
     * @return a device configuration
     */
    DeviceConfiguration getDeviceConfiguration();

    /**
     * Gets the receiver's collection TimeZone.
     * This is the timeZone in which interval data is stored
     * in the database. All eiserver protocols and import modules
     * convert between device time and the configured collection TimeZone
     *
     * @return the receiver's collection TimeZone
     */
    TimeZone getTimeZone();

    void setTimeZone(TimeZone timeZone);

    void setSerialNumber(String serialNumber);

    void setYearOfCertification(Integer yearOfCertification);

    /**
     * Gets the year of certification of a device.
     *
     * @return a certification year
     */
    Integer getYearOfCertification();

    /**
     * Gets the receiver's last modification date.
     *
     * @return the last modification timestamp.
     */
    Instant getModificationDate();

    @Override
    List<LogBook> getLogBooks();

    LogBook.LogBookUpdater getLogBookUpdaterFor(LogBook logBook);

    LoadProfile.LoadProfileUpdater getLoadProfileUpdaterFor(LoadProfile loadProfile);

    List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter);

    TypedProperties getDeviceProtocolProperties();

    void setProtocolProperty(String name, Object value);

    void removeProtocolProperty(String name);

    /**
     * Indicates if there are properties for the device and the specified {@link SecurityPropertySet}.
     *
     * @param securityPropertySet The SecurityPropertySet
     * @return A flag that indicates if this Device has properties for the SecurityPropertySet
     */
    boolean hasSecurityProperties(SecurityPropertySet securityPropertySet);

    /**
     * Tests if all the security properties that are define in the configuration level
     * are valid for this specified Device.
     * Security properties for a SecurityPropertySet can be invalid for the following reasons:
     * <ul>
     * <li>No properties have been defined</li>
     * <li>Some or all of the required properties have not been specified yet</li>
     * </ul>
     *
     * @return A flag that indicates if all security properties are valid for this Device
     * @see DeviceConfiguration#getSecurityPropertySets()
     * @see SecurityProperty#isComplete()
     */
    boolean securityPropertiesAreValid();

    List<SecurityProperty> getSecurityProperties(SecurityPropertySet securityPropertySet);

    List<SecurityProperty> getAllSecurityProperties(SecurityPropertySet securityPropertySet);

    void setSecurityProperties(SecurityPropertySet securityPropertySet, TypedProperties properties);

    List<ProtocolDialectProperties> getProtocolDialectPropertiesList();

    Optional<ProtocolDialectProperties> getProtocolDialectProperties(String dialectName);

    void setProtocolDialectProperty(String dialectName, String propertyName, Object value);

    void removeProtocolDialectProperty(String dialectName, String propertyName);

    /**
     * Stores the given MeterReadings.
     *
     * @param meterReading the meterReadings which will be stored
     */
    void store(MeterReading meterReading);

    boolean hasData();

    public MeterActivation activate(Instant start);

    /**
     * Terminates the current MeterActivation on this Device.
     *
     * @param when The instant in time when the MeterActivation will end
     */
    public void deactivate(Instant when);

    /**
     * Terminates the current MeterActivation on this Device right now.
     */
    public void deactivateNow();

    Optional<? extends MeterActivation> getCurrentMeterActivation();

    List<MeterActivation> getMeterActivationsMostRecentFirst();

    /**
     * Gets a list of all device multipliers that were active for a device.
     *
     * @return a list of device multipliers
     */
    List<DeviceMultiplier> getDeviceMultipliers();

    /**
     * Gets the active device multiplier for a certain Timestamp.
     *
     * @param date The timestamp
     * @return a device multiplier
     */
    DeviceMultiplier getDeviceMultiplier(Instant date);

    /**
     * Gets the active device multiplier at the moment the method is called.
     *
     * @return a device multiplier
     */
    DeviceMultiplier getDeviceMultiplier();

    /**
     * Gets the Unique mRID of the device.
     */
    String getmRID();

    /**
     * Provides a builder that allows the creation of a ScheduledConnectionTask for the Device.
     *
     * @param partialOutboundConnectionTask the partialConnectionTask that will model the actual ScheduledConnectionTask
     * @return the builder
     */
    ScheduledConnectionTaskBuilder getScheduledConnectionTaskBuilder(PartialOutboundConnectionTask partialOutboundConnectionTask);

    /**
     * Provides a builder that allows the creation of an InboundConnectionTask for the Device.
     *
     * @param partialInboundConnectionTask the partialConnectionTask that will model the actual InboundConnectionTask
     * @return the builder
     */
    InboundConnectionTaskBuilder getInboundConnectionTaskBuilder(PartialInboundConnectionTask partialInboundConnectionTask);

    /**
     * Provides a builder that allows the creation of a ConnectionInitiationTask for the Device.
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

    public FirmwareComTaskExecutionUpdater getComTaskExecutionUpdater(FirmwareComTaskExecution comTaskExecution);

    void removeComTaskExecution(ComTaskExecution comTaskExecution);

    /**
     * Returns a {@link ComTaskExecutionBuilder} that will build a
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
    public ComTaskExecutionBuilder<ScheduledComTaskExecution> newScheduledComTaskExecution(ComSchedule comSchedule);

    public ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> newManuallyScheduledComTaskExecution(ComTaskEnablement comTaskEnablement, TemporalExpression temporalExpression);

    public ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> newAdHocComTaskExecution(ComTaskEnablement comTaskEnablement);

    public ComTaskExecutionBuilder<FirmwareComTaskExecution> newFirmwareComTaskExecution(ComTaskEnablement comTaskEnablement);

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
     * Adds this device to the mentioned EnumeratedEndDeviceGroup
     * for a specified period in time.
     *
     * @param enumeratedEndDeviceGroup The EnumeratedEndDeviceGroup
     * @param range The period in time during which this device will be part of the group
     */
    public void addToGroup(EnumeratedEndDeviceGroup enumeratedEndDeviceGroup, Range<Instant> range);

    DeviceValidation forValidation();

    DeviceEstimation forEstimation();

    public Optional<UsagePoint> getUsagePoint();

    GatewayType getConfigurationGatewayType();

    DeviceMessageBuilder newDeviceMessage(DeviceMessageId deviceMessageId);

    /**
     * Tests if there are open issues against this Device.
     *
     * @return A flag that indicates if there are open issues against this Device
     */
    public boolean hasOpenIssues();

    /**
     * @return a list of Issues which have the status open
     */
    public List<OpenIssue> getOpenIssues();

    /**
     * Gets the current {@link State} of this Device.
     *
     * @return The current State
     * @since 2.0
     */
    public State getState();

    /**
     * Gets the {@link State} of this Device as it was
     * known at the specified point in time.
     * May return an empty optional when the point in time
     * is before the creation time of this Device.
     *
     * @param instant The point in time
     * @return The State
     * @since 2.0
     */
    public Optional<State> getState(Instant instant);

    public long getVersion();

    public Instant getCreateTime();

    /**
     * Gets the {@link StateTimeline} for this Device.
     *
     * @return The StateTimeline
     * @since 2.0
     */
    public StateTimeline getStateTimeline();

    /**
     * Gets the List of {@link DeviceLifeCycleChangeEvent}s for this Device.
     * Note that the elements of the {@link StateTimeline} are also included
     * and will have type {@link DeviceLifeCycleChangeEvent.Type#STATE}.
     *
     * @return The List of DeviceLifeCycleChangeEvent
     * @since 2.0
     */
    public List<DeviceLifeCycleChangeEvent> getDeviceLifeCycleChangeEvents();

    /**
     * Gets the CIM dates that relate to thie life cycle of this Device.
     *
     * @return The CIMLifecycleDates
     * @since 2.0
     */
    public CIMLifeCycleDates getLifecycleDates();

    /**
     * Builder that support basic value setters for a ScheduledConnectionTask.
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
         * Creates the actual ScheduledConnectionTask with the objects set in this builder.
         *
         * @return the newly created ScheduledConnectionTask
         */
        ScheduledConnectionTask add();
    }

    /**
     * Builder that supports basic value setters for an InboundConnectionTask.
     */
    interface InboundConnectionTaskBuilder {

        InboundConnectionTaskBuilder setComPortPool(InboundComPortPool comPortPool);

        InboundConnectionTaskBuilder setProperty(String propertyName, Object value);

        InboundConnectionTaskBuilder setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus status);

        /**
         * Creates the actual InboundConnectionTask with the objects set in this builder.
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
         * Creates the actual ConnectionInitiationTask with the objects set in this builder.
         *
         * @return the newly created ConnectionInitiationTask
         */
        ConnectionInitiationTask add();
    }

    interface DeviceMessageBuilder {

        /**
         * Add a key/value-Pair which will result in a DeviceMessageAttribute.
         * If you try to add the same key twice, then the first one will be overwritten.
         *
         * @param key   the key of the attribute
         * @param value the value of the attribute
         * @return this builder
         */
        DeviceMessageBuilder addProperty(String key, Object value);

        /**
         * Set the release date of the currently building DeviceMessage.
         *
         * @param releaseDate the date when this message <i>may</i> be executed
         * @return this builder
         */
        DeviceMessageBuilder setReleaseDate(Instant releaseDate);

        /**
         * Set a trackingId for the currently building DeviceMessage.
         * <br/>
         * (a TrackingID should be a business process 'item', most probably it will not be set by the User)
         *
         * @param trackingId the trackingId
         * @return this builder
         */
        DeviceMessageBuilder setTrackingId(String trackingId);

        /**
         * Create the actual DeviceMessage based on the info in the builder.
         *
         * @return the newly created DeviceMessage
         */
        DeviceMessage<Device> add();
    }

}