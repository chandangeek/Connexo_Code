/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.HasmRID;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.device.config.AllowedCalendar;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.GatewayType;
import com.energyict.mdc.common.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.common.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.common.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.protocol.TrackingCategory;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.common.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendarInformation;

import aQute.bnd.annotation.ConsumerType;
import com.energyict.obis.ObisCode;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Consumer;

@ConsumerType
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlAccessorType(XmlAccessType.NONE)
public interface Device extends com.energyict.mdc.upl.meterdata.Device, HasId, HasmRID, HasName {

    Meter getMeter();

    /**
     * Gets the receiver's Channels.
     *
     * @return a <CODE>List</CODE> of <CODE>Channel</CODE> objects in ordinal order
     */
    List<Channel> getChannels();

    /**
     * Gets the device serial number.
     *
     * @return the serial number.
     */
    @XmlAttribute
    String getSerialNumber();

    /**
     * Gets the device protocol properties.
     *
     * @return the list of properties.
     */
    List<DeviceProtocolProperty> getDeviceProperties();

    /**
     * Gets the {@link Register}s defined for this device.
     *
     * @return a List of Register objects
     */
    List<Register> getRegisters();

    /**
     * Gets the {@link Register} with the given obis code which is known by the Device.
     *
     * @param code Obis code to match
     * @return the register or null.
     * @deprecate this method will fetch register on device configuration level rather than overruled OBIS codes on the Device level.
     */
    @Deprecated
    Optional<Register> getRegisterWithDeviceObisCode(ObisCode code);

    /**
     * Gets the {@link LoadProfile}s defined for this device.
     *
     * @return the LoadProfiles
     */
    List<LoadProfile> getLoadProfiles();

    /**
     * Checks if this device is a logical Slave (depends on settings in its device type).
     *
     * @return A flag that indicates if this Device is a logical slave
     */
    boolean isLogicalSlave();

    void save();

    void delete();

    Optional<Location> getLocation();

    void setLocation(Location location);

    Optional<SpatialCoordinates> getSpatialCoordinates();

    void setSpatialCoordinates(SpatialCoordinates geoCoordinates);

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
    DeviceType getDeviceType();

    void setDeviceType(DeviceType ignore);

    List<DeviceMessage> getMessages();

    List<DeviceMessage> getFirmwareMessages();

    /**
     * Gets the released pending messages for this device.
     *
     * @return a List of all messages of this device
     */
    List<DeviceMessage> getMessagesByState(DeviceMessageStatus status);


    /**
     * Gets the {@link DeviceProtocolPluggableClass} configured for this device.
     *
     * @return the used {@link DeviceProtocolPluggableClass}
     */
    Optional<DeviceProtocolPluggableClass> getDeviceProtocolPluggableClass();

    /**
     * Gets the device configuration of a device.
     *
     * @return a device configuration
     */
    DeviceConfiguration getDeviceConfiguration();

    void setDeviceConfiguration(DeviceConfiguration ignore);

    /**
     * Gets the receiver's collection TimeZone.
     * This is the timeZone in which interval data is stored
     * in the database. All eiserver protocols and import modules
     * convert between device time and the configured collection TimeZone
     *
     * @return the receiver's collection TimeZone
     * @deprecated use getZone()
     */
    @Deprecated
    TimeZone getTimeZone();

    @Deprecated
    void setTimeZone(TimeZone timeZone);

    ZoneId getZone();

    void setZone(ZoneId zone);

    void setSerialNumber(String serialNumber);

    void setMultiplier(BigDecimal multiplier, Instant from);

    BigDecimal getMultiplier();

    void setMultiplier(BigDecimal multiplier);

    Optional<BigDecimal> getMultiplierAt(Instant multiplierEffectiveTimeStamp);

    Instant getMultiplierEffectiveTimeStamp();

    /**
     * Gets the year of certification of a device.
     *
     * @return a certification year
     */
    Integer getYearOfCertification();

    void setYearOfCertification(Integer yearOfCertification);

    /**
     * Gets the receiver's last modification date.
     *
     * @return the last modification timestamp.
     */
    Instant getModificationDate();

    List<LogBook> getLogBooks();

    LogBook.LogBookUpdater getLogBookUpdaterFor(LogBook logBook);

    LoadProfile.LoadProfileUpdater getLoadProfileUpdaterFor(LoadProfile loadProfile);

    long getDeviceEventsCountByFilter(EndDeviceEventRecordFilterSpecification filter);

    List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter);

    default List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter, Integer from, Integer to){
        return getDeviceEventsByFilter(filter);
    }

    TypedProperties getDeviceProtocolProperties();

    void setProtocolProperty(String name, Object value);

    void removeProtocolProperty(String name);

    void addZone(String zoneName, String zoneTypeName);
    void removeZonesOnDevice();

    Multimap<String,String> getZones();

    /**
     * Resolve the security accessors and their types to their actual values (if present) and return them as TypedProperties.
     */
    TypedProperties getSecurityProperties(SecurityPropertySet securityPropertySet);

    Optional<SecurityAccessor> getSecurityAccessorByName(String securityAccessorName);

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

    /**
     * Stores the given MeterReadings.
     *
     * @param meterReading the meterReadings which will be stored
     * @param readingDate used to set the reading date in case data is imported from another system. If data is collected, it can be set to null
     */
    void store(MeterReading meterReading, Instant readingDate);

    boolean hasData();

    void touchDevice();

    /**
     * Activates the device on a usage point. Either end the current MeterActivation and create a new one
     * based on previous MeterActivation but with the target Usage Point, or just create a new MeterActivation.
     * If this constitutes no change vs the current activation, the current MeterActivation will be returned.
     * Activation can fail if:
     * <ul>
     * <li>the usagePoint is linked to another device</li>
     * <li>the device doesn't provide all rFired reading types that specified in the metrology configurations of the usagePoint</li>
     *</ul>
     * @param start start of the meterActivation
     * @param usagePoint the Usage Point to be linked to the device
     * @param meterRole
     * @return the new meterActivation
     */
    MeterActivation activate(Instant start, UsagePoint usagePoint, MeterRole meterRole);

    /**
     * Terminates the current MeterActivation on this Device.
     *
     * @param when The instant in time when the MeterActivation will end
     */
    void deactivate(Instant when);

    /**
     * Terminates the current MeterActivation on this Device right now.
     */
    void deactivateNow();

    Optional<? extends MeterActivation> getCurrentMeterActivation();

    Optional<? extends MeterActivation> getMeterActivation(Instant when);

    List<MeterActivation> getMeterActivationsMostRecentFirst();

    /**
     * Provides a list of all meteractivations which were effective for the given range.
     *
     * @param range the range of meteractivations to request
     * @return a (potentially empty) list of effective meterActivations based on the given range
     */
    List<MeterActivation> getMeterActivations(Range<Instant> range);

    /**
     * Gets the Unique mRID of the device.
     */
    @XmlAttribute
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

    void removePermanentlyConnectionTask(ConnectionTask<?, ?> connectionTask);
    /**
     * Gets the ComTaskExecutions that are configured against this Device.
     *
     * @return The List of ComTaskExecutions
     */
    List<ComTaskExecution> getComTaskExecutions();

    ComTaskExecutionUpdater getComTaskExecutionUpdater(ComTaskExecution comTaskExecution);

    void removeComTaskExecution(ComTaskExecution comTaskExecution);

    /**
     * Returns a {@link ComTaskExecutionBuilder} that will build a
     * ComTaskExecution for a {@link ComSchedule} on this Device.
     * This will enable all the current and future {@link ComTask}s
     * that are contained in the ComSchedule, for execution on this Device.
     * Note that a ComSchedule can only be added once to a Device.
     * Note also that a ComTask that is contained in a ComSchedule
     * cannot be manually scheduled on that Device.
     *
     * @param comSchedule The ComSchedule
     */
    ComTaskExecutionBuilder newScheduledComTaskExecution(ComSchedule comSchedule);

    ComTaskExecutionBuilder newManuallyScheduledComTaskExecution(ComTaskEnablement comTaskEnablement, TemporalExpression temporalExpression);

    ComTaskExecutionBuilder newAdHocComTaskExecution(ComTaskEnablement comTaskEnablement);

    ComTaskExecutionBuilder newFirmwareComTaskExecution(ComTaskEnablement comTaskEnablement);

    List<ProtocolDialectConfigurationProperties> getProtocolDialects();

    /**
     * Removes the {@link ComSchedule} from this Device,
     * removing the scheduled execution of the {@link ComTask}s
     * that are contained in the ComSchedule, against this Device.
     *
     * @param comSchedule The ComSchedule
     */
    void removeComSchedule(ComSchedule comSchedule);

    /**
     * Adds this device to the mentioned EnumeratedEndDeviceGroup
     * for a specified period in time.
     *
     * @param enumeratedEndDeviceGroup The EnumeratedEndDeviceGroup
     * @param range The period in time during which this device will be part of the group
     */
    void addToGroup(EnumeratedEndDeviceGroup enumeratedEndDeviceGroup, Range<Instant> range);

    DeviceValidation forValidation();

    DeviceEstimation forEstimation();

    Optional<UsagePoint> getUsagePoint();

    GatewayType getConfigurationGatewayType();

    /**
     * Build a new device message with the default tracking category 'manual'
     *
     * @param deviceMessageId message id to create a device message with
     * @return device message builder
     */
    DeviceMessageBuilder newDeviceMessage(DeviceMessageId deviceMessageId);

    DeviceMessageBuilder newDeviceMessage(DeviceMessageId deviceMessageId, TrackingCategory trackingCategory);

    /**
     * Tests if there are open issues against this Device.
     *
     * @return A flag that indicates if there are open issues against this Device
     */
    boolean hasOpenIssues();

    /**
     * @return a list of Issues which have the status open
     */
    List<OpenIssue> getOpenIssues();

    /**
     * Gets the current {@link State} of this Device.
     *
     * @return The current State
     * @since 2.0
     */
    State getState();

    /**
     * Gets the current {@link Stage} of the State of this Device.
     *
     * @return The current Stage
     */
    Stage getStage();

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
    Optional<State> getState(Instant instant);

    long getVersion();

    Instant getCreateTime();

    /**
     * Gets the {@link StateTimeline} for this Device.
     *
     * @return The StateTimeline
     * @since 2.0
     */
    StateTimeline getStateTimeline();

    /**
     * Gets the List of {@link DeviceLifeCycleChangeEvent}s for this Device.
     * Note that the elements of the {@link StateTimeline} are also included
     * and will have type {@link DeviceLifeCycleChangeEvent.Type#STATE}.
     *
     * @return The List of DeviceLifeCycleChangeEvent
     * @since 2.0
     */
    List<DeviceLifeCycleChangeEvent> getDeviceLifeCycleChangeEvents();

    /**
     * Gets the CIM dates that relate to the life cycle of this Device.
     *
     * @return The CIMLifecycleDates
     * @since 2.0
     */
    CIMLifecycleDates getLifecycleDates();

    CalendarSupport calendars();

    Optional<ReadingTypeObisCodeUsage> getReadingTypeObisCodeUsage(ReadingType readingType);

    Channel.ChannelUpdater getChannelUpdaterFor(Channel channel);

    Register.RegisterUpdater getRegisterUpdaterFor(Register register);

    void runStatusInformationTask(Consumer<ComTaskExecution> requestedAction);

    Optional<Device> getHistory(Instant when);

    void addInBatch(Batch batch);

    void removeFromBatch(Batch batch);

    Optional<Batch> getBatch();

    void setConnectionTaskForComTaskExecutions(ConnectionTask connectionTask);

    String getManufacturer();
    void setManufacturer(String manufacturer);

    String getModelNumber();
    void setModelNumber(String modelNumber);

    String getModelVersion();
    void setModelVersion(String modelVersion);

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public String getXmlType();

    public void setXmlType(String ignore);

    /**
     * Returns all SecurityAccessors defined for this device. The returned list will contain accessors of all kinds: certfificates, keys and passphrases
     * @return all SecurityAccessors defined for this device.
     */
    List<SecurityAccessor> getSecurityAccessors();

    /**
     * Get the SecurityAccessor (aka 'value') for a SecurityAccessorType, if any
     * @param securityAccessorType The {@link SecurityAccessorType} whose value will be retrieved.
     * @return The actual value (SecurityAccessor), or Optional.empty() if no value could be found
     */
    Optional<SecurityAccessor> getSecurityAccessor(SecurityAccessorType securityAccessorType);

    /**
     * Creates a new SecurityAccessor for a certain SecurityAccessorType. The SecurityAccessor is merely an empty placeholder at this point.
     * Users can set the value by calling the setter.
     * @param securityAccessorType The type of security accessor to be created
     * @return An empty security accessor.
     */
    SecurityAccessor newSecurityAccessor(SecurityAccessorType securityAccessorType);

    void removeSecurityAccessor(SecurityAccessor securityAccessor);

    /**
     * Builder that support basic value setters for a ScheduledConnectionTask.
     */
    @ConsumerType
    interface ScheduledConnectionTaskBuilder {

        ScheduledConnectionTaskBuilder setCommunicationWindow(ComWindow communicationWindow);

        ScheduledConnectionTaskBuilder setComPortPool(OutboundComPortPool comPortPool);

        ScheduledConnectionTaskBuilder setConnectionStrategy(ConnectionStrategy connectionStrategy);

        ScheduledConnectionTaskBuilder setInitiatorTask(ConnectionInitiationTask connectionInitiationTask);

        ScheduledConnectionTaskBuilder setNextExecutionSpecsFrom(TemporalExpression temporalExpression);

        ScheduledConnectionTaskBuilder setProperty(String propertyName, Object value);

        ScheduledConnectionTaskBuilder setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections);

        ScheduledConnectionTaskBuilder setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus status);

        ScheduledConnectionTaskBuilder setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties properties);

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
    @ConsumerType
    interface InboundConnectionTaskBuilder {

        InboundConnectionTaskBuilder setComPortPool(InboundComPortPool comPortPool);

        InboundConnectionTaskBuilder setProperty(String propertyName, Object value);

        InboundConnectionTaskBuilder setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus status);

        InboundConnectionTaskBuilder setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties properties);

        /**
         * Creates the actual InboundConnectionTask with the objects set in this builder.
         *
         * @return the newly created InboundConnectionTask
         */
        InboundConnectionTask add();
    }

    @ConsumerType
    interface ConnectionInitiationTaskBuilder {

        ConnectionInitiationTaskBuilder setComPortPool(OutboundComPortPool comPortPool);

        ConnectionInitiationTaskBuilder setProperty(String propertyName, Object value);

        ConnectionInitiationTaskBuilder setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus status);

        ConnectionInitiationTaskBuilder setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties properties);

        /**
         * Creates the actual ConnectionInitiationTask with the objects set in this builder.
         *
         * @return the newly created ConnectionInitiationTask
         */
        ConnectionInitiationTask add();
    }

    @ConsumerType
    interface DeviceMessageBuilder {

        /**
         * Add a key/value-Pair which will result in a DeviceMessageAttribute.
         * If you try to add the same key twice, then the first one will be overwritten.
         *
         * @param key the key of the attribute
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
         * Set a tracking id information for the currently building DeviceMessage
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
        DeviceMessage add();
    }

    @ConsumerType
    interface CalendarSupport {
        Optional<ActiveEffectiveCalendar> getActive();

        /**
         * Gets the {@link PassiveCalendar} as reported by the actual device
         * while status information was obtained.
         *
         * @return The actual passive calendar as reported by the actual device
         */
        Optional<PassiveCalendar> getPassive();

        /**
         * Gets the {@link PassiveCalendar} that is planned to be sent
         * to the actual device. Note that this PassiveEffectiveCalendar
         * will be linked to a DeviceMessage.
         *
         * @return The actual passive calendar that is planned to be sent to the actual device
         */
        Optional<PassiveCalendar> getPlannedPassive();

        void updateCalendars(CollectedCalendarInformation collectedData);

        void setPassive(AllowedCalendar passiveCalendar, Instant activationDate, DeviceMessage deviceMessage);
    }

}
