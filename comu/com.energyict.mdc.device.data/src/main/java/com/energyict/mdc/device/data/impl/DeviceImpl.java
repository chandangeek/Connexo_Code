package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.*;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.data.exceptions.CannotDeleteComScheduleFromDevice;
import com.energyict.mdc.device.data.exceptions.CannotDeleteComTaskExecutionWhichIsNotFromThisDevice;
import com.energyict.mdc.device.data.exceptions.CannotDeleteConnectionTaskWhichIsNotFromThisDevice;
import com.energyict.mdc.device.data.exceptions.DeviceProtocolPropertyException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.ProtocolDialectConfigurationPropertiesIsRequiredException;
import com.energyict.mdc.device.data.exceptions.StillGatewayException;
import com.energyict.mdc.device.data.impl.constraintvalidators.DeviceConfigurationIsPresentAndActive;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueComTaskScheduling;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueMrid;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionInitiationTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ManuallyScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.dynamic.relation.CanLock;
import com.energyict.mdc.dynamic.relation.RelationTransaction;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.DeviceMultiplier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.TemporalReference;
import com.elster.jupiter.orm.associations.Temporals;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.Lists;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.google.common.collect.Range;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.energyict.mdc.protocol.pluggable.SecurityPropertySetRelationAttributeTypeNames.DEVICE_ATTRIBUTE_NAME;
import static com.energyict.mdc.protocol.pluggable.SecurityPropertySetRelationAttributeTypeNames.SECURITY_PROPERTY_SET_ATTRIBUTE_NAME;
import static com.energyict.mdc.protocol.pluggable.SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME;
import static java.util.stream.Collectors.toList;

@UniqueMrid(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_DEVICE_MRID + "}")
@UniqueComTaskScheduling(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_COMTASK_SCHEDULING + "}")
public class DeviceImpl implements Device, CanLock {

    private final DataModel dataModel;
    private final EventService eventService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final MeteringService meteringService;
    private final ValidationService validationService;
    private final ServerConnectionTaskService connectionTaskService;
    private final ServerCommunicationTaskService communicationTaskService;
    private final ServerDeviceService deviceService;
    private final SecurityPropertyService securityPropertyService;
    private final ProtocolPluggableService protocolPluggableService;

    private final List<LoadProfile> loadProfiles = new ArrayList<>();
    private final List<LogBook> logBooks = new ArrayList<>();
    private long id;

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_TYPE_REQUIRED_KEY + "}")
    private final Reference<DeviceType> deviceType = ValueReference.absent();
    @DeviceConfigurationIsPresentAndActive(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_CONFIGURATION_REQUIRED_KEY + "}")
    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();

    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED_KEY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MRID_REQUIRED_KEY + "}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MRID_REQUIRED_KEY + "}")
    private String mRID;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String serialNumber;
    private String timeZoneId;
    private TimeZone timeZone;
    private Instant modificationDate;
    private Instant yearOfCertification;

    @Valid
    private TemporalReference<CommunicationGatewayReference> communicationGatewayReferenceDevice = Temporals.absent();
    @Valid
    private TemporalReference<PhysicalGatewayReference> physicalGatewayReferenceDevice = Temporals.absent();
    @Valid
    private List<DeviceProtocolProperty> deviceProperties = new ArrayList<>();
    @Valid
    private List<ConnectionTaskImpl<?, ?>> connectionTasks;
    @Valid
    private List<ComTaskExecutionImpl> comTaskExecutions;
    @Valid
    private List<DeviceMessageImpl> deviceMessages = new ArrayList<>();

    private List<ProtocolDialectProperties> dialectPropertiesList = new ArrayList<>();
    private List<ProtocolDialectProperties> newDialectProperties = new ArrayList<>();
    private List<ProtocolDialectProperties> dirtyDialectProperties = new ArrayList<>();

    private final Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider;
    private final Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider;
    private final Provider<ConnectionInitiationTaskImpl> connectionInitiationTaskProvider;
    private final Provider<ScheduledComTaskExecutionImpl> scheduledComTaskExecutionProvider;
    private final Provider<ManuallyScheduledComTaskExecutionImpl> manuallyScheduledComTaskExecutionProvider;
    private transient DeviceValidationImpl deviceValidation;

    @Inject
    public DeviceImpl(
            DataModel dataModel,
            EventService eventService,
            Thesaurus thesaurus,
            Clock clock,
            MeteringService meteringService,
            ValidationService validationService,
            ServerConnectionTaskService connectionTaskService,
            ServerCommunicationTaskService communicationTaskService,
            ServerDeviceService deviceService,
            SecurityPropertyService securityPropertyService,
            Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider,
            Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider,
            Provider<ConnectionInitiationTaskImpl> connectionInitiationTaskProvider,
            Provider<ScheduledComTaskExecutionImpl> scheduledComTaskExecutionProvider,
            ProtocolPluggableService protocolPluggableService,
            Provider<ManuallyScheduledComTaskExecutionImpl> manuallyScheduledComTaskExecutionProvider) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.meteringService = meteringService;
        this.validationService = validationService;
        this.connectionTaskService = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
        this.deviceService = deviceService;
        this.securityPropertyService = securityPropertyService;
        this.scheduledConnectionTaskProvider = scheduledConnectionTaskProvider;
        this.inboundConnectionTaskProvider = inboundConnectionTaskProvider;
        this.connectionInitiationTaskProvider = connectionInitiationTaskProvider;
        this.scheduledComTaskExecutionProvider = scheduledComTaskExecutionProvider;
        this.manuallyScheduledComTaskExecutionProvider = manuallyScheduledComTaskExecutionProvider;
        this.protocolPluggableService = protocolPluggableService;
    }

    DeviceImpl initialize(DeviceConfiguration deviceConfiguration, String name, String mRID) {
        this.deviceConfiguration.set(deviceConfiguration);
        this.setDeviceTypeFromDeviceConfiguration();
        setName(name);
        this.setMRID(mRID);
        createLoadProfiles();
        createLogBooks();
        return this;
    }

    ValidationService getValidationService() {
        return validationService;
    }

    private void setDeviceTypeFromDeviceConfiguration() {
        if (this.deviceConfiguration.isPresent()) {
            this.deviceType.set(this.deviceConfiguration.get().getDeviceType());
        }
    }

    private void createLoadProfiles() {
        if (this.getDeviceConfiguration() != null) {
            this.loadProfiles.addAll(
                this.getDeviceConfiguration()
                    .getLoadProfileSpecs()
                    .stream()
                    .map(loadProfileSpec -> this.dataModel.getInstance(LoadProfileImpl.class).initialize(loadProfileSpec, this))
                    .collect(Collectors.toList()));
        }
    }

    private void createLogBooks() {
        if (this.getDeviceConfiguration() != null) {
            this.logBooks.addAll(
                this.getDeviceConfiguration()
                    .getLogBookSpecs()
                    .stream()
                    .map(logBookSpec -> this.dataModel.getInstance(LogBookImpl.class).initialize(logBookSpec, this))
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public void save() {
        this.modificationDate = this.clock.instant();
        if (this.id > 0) {
            Save.UPDATE.save(dataModel, this);
            this.saveNewAndDirtyDialectProperties();
            this.notifyUpdated();
        } else {
            Save.CREATE.save(dataModel, this);
            this.createKoreMeter();
            this.saveNewDialectProperties();
            this.notifyCreated();
        }
        this.saveAllConnectionTasks();
        this.saveAllComTaskExecutions();
    }

    private void saveNewAndDirtyDialectProperties() {
        this.saveNewDialectProperties();
        this.saveDirtyDialectProperties();
    }

    private void saveNewDialectProperties() {
        this.saveDialectProperties(this.newDialectProperties);
        this.dialectPropertiesList.addAll(this.newDialectProperties);
        this.newDialectProperties = new ArrayList<>();
    }

    private void saveDirtyDialectProperties() {
        this.saveDialectProperties(this.dirtyDialectProperties);
        this.dirtyDialectProperties = new ArrayList<>();
    }

    private void saveDialectProperties(List<ProtocolDialectProperties> dialectProperties) {
        for (ProtocolDialectProperties newDialectProperty : dialectProperties) {
            this.save((ProtocolDialectPropertiesImpl) newDialectProperty);
        }
    }

    private void save(ProtocolDialectPropertiesImpl dialectProperties) {
        dialectProperties.save();
    }

    private void saveAllConnectionTasks() {
        if (this.connectionTasks != null) {
            // No need to call the getConnectionTaskImpls getter because if they have not been loaded before, they cannot be dirty
            for (ConnectionTaskImpl<?, ?> connectionTask : connectionTasks) {
                connectionTask.save();
            }
        }
    }

    private void saveAllComTaskExecutions() {
        if (this.comTaskExecutions != null) {
            // No need to call the getComTaskExecutionImpls getter because if they have not been loaded before, they cannot be dirty
            this.comTaskExecutions
                    .stream()
                    .forEach(ComTaskExecutionImpl::save);
        }
    }

    private void notifyUpdated() {
        this.eventService.postEvent(UpdateEventType.DEVICE.topic(), this);
    }

    private void notifyCreated() {
        this.eventService.postEvent(CreateEventType.DEVICE.topic(), this);
    }

    private void notifyDeleted() {
        this.eventService.postEvent(DeleteEventType.DEVICE.topic(), this);
    }

    @Override
    public void delete() {
        this.validateDelete();
        this.notifyDeviceIsGoingToBeDeleted();
        this.doDelete();
        this.notifyDeleted();
    }

    private void notifyDeviceIsGoingToBeDeleted() {
        this.eventService.postEvent(EventType.DEVICE_BEFORE_DELETE.topic(), this);
    }

    private void doDelete() {
        deleteProperties();
        deleteLoadProfiles();
        deleteLogBooks();
        deleteComTaskExecutions();
        deleteConnectionTasks();
        // TODO delete messages
        // TODO delete security properties
        this.deleteKoreMeterIfExists();
        this.getDataMapper().remove(this);
    }

    private void deleteComTaskExecutions() {
        for (ComTaskExecution comTaskExecution : this.communicationTaskService.findAllComTaskExecutionsIncludingObsoleteForDevice(this)) {
            ((ComTaskExecutionImpl) comTaskExecution).delete();
        }
    }

    private void deleteConnectionTasks() {
        for (ConnectionTaskImpl<?, ?> connectionTask : this.getConnectionTaskImpls()) {
            connectionTask.delete();
        }
    }

    private void deleteLogBooks() {
        this.logBooks.clear();
    }

    private void deleteLoadProfiles() {
        this.loadProfiles.clear();
    }

    private void deleteProperties() {
        this.deviceProperties.clear();
    }

    private void validateDelete() {
        validateGatewayUsage();
    }

    private void validateGatewayUsage() {
        List<Device> physicalConnectedDevices = getPhysicalConnectedDevices();
        if (!physicalConnectedDevices.isEmpty()) {
            throw StillGatewayException.forPhysicalGateway(thesaurus, this, physicalConnectedDevices.toArray(new Device[physicalConnectedDevices.size()]));
        }
        List<Device> communicationReferencingDevices = getCommunicationReferencingDevices();
        if (!communicationReferencingDevices.isEmpty()) {
            throw StillGatewayException.forCommunicationGateway(thesaurus, this, communicationReferencingDevices.toArray(new Device[communicationReferencingDevices.size()]));
        }
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration.orNull();
    }

    @Override
    public DeviceType getDeviceType() {
        return this.getDeviceConfiguration().getDeviceType();
    }

    @Override
    public void setName(String name) {
        this.name = null;
        if (name != null) {
            this.name = name.trim();
        }
    }

    private void setMRID(String mRID) {
        this.mRID = null;
        if (mRID != null) {
            this.mRID = mRID.trim();
        }
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TimeZone getTimeZone() {
        if (this.timeZone == null) {
            if (!Checks.is(timeZoneId).empty() && Arrays.asList(TimeZone.getAvailableIDs()).contains(this.timeZoneId)) {
                this.timeZone = TimeZone.getTimeZone(timeZoneId);
            } else {
                return getSystemTimeZone();
            }
        }
        return this.timeZone;
    }

    private TimeZone getSystemTimeZone() {
        return TimeZone.getDefault();
    }

    @Override
    public void setTimeZone(TimeZone timeZone) {
        if (timeZone != null) {
            this.timeZoneId = timeZone.getID();
        } else {
            this.timeZoneId = "";
        }
        this.timeZone = timeZone;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public void setYearOfCertification(Instant yearOfCertification) {
        this.yearOfCertification = yearOfCertification;
    }

    @Override
    public Instant getYearOfCertification() {
        return this.yearOfCertification;
    }

    public Instant getModDate() {
        return this.modificationDate;
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> channels = new ArrayList<>();
        for (LoadProfile loadProfile : loadProfiles) {
            channels.addAll(loadProfile.getChannels());
        }
        return channels;
    }

    @Override
    public BaseChannel getChannel(String name) {
        for (Channel channel : getChannels()) {
            if (channel.getChannelSpec().getName().equals(name)) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public List<Register> getRegisters() {
        List<Register> registers = new ArrayList<>();
        for (RegisterSpec registerSpec : getDeviceConfiguration().getRegisterSpecs()) {
            registers.add(this.newRegisterFor(registerSpec));
        }
        return registers;
    }

    @Override
    public Register getRegisterWithDeviceObisCode(ObisCode code) {
        for (RegisterSpec registerSpec : getDeviceConfiguration().getRegisterSpecs()) {
            if (registerSpec.getDeviceObisCode().equals(code)) {
                return this.newRegisterFor(registerSpec);
            }
        }
        return null;
    }

    private RegisterImpl newRegisterFor(RegisterSpec registerSpec) {
        for (RegisterFactory factory : RegisterFactory.values()) {
            if (factory.appliesTo(registerSpec)) {
                return factory.newRegister(this, registerSpec);
            }
        }
        return RegisterFactory.Numerical.newRegister(this, registerSpec);
    }

    @Override
    public List<Device> getPhysicalConnectedDevices() {
        return this.deviceService.findPhysicalConnectedDevicesFor(this);
    }

    @Override
    public Device getPhysicalGateway() {
        return this.getPhysicalGateway(clock.instant());
    }

    @Override
    public Device getPhysicalGateway(Instant timestamp) {
        return this.physicalGatewayReferenceDevice.effective(timestamp)
                .map(PhysicalGatewayReference::getGateway)
                .orElse(null);
    }

    private void topologyChanged() {
        List<ComTaskExecution> comTasksForDefaultConnectionTask = this.communicationTaskService.findComTasksByDefaultConnectionTask(this);
        Device gateway = this.getPhysicalGateway();
        if (gateway != null) {
            updateComTasksToUseNewDefaultConnectionTask(comTasksForDefaultConnectionTask);
        } else {
            updateComTasksToUseNonExistingDefaultConnectionTask(comTasksForDefaultConnectionTask);
        }

    }

    private void updateComTasksToUseNonExistingDefaultConnectionTask(List<ComTaskExecution> comTasksForDefaultConnectionTask) {
        for (ComTaskExecution comTaskExecution : comTasksForDefaultConnectionTask) {
            ComTaskExecutionUpdater<? extends ComTaskExecutionUpdater<?, ?>, ? extends ComTaskExecution> comTaskExecutionUpdater = comTaskExecution.getUpdater();
            comTaskExecutionUpdater.connectionTask(null);
            comTaskExecutionUpdater.useDefaultConnectionTask(true);
            comTaskExecutionUpdater.update();
        }
    }

    private void updateComTasksToUseNewDefaultConnectionTask(List<ComTaskExecution> comTasksForDefaultConnectionTask) {
        ConnectionTask<?, ?> defaultConnectionTaskForGateway = getDefaultConnectionTask();
        for (ComTaskExecution comTaskExecution : comTasksForDefaultConnectionTask) {
            ComTaskExecutionUpdater<? extends ComTaskExecutionUpdater<?, ?>, ? extends ComTaskExecution> comTaskExecutionUpdater = comTaskExecution.getUpdater();
            comTaskExecutionUpdater.useDefaultConnectionTask(defaultConnectionTaskForGateway);
            comTaskExecutionUpdater.update();
        }
    }

    private ConnectionTask<?, ?> getDefaultConnectionTask() {
        for (ConnectionTaskImpl<?, ?> connectionTask : this.getConnectionTaskImpls()) {
            if (connectionTask.isDefault()) {
                return connectionTask;
            }
        }
        return null;
    }

    @Override
    public void setPhysicalGateway(BaseDevice gateway) {
        if (gateway != null) {
            Instant currentTime = clock.instant();
            terminateTemporal(currentTime, this.physicalGatewayReferenceDevice);
            PhysicalGatewayReferenceImpl physicalGatewayReference =
                    this.dataModel.getInstance(PhysicalGatewayReferenceImpl.class)
                            .createFor(Interval.startAt(currentTime), (Device) gateway, this);
            savePhysicalGateway(physicalGatewayReference);
            topologyChanged();
        }
    }

    private void savePhysicalGateway(PhysicalGatewayReferenceImpl physicalGatewayReference) {
        Save.action(getId()).validate(this.dataModel, physicalGatewayReference);
        this.physicalGatewayReferenceDevice.add(physicalGatewayReference);
    }

    @Override
    public void clearPhysicalGateway() {
        terminateTemporal(clock.instant(), this.physicalGatewayReferenceDevice);
        topologyChanged();
    }

    private void terminateTemporal(Instant currentTime, TemporalReference<? extends GatewayReference> temporalReference) {
        Optional<? extends GatewayReference> currentGateway = temporalReference.effective(currentTime);
        if (currentGateway.isPresent()) {
            GatewayReference gateway = currentGateway.get();
            gateway.terminate(currentTime);
            this.dataModel.update(gateway);
        }
    }

    @Override
    public void setCommunicationGateway(Device gateway) {
        if (gateway != null) {
            Instant currentTime = clock.instant();
            terminateTemporal(currentTime, this.communicationGatewayReferenceDevice);
            CommunicationGatewayReferenceImpl communicationGatewayReference =
                    this.dataModel.getInstance(CommunicationGatewayReferenceImpl.class)
                            .createFor(Interval.startAt(currentTime), gateway, this);
            saveCommunicationGateway(communicationGatewayReference);
            topologyChanged();
        }
    }

    private void saveCommunicationGateway(CommunicationGatewayReferenceImpl communicationGatewayReference) {
        Save.action(getId()).validate(this.dataModel, communicationGatewayReference);
        this.communicationGatewayReferenceDevice.add(communicationGatewayReference);
    }

    @Override
    public void clearCommunicationGateway() {
        terminateTemporal(clock.instant(), this.communicationGatewayReferenceDevice);
        topologyChanged();
    }

    @Override
    public List<Device> getCommunicationReferencingDevices() {
        return this.deviceService.findCommunicationReferencingDevicesFor(this);
    }

    @Override
    public List<Device> getCommunicationReferencingDevices(Instant timestamp) {
        return this.deviceService.findCommunicationReferencingDevicesFor(this, timestamp);
    }

    @Override
    public List<Device> getAllCommunicationReferencingDevices() {
        return this.getAllCommunicationReferencingDevices(clock.instant());
    }

    @Override
    public List<Device> getAllCommunicationReferencingDevices(Instant timestamp) {
        Map<Long, Device> allDevicesInTopology = new HashMap<>();
        this.collectAllCommunicationReferencingDevices(timestamp, this, allDevicesInTopology);
        return new ArrayList<>(allDevicesInTopology.values());
    }

    private void collectAllCommunicationReferencingDevices(Instant timestamp, Device topologyRoot, Map<Long, Device> devices) {
        topologyRoot.getCommunicationReferencingDevices(timestamp)
                .stream()
                // Filter the devices that were not encountered yet
                .filter(device -> !devices.containsKey(device.getId()))
                // and collect the referencing devices for those
                .forEach(device -> {
                    devices.put(device.getId(), device);
                    this.collectAllCommunicationReferencingDevices(timestamp, device, devices);
        });
    }

    @Override
    public DeviceTopology getCommunicationTopology(Range<Instant> period) {
        return this.deviceService.buildCommunicationTopology(this, period);
    }

    @Override
    public Device getCommunicationGateway() {
        return this.getCommunicationGateway(clock.instant());
    }

    @Override
    public Device getCommunicationGateway(Instant timestamp) {
        Optional<CommunicationGatewayReference> communicationGatewayReferenceOptional = this.communicationGatewayReferenceDevice.effective(timestamp);
        if (communicationGatewayReferenceOptional.isPresent()) {
            return communicationGatewayReferenceOptional.get().getGateway();
        }
        return null;
    }

    @Override
    public boolean isLogicalSlave() {
        return getDeviceType().isLogicalSlave();
    }

    @Override
    public List<DeviceMessage<Device>> getMessages() {
        return Collections.unmodifiableList(this.deviceMessages);
    }

    @Override
    public List<DeviceMessage<Device>> getMessagesByState(DeviceMessageStatus status) {
        return this.deviceMessages.stream().filter(deviceMessage -> deviceMessage.getStatus().equals(status)).collect(toList());
    }

    @Override
    public DeviceProtocolPluggableClass getDeviceProtocolPluggableClass() {
        return getDeviceType().getDeviceProtocolPluggableClass();
    }

    @Override
    public List<LogBook> getLogBooks() {
        return Collections.unmodifiableList(this.logBooks);
    }

    @Override
    public LogBook.LogBookUpdater getLogBookUpdaterFor(LogBook logBook) {
        return new LogBookUpdaterForDevice((LogBookImpl) logBook);
    }

    @Override
    public void lock() {
        try {
            try (PreparedStatement stmnt = getLockSqlBuilder().getStatement(dataModel.getConnection(true))) {
                try (ResultSet rs = stmnt.executeQuery()) {
                    if (rs.next()) {
                        return;
                    }
                    else {
                        throw new ApplicationException("Tuple not found");
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    private SqlBuilder getLockSqlBuilder() {
        SqlBuilder sqlBuilder = new SqlBuilder("select *");
        sqlBuilder.append(" from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where id = ?");
        sqlBuilder.bindLong(this.getId());
        sqlBuilder.append(" for update");
        return sqlBuilder;
    }


    class LogBookUpdaterForDevice extends LogBookImpl.LogBookUpdater {

        protected LogBookUpdaterForDevice(LogBookImpl logBook) {
            super(logBook);
        }
    }

    public List<LoadProfile> getLoadProfiles() {
        return Collections.unmodifiableList(this.loadProfiles);
    }

    @Override
    public LoadProfile.LoadProfileUpdater getLoadProfileUpdaterFor(LoadProfile loadProfile) {
        return new LoadProfileUpdaterForDevice((LoadProfileImpl) loadProfile);
    }

    class LoadProfileUpdaterForDevice extends LoadProfileImpl.LoadProfileUpdater {

        protected LoadProfileUpdaterForDevice(LoadProfileImpl loadProfile) {
            super(loadProfile);
        }
    }

    public TypedProperties getDeviceProtocolProperties() {
        TypedProperties properties = TypedProperties.inheritingFrom(this.getDeviceProtocolPluggableClass().getProperties());
        TypedProperties localProperties = getLocalProperties(this.getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs());
        properties.setAllProperties(localProperties);
        return properties;
    }


    private String getPropertyValue(String name, Object value) {
        String propertyValue = null;
        for (PropertySpec propertySpec : this.getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs()) {
            if (propertySpec.getName().equals(name)) {
                propertyValue = propertySpec.getValueFactory().toStringValue(value);
            }
        }
        return propertyValue;
    }

    private boolean propertyExistsOnDeviceProtocol(String name) {
        for (PropertySpec propertySpec : this.getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs()) {
            if (propertySpec.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ProtocolDialectProperties> getProtocolDialectPropertiesList() {
        List<ProtocolDialectProperties> all = new ArrayList<>(this.dialectPropertiesList.size() + this.newDialectProperties.size());
        all.addAll(this.dialectPropertiesList);
        all.addAll(this.newDialectProperties);
        return all;
    }

    @Override
    public ProtocolDialectProperties getProtocolDialectProperties(String dialectName) {
        ProtocolDialectProperties dialectProperties = this.getProtocolDialectPropertiesFrom(dialectName, this.dialectPropertiesList);
        if (dialectProperties != null) {
            return dialectProperties;
        } else {
            // Attempt to find the dialect properties in the list of new ones that have not been saved yet
            return this.getProtocolDialectPropertiesFrom(dialectName, this.newDialectProperties);
        }
    }

    private ProtocolDialectProperties getProtocolDialectPropertiesFrom(String dialectName, List<ProtocolDialectProperties> propertiesList) {
        for (ProtocolDialectProperties properties : propertiesList) {
            if (properties.getDeviceProtocolDialectName().equals(dialectName)) {
                return properties;
            }
        }
        return null;
    }

    @Override
    public void setProtocolDialectProperty(String dialectName, String propertyName, Object value) {
        ProtocolDialectProperties dialectProperties = this.getProtocolDialectProperties(dialectName);
        if (dialectProperties == null) {
            dialectProperties = createNewLocalDialectProperties(dialectName);
        } else {
            this.dirtyDialectProperties.add(dialectProperties);
        }
        dialectProperties.setProperty(propertyName, value);
    }

    private ProtocolDialectProperties createNewLocalDialectProperties(String dialectName) {
        ProtocolDialectProperties dialectProperties;
        ProtocolDialectConfigurationProperties configurationProperties = this.getProtocolDialectConfigurationProperties(dialectName);
        if (configurationProperties != null) {
            dialectProperties = this.dataModel.getInstance(ProtocolDialectPropertiesImpl.class).initialize(this, configurationProperties);
            this.newDialectProperties.add(dialectProperties);
        } else {
            throw new ProtocolDialectConfigurationPropertiesIsRequiredException();
        }
        return dialectProperties;
    }

    private ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties(String dialectName) {
        List<ProtocolDialectConfigurationProperties> allConfigurationProperties =
                this.getDeviceConfiguration().getCommunicationConfiguration().getProtocolDialectConfigurationPropertiesList();
        for (ProtocolDialectConfigurationProperties configurationProperties : allConfigurationProperties) {
            if (configurationProperties.getDeviceProtocolDialectName().equals(dialectName)) {
                return configurationProperties;
            }
        }
        return null;
    }

    @Override
    public void removeProtocolDialectProperty(String dialectName, String propertyName) {
        ProtocolDialectProperties dialectProperties = this.getProtocolDialectProperties(dialectName);
        if (dialectProperties != null) {
            dialectProperties.removeProperty(propertyName);
        } else {
            createNewLocalDialectProperties(dialectName);
        }
        if ((dialectProperties != null) && !this.dirtyDialectProperties.contains(dialectProperties)) {
            this.dirtyDialectProperties.add(dialectProperties);
        }
    }

    @Override
    public void setProtocolProperty(String name, Object value) {
        if (propertyExistsOnDeviceProtocol(name)) {
            String propertyValue = getPropertyValue(name, value);
            boolean notUpdated = !updatePropertyIfExists(name, propertyValue);
            if (notUpdated) {
                addDeviceProperty(name, propertyValue);
            }
        } else {
            throw DeviceProtocolPropertyException.propertyDoesNotExistForDeviceProtocol(thesaurus, name, this.getDeviceProtocolPluggableClass().getDeviceProtocol(), this);
        }
    }

    @Override
    public void setSecurityProperties(SecurityPropertySet securityPropertySet, TypedProperties typedProperties) {
        try {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass();
            RelationType relationType = protocolPluggableService.findSecurityPropertyRelationType(deviceProtocolPluggableClass);
            RelationTransaction transaction = relationType.newRelationTransaction();
            transaction.setFrom(Date.from(clock.instant()));
            transaction.setTo(null);
            transaction.set(DEVICE_ATTRIBUTE_NAME, this);
            transaction.set(SECURITY_PROPERTY_SET_ATTRIBUTE_NAME, securityPropertySet);
            typedProperties.propertyNames().stream().forEach(p -> transaction.set(p, typedProperties.getPropertyValue(p)));
            transaction.set(STATUS_ATTRIBUTE_NAME, isSecurityPropertySetComplete(securityPropertySet,typedProperties));
            transaction.execute();
        } catch (BusinessException | SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isSecurityPropertySetComplete(SecurityPropertySet securityPropertySet, TypedProperties typedProperties){
        if (securityPropertySet.getPropertySpecs().stream().anyMatch(p -> p.isRequired() && typedProperties.getPropertyValue(p.getName()) == null)) {
            return false;
        } else {
            return true;
        }
    }

    private void addDeviceProperty(String name, String propertyValue) {
        if (propertyValue != null) {
            InfoType infoType = this.deviceService.findInfoType(name);
            DeviceProtocolPropertyImpl deviceProtocolProperty = this.dataModel.getInstance(DeviceProtocolPropertyImpl.class).initialize(this, infoType, propertyValue);
            Save.CREATE.validate(dataModel, deviceProtocolProperty);
            this.deviceProperties.add(deviceProtocolProperty);
        }
    }

    private boolean updatePropertyIfExists(String name, String propertyValue) {
        for (DeviceProtocolProperty deviceProperty : deviceProperties) {
            if (deviceProperty.getName().equals(name)) {
                deviceProperty.setValue(propertyValue);
                deviceProperty.update();
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeProperty(String name) {
        for (DeviceProtocolProperty deviceProtocolProperty : deviceProperties) {
            if (deviceProtocolProperty.getName().equals(name)) {
                this.deviceProperties.remove(deviceProtocolProperty);
                break;
            }
        }
    }

    private TypedProperties getLocalProperties(List<PropertySpec> propertySpecs) {
        TypedProperties properties = TypedProperties.empty();
        for (PropertySpec propertySpec : propertySpecs) {
            DeviceProtocolProperty deviceProtocolProperty = findDevicePropertyFor(propertySpec);
            if (deviceProtocolProperty != null) {
                properties.setProperty(deviceProtocolProperty.getName(), propertySpec.getValueFactory().fromStringValue(deviceProtocolProperty.getPropertyValue()));
            }
        }
        return properties;
    }

    private DeviceProtocolProperty findDevicePropertyFor(PropertySpec propertySpec) {
        for (DeviceProtocolProperty deviceProperty : this.deviceProperties) {
            if (deviceProperty.getName().equals(propertySpec.getName())) {
                return deviceProperty;
            }
        }
        return null;
    }

    @Override
    public void store(MeterReading meterReading) {
        Optional<AmrSystem> amrSystem = getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            Meter meter = findOrCreateKoreMeter(amrSystem.get());
            meter.store(meterReading);
        }
    }

    Optional<Meter> findKoreMeter(AmrSystem amrSystem) {
        return amrSystem.findMeter(String.valueOf(getId()));
    }

    Meter findOrCreateKoreMeter(AmrSystem amrSystem) {
        Optional<Meter> holder = this.findKoreMeter(amrSystem);
        if (!holder.isPresent()) {
            return createKoreMeter(amrSystem);
        } else {
            return holder.get();
        }
    }

    private Meter createKoreMeter() {
        return this.createKoreMeter(this.getMdcAmrSystem().orElseThrow(() -> new RuntimeException("The MDC AMR system does not exist")));
    }

    Meter createKoreMeter(AmrSystem amrSystem) {
        Meter meter = amrSystem.newMeter(String.valueOf(getId()), getmRID());
        meter.save();
        return meter;
    }

    private void deleteKoreMeterIfExists() {
        Optional<AmrSystem> amrSystem = this.getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            Optional<Meter> holder = this.findKoreMeter(amrSystem.get());
            if (holder.isPresent()) {
                holder.get().delete();
            }
        }
    }

    private Optional<AmrSystem> getMdcAmrSystem() {
        return this.meteringService.findAmrSystem(1);
    }

    List<ReadingRecord> getReadingsFor(Register<?> register, Range<Instant> interval) {
        Optional<AmrSystem> amrSystem = getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            Optional<Meter> meter = this.findKoreMeter(amrSystem.get());
            if (meter.isPresent()) {
                List<? extends BaseReadingRecord> readings = meter.get().getReadings(interval, register.getRegisterSpec().getRegisterType().getReadingType());
                List<ReadingRecord> readingRecords = new ArrayList<>(readings.size());
                for (BaseReadingRecord reading : readings) {
                    readingRecords.add((ReadingRecord) reading);
                }
                return readingRecords;
            }
        }
        return Collections.emptyList();
    }

    List<LoadProfileReading> getChannelData(LoadProfile loadProfile, Interval interval) {
        Optional<AmrSystem> amrSystem = getMdcAmrSystem();
        List<LoadProfileReading> loadProfileReadings = Collections.emptyList();
        boolean meterHasData = false;
        if (amrSystem.isPresent()) {
            Optional<Meter> meter = this.findKoreMeter(amrSystem.get());
            if (meter.isPresent()) {
                Map<Instant, LoadProfileReadingImpl> sortedLoadProfileReadingMap =
                        getPreFilledLoadProfileReadingMap(
                                loadProfile,
                                Range.openClosed(interval.getStart(), interval.getEnd()),
                                meter.get());
                Interval clipped = interval.withEnd(lastReadingClipped(loadProfile, interval));
                for (Channel channel : loadProfile.getChannels()) {
                    meterHasData |= this.addChannelDataToMap(clipped, meter.get(), channel, sortedLoadProfileReadingMap);
                }
                if (meterHasData) {
                    loadProfileReadings = new ArrayList<>(sortedLoadProfileReadingMap.values());
                }
            }
        }

        return Lists.reverse(loadProfileReadings);
    }

    List<LoadProfileReading> getChannelData(Channel channel, Interval interval) {
        Optional<AmrSystem> amrSystem = getMdcAmrSystem();
        List<LoadProfileReading> loadProfileReadings = Collections.emptyList();
        boolean meterHasData;
        if (amrSystem.isPresent()) {
            Optional<Meter> meter = this.findKoreMeter(amrSystem.get());
            if (meter.isPresent()) {
                Map<Instant, LoadProfileReadingImpl> sortedLoadProfileReadingMap =
                        getPreFilledLoadProfileReadingMap(
                                channel.getLoadProfile(),
                                Range.openClosed(interval.getStart(), interval.getEnd()),
                                meter.get());
                Interval clipped = interval.withEnd(lastReadingClipped(channel.getLoadProfile(), interval));
                meterHasData = this.addChannelDataToMap(clipped, meter.get(), channel, sortedLoadProfileReadingMap);
                if (meterHasData) {
                    loadProfileReadings = new ArrayList<>(sortedLoadProfileReadingMap.values());
                }
            }
        }
        return Lists.reverse(loadProfileReadings);
    }

    List<EndDeviceEventRecord> getLogBookDeviceEventsByFilter(LogBook logBook, EndDeviceEventRecordFilterSpecification filter) {
        Optional<AmrSystem> amrSystem = getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            Optional<Meter> meter = this.findKoreMeter(amrSystem.get());
            if (meter.isPresent()) {
                filter.logBookId = logBook.getId();
                return meter.get().getDeviceEventsByFilter(filter);
            }
        }
        return Collections.emptyList();
    }

    /**
     * Adds meter readings for a single channel the timeslot-map.
     *
     * @param interval                    The interval over which meter readings are requested
     * @param meter                       The meter for which readings are requested
     * @param mdcChannel                  The meter's channel for which readings are requested
     * @param sortedLoadProfileReadingMap The map to add the readings too in the correct timeslot
     * @return true if any readings were added to the map, false otherwise
     */
    private boolean addChannelDataToMap(Interval interval, Meter meter, Channel mdcChannel, Map<Instant, LoadProfileReadingImpl> sortedLoadProfileReadingMap) {
        boolean meterHasData = false;
        List<MeterActivation> meterActivations = this.getSortedMeterActivations(meter, interval);
        for (MeterActivation meterActivation : meterActivations) {
            Interval meterActivationInterval = meterActivation.getInterval().intersection(interval);
            ReadingType readingType = mdcChannel.getChannelSpec().getReadingType();
            List<IntervalReadingRecord> meterReadings = (List<IntervalReadingRecord>) meter.getReadings(meterActivationInterval.toOpenClosedRange(), readingType);
            if (!meterReadings.isEmpty()) {
                meterHasData = true;
            }
            for (IntervalReadingRecord meterReading : meterReadings) {
                LoadProfileReadingImpl loadProfileReading = sortedLoadProfileReadingMap.get(meterReading.getTimeStamp());
                loadProfileReading.setChannelData(mdcChannel, meterReading);
                loadProfileReading.setFlags(getFlagsFromProfileStatus(meterReading.getProfileStatus()));
                loadProfileReading.setReadingTime(meterReading.getReportedDateTime());
            }

            Optional<com.elster.jupiter.metering.Channel> koreChannel = this.getChannel(meterActivation, readingType);
            if (koreChannel.isPresent()) {
                List<DataValidationStatus> validationStatus = forValidation().getValidationStatus(mdcChannel, meterReadings, meterActivationInterval.toClosedRange());
                validationStatus.stream()
                        .filter(s -> s.getReadingTimestamp().isAfter(meterActivationInterval.getStart()))
                        .forEach(s -> {
                            LoadProfileReadingImpl loadProfileReading = sortedLoadProfileReadingMap.get(s.getReadingTimestamp());
                            if (loadProfileReading != null) {
                                loadProfileReading.setDataValidationStatus(mdcChannel, s);
                                //code below is the processing of removed readings
                                Optional<? extends ReadingQuality> readingQuality = s.getReadingQualities().stream().filter(rq -> rq.getType().equals(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.REJECTED))).findAny();
                                if (readingQuality.isPresent()) {
                                    loadProfileReading.setReadingTime(((ReadingQualityRecord)readingQuality.get()).getTimestamp());
                                }
                            }
                        });
            }
        }
        return meterHasData;
    }

    private List<ProfileStatus.Flag> getFlagsFromProfileStatus(ProfileStatus profileStatus) {
        List<ProfileStatus.Flag> flags = new ArrayList<>();
        for (ProfileStatus.Flag flag : ProfileStatus.Flag.values()) {
            if (profileStatus.get(flag)) {
                flags.add(flag);
            }
        }
        return flags;
    }

    /**
     * Creates a map of LoadProfileReadings (k,v -> timestamp of end of interval, placeholder for readings) (without a reading value),
     * just a list of placeholders for each reading interval within the requestedInterval for all datetimes
     * that occur with the bounds of a meter activation and load profile's last reading.
     *
     * @param loadProfile     The LoadProfile
     * @param requestedInterval interval over which user wants to see readings
     * @param meter           The Meter
     * @return
     */
    private Map<Instant, LoadProfileReadingImpl> getPreFilledLoadProfileReadingMap(LoadProfile loadProfile, Range<Instant> requestedInterval, Meter meter) {
        // TODO: what if there are gaps in the meter activations
        Map<Instant, LoadProfileReadingImpl> loadProfileReadingMap = new TreeMap<>();
        TemporalAmount intervalLength = this.intervalLength(loadProfile);
        List<MeterActivation> allMeterActivations = new ArrayList<>(meter.getMeterActivations());
        List<MeterActivation> affectedMeterActivations =
                allMeterActivations
                        .stream()
                        .filter(ma -> ma.overlaps(requestedInterval))
                        .collect(toList());
        for (MeterActivation affectedMeterActivation : affectedMeterActivations) {
            Range<Instant> requestedIntervalClippedToMeterActivation = requestedInterval.intersection(affectedMeterActivation.getRange());
            ZonedDateTime requestStart = this.prefilledIntervalStart(loadProfile, affectedMeterActivation.getZoneId(), requestedIntervalClippedToMeterActivation);
            ZonedDateTime requestEnd =
                    ZonedDateTime.ofInstant(
                            this.lastReadingClipped(loadProfile, requestedInterval),
                            affectedMeterActivation.getZoneId());
            Range<Instant> meterActivationInterval = Range.closedOpen(requestStart.toInstant(), requestEnd.toInstant());
            while (meterActivationInterval.contains(requestStart.toInstant())) {
                ZonedDateTime readingTimestamp = requestStart.plus(intervalLength);
                LoadProfileReadingImpl value = new LoadProfileReadingImpl();
                value.setInterval(Interval.of(requestStart.toInstant(), readingTimestamp.toInstant()));
                loadProfileReadingMap.put(readingTimestamp.toInstant(), value);
                requestStart = readingTimestamp;
            }
        }
        return loadProfileReadingMap;
    }

    private ZonedDateTime prefilledIntervalStart(LoadProfile loadProfile, ZoneId zoneId, Range<Instant> requestedIntervalClippedToMeterActivation) {
        switch (loadProfile.getInterval().getTimeUnit()) {
            case MINUTES: // Intentional fall-through
            case HOURS: {
                return this.prefilledIntervalStartWithIntervalWithinDay(loadProfile, zoneId, requestedIntervalClippedToMeterActivation);
            }
            case DAYS: {
                return ZonedDateTime
                        .ofInstant(
                                requestedIntervalClippedToMeterActivation.lowerEndpoint(),
                                zoneId)
                        .truncatedTo(this.trunctationUnit(loadProfile));    // round start time to interval boundary
            }
            case WEEKS: {
                return ZonedDateTime
                            .ofInstant(requestedIntervalClippedToMeterActivation.lowerEndpoint(), zoneId)
                            .toLocalDate()
                            .with(ChronoField.DAY_OF_WEEK, 1)
                            .atStartOfDay()
                            .atZone(zoneId);
            }
            case MONTHS: {
                return ZonedDateTime
                            .ofInstant(requestedIntervalClippedToMeterActivation.lowerEndpoint(), zoneId)
                            .toLocalDate()
                            .with(ChronoField.DAY_OF_MONTH, 1)
                            .atStartOfDay()
                            .atZone(zoneId);
            }
            case YEARS: {
                return ZonedDateTime
                            .ofInstant(requestedIntervalClippedToMeterActivation.lowerEndpoint(), zoneId)
                            .toLocalDate()
                            .with(ChronoField.DAY_OF_YEAR, 1)
                            .atStartOfDay()
                            .atZone(zoneId);
            }
            case MILLISECONDS:  //Intentional fall-through
            case SECONDS:   //Intentional fall-through
            default: {
                throw new IllegalArgumentException("Unsupported load profile interval length unit " + loadProfile.getInterval().getTimeUnit());
            }
        }
    }

    private ZonedDateTime prefilledIntervalStartWithIntervalWithinDay(LoadProfile loadProfile, ZoneId zoneId, Range<Instant> requestedIntervalClippedToMeterActivation) {
        /* Implementation note: truncate meter activation end point of the interval to the interval length
         * and then increment with interval length until start >= meter activation start
         * to cater for the situation where meter activation is e.g. 8h43 with interval length of 15mins
         * where truncating would start at 8h00 */
        ZonedDateTime attempt =
                ZonedDateTime
                    .ofInstant(
                        requestedIntervalClippedToMeterActivation.lowerEndpoint(),
                        zoneId)
                    .truncatedTo(this.trunctationUnit(loadProfile));    // round start time to interval boundary
        while (attempt.toInstant().isBefore(requestedIntervalClippedToMeterActivation.lowerEndpoint())) {
            attempt = attempt.plus(this.intervalLength(loadProfile));
        }
        return attempt;
    }

    private TemporalUnit trunctationUnit (LoadProfile loadProfile) {
        switch (loadProfile.getInterval().getTimeUnit()) {
            case MINUTES: {
                return ChronoUnit.HOURS;
            }
            case HOURS: {
                return ChronoUnit.DAYS;
            }
            case DAYS: {
                return ChronoUnit.DAYS;
            }
            case WEEKS: {
                return ChronoUnit.WEEKS;
            }
            case MONTHS: {
                return ChronoUnit.MONTHS;
            }
            case YEARS: {
                return ChronoUnit.YEARS;
            }
            case MILLISECONDS:  //Intentional fall-through
            case SECONDS:   //Intentional fall-through
            default: {
                throw new IllegalArgumentException("Unsupported load profile interval length unit " + loadProfile.getInterval().getTimeUnit());
            }
        }
    }

    private TemporalAmount intervalLength(LoadProfile loadProfile) {
        switch (loadProfile.getInterval().getTimeUnit()) {
            case MILLISECONDS: {
                return Duration.ofMillis(loadProfile.getInterval().getCount());
            }
            case SECONDS: {
                return Duration.ofSeconds(loadProfile.getInterval().getCount());
            }
            case MINUTES: {
                return Duration.ofMinutes(loadProfile.getInterval().getCount());
            }
            case HOURS: {
                return Duration.ofHours(loadProfile.getInterval().getCount());
            }
            case DAYS: {
                return Period.ofDays(loadProfile.getInterval().getCount());
            }
            case WEEKS: {
                return Period.ofWeeks(loadProfile.getInterval().getCount());
            }
            case MONTHS: {
                return Period.ofMonths(loadProfile.getInterval().getCount());
            }
            case YEARS: {
                return Period.ofYears(loadProfile.getInterval().getCount());
            }
            default: {
                throw new IllegalArgumentException("Unsupported load profile interval length unit " + loadProfile.getInterval().getTimeUnit());
            }
        }
    }

    private Instant lastReadingClipped(LoadProfile loadProfile, Interval requestInterval) {
        if (loadProfile.getLastReading().isPresent() && requestInterval.getEnd().isAfter(loadProfile.getLastReading().get())) {
            return loadProfile.getLastReading().get();
        } else {
            return Instant.ofEpochMilli(requestInterval.getEnd().toEpochMilli());
        }
    }

    private Instant lastReadingClipped(LoadProfile loadProfile, Range<Instant> interval) {
        if (loadProfile.getLastReading().isPresent() && interval.contains(loadProfile.getLastReading().get())) {
            return loadProfile.getLastReading().get();
        }
        else {
            return interval.upperEndpoint();
        }
    }

    Optional<ReadingRecord> getLastReadingFor(Register<?> register) {
        Optional<AmrSystem> amrSystem = getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            Optional<Meter> meter = this.findKoreMeter(amrSystem.get());
            if (meter.isPresent()) {
                return this.getLastReadingsFor(register, meter.get());
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    private Optional<ReadingRecord> getLastReadingsFor(Register register, Meter meter) {
        ReadingType readingType = register.getRegisterSpec().getRegisterType().getReadingType();
        for (MeterActivation meterActivation : this.getSortedMeterActivations(meter)) {
            Optional<com.elster.jupiter.metering.Channel> channel = this.getChannel(meterActivation, readingType);
            if (channel.isPresent()) {
                Instant lastReadingDate = channel.get().getLastDateTime();
                if (lastReadingDate != null) {
                    return this.getLast(channel.get().getRegisterReadings(Interval.of(lastReadingDate, lastReadingDate).toClosedRange()));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Sorts the {@link MeterActivation}s of the specified {@link Meter}
     * where the most recent activations are returned first.
     *
     * @param meter The Meter
     * @return The List of MeterActivation
     */
    private List<? extends MeterActivation> getSortedMeterActivations(Meter meter) {
        List<? extends MeterActivation> meterActivations = new ArrayList<>(meter.getMeterActivations());    // getMeterActivations returns ImmutableList
        Collections.reverse(meterActivations);
        return meterActivations;
    }

    Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Channel channel, Instant when) {
        return findKoreChannel(channel::getReadingType, when);
    }

    Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Register<?> register, Instant when) {
        return findKoreChannel(() -> register.getReadingType(), when);
    }

    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Supplier<ReadingType> readingTypeSupplier, Instant when) {
        Optional<Meter> found = findKoreMeter(getMdcAmrSystem().get());
        if (found.isPresent()) {
            Optional<? extends MeterActivation> meterActivation = found.get().getMeterActivation(when);
            if (meterActivation.isPresent()) {
                return Optional.ofNullable(getChannel(meterActivation.get(), readingTypeSupplier.get()).orElse(null));
            }
        }
        return Optional.empty();
    }

    List<com.elster.jupiter.metering.Channel> findKoreChannels(Channel channel) {
        return findKoreChannels(channel::getReadingType);
    }

    List<com.elster.jupiter.metering.Channel> findKoreChannels(Register<?> register) {
        return findKoreChannels(() -> register.getReadingType());
    }

    List<com.elster.jupiter.metering.Channel> findKoreChannels(Supplier<ReadingType> readingTypeSupplier) {
        Optional<Meter> found = findKoreMeter(getMdcAmrSystem().get());
        if (found.isPresent()) {
            return found.get().getMeterActivations().stream()
                    .map(m -> getChannel(m, readingTypeSupplier.get()))
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Optional<com.elster.jupiter.metering.Channel> getChannel(MeterActivation meterActivation, ReadingType readingType) {
        for (com.elster.jupiter.metering.Channel channel : meterActivation.getChannels()) {
            if (channel.getReadingTypes().contains(readingType)) {
                return java.util.Optional.of(channel);
            }
        }
        return java.util.Optional.empty();
    }

    /**
     * Sorts the {@link MeterActivation}s of the specified {@link Meter}
     * that overlap with the {@link Interval}, where the most recent activations are returned first.
     *
     * @param meter    The Meter
     * @param interval The Interval
     * @return The List of MeterActivation
     */
    private List<MeterActivation> getSortedMeterActivations(Meter meter, Interval interval) {
        List<? extends MeterActivation> allActivations = meter.getMeterActivations();
        List<MeterActivation> overlapping = new ArrayList<>(allActivations.size());
        for (MeterActivation activation : allActivations) {
            if (activation.overlaps(interval.toClosedRange())) {
                overlapping.add(activation);
            }
        }
        Collections.reverse(overlapping);
        return overlapping;
    }

    private Optional<ReadingRecord> getLast(List<ReadingRecord> readings) {
        if (readings.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(readings.get(readings.size() - 1));
        }
    }

    public List<DeviceMultiplier> getDeviceMultipliers() {
        return Collections.emptyList();
    }

    public DeviceMultiplier getDeviceMultiplier(Instant date) {
        return null;
    }

    public DeviceMultiplier getDeviceMultiplier() {
        return null;
    }

    public DataMapper<DeviceImpl> getDataMapper() {
        return this.dataModel.mapper(DeviceImpl.class);
    }

    @Override
    public String getmRID() {
        return mRID;
    }

    @Override
    public ScheduledConnectionTaskBuilder getScheduledConnectionTaskBuilder(PartialOutboundConnectionTask partialOutboundConnectionTask) {
        return new ScheduledConnectionTaskBuilderForDevice(this, partialOutboundConnectionTask);
    }

    @Override
    public InboundConnectionTaskBuilder getInboundConnectionTaskBuilder(PartialInboundConnectionTask partialInboundConnectionTask) {
        return new InboundConnectionTaskBuilderForDevice(this, partialInboundConnectionTask);
    }

    @Override
    public ConnectionInitiationTaskBuilder getConnectionInitiationTaskBuilder(PartialConnectionInitiationTask partialConnectionInitiationTask) {
        return new ConnectionInitiationTaskBuilderForDevice(this, partialConnectionInitiationTask);
    }

    @Override
    public List<ConnectionTask<?, ?>> getConnectionTasks() {
        return new ArrayList<ConnectionTask<?, ?>>(this.getConnectionTaskImpls());
    }

    private List<ConnectionTaskImpl<?, ?>> getConnectionTaskImpls() {
        if (this.connectionTasks == null) {
            this.loadConnectionTasks();
        }
        return this.connectionTasks;
    }

    private void loadConnectionTasks() {
        List<ConnectionTaskImpl<?, ?>> connectionTaskImpls = new ArrayList<>();
        for (ConnectionTask connectionTask : this.connectionTaskService.findConnectionTasksByDevice(this)) {
            connectionTaskImpls.add((ConnectionTaskImpl<?, ?>) connectionTask);
        }
        this.connectionTasks = connectionTaskImpls;
    }

    @Override
    public List<ScheduledConnectionTask> getScheduledConnectionTasks() {
        List<ConnectionTask<?, ?>> allConnectionTasks = this.getConnectionTasks();
        List<ScheduledConnectionTask> outboundConnectionTasks = new ArrayList<>(allConnectionTasks.size());   // Worst case: all connection tasks are scheduled
        for (ConnectionTask<?, ?> connectionTask : allConnectionTasks) {
            if (connectionTask instanceof ScheduledConnectionTask) {
                outboundConnectionTasks.add((ScheduledConnectionTask) connectionTask);
            }
        }
        return outboundConnectionTasks;
    }

    @Override
    public List<ConnectionInitiationTask> getConnectionInitiationTasks() {
        List<ConnectionTask<?, ?>> allConnectionTasks = this.getConnectionTasks();
        List<ConnectionInitiationTask> initiationTasks = new ArrayList<>(allConnectionTasks.size());   // Worst case: all connection tasks are initiators
        for (ConnectionTask<?, ?> connectionTask : allConnectionTasks) {
            if (connectionTask instanceof ConnectionInitiationTask) {
                initiationTasks.add((ConnectionInitiationTask) connectionTask);
            }
        }
        return initiationTasks;
    }

    @Override
    public List<InboundConnectionTask> getInboundConnectionTasks() {
        List<ConnectionTask<?, ?>> allConnectionTasks = this.getConnectionTasks();
        List<InboundConnectionTask> inboundConnectionTasks = new ArrayList<>(allConnectionTasks.size());   // Worst case: all connection tasks are inbound
        for (ConnectionTask<?, ?> connectionTask : allConnectionTasks) {
            if (connectionTask instanceof InboundConnectionTask) {
                inboundConnectionTasks.add((InboundConnectionTask) connectionTask);
            }
        }
        return inboundConnectionTasks;
    }

    @Override
    public void removeConnectionTask(ConnectionTask<?, ?> connectionTask) {
        Iterator<ConnectionTaskImpl<?, ?>> connectionTaskIterator = this.getConnectionTaskImpls().iterator();
        boolean removedNone = true;
        while (connectionTaskIterator.hasNext() && removedNone) {
            ConnectionTaskImpl<?, ?> connectionTaskToRemove = connectionTaskIterator.next();
            if (connectionTaskToRemove.getId() == connectionTask.getId()) {
                connectionTaskToRemove.delete();
                connectionTaskIterator.remove();
                removedNone = false;
            }
        }
        if (removedNone) {
            throw new CannotDeleteConnectionTaskWhichIsNotFromThisDevice(this.thesaurus, connectionTask, this);

        }
    }

    @Override
    public List<ComTaskExecution> getComTaskExecutions() {
        return new ArrayList<ComTaskExecution>(this.getComTaskExecutionImpls());
    }

    private List<ComTaskExecutionImpl> getComTaskExecutionImpls() {
        if (this.comTaskExecutions == null) {
            this.loadComTaskExecutions();
        }
        return this.comTaskExecutions;
    }

    private void loadComTaskExecutions() {
        List<ComTaskExecutionImpl> comTaskExecutionImpls = new ArrayList<>();
        for (ComTaskExecution comTaskExecution : this.communicationTaskService.findComTaskExecutionsByDevice(this)) {
            comTaskExecutionImpls.add((ComTaskExecutionImpl) comTaskExecution);
        }
        this.comTaskExecutions = comTaskExecutionImpls;
    }

    private void add(ComTaskExecutionImpl comTaskExecution) {
        this.getComTaskExecutionImpls().add(comTaskExecution);
        if (this.id != 0) {
            Save.CREATE.validate(this.dataModel, this);  // To validate that all scheduled ComTasks are unique
        }
    }

    @Override
    public ComTaskExecutionBuilder<ScheduledComTaskExecution> newScheduledComTaskExecution(ComSchedule comSchedule) {
        return new ScheduledComTaskExecutionBuilderForDevice(scheduledComTaskExecutionProvider, this, comSchedule);
    }

    @Override
    public AdHocComTaskExecutionBuilderForDevice newAdHocComTaskExecution(ComTaskEnablement comTaskEnablement, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
        return new AdHocComTaskExecutionBuilderForDevice(manuallyScheduledComTaskExecutionProvider, this, comTaskEnablement, protocolDialectConfigurationProperties);
    }

    @Override
    public ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> newManuallyScheduledComTaskExecution(ComTaskEnablement comTaskEnablement, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, TemporalExpression temporalExpression) {
        return new ManuallyScheduledComTaskExecutionBuilderForDevice(
                this.manuallyScheduledComTaskExecutionProvider,
                this,
                comTaskEnablement,
                protocolDialectConfigurationProperties,
                temporalExpression);
    }

    @Override
    public ManuallyScheduledComTaskExecutionUpdater getComTaskExecutionUpdater(ManuallyScheduledComTaskExecution comTaskExecution) {
        return comTaskExecution.getUpdater();
    }

    @Override
    public ScheduledComTaskExecutionUpdater getComTaskExecutionUpdater(ScheduledComTaskExecution comTaskExecution) {
        return comTaskExecution.getUpdater();
    }

    @Override
    public void removeComTaskExecution(ComTaskExecution comTaskExecution) {
        Iterator<ComTaskExecutionImpl> comTaskExecutionIterator = this.getComTaskExecutionImpls().iterator();
        while (comTaskExecutionIterator.hasNext()) {
            ComTaskExecution comTaskExecutionToRemove = comTaskExecutionIterator.next();
            if (comTaskExecutionToRemove.getId() == comTaskExecution.getId()) {
                comTaskExecution.makeObsolete();
                comTaskExecutionIterator.remove();
                return;
            }
        }
        throw new CannotDeleteComTaskExecutionWhichIsNotFromThisDevice(thesaurus, comTaskExecution, this);
    }

    @Override
    public void removeComSchedule(ComSchedule comSchedule) {
        Iterator<ComTaskExecutionImpl> comTaskExecutionIterator = this.getComTaskExecutionImpls().iterator();
        while (comTaskExecutionIterator.hasNext()) {
            ComTaskExecutionImpl comTaskExecution = comTaskExecutionIterator.next();
            if (comTaskExecution.executesComSchedule(comSchedule)) {
                comTaskExecution.makeObsolete();
                comTaskExecutionIterator.remove();
                return;
            }
        }
        throw new CannotDeleteComScheduleFromDevice(this.thesaurus, comSchedule, this);
    }

    @Override
    public int countNumberOfEndDeviceEvents(List<EndDeviceEventType> eventTypes, Interval interval) {
        int eventCounter = 0;
        Optional<AmrSystem> amrSystem = this.getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            for (Device slaveDevice : this.getPhysicalConnectedDevices()) {
                Optional<Meter> slaveMeter = amrSystem.get().findMeter(String.valueOf(slaveDevice.getId()));
                if (slaveMeter.isPresent()) {
                    eventCounter = eventCounter + this.countUniqueEndDeviceEvents(slaveMeter.get(), eventTypes, interval);
                }
            }
        }
        return eventCounter;
    }

    @Override
    public List<SecurityProperty> getSecurityProperties(SecurityPropertySet securityPropertySet) {
        return this.getSecurityProperties(clock.instant(), securityPropertySet);
    }

    @Override
    public List<SecurityProperty> getAllSecurityProperties(SecurityPropertySet securityPropertySet) {
        return this.getAllSecurityProperties(clock.instant(), securityPropertySet);
    }

    @Override
    public List<ProtocolDialectConfigurationProperties> getProtocolDialects() {
        return this.getDeviceConfiguration().getProtocolDialectConfigurationPropertiesList();
    }

    private List<SecurityProperty> getSecurityProperties(Instant when, SecurityPropertySet securityPropertySet) {
        return this.securityPropertyService.getSecurityProperties(this, when, securityPropertySet);
    }

    private List<SecurityProperty> getAllSecurityProperties(Instant when, SecurityPropertySet securityPropertySet) {
        return this.securityPropertyService.getAllSecurityProperties(this, when, securityPropertySet);
    }

    @Override
    public boolean hasSecurityProperties(SecurityPropertySet securityPropertySet) {
        return this.hasSecurityProperties(clock.instant(), securityPropertySet);
    }

    @Override
    public DeviceValidation forValidation() {
        if (deviceValidation == null) {
            deviceValidation = new DeviceValidationImpl(getMdcAmrSystem().get(), validationService, clock, this);
        }
        return deviceValidation;
    }

    @Override
    public GatewayType getConfigurationGatewayType(){
        DeviceConfiguration configuration = getDeviceConfiguration();
        if (configuration == null) {
            return GatewayType.NONE;
        }
        return configuration.getGetwayType();
    }

    @Override
    public DeviceMessageBuilder newDeviceMessage(DeviceMessageId deviceMessageId) {
        return new InternalDeviceMessageBuilder(deviceMessageId);
    }

    private class InternalDeviceMessageBuilder implements DeviceMessageBuilder{

        private final DeviceMessageImpl deviceMessage;

        public InternalDeviceMessageBuilder(DeviceMessageId deviceMessageId) {
            deviceMessage = DeviceImpl.this.dataModel.getInstance(DeviceMessageImpl.class).initialize(DeviceImpl.this, deviceMessageId);
        }

        @Override
        public DeviceMessageBuilder addProperty(String key, Object value) {
            this.deviceMessage.addProperty(key, value);
            return this;
        }

        @Override
        public DeviceMessageBuilder setReleaseDate(Instant releaseDate) {
            this.deviceMessage.setReleaseDate(releaseDate);
            return this;
        }

        @Override
        public DeviceMessageBuilder setTrackingId(String trackingId) {
            this.deviceMessage.setTrackingId(trackingId);
            return this;
        }

        @Override
        public DeviceMessage<Device> add() {
            this.deviceMessage.save();
            DeviceImpl.this.deviceMessages.add(this.deviceMessage);
            return this.deviceMessage;
        }
    }

    private boolean hasSecurityProperties(Instant when, SecurityPropertySet securityPropertySet) {
        return this.securityPropertyService.hasSecurityProperties(this, when, securityPropertySet);
    }

    private int countUniqueEndDeviceEvents(Meter slaveMeter, List<EndDeviceEventType> eventTypes, Interval interval) {
        Set<String> deviceEventTypes = new HashSet<>();
        for (EndDeviceEventRecord endDeviceEvent : slaveMeter.getDeviceEvents(interval.toClosedRange(), eventTypes)) {
            deviceEventTypes.add(endDeviceEvent.getMRID());
        }
        return deviceEventTypes.size();
    }

    private class ConnectionInitiationTaskBuilderForDevice extends ConnectionInitiationTaskImpl.AbstractConnectionInitiationTaskBuilder {

        private ConnectionInitiationTaskBuilderForDevice(Device device, PartialConnectionInitiationTask partialConnectionInitiationTask) {
            super(connectionInitiationTaskProvider.get());
            getConnectionInitiationTask().initialize(device, partialConnectionInitiationTask, partialConnectionInitiationTask.getComPortPool());
        }

        @Override
        public ConnectionInitiationTaskBuilder setComPortPool(OutboundComPortPool comPortPool) {
            getConnectionInitiationTask().setComPortPool(comPortPool);
            return this;
        }

        @Override
        public ConnectionInitiationTaskBuilder setProperty(String propertyName, Object value) {
            getConnectionInitiationTask().setProperty(propertyName, value);
            return this;
        }

        @Override
        public ConnectionInitiationTask add() {
            getConnectionInitiationTask().save();
            DeviceImpl.this.connectionTasks = null;
            return getConnectionInitiationTask();
        }
    }

    private class InboundConnectionTaskBuilderForDevice extends InboundConnectionTaskImpl.AbstractInboundConnectionTaskBuilder {

        private InboundConnectionTaskBuilderForDevice(Device device, PartialInboundConnectionTask partialInboundConnectionTask) {
            super(inboundConnectionTaskProvider.get());
            this.getInboundConnectionTask().initialize(device, partialInboundConnectionTask, partialInboundConnectionTask.getComPortPool());
        }

        @Override
        public InboundConnectionTaskBuilder setComPortPool(InboundComPortPool comPortPool) {
            this.getInboundConnectionTask().setComPortPool(comPortPool);
            return this;
        }

        @Override
        public InboundConnectionTaskBuilder setProperty(String propertyName, Object value) {
            this.getInboundConnectionTask().setProperty(propertyName, value);
            return this;
        }

        @Override
        public InboundConnectionTask add() {
            InboundConnectionTaskImpl inboundConnectionTask = this.getInboundConnectionTask();
            inboundConnectionTask.save();
            DeviceImpl.this.connectionTasks = null;
            return this.getInboundConnectionTask();
        }
    }

    private class ScheduledConnectionTaskBuilderForDevice extends ScheduledConnectionTaskImpl.AbstractScheduledConnectionTaskBuilder {

        private ScheduledConnectionTaskBuilderForDevice(Device device, PartialOutboundConnectionTask partialOutboundConnectionTask) {
            super(scheduledConnectionTaskProvider.get());
            this.getScheduledConnectionTask().initialize(device, (PartialScheduledConnectionTask) partialOutboundConnectionTask, partialOutboundConnectionTask.getComPortPool());
            if (partialOutboundConnectionTask.getNextExecutionSpecs() != null) {
                this.getScheduledConnectionTask().setNextExecutionSpecsFrom(partialOutboundConnectionTask.getNextExecutionSpecs().getTemporalExpression());
            }
            this.getScheduledConnectionTask().setConnectionStrategy(((PartialScheduledConnectionTask) partialOutboundConnectionTask).getConnectionStrategy());
            this.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        }

        @Override
        public ScheduledConnectionTaskBuilder setCommunicationWindow(ComWindow communicationWindow) {
            this.getScheduledConnectionTask().setCommunicationWindow(communicationWindow);
            return this;
        }

        @Override
        public ScheduledConnectionTaskBuilder setComPortPool(OutboundComPortPool comPortPool) {
            this.getScheduledConnectionTask().setComPortPool(comPortPool);
            return this;
        }

        @Override
        public ScheduledConnectionTaskBuilder setConnectionStrategy(ConnectionStrategy connectionStrategy) {
            this.getScheduledConnectionTask().setConnectionStrategy(connectionStrategy);
            return this;
        }

        @Override
        public ScheduledConnectionTaskBuilder setInitiatorTask(ConnectionInitiationTask connectionInitiationTask) {
            this.getScheduledConnectionTask().setInitiatorTask(connectionInitiationTask);
            return this;
        }

        @Override
        public ScheduledConnectionTaskBuilder setNextExecutionSpecsFrom(TemporalExpression temporalExpression) {
            this.getScheduledConnectionTask().setNextExecutionSpecsFrom(temporalExpression);
            return this;
        }

        @Override
        public ScheduledConnectionTaskBuilder setProperty(String propertyName, Object value) {
            this.getScheduledConnectionTask().setProperty(propertyName, value);
            return this;
        }

        @Override
        public ScheduledConnectionTaskBuilder setSimultaneousConnectionsAllowed(boolean allowSimultaneousConnections) {
            this.getScheduledConnectionTask().setSimultaneousConnectionsAllowed(allowSimultaneousConnections);
            return this;
        }

        @Override
        public ScheduledConnectionTask add() {
            this.getScheduledConnectionTask().save();
            DeviceImpl.this.connectionTasks = null;
            return this.getScheduledConnectionTask();
        }
    }

    public class ScheduledComTaskExecutionBuilderForDevice
            extends ScheduledComTaskExecutionImpl.ScheduledComTaskExecutionBuilderImpl {

        private Set<ComTaskExecution> executionsToDelete = new HashSet<>();

        private ScheduledComTaskExecutionBuilderForDevice(Provider<ScheduledComTaskExecutionImpl> comTaskExecutionProvider, Device device, ComSchedule comSchedule) {
            super(comTaskExecutionProvider.get());
            this.initExecutionsToDelete(device, comSchedule);
            this.getComTaskExecution().initialize(device, comSchedule);
        }

        private void initExecutionsToDelete(Device device, ComSchedule comSchedule) {
            for(ComTaskExecution comTaskExecution:device.getComTaskExecutions()){
                if(!comTaskExecution.usesSharedSchedule()){
                    for(ComTask comTask : comSchedule.getComTasks()){
                        if(comTaskExecution.getComTasks().get(0).getId()==comTask.getId()){
                            this.executionsToDelete.add(comTaskExecution);
                        }
                    }
                }
            }
            comSchedule.getComTasks();
        }

        @Override
        public ScheduledComTaskExecution add() {
            for(ComTaskExecution comTaskExecutionToDelete:executionsToDelete){
                DeviceImpl.this.removeComTaskExecution(comTaskExecutionToDelete);
            }
            ScheduledComTaskExecution comTaskExecution = super.add();
            DeviceImpl.this.add((ComTaskExecutionImpl) comTaskExecution);
            return comTaskExecution;
        }
    }

    public class AdHocComTaskExecutionBuilderForDevice
            extends ManuallyScheduledComTaskExecutionImpl.ManuallyScheduledComTaskExecutionBuilderImpl {

        private AdHocComTaskExecutionBuilderForDevice(Provider<ManuallyScheduledComTaskExecutionImpl> comTaskExecutionProvider, Device device, ComTaskEnablement comTaskEnablement, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
            super(comTaskExecutionProvider.get());
            this.getComTaskExecution().initializeAdhoc(device, comTaskEnablement, protocolDialectConfigurationProperties);
        }


        @Override
        public ManuallyScheduledComTaskExecution add() {
            ManuallyScheduledComTaskExecution comTaskExecution = super.add();
            DeviceImpl.this.getComTaskExecutionImpls().add((ComTaskExecutionImpl) comTaskExecution);
            return comTaskExecution;
        }
    }

    public class ManuallyScheduledComTaskExecutionBuilderForDevice
            extends ManuallyScheduledComTaskExecutionImpl.ManuallyScheduledComTaskExecutionBuilderImpl {

        private ManuallyScheduledComTaskExecutionBuilderForDevice(Provider<ManuallyScheduledComTaskExecutionImpl> comTaskExecutionProvider, Device device, ComTaskEnablement comTaskEnablement, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, TemporalExpression temporalExpression) {
            super(comTaskExecutionProvider.get());
            this.getComTaskExecution().initialize(device, comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
        }

        @Override
        public ManuallyScheduledComTaskExecution add() {
            ManuallyScheduledComTaskExecution comTaskExecution = super.add();
            DeviceImpl.this.getComTaskExecutionImpls().add((ComTaskExecutionImpl) comTaskExecution);
            return comTaskExecution;
        }
    }

    private enum RegisterFactory {
        Text {
            @Override
            boolean appliesTo(RegisterSpec registerSpec) {
                return registerSpec.isTextual();
            }

            @Override
            RegisterImpl newRegister(DeviceImpl device, RegisterSpec registerSpec) {
                return new TextRegisterImpl(device, registerSpec);
            }
        },

        Billing {
            @Override
            boolean appliesTo(RegisterSpec registerSpec) {
                Set<Aggregate> eventAggregates = EnumSet.of(Aggregate.AVERAGE, Aggregate.SUM, Aggregate.MAXIMUM, Aggregate.SECONDMAXIMUM, Aggregate.THIRDMAXIMUM, Aggregate.FOURTHMAXIMUM, Aggregate.FIFTHMAXIMIMUM, Aggregate.MINIMUM, Aggregate.SECONDMINIMUM);
                return eventAggregates.contains(this.getReadingType(registerSpec).getAggregate());
            }

            @Override
            RegisterImpl newRegister(DeviceImpl device, RegisterSpec registerSpec) {
                return new BillingRegisterImpl(device, registerSpec);
            }
        },

        Flags {
            @Override
            boolean appliesTo(RegisterSpec registerSpec) {
                return this.getReadingType(registerSpec).getUnit().equals(ReadingTypeUnit.BOOLEANARRAY);
            }

            @Override
            RegisterImpl newRegister(DeviceImpl device, RegisterSpec registerSpec) {
                return new FlagsRegisterImpl(device, registerSpec);
            }
        },

        Numerical {
            @Override
            boolean appliesTo(RegisterSpec registerSpec) {
                // When all others fail, use numerical
                return true;
            }

            @Override
            RegisterImpl newRegister(DeviceImpl device, RegisterSpec registerSpec) {
                return new NumericalRegisterImpl(device, registerSpec);
            }
        };

        ReadingType getReadingType(RegisterSpec registerSpec) {
            return registerSpec.getRegisterType().getReadingType();
        }

        abstract boolean appliesTo(RegisterSpec registerSpec);

        abstract RegisterImpl newRegister(DeviceImpl device, RegisterSpec registerSpec);
    }

}
