package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.DatabaseException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceLifeCycleChangeEvent;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exceptions.CannotDeleteComScheduleFromDevice;
import com.energyict.mdc.device.data.exceptions.CannotDeleteComTaskExecutionWhichIsNotFromThisDevice;
import com.energyict.mdc.device.data.exceptions.CannotDeleteConnectionTaskWhichIsNotFromThisDevice;
import com.energyict.mdc.device.data.exceptions.DeviceProtocolPropertyException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.ProtocolDialectConfigurationPropertiesIsRequiredException;
import com.energyict.mdc.device.data.impl.constraintvalidators.DeviceConfigurationIsPresentAndActive;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueComTaskScheduling;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueMrid;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionInitiationTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.FirmwareComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ManuallyScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
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
import com.energyict.mdc.dynamic.relation.CanLock;
import com.energyict.mdc.dynamic.relation.RelationTransaction;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
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
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Functions.asStream;
import static com.energyict.mdc.protocol.pluggable.SecurityPropertySetRelationAttributeTypeNames.DEVICE_ATTRIBUTE_NAME;
import static com.energyict.mdc.protocol.pluggable.SecurityPropertySetRelationAttributeTypeNames.SECURITY_PROPERTY_SET_ATTRIBUTE_NAME;
import static com.energyict.mdc.protocol.pluggable.SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME;
import static java.util.stream.Collectors.toList;

@UniqueMrid(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_DEVICE_MRID + "}")
@UniqueComTaskScheduling(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_COMTASK_SCHEDULING + "}")
public class DeviceImpl implements Device, CanLock {

    private final DataModel dataModel;
    private final EventService eventService;
    private final IssueService issueService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final MeteringService meteringService;
    private final ValidationService validationService;
    private final ServerConnectionTaskService connectionTaskService;
    private final ServerCommunicationTaskService communicationTaskService;
    private final SecurityPropertyService securityPropertyService;
    private final ProtocolPluggableService protocolPluggableService;

    private final List<LoadProfile> loadProfiles = new ArrayList<>();
    private final List<LogBook> logBooks = new ArrayList<>();

    @SuppressWarnings("unused")
    private long id;

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_TYPE_REQUIRED + "}")
    private final Reference<DeviceType> deviceType = ValueReference.absent();
    @DeviceConfigurationIsPresentAndActive(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_CONFIGURATION_REQUIRED + "}")
    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();

    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MRID_REQUIRED + "}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MRID_REQUIRED + "}")
    private String mRID;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String serialNumber;
    @Size(max = 32, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String timeZoneId;
    private TimeZone timeZone;
    private Integer yearOfCertification;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

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
    private final Provider<FirmwareComTaskExecutionImpl> firmwareComTaskExecutionProvider;
    private transient DeviceValidationImpl deviceValidation;
    private final Reference<DeviceEstimation> deviceEstimation = ValueReference.absent();

    @Inject
    public DeviceImpl(
            DataModel dataModel,
            EventService eventService,
            IssueService issueService, Thesaurus thesaurus,
            Clock clock,
            MeteringService meteringService,
            ValidationService validationService,
            ServerConnectionTaskService connectionTaskService,
            ServerCommunicationTaskService communicationTaskService,
            SecurityPropertyService securityPropertyService,
            Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider,
            Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider,
            Provider<ConnectionInitiationTaskImpl> connectionInitiationTaskProvider,
            Provider<ScheduledComTaskExecutionImpl> scheduledComTaskExecutionProvider,
            ProtocolPluggableService protocolPluggableService,
            Provider<ManuallyScheduledComTaskExecutionImpl> manuallyScheduledComTaskExecutionProvider, Provider<FirmwareComTaskExecutionImpl> firmwareComTaskExecutionProvider) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.issueService = issueService;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.meteringService = meteringService;
        this.validationService = validationService;
        this.connectionTaskService = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
        this.securityPropertyService = securityPropertyService;
        this.scheduledConnectionTaskProvider = scheduledConnectionTaskProvider;
        this.inboundConnectionTaskProvider = inboundConnectionTaskProvider;
        this.connectionInitiationTaskProvider = connectionInitiationTaskProvider;
        this.scheduledComTaskExecutionProvider = scheduledComTaskExecutionProvider;
        this.manuallyScheduledComTaskExecutionProvider = manuallyScheduledComTaskExecutionProvider;
        this.firmwareComTaskExecutionProvider = firmwareComTaskExecutionProvider;
        this.protocolPluggableService = protocolPluggableService;
    }

    DeviceImpl initialize(DeviceConfiguration deviceConfiguration, String name, String mRID) {
        this.createTime = this.clock.instant();
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
        boolean alreadyPersistent = this.id > 0;
        if (alreadyPersistent) {
            Save.UPDATE.save(dataModel, this);
            this.saveNewAndDirtyDialectProperties();
        } else {
            Save.CREATE.save(dataModel, this);
            this.createKoreMeter();
            this.saveNewDialectProperties();
        }
        this.saveAllConnectionTasks();
        this.saveAllComTaskExecutions();
        if (alreadyPersistent) {
            this.notifyUpdated();
        }
        else {
            this.notifyCreated();
        }
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
    public void setYearOfCertification(Integer yearOfCertification) {
        this.yearOfCertification = yearOfCertification;
    }
    @Override

    public Integer getYearOfCertification() {
        return this.yearOfCertification;
    }

    public Instant getModificationDate() {
        return this.modTime;
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
                    if (!rs.next()) {
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

    @Override
    public List<ProtocolDialectProperties> getProtocolDialectPropertiesList() {
        List<ProtocolDialectProperties> all = new ArrayList<>(this.dialectPropertiesList.size() + this.newDialectProperties.size());
        all.addAll(this.dialectPropertiesList);
        all.addAll(this.newDialectProperties);
        return all;
    }

    @Override
    public Optional<ProtocolDialectProperties> getProtocolDialectProperties(String dialectName) {
        Optional<ProtocolDialectProperties> dialectProperties = this.getProtocolDialectPropertiesFrom(dialectName, this.dialectPropertiesList);
        if (dialectProperties.isPresent()) {
            return dialectProperties;
        } else {
            // Attempt to find the dialect properties in the list of new ones that have not been saved yet
            return this.getProtocolDialectPropertiesFrom(dialectName, this.newDialectProperties);
        }
    }

    private Optional<ProtocolDialectProperties> getProtocolDialectPropertiesFrom(String dialectName, List<ProtocolDialectProperties> propertiesList) {
        for (ProtocolDialectProperties properties : propertiesList) {
            if (properties.getDeviceProtocolDialectName().equals(dialectName)) {
                return Optional.of(properties);
            }
        }
        return Optional.empty();
    }

    @Override
    public void setProtocolDialectProperty(String dialectName, String propertyName, Object value) {
        Optional<ProtocolDialectProperties> dialectProperties = this.getProtocolDialectProperties(dialectName);
        if (!dialectProperties.isPresent()) {
            ProtocolDialectProperties newDialectProperties = this.createNewLocalDialectProperties(dialectName);
            newDialectProperties.setProperty(propertyName, value);
        } else {
            dialectProperties.get().setProperty(propertyName, value);
            this.dirtyDialectProperties.add(dialectProperties.get());
        }
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
                this.getDeviceConfiguration().getProtocolDialectConfigurationPropertiesList();
        for (ProtocolDialectConfigurationProperties configurationProperties : allConfigurationProperties) {
            if (configurationProperties.getDeviceProtocolDialectName().equals(dialectName)) {
                return configurationProperties;
            }
        }
        return null;
    }

    @Override
    public void removeProtocolDialectProperty(String dialectName, String propertyName) {
        Optional<ProtocolDialectProperties> dialectProperties = this.getProtocolDialectProperties(dialectName);
        if (dialectProperties.isPresent()) {
            dialectProperties.get().removeProperty(propertyName);
        } else {
            createNewLocalDialectProperties(dialectName);
        }
        if ((dialectProperties.isPresent()) && !this.dirtyDialectProperties.contains(dialectProperties.get())) {
            this.dirtyDialectProperties.add(dialectProperties.get());
        }
    }

    @Override
    public void setProtocolProperty(String name, Object value) {
        Optional<PropertySpec> optionalPropertySpec = getPropertySpecForProperty(name);
        if (optionalPropertySpec.isPresent()) {
            String propertyValue = optionalPropertySpec.get().getValueFactory().toStringValue(value);
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
            transaction.setFrom(clock.instant());
            transaction.setTo(null);
            transaction.set(DEVICE_ATTRIBUTE_NAME, this);
            transaction.set(SECURITY_PROPERTY_SET_ATTRIBUTE_NAME, securityPropertySet);
            typedProperties.propertyNames().stream().forEach(p -> transaction.set(p, typedProperties.getLocalValue(p)));
            transaction.set(STATUS_ATTRIBUTE_NAME, isSecurityPropertySetComplete(securityPropertySet,typedProperties));
            transaction.execute();
        } catch (BusinessException | SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isSecurityPropertySetComplete(SecurityPropertySet securityPropertySet, TypedProperties typedProperties){
        return !securityPropertySet.getPropertySpecs()
                    .stream()
                    .anyMatch(p -> p.isRequired() && !typedProperties.hasLocalValueFor(p.getName()));
    }

    private void addDeviceProperty(String name, String propertyValue) {
        if (propertyValue != null) {
            DeviceProtocolPropertyImpl deviceProtocolProperty = this.dataModel.getInstance(DeviceProtocolPropertyImpl.class).initialize(this, name, propertyValue);
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
    public void removeProtocolProperty(String name) {
        for (DeviceProtocolProperty deviceProtocolProperty : deviceProperties) {
            if (deviceProtocolProperty.getName().equals(name)) {
                this.deviceProperties.remove(deviceProtocolProperty);
                break;
            }
        }
    }

    @Override
    public TypedProperties getDeviceProtocolProperties() {
        TypedProperties properties = TypedProperties.inheritingFrom(this.getDeviceConfiguration().getDeviceProtocolProperties().getTypedProperties());
        this.addLocalProperties(properties, this.getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs());
        return properties;
    }


    private Optional<PropertySpec> getPropertySpecForProperty(String name) {
        return this.getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs().stream().filter(spec->spec.getName().equals(name)).findFirst();
    }

    private void addLocalProperties(TypedProperties properties,  List<PropertySpec> propertySpecs) {
        for (PropertySpec propertySpec : propertySpecs) {
            DeviceProtocolProperty deviceProtocolProperty = findDevicePropertyFor(propertySpec);
            if (deviceProtocolProperty != null) {
                properties.setProperty(deviceProtocolProperty.getName(), propertySpec.getValueFactory().fromStringValue(deviceProtocolProperty.getPropertyValue()));
            }
        }
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

    @Override
    public Optional<UsagePoint> getUsagePoint() {
        return this.getOptionalMeterAspect(this::getUsagePointFromMeterActivation);
    }

    private Optional<UsagePoint> getUsagePointFromMeterActivation(Meter meter) {
        Optional<? extends MeterActivation> currentMeterActivation = meter.getCurrentMeterActivation();
        if (currentMeterActivation.isPresent()) {
            return currentMeterActivation.get().getUsagePoint();
        }
        else {
            return Optional.empty();
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
        return this.createKoreMeter(this.getMdcAmrSystem().orElseThrow(this.mdcAMRSystemDoesNotExist()));
    }

    private Supplier<RuntimeException> mdcAMRSystemDoesNotExist() {
        return () -> new RuntimeException("The MDC AMR system does not exist");
    }

    private Supplier<IllegalStateException> noMeterActivationAt(Instant timestamp) {
        return () -> new IllegalStateException("No meter activation found on " + timestamp);
    }

    Meter createKoreMeter(AmrSystem amrSystem) {
        FiniteStateMachine stateMachine = this.getDeviceType().getDeviceLifeCycle().getFiniteStateMachine();
        Meter meter = amrSystem.newMeter(stateMachine, String.valueOf(getId()), getmRID());
        meter.setSerialNumber(getSerialNumber());
        meter.getLifecycleDates().setReceivedDate(this.clock.instant());
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
        return this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
    }

    List<ReadingRecord> getReadingsFor(Register<?> register, Range<Instant> interval) {
        return this.getListMeterAspect(meter -> this.getReadingsFor(register, interval, meter));
    }

    private List<ReadingRecord> getReadingsFor(Register<?> register, Range<Instant> interval, Meter meter) {
        List<? extends BaseReadingRecord> readings = meter.getReadings(interval, register.getRegisterSpec().getRegisterType().getReadingType());
        return readings
                .stream()
                .map(ReadingRecord.class::cast)
                .collect(Collectors.toList());
    }

    List<LoadProfileReading> getChannelData(LoadProfile loadProfile, Range<Instant> interval) {
        Optional<AmrSystem> amrSystem = getMdcAmrSystem();
        List<LoadProfileReading> loadProfileReadings = Collections.emptyList();
        boolean meterHasData = false;
        if (amrSystem.isPresent()) {
            Optional<Meter> meter = this.findKoreMeter(amrSystem.get());
            if (meter.isPresent()) {
                Map<Instant, LoadProfileReadingImpl> sortedLoadProfileReadingMap =
                        getPreFilledLoadProfileReadingMap(
                                loadProfile,
                                interval,
                                meter.get());
                Range<Instant> clipped = Ranges.openClosed(interval.lowerEndpoint(), this.lastReadingClipped(loadProfile, interval));
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

    List<LoadProfileReading> getChannelData(Channel channel, Range<Instant> interval) {
        Optional<AmrSystem> amrSystem = getMdcAmrSystem();
        List<LoadProfileReading> loadProfileReadings = Collections.emptyList();
        boolean meterHasData;
        if (amrSystem.isPresent()) {
            Optional<Meter> meter = this.findKoreMeter(amrSystem.get());
            if (meter.isPresent()) {
                Map<Instant, LoadProfileReadingImpl> sortedLoadProfileReadingMap =
                        getPreFilledLoadProfileReadingMap(
                                channel.getLoadProfile(),
                                interval,
                                meter.get());
                Range<Instant> clipped = Ranges.openClosed(meterActivationClipped(meter.get(), interval), lastReadingClipped(channel.getLoadProfile(), interval));
                meterHasData = this.addChannelDataToMap(clipped, meter.get(), channel, sortedLoadProfileReadingMap);
                if (meterHasData) {
                    loadProfileReadings = new ArrayList<>(sortedLoadProfileReadingMap.values());
                }
            }
        }
        return Lists.reverse(loadProfileReadings);
    }

    public List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter) {
        return this.getListMeterAspect(meter -> meter.getDeviceEventsByFilter(filter));
    }

    /**
     * Adds meter readings for a single channel to the timeslot-map.
     *
     * @param interval                    The interval over which meter readings are requested
     * @param meter                       The meter for which readings are requested
     * @param mdcChannel                  The meter's channel for which readings are requested
     * @param sortedLoadProfileReadingMap The map to add the readings too in the correct timeslot
     * @return true if any readings were added to the map, false otherwise
     */
    private boolean addChannelDataToMap(Range<Instant> interval, Meter meter, Channel mdcChannel, Map<Instant, LoadProfileReadingImpl> sortedLoadProfileReadingMap) {
        boolean meterHasData = false;
        List<MeterActivation> meterActivations = this.getSortedMeterActivations(meter, Ranges.closed(interval.lowerEndpoint(), interval.upperEndpoint()));
        for (MeterActivation meterActivation : meterActivations) {
            Range<Instant> meterActivationInterval = meterActivation.getRange().intersection(interval);
            meterHasData |= meterActivationInterval.lowerEndpoint()!=meterActivationInterval.upperEndpoint();
            ReadingType readingType = mdcChannel.getChannelSpec().getReadingType();
            List<IntervalReadingRecord> meterReadings = (List<IntervalReadingRecord>) meter.getReadings(meterActivationInterval, readingType);
            for (IntervalReadingRecord meterReading : meterReadings) {
                LoadProfileReadingImpl loadProfileReading = sortedLoadProfileReadingMap.get(meterReading.getTimeStamp());
                loadProfileReading.setChannelData(mdcChannel, meterReading);
                loadProfileReading.setFlags(getFlagsFromProfileStatus(meterReading.getProfileStatus()));
                loadProfileReading.setReadingTime(meterReading.getReportedDateTime());
            }

            Optional<com.elster.jupiter.metering.Channel> koreChannel = this.getChannel(meterActivation, readingType);
            if (koreChannel.isPresent()) {
                List<DataValidationStatus> validationStatus = forValidation().getValidationStatus(mdcChannel, meterReadings, meterActivationInterval);
                validationStatus.stream()
                        .filter(s -> s.getReadingTimestamp().isAfter(meterActivationInterval.lowerEndpoint()))
                        .forEach(s -> {
                            LoadProfileReadingImpl loadProfileReading = sortedLoadProfileReadingMap.get(s.getReadingTimestamp());
                            if (loadProfileReading != null) {
                                loadProfileReading.setDataValidationStatus(mdcChannel, s);
                                //code below is the processing of removed readings
                                Optional<? extends ReadingQuality> readingQuality = s.getReadingQualities()
                                        .stream()
                                        .filter(rq -> rq.getType().equals(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.REJECTED)))
                                        .findAny();
                                if (readingQuality.isPresent()) {
                                    loadProfileReading.setReadingTime(((ReadingQualityRecord) readingQuality.get()).getTimestamp());
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
     * just a list of placeholders for each reading interval within the requestedInterval for all timestamps
     * that occur with the bounds of a meter activation and load profile's last reading.
     *
     * @param loadProfile     The LoadProfile
     * @param requestedInterval interval over which user wants to see readings
     * @param meter           The Meter
     * @return The map
     */
    private Map<Instant, LoadProfileReadingImpl> getPreFilledLoadProfileReadingMap(LoadProfile loadProfile, Range<Instant> requestedInterval, Meter meter) {
        // TODO: what if there are gaps in the meter activations
        Map<Instant, LoadProfileReadingImpl> loadProfileReadingMap = new TreeMap<>();
        TemporalAmount intervalLength = this.intervalLength(loadProfile);
        List<MeterActivation> allMeterActivations = new ArrayList<>(meter.getMeterActivations());
        allMeterActivations
            .stream()
            .filter(ma -> ma.overlaps(requestedInterval))
            .forEach(affectedMeterActivation -> {
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
                    value.setRange(Ranges.openClosed(requestStart.toInstant(), readingTimestamp.toInstant()));
                    loadProfileReadingMap.put(readingTimestamp.toInstant(), value);
                    requestStart = readingTimestamp;
                }
            });
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
                        .truncatedTo(this.truncationUnit(loadProfile));    // round start time to interval boundary
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
                return prefilledIntervalStartWithIntervalMonth(loadProfile,zoneId,requestedIntervalClippedToMeterActivation);

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
         * and then increment with interval length until start > meter activation start
         * to cater for the situation where meter activation is e.g. 8h43 with interval length of 15mins
         * where truncating would start at 8h00
         * So in case meter activation is at 8h13, the first reported interval will be (8:00,8:15]
         *    in case meter activation is exactly 8h15 , the first reported interval will be (8:15,8:30]
         */
        ZonedDateTime nextAttempt =
                ZonedDateTime
                    .ofInstant(
                        requestedIntervalClippedToMeterActivation.lowerEndpoint(),
                        zoneId)
                    .truncatedTo(this.truncationUnit(loadProfile));    // round start time to interval boundary
        ZonedDateTime latestAttemptBefore = nextAttempt;

        while (nextAttempt.toInstant().isBefore(requestedIntervalClippedToMeterActivation.lowerEndpoint()) || nextAttempt.toInstant().equals(requestedIntervalClippedToMeterActivation.lowerEndpoint())) {
            latestAttemptBefore = nextAttempt;
            nextAttempt = nextAttempt.plus(this.intervalLength(loadProfile));
        }
        return latestAttemptBefore;
    }

    private ZonedDateTime prefilledIntervalStartWithIntervalMonth(LoadProfile loadProfile, ZoneId zoneId, Range<Instant> requestedIntervalClippedToMeterActivation) {
        /* Implementation note: truncate meter activation end point of the interval to the interval length
         * and then increment with interval length until start > meter activation start
         * to cater for the situation where meter activation is e.g. 8h43 with interval length of 15mins
         * where truncating would start at 8h00
         * So in case meter activation is at 8h13, the first reported interval will be (8:00,8:15]
         *    in case meter activation is exactly 8h15 , the first reported interval will be (8:15,8:30]
         */
        ZonedDateTime nextAttempt =
                ZonedDateTime
                    .ofInstant(
                        requestedIntervalClippedToMeterActivation.lowerEndpoint(),
                        zoneId)
                    .with(ChronoField.DAY_OF_MONTH, 1).toLocalDate().atStartOfDay(zoneId);

        while (nextAttempt.toInstant().isAfter(requestedIntervalClippedToMeterActivation.lowerEndpoint()) || nextAttempt.toInstant().equals(requestedIntervalClippedToMeterActivation.lowerEndpoint())) {
            nextAttempt = nextAttempt.minus(this.intervalLength(loadProfile));
        }
        return nextAttempt;
    }

    private TemporalUnit truncationUnit(LoadProfile loadProfile) {
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

    private Instant meterActivationClipped(Meter meter, Range<Instant> interval) {
        if (meter.getCurrentMeterActivation().isPresent() && interval.contains(meter.getCurrentMeterActivation().get().getStart())) {
            return meter.getCurrentMeterActivation().get().getStart();
        }
        else {
            return interval.lowerEndpoint();
        }
    }

    private Instant lastReadingClipped(LoadProfile loadProfile, Range<Instant> interval) {
        if (loadProfile.getLastReading().isPresent()) {
            if (interval.contains(loadProfile.getLastReading().get())) {
                return loadProfile.getLastReading().get();
            } else if (interval.upperEndpoint().isBefore(loadProfile.getLastReading().get())) {
                return interval.upperEndpoint();
            } else {
                return interval.lowerEndpoint(); // empty interval: interval is completely after last reading
            }
        }
        return interval.upperEndpoint();
    }

    Optional<ReadingRecord> getLastReadingFor(Register<?> register) {
        return this.getOptionalMeterAspect(meter -> this.getLastReadingsFor(register, meter));
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

    @Override
    public boolean hasData() {
        return this.getOptionalMeterAspect(this::hasData).get();
    }

    private Optional<Boolean> hasData(Meter meter) {
        if (meter.hasData()) {
            return Optional.of(true);
        }
        else {
            return Optional.of(false);
        }
    }

    boolean hasData(Channel channel) {
        return this.hasData(this.findKoreChannels(channel));
    }

    boolean hasData(Register<?> register) {
        return this.hasData(this.findKoreChannels(register));
    }

    private boolean hasData(List<com.elster.jupiter.metering.Channel> channels) {
        return channels
                .stream()
                .anyMatch(com.elster.jupiter.metering.Channel::hasData);
    }

    @Override
    public MeterActivation activate(Instant start) {
        AmrSystem amrSystem = this.getMdcAmrSystem().orElseThrow(this.mdcAMRSystemDoesNotExist());
        return this.findOrCreateKoreMeter(amrSystem).activate(start);
    }

    @Override
    public void deactivate(Instant when) {
        this.getCurrentMeterActivation().ifPresent(meterActivation -> meterActivation.endAt(when));
    }

    @Override
    public void deactivateNow() {
        this.deactivate(this.clock.instant());
    }

    @Override
    public Optional<MeterActivation> getCurrentMeterActivation() {
        return this.getOptionalMeterAspect(m -> m.getCurrentMeterActivation().map(Function.<MeterActivation>identity()));
    }

    private <AT> Optional<AT> getOptionalMeterAspect(Function<Meter, Optional<AT>> aspectFunction) {
        Optional<AmrSystem> amrSystem = this.getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            Optional<Meter> meter = this.findKoreMeter(amrSystem.get());
            if (meter.isPresent()) {
                return aspectFunction.apply(meter.get());
            }
            else {
                return Optional.empty();
            }
        }
        else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private <AT> List<AT> getListMeterAspect(Function<Meter, List<AT>> aspectFunction) {
        Optional<AmrSystem> amrSystem = this.getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            Optional<Meter> meter = this.findKoreMeter(amrSystem.get());
            if (meter.isPresent()) {
                return aspectFunction.apply(meter.get());
            }
            else {
                return Collections.emptyList();
            }
        }
        else {
            return Collections.emptyList();
        }
    }

    List<MeterActivation> getMeterActivations() {
        return this.getListMeterAspect(this::getMeterActivations);
    }

    private List<MeterActivation> getMeterActivations(Meter meter) {
        return new ArrayList<>(meter.getMeterActivations());
    }

    @Override
    public List<MeterActivation> getMeterActivationsMostRecentFirst() {
        return this.getListMeterAspect(this::getSortedMeterActivations);
    }

    /**
     * Sorts the {@link MeterActivation}s of the specified {@link Meter}
     * where the most recent activations are returned first.
     *
     * @param meter The Meter
     * @return The List of MeterActivation
     */
    private List<MeterActivation> getSortedMeterActivations(Meter meter) {
        List<MeterActivation> meterActivations = new ArrayList<>(meter.getMeterActivations());    // getMeterActivations returns ImmutableList
        Collections.reverse(meterActivations);
        return meterActivations;
    }

    /**
     * Ensures that there is a MeterActivation at the specified instant in time.
     *
     * @param when The Instant in time
     */
    void ensureActiveOn(Instant when) {
        Optional<AmrSystem> amrSystem = this.getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            Meter meter = this.findKoreMeter(amrSystem.get()).get();
            if (meter.getMeterActivations().isEmpty()) {
                meter.activate(when);
            }
        }
        else {
            throw this.mdcAMRSystemDoesNotExist().get();
        }
    }

    Optional<MeterActivation> getMeterActivation(Instant when) {
        return this.getOptionalMeterAspect(meter -> meter.getMeterActivation(when).map(MeterActivation.class::cast));
    }

    Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Channel channel, Instant when) {
        return findKoreChannel(channel::getReadingType, when);
    }

    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Supplier<ReadingType> readingTypeSupplier, Instant when) {
        return this.getOptionalMeterAspect(meter -> this.findKoreChannel(meter, readingTypeSupplier, when));
    }

    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Meter meter, Supplier<ReadingType> readingTypeSupplier, Instant when) {
        Optional<? extends MeterActivation> meterActivation = meter.getMeterActivation(when);
        if (meterActivation.isPresent()) {
            return Optional.ofNullable(getChannel(meterActivation.get(), readingTypeSupplier.get()).orElse(null));
        }
        else {
            return Optional.empty();
        }
    }

    List<com.elster.jupiter.metering.Channel> findKoreChannels(Channel channel) {
        return findKoreChannels(channel::getReadingType);
    }

    List<com.elster.jupiter.metering.Channel> findKoreChannels(Register<?> register) {
        return findKoreChannels(register::getReadingType);
    }

    List<com.elster.jupiter.metering.Channel> findKoreChannels(Supplier<ReadingType> readingTypeSupplier) {
        return this.getListMeterAspect(meter -> this.findKoreChannels(readingTypeSupplier, meter));
    }

    com.elster.jupiter.metering.Channel findOrCreateKoreChannel(Instant when, Register<?> register) {
        Optional<MeterActivation> meterActivation = this.getMeterActivation(when);
        if (meterActivation.isPresent()) {
            return this.getChannel(meterActivation.get(), register.getReadingType())
                  .orElse(meterActivation.get().createChannel(register.getReadingType()));
        }
        else {
            throw this.noMeterActivationAt(when).get();
        }
    }

    private List<com.elster.jupiter.metering.Channel> findKoreChannels(Supplier<ReadingType> readingTypeSupplier, Meter meter) {
        return meter.getMeterActivations().stream()
                .map(m -> getChannel(m, readingTypeSupplier.get()))
                .flatMap(asStream())
                .collect(Collectors.toList());
    }

    private Optional<com.elster.jupiter.metering.Channel> getChannel(MeterActivation meterActivation, ReadingType readingType) {
        return meterActivation.getChannels().stream().filter(channel -> channel.getReadingTypes().contains(readingType)).findFirst();
    }

    /**
     * Sorts the {@link MeterActivation}s of the specified {@link Meter}
     * that overlap with the {@link Interval}, where the most recent activations are returned first.
     *
     * @param meter    The Meter
     * @param interval The Interval
     * @return The List of MeterActivation
     */
    private List<MeterActivation> getSortedMeterActivations(Meter meter, Range<Instant> interval) {
        List<MeterActivation> overlapping = new ArrayList<>();
        meter.getMeterActivations()
                .stream()
                .filter(activation -> activation.overlaps(interval))
                .forEach(overlapping::add);
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
        return new ArrayList<>(this.getConnectionTaskImpls());
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
        return new ArrayList<>(this.getComTaskExecutionImpls());
    }

    private List<ComTaskExecutionImpl> getComTaskExecutionImpls() {
        return this.getAllComTaskExecutionImpls()
                .stream()
                .filter(cte -> !cte.isObsolete())
                .collect(Collectors.toList());
    }

    private List<ComTaskExecutionImpl> getAllComTaskExecutionImpls() {
        this.ensureComTaskExecutionsLoaded();
        return this.comTaskExecutions;
    }

    private void ensureComTaskExecutionsLoaded() {
        if (this.comTaskExecutions == null) {
            this.loadComTaskExecutions();
        }
    }

    private void loadComTaskExecutions() {
        this.comTaskExecutions =
            this.communicationTaskService
                .findComTaskExecutionsByDevice(this)
                .stream()
                .map(comTaskExecution -> (ComTaskExecutionImpl) comTaskExecution)
                .collect(Collectors.toList());
    }

    private void add(ComTaskExecutionImpl comTaskExecution) {
        this.ensureComTaskExecutionsLoaded();
        this.comTaskExecutions.add(comTaskExecution);
        if (this.id != 0) {
            Save.CREATE.validate(this.dataModel, this);  // To validate that all scheduled ComTasks are unique
        }
    }

    @Override
    public ComTaskExecutionBuilder<ScheduledComTaskExecution> newScheduledComTaskExecution(ComSchedule comSchedule) {
        return new ScheduledComTaskExecutionBuilderForDevice(scheduledComTaskExecutionProvider, this, comSchedule);
    }

    @Override
    public AdHocComTaskExecutionBuilderForDevice newAdHocComTaskExecution(ComTaskEnablement comTaskEnablement) {
        return new AdHocComTaskExecutionBuilderForDevice(manuallyScheduledComTaskExecutionProvider, this, comTaskEnablement);
    }

    @Override
    public ComTaskExecutionBuilder<FirmwareComTaskExecution> newFirmwareComTaskExecution(ComTaskEnablement comTaskEnablement) {
        return new FirmwareComTaskExecutionBuilderForDevice(firmwareComTaskExecutionProvider, this, comTaskEnablement);
    }

    @Override
    public ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> newManuallyScheduledComTaskExecution(ComTaskEnablement comTaskEnablement, TemporalExpression temporalExpression) {
        return new ManuallyScheduledComTaskExecutionBuilderForDevice(
                this.manuallyScheduledComTaskExecutionProvider,
                this,
                comTaskEnablement,
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
    public FirmwareComTaskExecutionUpdater getComTaskExecutionUpdater(FirmwareComTaskExecution comTaskExecution) {
        return comTaskExecution.getUpdater();
    }

    @Override
    public void removeComTaskExecution(ComTaskExecution comTaskExecution) {
        this.ensureComTaskExecutionsLoaded();
        Iterator<ComTaskExecutionImpl> comTaskExecutionIterator = this.comTaskExecutions.iterator();
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
        this.ensureComTaskExecutionsLoaded();
        Iterator<ComTaskExecutionImpl> comTaskExecutionIterator = this.comTaskExecutions.iterator();
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
        return this.securityPropertyService.getSecurityPropertiesIgnoringPrivileges(this, when, securityPropertySet);
    }

    @Override
    public boolean hasSecurityProperties(SecurityPropertySet securityPropertySet) {
        return this.hasSecurityProperties(clock.instant(), securityPropertySet);
    }

    @Override
    public boolean securityPropertiesAreValid() {
        return this.securityPropertyService.securityPropertiesAreValid(this);
    }

    @Override
    public DeviceValidation forValidation() {
        if (deviceValidation == null) {
            deviceValidation = new DeviceValidationImpl(getMdcAmrSystem().get(), validationService, clock, this);
        }
        return deviceValidation;
    }

    @Override
    public DeviceEstimation forEstimation() {
        return deviceEstimation.orElseGet(() -> {
           DeviceEstimation deviceEstimation = dataModel.getInstance(DeviceEstimationImpl.class).init(this, false);
           this.deviceEstimation.set(deviceEstimation);
           return deviceEstimation;
        });
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

    @Override
    public void addToGroup(EnumeratedEndDeviceGroup enumeratedEndDeviceGroup, Range<Instant> range) {
        Optional<AmrSystem> amrSystem = this.getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            enumeratedEndDeviceGroup.add(this.findOrCreateKoreMeter(amrSystem.get()), range);
        }
    }

    @Override
    public boolean hasOpenIssues() {
        return this.getOptionalMeterAspect(this::hasOpenIssues).get();
    }

    public Optional<Boolean> hasOpenIssues(Meter meter) {
        List<OpenIssue> openIssues = this.issueService.query(OpenIssue.class).select(where("device").isEqualTo(meter));
        if (openIssues.isEmpty()) {
            return Optional.of(false);
        }
        else {
            return Optional.of(true);
        }
    }

    @Override
    public State getState() {
        return this.getState(this.clock.instant()).get();
    }

    @Override
    public Optional<State> getState(Instant instant) {
        Optional<AmrSystem> amrSystem = getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            if (this.id > 0) {
                Optional<Meter> meter = this.findKoreMeter(amrSystem.get());
                if (meter.isPresent()) {
                    return meter.get().getState(instant);
                }
                else {
                    // Kore meter was not created yet
                    throw new IllegalStateException("Kore meter was not created when this Device was created");
                }
            }
            else {
                return Optional.of(this.getDeviceType().getDeviceLifeCycle().getFiniteStateMachine().getInitialState());
            }
        }
        else {
            throw new IllegalStateException("MDC AMR system does not exist");
        }
    }

    @Override
    public StateTimeline getStateTimeline() {
        return this.getOptionalMeterAspect(EndDevice::getStateTimeline).get();
    }

    @Override
    public LifecycleDates getLifecycleDates() {
        Optional<AmrSystem> amrSystem = this.getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            Optional<Meter> meter = this.findKoreMeter(amrSystem.get());
            if (meter.isPresent()) {
                return meter.get().getLifecycleDates();
            }
            else {
                return new NoCimLifeCycleDates();
            }
        }
        else {
            return new NoCimLifeCycleDates();
        }
    }

    @Override
    public List<DeviceLifeCycleChangeEvent> getDeviceLifeCycleChangeEvents() {
        // Merge the StateTimeline with the list of change events from my DeviceType.
        Deque<StateTimeSlice> stateTimeSlices = new LinkedList<>(this.getStateTimeline().getSlices());
        Deque<com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent> deviceTypeChangeEvents = new LinkedList<>(this.getDeviceTypeLifeCycleChangeEvents());
        List<DeviceLifeCycleChangeEvent> changeEvents = new ArrayList<>();
        boolean ready;
        do {
            DeviceLifeCycleChangeEvent newEvent = this.newEventForMostRecent(stateTimeSlices, deviceTypeChangeEvents);
            changeEvents.add(newEvent);
            ready = stateTimeSlices.isEmpty() && deviceTypeChangeEvents.isEmpty();
        } while (!ready);
        return changeEvents;
    }

    private List<com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent> getDeviceTypeLifeCycleChangeEvents() {
        return this.getDeviceType()
                .getDeviceLifeCycleChangeEvents()
                .stream()
                .filter(each -> each.getTimestamp().isAfter(this.createTime))
                .collect(Collectors.toList());
    }

    private DeviceLifeCycleChangeEvent newEventForMostRecent(Deque<StateTimeSlice> stateTimeSlices, Deque<com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent> deviceTypeChangeEvents) {
        if (stateTimeSlices.isEmpty()) {
            return DeviceLifeCycleChangeEventImpl.from(deviceTypeChangeEvents.removeFirst());
        }
        else if (deviceTypeChangeEvents.isEmpty()) {
            return DeviceLifeCycleChangeEventImpl.from(stateTimeSlices.removeFirst());
        }
        else {
            // Compare both timestamps and create event from the most recent one
            StateTimeSlice stateTimeSlice = stateTimeSlices.peekFirst();
            com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent deviceLifeCycleChangeEvent = deviceTypeChangeEvents.peekFirst();
            if (stateTimeSlice.getPeriod().lowerEndpoint().equals(deviceLifeCycleChangeEvent.getTimestamp())) {
                // Give precedence to the device life cycle change but also consume the state change so the latter is ignored
                stateTimeSlices.removeFirst();
                return DeviceLifeCycleChangeEventImpl.from(deviceTypeChangeEvents.removeFirst());
            }
            else if (stateTimeSlice.getPeriod().lowerEndpoint().isBefore(deviceLifeCycleChangeEvent.getTimestamp())) {
                return DeviceLifeCycleChangeEventImpl.from(stateTimeSlices.removeFirst());
            }
            else {
                return DeviceLifeCycleChangeEventImpl.from(deviceTypeChangeEvents.removeFirst());
            }
        }
    }

    @Override
    public long getVersion() {
        return version;
    }
    
    @Override
    public Instant getCreateTime() {
        return createTime;
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

        private AdHocComTaskExecutionBuilderForDevice(Provider<ManuallyScheduledComTaskExecutionImpl> comTaskExecutionProvider, Device device, ComTaskEnablement comTaskEnablement) {
            super(comTaskExecutionProvider.get());
            this.getComTaskExecution().initializeAdhoc(device, comTaskEnablement);
        }


        @Override
        public ManuallyScheduledComTaskExecution add() {
            ManuallyScheduledComTaskExecution comTaskExecution = super.add();
            DeviceImpl.this.add((ComTaskExecutionImpl) comTaskExecution);
            return comTaskExecution;
        }
    }

    public class FirmwareComTaskExecutionBuilderForDevice extends FirmwareComTaskExecutionImpl.FirmwareComTaskExecutionBuilderImpl {

        private FirmwareComTaskExecutionBuilderForDevice(Provider<FirmwareComTaskExecutionImpl> comTaskExecutionProvider, Device device, ComTaskEnablement comTaskEnablement) {
            super(comTaskExecutionProvider.get());
            this.getComTaskExecution().initializeFirmwareTask(device, comTaskEnablement);
        }

        @Override
        public FirmwareComTaskExecution add() {
            FirmwareComTaskExecution firmwareComTaskExecution = super.add();
            DeviceImpl.this.add((ComTaskExecutionImpl) firmwareComTaskExecution);
            return firmwareComTaskExecution;
        }
    }

    public class ManuallyScheduledComTaskExecutionBuilderForDevice
            extends ManuallyScheduledComTaskExecutionImpl.ManuallyScheduledComTaskExecutionBuilderImpl {

        private ManuallyScheduledComTaskExecutionBuilderForDevice(Provider<ManuallyScheduledComTaskExecutionImpl> comTaskExecutionProvider, Device device, ComTaskEnablement comTaskEnablement, TemporalExpression temporalExpression) {
            super(comTaskExecutionProvider.get());
            this.getComTaskExecution().initialize(device, comTaskEnablement, temporalExpression);
        }

        @Override
        public ManuallyScheduledComTaskExecution add() {
            ManuallyScheduledComTaskExecution comTaskExecution = super.add();
            DeviceImpl.this.add((ComTaskExecutionImpl) comTaskExecution);
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

    private class NoCimLifeCycleDates implements LifecycleDates {
        @Override
        public Optional<Instant> getManufacturedDate() {
            return Optional.empty();
        }

        @Override
        public void setManufacturedDate(Instant manufacturedDate) {
            // Ignore blissfully
        }

        @Override
        public Optional<Instant> getPurchasedDate() {
            return Optional.empty();
        }

        @Override
        public void setPurchasedDate(Instant purchasedDate) {
            // Ignore blissfully
        }

        @Override
        public Optional<Instant> getReceivedDate() {
            return Optional.empty();
        }

        @Override
        public void setReceivedDate(Instant receivedDate) {
            // Ignore blissfully
        }

        @Override
        public Optional<Instant> getInstalledDate() {
            return Optional.empty();
        }

        @Override
        public void setInstalledDate(Instant installedDate) {
            // Ignore blissfully
        }

        @Override
        public Optional<Instant> getRemovedDate() {
            return Optional.empty();
        }

        @Override
        public void setRemovedDate(Instant removedDate) {
            // Ignore blissfully
        }

        @Override
        public Optional<Instant> getRetiredDate() {
            return Optional.empty();
        }

        @Override
        public void setRetiredDate(Instant retiredDate) {
            // Ignore blissfully
        }
    }
}
