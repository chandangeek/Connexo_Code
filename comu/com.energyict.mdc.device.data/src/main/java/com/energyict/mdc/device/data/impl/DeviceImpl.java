package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.TemporalReference;
import com.elster.jupiter.orm.associations.Temporals;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.elster.jupiter.util.streams.Predicates;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceLifeCycleChangeEvent;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.PassiveCalendar;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.ReadingTypeObisCodeUsage;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exceptions.CannotChangeDeviceConfigStillUnresolvedConflicts;
import com.energyict.mdc.device.data.exceptions.CannotDeleteComScheduleFromDevice;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;
import com.energyict.mdc.device.data.exceptions.DeviceProtocolPropertyException;
import com.energyict.mdc.device.data.exceptions.MultiplierConfigurationException;
import com.energyict.mdc.device.data.exceptions.NoMeterActivationAt;
import com.energyict.mdc.device.data.exceptions.NoStatusInformationTaskException;
import com.energyict.mdc.device.data.exceptions.ProtocolDialectConfigurationPropertiesIsRequiredException;
import com.energyict.mdc.device.data.impl.configchange.ServerDeviceForConfigChange;
import com.energyict.mdc.device.data.impl.configchange.ServerSecurityPropertyServiceForConfigChange;
import com.energyict.mdc.device.data.impl.constraintvalidators.DeviceConfigurationIsPresentAndActive;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueComTaskScheduling;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueMrid;
import com.energyict.mdc.device.data.impl.constraintvalidators.ValidOverruledAttributes;
import com.energyict.mdc.device.data.impl.constraintvalidators.ValidSecurityProperties;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.security.ServerDeviceForValidation;
import com.energyict.mdc.device.data.impl.sync.SyncDeviceWithKoreForInfo;
import com.energyict.mdc.device.data.impl.sync.SyncDeviceWithKoreForRemoval;
import com.energyict.mdc.device.data.impl.sync.SyncDeviceWithKoreForSimpleUpdate;
import com.energyict.mdc.device.data.impl.sync.SynchDeviceWithKoreForConfigurationChange;
import com.energyict.mdc.device.data.impl.sync.SynchDeviceWithKoreForMultiplierChange;
import com.energyict.mdc.device.data.impl.sync.SynchDeviceWithKoreForUsagePointChange;
import com.energyict.mdc.device.data.impl.sync.SynchNewDeviceWithKore;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionInitiationTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.FirmwareComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ManuallyScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTask;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskPropertyProvider;
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
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.TrackingCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.FirmwareManagementTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tasks.TopologyTask;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.math.BigDecimal;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.stream.Collectors.toList;

@LiteralSql
@UniqueMrid(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_DEVICE_MRID + "}")
@UniqueComTaskScheduling(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_COMTASK + "}")
@DeviceImpl.HasValidShipmentDate(groups = {Save.Create.class})
@ValidSecurityProperties(groups = {Save.Update.class})
@ValidOverruledAttributes(groups = {Save.Update.class})
public class DeviceImpl implements Device, ServerDeviceForConfigChange, ServerDeviceForValidation, ServerDevice {

    private static final BigDecimal maxMultiplier = BigDecimal.valueOf(Integer.MAX_VALUE);

    private final DataModel dataModel;
    private final EventService eventService;
    private final IssueService issueService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final MeteringService meteringService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final ValidationService validationService;
    private final SecurityPropertyService securityPropertyService;
    private final MeteringGroupsService meteringGroupsService;
    private final CustomPropertySetService customPropertySetService;

    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final ThreadPrincipalService threadPrincipalService;
    private final UserPreferencesService userPreferencesService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final List<LoadProfile> loadProfiles = new ArrayList<>();
    private final List<LogBook> logBooks = new ArrayList<>();

    @SuppressWarnings("unused")
    private long id;
    private List<ReadingTypeObisCodeUsageImpl> readingTypeObisCodeUsages = new ArrayList<>();
    private final Reference<DeviceType> deviceType = ValueReference.absent();
    @DeviceConfigurationIsPresentAndActive(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();

    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String mRID;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String serialNumber;
    @Size(max = 32, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String timeZoneId;
    private ZoneId zoneId;
    private Integer yearOfCertification;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    private Reference<Meter> meter = ValueReference.absent();

    @Valid
    private List<DeviceProtocolProperty> deviceProperties = new ArrayList<>();
    @Valid
    private List<ConnectionTaskImpl<?, ?>> connectionTasks = new ArrayList<>();
    @Valid
    private List<ComTaskExecutionImpl> comTaskExecutions = new ArrayList<>();
    @Valid
    private List<DeviceMessageImpl> deviceMessages = new ArrayList<>();

    private List<ProtocolDialectPropertiesImpl> dialectPropertiesList = new ArrayList<>();
    private List<ProtocolDialectPropertiesImpl> newDialectProperties = new ArrayList<>();
    private List<ProtocolDialectPropertiesImpl> dirtyDialectProperties = new ArrayList<>();
    private Reference<PassiveCalendar> passiveCalendar = ValueReference.absent();
    private Reference<PassiveCalendar> plannedPassiveCalendar = ValueReference.absent();
    private TemporalReference<ServerActiveEffectiveCalendar> activeCalendar = Temporals.absent();

    private Map<SecurityPropertySet, TypedProperties> dirtySecurityProperties = new HashMap<>();

    private final Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider;
    private final Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider;
    private final Provider<ConnectionInitiationTaskImpl> connectionInitiationTaskProvider;
    private final Provider<ScheduledComTaskExecutionImpl> scheduledComTaskExecutionProvider;
    private final Provider<ManuallyScheduledComTaskExecutionImpl> manuallyScheduledComTaskExecutionProvider;
    private final Provider<FirmwareComTaskExecutionImpl> firmwareComTaskExecutionProvider;
    private transient DeviceValidationImpl deviceValidation;
    private final Reference<DeviceEstimation> deviceEstimation = ValueReference.absent();

    private transient AmrSystem amrSystem;

    private Optional<Location> location = Optional.empty();
    private Optional<SpatialCoordinates> spatialCoordinates = Optional.empty();
    private boolean dirtyMeter = false;
    private static Map<Predicate<Class<? extends ProtocolTask>>, Integer> scorePerProtocolTask;

    // Next objects separate 'Kore' Specific Behaviour
    // Will help us with Kore specific stuff as there is Meter, MeterActivation, Multiplier....
    private transient SyncDeviceWithKoreForInfo koreHelper;
    // 'Synchronize with Kore' actions once this device is saved;
    private transient List<SyncDeviceWithKoreMeter> syncsWithKore = new ArrayList<>();

    @Inject
    public DeviceImpl(
            DataModel dataModel,
            EventService eventService,
            IssueService issueService,
            Thesaurus thesaurus,
            Clock clock,
            MeteringService meteringService,
            MetrologyConfigurationService metrologyConfigurationService,
            ValidationService validationService,
            SecurityPropertyService securityPropertyService,
            Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider,
            Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider,
            Provider<ConnectionInitiationTaskImpl> connectionInitiationTaskProvider,
            Provider<ScheduledComTaskExecutionImpl> scheduledComTaskExecutionProvider,
            Provider<ManuallyScheduledComTaskExecutionImpl> manuallyScheduledComTaskExecutionProvider,
            Provider<FirmwareComTaskExecutionImpl> firmwareComTaskExecutionProvider,
            MeteringGroupsService meteringGroupsService,
            CustomPropertySetService customPropertySetService,
            MdcReadingTypeUtilService readingTypeUtilService,
            ThreadPrincipalService threadPrincipalService,
            UserPreferencesService userPreferencesService,
            DeviceConfigurationService deviceConfigurationService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.issueService = issueService;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.validationService = validationService;
        this.securityPropertyService = securityPropertyService;
        this.scheduledConnectionTaskProvider = scheduledConnectionTaskProvider;
        this.inboundConnectionTaskProvider = inboundConnectionTaskProvider;
        this.connectionInitiationTaskProvider = connectionInitiationTaskProvider;
        this.scheduledComTaskExecutionProvider = scheduledComTaskExecutionProvider;
        this.manuallyScheduledComTaskExecutionProvider = manuallyScheduledComTaskExecutionProvider;
        this.firmwareComTaskExecutionProvider = firmwareComTaskExecutionProvider;
        this.meteringGroupsService = meteringGroupsService;
        this.customPropertySetService = customPropertySetService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.threadPrincipalService = threadPrincipalService;
        this.userPreferencesService = userPreferencesService;
        this.deviceConfigurationService = deviceConfigurationService;
        // Helper to get activation info... from 'Kore'
        this.koreHelper = new SyncDeviceWithKoreForInfo(this, this.meteringService, this.readingTypeUtilService, clock);
        this.koreHelper.syncWithKore(this);
    }

    DeviceImpl initialize(DeviceConfiguration deviceConfiguration, String name, String mRID, Instant startDate) {
        this.createTime = this.clock.instant();
        this.deviceConfiguration.set(deviceConfiguration);
        this.setDeviceTypeFromDeviceConfiguration();
        setName(name);
        this.setmRID(mRID);
        this.koreHelper.setInitialMeterActivationStartDate(startDate);
        createLoadProfiles();
        createLogBooks();
        return this;
    }

    boolean syncWithKore(SyncDeviceWithKoreMeter syncDeviceWithKore) {
        return syncsWithKore.add(syncDeviceWithKore);
    }

    ValidationService getValidationService() {
        return validationService;
    }

    @Override
    public void setInitialActivationStartDate(Instant startDate) {
        koreHelper.setInitialMeterActivationStartDate(startDate);
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
                            .map(loadProfileSpec -> this.dataModel.getInstance(LoadProfileImpl.class)
                                    .initialize(loadProfileSpec, this))
                            .collect(Collectors.toList()));
        }
    }

    private void createLogBooks() {
        if (this.getDeviceConfiguration() != null) {
            this.logBooks.addAll(
                    this.getDeviceConfiguration()
                            .getLogBookSpecs()
                            .stream()
                            .map(logBookSpec -> this.dataModel.getInstance(LogBookImpl.class)
                                    .initialize(logBookSpec, this))
                            .collect(Collectors.toList()));
        }
    }

    void postSave() {
        if (this.meter.isPresent()) {
            this.meter.get().setMRID(getmRID());
            this.location.ifPresent(location1 -> this.meter.get().setLocation(location1));
            this.spatialCoordinates.ifPresent(spatialCoordinates1 -> this.meter.get().setSpatialCoordinates(spatialCoordinates1));
            this.meter.get().update();
        }
        this.saveDirtySecurityProperties();
        this.saveDirtyConnectionProperties();
        this.saveNewAndDirtyDialectProperties();
        this.notifyUpdated();
    }

    @Override
    public void save() {
        boolean alreadyPersistent = this.id > 0;
        if (alreadyPersistent) {
            Save.UPDATE.save(dataModel, this);
            postSave();
        } else {
            Save.CREATE.save(dataModel, this);
            this.meter.set(this.createKoreMeter(getMdcAmrSystem()));
            dataModel.update(this);

            //TODO check if this should be in the syncsWithKore
            this.location.ifPresent(location -> this.meter.get().setLocation(location));
            this.spatialCoordinates.ifPresent(coordinates -> this.meter.get().setSpatialCoordinates(coordinates));


            //All actions to take to sync with Kore once a Device is created
            syncsWithKore.add(new SynchNewDeviceWithKore(this, koreHelper.getInitialMeterActivationStartDate(), meteringService, readingTypeUtilService, clock));

            this.saveNewDialectProperties();
            this.createComTaskExecutionsForEnablementsMarkedAsAlwaysExecuteForInbound();

            this.notifyCreated();
        }
        executeSyncs();
    }

    void validateForUpdate() {
        Save.UPDATE.save(dataModel, this);
    }

    void executeSyncs() {
        // We order the synchronisation actions: first those which create a meter activation, followed
        // by those which reuse the last created meter activation (and just update it).
        syncsWithKore.stream().sorted(new CanUpdateMeterActivationLast()).forEach((x) -> x.syncWithKore(DeviceImpl.this));
        syncsWithKore.clear();
    }

    @Override
    public Optional<Location> getLocation() {
        Optional<Location> updatedValue = getUpdatedLocation();
        if (updatedValue.isPresent()) {
            return updatedValue;
        }
        if (meter.isPresent()) {
            return meter.get().getLocation();
        }
        return Optional.empty();
    }

    @Override
    public void setLocation(Location location) {
        Optional<SyncDeviceWithKoreForSimpleUpdate> currentKoreUpdater = getKoreMeterUpdater();
        SyncDeviceWithKoreForSimpleUpdate koreUpdater = new SyncDeviceWithKoreForSimpleUpdate(this, meteringService, readingTypeUtilService);
        if (!currentKoreUpdater.isPresent()) {
            syncsWithKore.add(koreUpdater);
        } else {
            koreUpdater = currentKoreUpdater.get();
        }
        koreUpdater.setLocation(location);
    }

    @Override
    public Optional<SpatialCoordinates> getSpatialCoordinates() {
        Optional<SpatialCoordinates> updatedValue = getUpdatedSpatialCoordinates();
        if (updatedValue.isPresent()) {
            return updatedValue;
        }
        return getMeter().getOptional().map(EndDevice::getSpatialCoordinates).orElse(Optional.empty());
    }

    @Override
    public void setSpatialCoordinates(SpatialCoordinates spatialCoordinates) {
        Optional<SyncDeviceWithKoreForSimpleUpdate> currentKoreUpdater = getKoreMeterUpdater();
        SyncDeviceWithKoreForSimpleUpdate koreUpdater = new SyncDeviceWithKoreForSimpleUpdate(this, meteringService, readingTypeUtilService);
        if (!currentKoreUpdater.isPresent()) {
            syncsWithKore.add(koreUpdater);
        } else {
            koreUpdater = currentKoreUpdater.get();
        }
        koreUpdater.setSpatialCoordinates(spatialCoordinates);
    }

    private Optional<Location> getUpdatedLocation() {
        Optional<SyncDeviceWithKoreForSimpleUpdate> currentKoreUpdater = getKoreMeterUpdater();
        if (currentKoreUpdater.isPresent()) {
            SyncDeviceWithKoreForSimpleUpdate simpleKoreUpdater = currentKoreUpdater.get();
            return simpleKoreUpdater.getLocation();
        }
        return Optional.empty();
    }

    private Optional<SpatialCoordinates> getUpdatedSpatialCoordinates() {
        Optional<SyncDeviceWithKoreForSimpleUpdate> currentKoreUpdater = getKoreMeterUpdater();
        if (currentKoreUpdater.isPresent()) {
            SyncDeviceWithKoreForSimpleUpdate simpleKoreUpdater = currentKoreUpdater.get();
            return simpleKoreUpdater.getSpatialCoordinates();
        }
        return Optional.empty();
    }

    private Optional<SyncDeviceWithKoreForSimpleUpdate> getKoreMeterUpdater() {
        Optional<SyncDeviceWithKoreMeter> currentKoreUpdater = syncsWithKore.stream()
                .filter((x) -> getClass().isAssignableFrom(SyncDeviceWithKoreForSimpleUpdate.class))
                .findFirst();
        if (currentKoreUpdater.isPresent()) {
            return Optional.of((SyncDeviceWithKoreForSimpleUpdate) currentKoreUpdater.get());
        }
        return Optional.empty();
    }

    private void saveDirtyConnectionProperties() {
        this.getConnectionTaskImpls()
                .filter(ConnectionTaskImpl::hasDirtyProperties)
                .forEach(ConnectionTaskPropertyProvider::saveAllProperties);
    }

    private void createComTaskExecutionsForEnablementsMarkedAsAlwaysExecuteForInbound() {
        List<ComTaskEnablement> comTaskEnablements = getDeviceConfiguration().getComTaskEnablements();
        comTaskEnablements.stream()
                .filter(ComTaskEnablement::isIgnoreNextExecutionSpecsForInbound)
                .forEach(cte -> newManuallyScheduledComTaskExecution(cte, null).add());
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

    private void saveDirtySecurityProperties() {
        for (SecurityPropertySet securityPropertySet : dirtySecurityProperties.keySet()) {
            //Persist the dirty values
            this.securityPropertyService.setSecurityProperties(this, securityPropertySet, dirtySecurityProperties.get(securityPropertySet));
        }
        this.dirtySecurityProperties.clear();
    }

    public Map<SecurityPropertySet, TypedProperties> getDirtySecurityProperties() {
        return dirtySecurityProperties;
    }

    private void saveDialectProperties(List<ProtocolDialectPropertiesImpl> dialectProperties) {
        dialectProperties.stream().forEach(ProtocolDialectPropertiesImpl::save);
    }

    private void notifyUpdated() {
        this.eventService.postEvent(UpdateEventType.DEVICE.topic(), this);
    }

    private void notifyCreated() {
        this.eventService.postEvent(CreateEventType.DEVICE.topic(), this);
        // In addition notify the creation of ConnectionTasks and ComTaskExecutions
        this.getConnectionTaskImpls().forEach(ConnectionTaskImpl::notifyCreated);
        this.getComTaskExecutionImpls().forEach(ComTaskExecutionImpl::notifyCreated);
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
        this.readingTypeObisCodeUsages.clear();
        deleteAllIssues();
        deleteProperties();
        this.removeCustomProperties();
        deleteLoadProfiles();
        deleteLogBooks();
        deleteComTaskExecutions();
        deleteConnectionTasks();
        deleteDeviceMessages();
        deleteSecuritySettings();
        removeDeviceFromStaticGroups();
        new SyncDeviceWithKoreForRemoval(this, meteringService, readingTypeUtilService, clock).syncWithKore(this);
        koreHelper.deactivateMeter(clock.instant());
        this.clearPassiveCalendar();
        this.getDataMapper().remove(this);
    }

    @SuppressWarnings("unchecked")
    private void removeCustomProperties() {
        this.getDeviceType().getCustomPropertySets().forEach(this::removeCustomPropertiesFor);
        this.getRegisters().stream()
                .forEach(register ->
                        this.getDeviceType()
                                .getRegisterTypeTypeCustomPropertySet(register.getRegisterSpec().getRegisterType())
                                .ifPresent(set -> this.customPropertySetService.removeValuesFor(set.getCustomPropertySet(), register
                                        .getRegisterSpec(), this.getId()))
                );

        this.getLoadProfiles().stream().flatMap(lp -> lp.getChannels().stream())
                .forEach(channel ->
                        this.getDeviceType()
                                .getLoadProfileTypeCustomPropertySet(channel.getChannelSpec()
                                        .getLoadProfileSpec()
                                        .getLoadProfileType())
                                .ifPresent(set -> this.customPropertySetService.removeValuesFor(set.getCustomPropertySet(), channel
                                        .getChannelSpec(), this.getId()))
                );
    }

    private void removeCustomPropertiesFor(RegisteredCustomPropertySet customPropertySet) {
        this.removeCustomPropertiesFor(customPropertySet.getCustomPropertySet());
    }

    @SuppressWarnings("unchecked")
    private void removeCustomPropertiesFor(CustomPropertySet customPropertySet) {
        this.customPropertySetService.removeValuesFor(customPropertySet, this);
    }

    private void removeDeviceFromStaticGroups() {
        if (this.meter.isPresent()) {
            if (findMdcAmrSystem().isPresent()) {
                this.meteringGroupsService.findEnumeratedEndDeviceGroupsContaining(this.meter.get()).stream()
                        .forEach(enumeratedEndDeviceGroup -> removeDeviceFromGroup(enumeratedEndDeviceGroup, this.meter.get()));
            }
        }
    }

    private void removeDeviceFromGroup(EnumeratedEndDeviceGroup group, EndDevice endDevice) {
        group
                .getEntries()
                .stream()
                .filter(each -> each.getEndDevice().getId() == endDevice.getId())
                .findFirst()
                .ifPresent(group::remove);
    }

    private void deleteAllIssues() {
        this.issueService
                .findStatus(IssueStatus.WONT_FIX)
                .ifPresent(this::wontFixOpenIssues);
        getListMeterAspect(this::getAllHistoricalIssuesForMeter).stream().forEach(Issue::delete);
    }

    private void wontFixOpenIssues(IssueStatus issueStatus) {
        this.getOpenIssues()
                .stream()
                .forEach(openIssue -> openIssue.close(issueStatus));
    }

    private void deleteSecuritySettings() {
        this.securityPropertyService.deleteSecurityPropertiesFor(this);
    }

    private void deleteComTaskExecutions() {
        this.comTaskExecutions.clear();
    }

    private void deleteConnectionTasks() {
        this.connectionTasks.forEach(ServerConnectionTask::notifyDelete);
        this.connectionTasks.clear();
    }

    private void deleteDeviceMessages() {
        getMessages().stream().forEach(DeviceMessage::delete);
    }

    private void deleteLogBooks() {
        this.logBooks.clear();
    }

    private void deleteLoadProfiles() {
        this.loadProfiles.clear();
    }

    private void deleteProperties() {
        this.deviceProperties.clear();
        for (ProtocolDialectProperties aDialectPropertiesList : dialectPropertiesList) {
            final ProtocolDialectPropertiesImpl protocolDialectProperties = (ProtocolDialectPropertiesImpl) aDialectPropertiesList;
            protocolDialectProperties.delete();
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

    public Reference<Meter> getMeter() {
        return this.meter;
    }

    @Override
    public SyncDeviceWithKoreForInfo getKoreHelper() {
        return this.koreHelper;
    }

    @Override
    public void setName(String name) {
        this.name = null;
        if (name != null) {
            this.name = name.trim();
        }
    }

    @Override
    public void setmRID(String mRID) {
        this.mRID = null;
        Optional.ofNullable(mRID)
                .map(String::trim)
                .ifPresent(trimmed -> this.mRID = trimmed);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(getZone());
    }

    @Override
    public ZoneId getZone() {
        if (this.zoneId == null) {
            if (!Checks.is(timeZoneId).empty() && ZoneId.getAvailableZoneIds().contains(this.timeZoneId)) {
                this.zoneId = ZoneId.of(timeZoneId);
            } else {
                return clock.getZone();
            }
        }
        return this.zoneId;
    }

    /**
     * @deprecated use setZone
     */
    @Deprecated
    @Override
    public void setTimeZone(TimeZone timeZone) {
        setZone(timeZone.toZoneId());
    }

    @Override
    public void setZone(ZoneId zone) {
        if (zone != null) {
            this.timeZoneId = zone.getId();
        } else {
            this.timeZoneId = "";
        }
        this.zoneId = zone;
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
    public BigDecimal getMultiplier() {
        return this.koreHelper.getMultiplier().orElse(BigDecimal.ONE);
    }

    @Override
    public Optional<BigDecimal> getMultiplierAt(Instant multiplierEffectiveTimeStamp) {
        return this.koreHelper.getMultiplierAt(multiplierEffectiveTimeStamp);
    }

    @Override
    public Instant getMultiplierEffectiveTimeStamp() {
        return this.koreHelper.getMultiplierEffectiveTimeStamp();
    }

    @Override
    public void setMultiplier(BigDecimal multiplier) {
        this.setMultiplier(multiplier, this.clock.instant());
    }

    @Override
    public void setMultiplier(BigDecimal multiplier, Instant from) {
        if (getMultiplier() == null || multiplier.compareTo(getMultiplier()) != 0) {
            validateMultiplierValue(multiplier);
            Instant now = clock.instant();
            Optional<Instant> startDateMultiplier = Optional.ofNullable(from);
            validateStartDateOfNewMultiplier(now, startDateMultiplier);
            SynchDeviceWithKoreForMultiplierChange multiplierChange = new SynchDeviceWithKoreForMultiplierChange(this, startDateMultiplier.get(), multiplier, meteringService, readingTypeUtilService);
            //All actions to take to sync with Kore once a Device is created
            syncsWithKore.add(multiplierChange);
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

    private void validateMultiplierValue(BigDecimal multiplier) {
        if (multiplier.compareTo(BigDecimal.ZERO) != 1) {
            throw MultiplierConfigurationException.multiplierShouldBeLargerThanZero(thesaurus);
        } else if (multiplier.compareTo(maxMultiplier) == 1) {
            throw MultiplierConfigurationException.multiplierValueExceedsMax(thesaurus);
        }
    }

    private void validateStartDateOfNewMultiplier(Instant now, Optional<Instant> startDateMultiplier) {
        if (startDateMultiplier.isPresent()) {
            if (this.meter.get().hasData() && startDateMultiplier.get().isBefore(now)) {
                throw MultiplierConfigurationException.canNotConfigureMultiplierInPastWhenYouAlreadyHaveData(thesaurus);
            }
            if (this.meter.get().getCurrentMeterActivation().isPresent()) {
                if (!this.meter.get().getCurrentMeterActivation().get().getRange().contains(startDateMultiplier.get())) {
                    throw MultiplierConfigurationException.canNotConfigureMultiplierWithStartDateOutOfCurrentMeterActivation(thesaurus);
                }
            }
        }
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
        return loadProfiles.stream().flatMap(lp -> lp.getChannels().stream()).collect(Collectors.toList());
    }

    @Override
    public List<Register> getRegisters() {
        return new ArrayList<>(getDeviceConfiguration().getRegisterSpecs().stream().map(this::newRegisterFor).collect(Collectors.toList()));
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

    public List<LoadProfile> getLoadProfiles() {
        return Collections.unmodifiableList(this.loadProfiles);
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
    public Optional<DeviceProtocolPluggableClass> getDeviceProtocolPluggableClass() {
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
    public LoadProfile.LoadProfileUpdater getLoadProfileUpdaterFor(LoadProfile loadProfile) {
        return new LoadProfileUpdaterForDevice((LoadProfileImpl) loadProfile);
    }

    class LogBookUpdaterForDevice extends LogBookImpl.LogBookUpdater {

        protected LogBookUpdaterForDevice(LogBookImpl logBook) {
            super(logBook);
        }
    }

    class LoadProfileUpdaterForDevice extends LoadProfileImpl.LoadProfileUpdater {

        protected LoadProfileUpdaterForDevice(LoadProfileImpl loadProfile) {
            super(loadProfile);
        }

        @Override
        public void update() {
            super.update();
            dataModel.touch(DeviceImpl.this);
        }
    }

    @Override
    public void validateDeviceCanChangeConfig(DeviceConfiguration destinationDeviceConfiguration) {
        if (this.getDeviceConfiguration().getId() == destinationDeviceConfiguration.getId()) {
            throw DeviceConfigurationChangeException.cannotChangeToSameConfig(thesaurus, this);
        }
        if (destinationDeviceConfiguration.getDeviceType().getId() != getDeviceType().getId()) {
            throw DeviceConfigurationChangeException.cannotChangeToConfigOfOtherDeviceType(thesaurus);
        }
        if (getDeviceType().isDataloggerSlave()) {
            throw DeviceConfigurationChangeException.cannotChangeConfigOfDataLoggerSlave(thesaurus);
        }
        if (getDeviceConfiguration().isDataloggerEnabled()) {
            throw DeviceConfigurationChangeException.cannotChangeConfigOfDataLoggerEnabledDevice(thesaurus);
        }
        if (destinationDeviceConfiguration.isDataloggerEnabled()) {
            throw DeviceConfigurationChangeException.cannotchangeConfigToDataLoggerEnabled(thesaurus);
        }
        checkIfAllConflictsAreSolved(this.getDeviceConfiguration(), destinationDeviceConfiguration);
    }


    private void checkIfAllConflictsAreSolved(DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration) {
        originDeviceConfiguration.getDeviceType().getDeviceConfigConflictMappings().stream()
                .filter(deviceConfigConflictMapping -> deviceConfigConflictMapping.getOriginDeviceConfiguration().getId() == originDeviceConfiguration.getId()
                        && deviceConfigConflictMapping.getDestinationDeviceConfiguration().getId() == destinationDeviceConfiguration.getId())
                .filter(Predicates.not(DeviceConfigConflictMapping::isSolved)).findFirst()
                .ifPresent(deviceConfigConflictMapping1 -> {
                    throw new CannotChangeDeviceConfigStillUnresolvedConflicts(thesaurus, this, destinationDeviceConfiguration);
                });
    }

    @Override
    public void setNewDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        this.deviceConfiguration.set(deviceConfiguration);
        SynchDeviceWithKoreForConfigurationChange multiplierChange = new SynchDeviceWithKoreForConfigurationChange(this, meteringService, readingTypeUtilService, clock);
        //All actions to take to sync with Kore once a Device is created
        syncsWithKore.add(multiplierChange);
    }

    @Override
    public void createNewMeterActivation(Instant meterActivationStartTime) {
        activate(meterActivationStartTime);
    }

//    @Override
//    public void updateMeterConfiguration(Instant updateTimeStamp) {
//        createMeterConfiguration(this.meter.get(), updateTimeStamp, getMultiplier().compareTo(BigDecimal.ONE) == 1);
//    }

    @Override
    public void removeLoadProfiles(List<LoadProfile> loadProfiles) {
        this.loadProfiles.removeAll(loadProfiles);
    }

    @Override
    public void addLoadProfiles(List<LoadProfileSpec> loadProfileSpecs) {
        this.loadProfiles.addAll(
                loadProfileSpecs.stream()
                        .map(loadProfileSpec -> this.dataModel.getInstance(LoadProfileImpl.class).initialize(loadProfileSpec, this))
                        .collect(Collectors.toList()));
    }

    @Override
    public void removeLogBooks(List<LogBook> logBooks) {
        this.logBooks.removeAll(logBooks);
    }

    @Override
    public void addLogBooks(List<LogBookSpec> logBookSpecs) {
        this.logBooks.addAll(
                logBookSpecs.stream()
                        .map(logBookSpec -> this.dataModel.getInstance(LogBookImpl.class).initialize(logBookSpec, this))
                        .collect(Collectors.toList()));
    }

    @Override
    public void updateSecurityProperties(SecurityPropertySet origin, SecurityPropertySet destination) {
        ((ServerSecurityPropertyServiceForConfigChange) securityPropertyService).updateSecurityPropertiesWithNewSecurityPropertySet(this, origin, destination);
    }

    @Override
    public void deleteSecurityPropertiesFor(SecurityPropertySet securityPropertySet) {
        ((ServerSecurityPropertyServiceForConfigChange) securityPropertyService).deleteSecurityPropertiesFor(this, securityPropertySet);
    }

    public Optional<ReadingType> getCalculatedReadingTypeFromMeterConfiguration(ReadingType readingType, Instant timeStamp) {
        Optional<MeterConfiguration> configuration = Optional.empty();
        if (this.meter.isPresent()) {
            configuration = this.meter.get().getConfiguration(timeStamp);
        }
        if (configuration.isPresent()) {
            Optional<MeterReadingTypeConfiguration> mrtConfiguration = configuration.get()
                    .getReadingTypeConfigs()
                    .stream()
                    .filter(meterReadingTypeConfiguration -> meterReadingTypeConfiguration.getMeasured().equals(readingType))
                    .findAny();
            if (mrtConfiguration.isPresent()) {
                return mrtConfiguration.get().getCalculated();
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public Optional<MeterReadingTypeConfiguration> getMeterReadingTypeConfigurationFor(ReadingType readingType) {
        if (this.meter.isPresent()) {
            Optional<MeterConfiguration> configuration = this.meter.get().getConfiguration(clock.instant());
            if (configuration.isPresent()) {
                return configuration.get().getReadingTypeConfiguration(readingType);
            }
        }
        return Optional.empty();
    }

    private Optional<ProtocolDialectProperties> getProtocolDialectPropertiesFrom(String dialectName, List<ProtocolDialectPropertiesImpl> propertiesList) {
        return propertiesList
                .stream()
                .filter(properties -> properties.getDeviceProtocolDialectName().equals(dialectName))
                .map(ProtocolDialectProperties.class::cast)
                .findAny();
    }

    @Override
    public void setProtocolDialectProperty(String dialectName, String propertyName, Object value) {
        Optional<ProtocolDialectProperties> dialectProperties = this.getProtocolDialectProperties(dialectName);
        if (!dialectProperties.isPresent()) {
            ProtocolDialectProperties newDialectProperties = this.createNewLocalDialectProperties(dialectName);
            newDialectProperties.setProperty(propertyName, value);
        } else {
            dialectProperties.get().setProperty(propertyName, value);
            this.dirtyDialectProperties.add((ProtocolDialectPropertiesImpl) dialectProperties.get());
        }
    }

    private ProtocolDialectProperties createNewLocalDialectProperties(String dialectName) {
        ProtocolDialectPropertiesImpl dialectProperties;
        ProtocolDialectConfigurationProperties configurationProperties = this.getProtocolDialectConfigurationProperties(dialectName);
        if (configurationProperties != null) {
            dialectProperties = this.dataModel.getInstance(ProtocolDialectPropertiesImpl.class).initialize(this, configurationProperties);
            this.newDialectProperties.add(dialectProperties);
        } else {
            throw new ProtocolDialectConfigurationPropertiesIsRequiredException(MessageSeeds.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED);
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
            ProtocolDialectProperties props = dialectProperties.get();
            props.removeProperty(propertyName);
            if (!this.dirtyDialectProperties.contains(props)) {
                this.dirtyDialectProperties.add((ProtocolDialectPropertiesImpl) props);
            }
        } else {
            createNewLocalDialectProperties(dialectName);
        }
    }

    @Override
    public void setProtocolProperty(String name, Object value) {
        Optional<PropertySpec> optionalPropertySpec = getPropertySpecForProperty(name);
        if (optionalPropertySpec.isPresent()) {
            String propertyValue = optionalPropertySpec.get().getValueFactory().toStringValue(value);
            boolean notUpdated = !updatePropertyIfExists(name, propertyValue);
            if (notUpdated) {
                addDeviceProperty(optionalPropertySpec, propertyValue);
            }
            if (getId() > 0) {
                dataModel.touch(this);
            }
        } else {
            throw DeviceProtocolPropertyException.propertyDoesNotExistForDeviceProtocol(name, this.getDeviceProtocolPluggableClass().get()
                    .getDeviceProtocol(), this, thesaurus, MessageSeeds.DEVICE_PROPERTY_NOT_ON_DEVICE_PROTOCOL);
        }
    }

    @Override
    public void
    setSecurityProperties(SecurityPropertySet securityPropertySet, TypedProperties typedProperties) {
        dirtySecurityProperties.put(securityPropertySet, typedProperties);
        //Don't persist yet, need to be validated (done in the save step of this device)
    }

    @Override
    public List<ProtocolDialectProperties> getProtocolDialectPropertiesList() {
        List<ProtocolDialectProperties> all = new ArrayList<>(this.dialectPropertiesList.size() + this.newDialectProperties
                .size());
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

    private void addDeviceProperty(Optional<PropertySpec> propertySpec, String propertyValue) {
        if (propertyValue != null) {
            DeviceProtocolPropertyImpl deviceProtocolProperty = this.dataModel.getInstance(DeviceProtocolPropertyImpl.class).initialize(this, propertySpec, propertyValue);
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
                dataModel.touch(this);
                break;
            }
        }
    }

    @Override
    public TypedProperties getDeviceProtocolProperties() {
        if (this.getDeviceProtocolPluggableClass().isPresent()) {
            TypedProperties properties = TypedProperties.inheritingFrom(this.getDeviceConfiguration().getDeviceProtocolProperties().getTypedProperties());
            this.addLocalProperties(properties, this.getDeviceProtocolPluggableClass().get().getDeviceProtocol().getPropertySpecs());
            return properties;
        } else {
            return TypedProperties.empty();
        }
    }


    private Optional<PropertySpec> getPropertySpecForProperty(String name) {
        return this.getDeviceProtocolPluggableClass().map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass
                .getDeviceProtocol().getPropertySpecs().stream().filter(spec -> spec.getName().equals(name)).findFirst()).orElse(Optional.empty());
    }

    private void addLocalProperties(TypedProperties properties, List<PropertySpec> propertySpecs) {
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
    public Optional<UsagePoint> getUsagePoint() {
        return koreHelper.getUsagePoint();
    }

    @Override
    public void setUsagePoint(UsagePoint usagePoint) {
        SynchDeviceWithKoreForUsagePointChange usagePointChange = new SynchDeviceWithKoreForUsagePointChange(this, clock.instant(), usagePoint, meteringService, readingTypeUtilService, deviceConfigurationService, thesaurus, userPreferencesService, threadPrincipalService);
        //All actions to take to sync with Kore once a Device is created
        syncsWithKore.add(usagePointChange);
    }

    private Supplier<RuntimeException> mdcAMRSystemDoesNotExist() {
        return () -> new RuntimeException("The MDC AMR system does not exist");
    }

    private Supplier<NoMeterActivationAt> noMeterActivationAt(Instant timestamp) {
        return () -> new NoMeterActivationAt(timestamp, thesaurus, MessageSeeds.NO_METER_ACTIVATION_AT);
    }

    private Meter createKoreMeter(AmrSystem amrSystem) {
        FiniteStateMachine stateMachine = this.getDeviceType().getDeviceLifeCycle().getFiniteStateMachine();
        Meter newMeter = amrSystem.newMeter(String.valueOf(getId()))
                .setMRID(getmRID())
                .setStateMachine(stateMachine)
                .setSerialNumber(getSerialNumber())
                .create();
        newMeter.getLifecycleDates().setReceivedDate(koreHelper.getInitialMeterActivationStartDate().get()); // date should be present
        newMeter.update();
        return newMeter;
    }

    private Optional<AmrSystem> findMdcAmrSystem() {
        return this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
    }

    private AmrSystem getMdcAmrSystem() {
        if (this.amrSystem == null) {
            this.amrSystem = findMdcAmrSystem().orElseThrow(mdcAMRSystemDoesNotExist());
        }
        return this.amrSystem;
    }

    List<ReadingRecord> getReadingsFor(Register<?, ?> register, Range<Instant> interval) {
        return this.getListMeterAspect(meter -> this.getReadingsFor(register, interval, meter));
    }

    private List<ReadingRecord> getReadingsFor(Register<?, ?> register, Range<Instant> interval, Meter meter) {
        List<? extends BaseReadingRecord> readings = meter.getReadings(interval, register.getRegisterSpec().getRegisterType().getReadingType());
        return readings
                .stream()
                .map(ReadingRecord.class::cast)
                .collect(Collectors.toList());
    }

    List<LoadProfileReading> getChannelData(LoadProfile loadProfile, Range<Instant> interval) {
        List<LoadProfileReading> loadProfileReadings = Collections.emptyList();
        boolean meterHasData = false;
        if (this.meter.isPresent()) {
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

        return Lists.reverse(loadProfileReadings);
    }

    List<LoadProfileReading> getChannelData(Channel channel, Range<Instant> interval) {
        List<LoadProfileReading> loadProfileReadings = Collections.emptyList();
        boolean meterHasData;
        if (this.meter.isPresent()) {
            Map<Instant, LoadProfileReadingImpl> sortedLoadProfileReadingMap =
                    getPreFilledLoadProfileReadingMap(
                            channel.getLoadProfile(),
                            interval,
                            meter.get());
            Range<Instant> clipped = Ranges.openClosed(interval.lowerEndpoint(), lastReadingClipped(channel.getLoadProfile(), interval));
            meterHasData = this.addChannelDataToMap(clipped, meter.get(), channel, sortedLoadProfileReadingMap);
            if (meterHasData) {
                loadProfileReadings = new ArrayList<>(sortedLoadProfileReadingMap.values());
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
     * @param interval The interval over which meter readings are requested
     * @param meter The meter for which readings are requested
     * @param mdcChannel The meter's channel for which readings are requested
     * @param sortedLoadProfileReadingMap The map to add the readings to in the correct timeslot
     * @return true if any readings were added to the map, false otherwise
     */
    private boolean addChannelDataToMap(Range<Instant> interval, Meter meter, Channel mdcChannel, Map<Instant, LoadProfileReadingImpl> sortedLoadProfileReadingMap) {
        boolean meterHasData = false;
        List<MeterActivation> meterActivations = this.getSortedMeterActivations(meter, Ranges.closed(interval.lowerEndpoint(), interval.upperEndpoint()));
        for (MeterActivation meterActivation : meterActivations) {
            Range<Instant> meterActivationInterval = meterActivation.getInterval().toOpenClosedRange().intersection(interval);
            meterHasData |= meterActivationInterval.lowerEndpoint() != meterActivationInterval.upperEndpoint();
            ReadingType readingType = mdcChannel.getReadingType(); // getChannelSpec().getReadingType();
            List<IntervalReadingRecord> meterReadings = (List<IntervalReadingRecord>) meter.getReadings(meterActivationInterval, readingType);
            for (IntervalReadingRecord meterReading : meterReadings) {
                LoadProfileReadingImpl loadProfileReading = sortedLoadProfileReadingMap.get(meterReading.getTimeStamp());
                loadProfileReading.setChannelData(mdcChannel, meterReading);
                loadProfileReading.setReadingQualities(mdcChannel, meterReading.getReadingQualities());
                loadProfileReading.setReadingTime(meterReading.getReportedDateTime());
            }

            Optional<com.elster.jupiter.metering.Channel> koreChannel = this.getChannel(meterActivation.getChannelsContainer(), readingType);
            if (koreChannel.isPresent()) {
                List<DataValidationStatus> validationStatus = forValidation().getValidationStatus(mdcChannel, meterReadings, meterActivationInterval);
                validationStatus.stream()
                        .filter(s -> s.getReadingTimestamp().isAfter(meterActivationInterval.lowerEndpoint()))
                        .forEach(s -> {
                            LoadProfileReadingImpl loadProfileReading = sortedLoadProfileReadingMap.get(s.getReadingTimestamp());
                            if (loadProfileReading != null) {
                                loadProfileReading.setDataValidationStatus(mdcChannel, s);
                                //code below is the processing of removed readings
                                s.getReadingQualities()
                                        .stream()
                                        .filter(rq -> rq.getType().qualityIndex().orElse(null) == QualityCodeIndex.REJECTED)
                                        .findAny()
                                        .map(ReadingQualityRecord.class::cast)
                                        .ifPresent(readingQuality -> loadProfileReading.setReadingTime(readingQuality.getTimestamp()));
                            }
                        });
            }
        }
        return meterHasData;
    }

    /**
     * Creates a map of LoadProfileReadings (k,v -> timestamp of end of interval, placeholder for readings) (without a reading value),
     * just a list of placeholders for each reading interval within the requestedInterval for all timestamps
     * that occur with the bounds of a meter activation and load profile's last reading.
     *
     * @param loadProfile The LoadProfile
     * @param requestedInterval interval over which user wants to see readings
     * @param meter The Meter
     * @return The map
     */
    private Map<Instant, LoadProfileReadingImpl> getPreFilledLoadProfileReadingMap(LoadProfile loadProfile, Range<Instant> requestedInterval, Meter meter) {
        // TODO: what if there are gaps in the meter activations
        Map<Instant, LoadProfileReadingImpl> loadProfileReadingMap = new TreeMap<>();
        TemporalAmount intervalLength = this.intervalLength(loadProfile);
        meter.getChannelsContainers()
                .stream()
                .filter(channelContainer -> channelContainer.overlaps(requestedInterval))
                .forEach(affectedChannelContainer -> {
                    Range<Instant> requestedIntervalClippedToMeterActivation = requestedInterval.intersection(affectedChannelContainer.getRange());
                    ZonedDateTime requestStart = this.prefilledIntervalStart(loadProfile, affectedChannelContainer.getZoneId(), requestedIntervalClippedToMeterActivation);
                    ZonedDateTime requestEnd =
                            ZonedDateTime.ofInstant(
                                    this.lastReadingClipped(loadProfile, requestedInterval),
                                    affectedChannelContainer.getZoneId());
                    if (!requestEnd.isBefore(requestStart)) {
                        Range<Instant> channelContainerInterval = Range.closedOpen(requestStart.toInstant(), requestEnd.toInstant());
                        while (channelContainerInterval.contains(requestStart.toInstant())) {
                            ZonedDateTime readingTimestamp = requestStart.plus(intervalLength);
                            if (requestedInterval.contains(readingTimestamp.toInstant())) {
                                LoadProfileReadingImpl value = new LoadProfileReadingImpl();
                                value.setRange(Ranges.openClosed(requestStart.toInstant(), readingTimestamp.toInstant()));
                                loadProfileReadingMap.put(readingTimestamp.toInstant(), value);
                            }
                            requestStart = readingTimestamp;
                        }
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
                return prefilledIntervalStartWithIntervalMonth(loadProfile, zoneId, requestedIntervalClippedToMeterActivation);

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

        while (nextAttempt.toInstant().isBefore(requestedIntervalClippedToMeterActivation.lowerEndpoint()) || nextAttempt.toInstant()
                .equals(requestedIntervalClippedToMeterActivation.lowerEndpoint())) {
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

        while (nextAttempt.toInstant().isAfter(requestedIntervalClippedToMeterActivation.lowerEndpoint()) || nextAttempt.toInstant()
                .equals(requestedIntervalClippedToMeterActivation.lowerEndpoint())) {
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

    private Instant lastReadingClipped(LoadProfile loadProfile, Range<Instant> interval) {
        Instant dataUntil = Instant.EPOCH;
        for (Channel channel : loadProfile.getChannels()) {
            if (channel.getLastDateTime().isPresent() && channel.getLastDateTime().get().isAfter(dataUntil)) {
                dataUntil = channel.getLastDateTime().get();
            }
        }
        if (!dataUntil.equals(Instant.EPOCH)) {
            if (interval.contains(dataUntil)) {
                return dataUntil;
            } else if (!interval.upperEndpoint().isAfter(dataUntil)) {
                return interval.upperEndpoint();
            } else {
                return interval.lowerEndpoint(); // empty interval: interval is completely after last reading
            }
        }
        return interval.upperEndpoint();
    }

    Optional<ReadingRecord> getLastReadingFor(Register<?, ?> register) {
        return this.getOptionalMeterAspect(meter -> this.getLastReadingsFor(register, meter));
    }

    @Override
    public void store(MeterReading meterReading) {
        this.meter.getOptional().ifPresent(meter -> meter.store(QualityCodeSystem.MDC, meterReading));
    }

    private Optional<ReadingRecord> getLastReadingsFor(Register register, Meter meter) {
        ReadingType readingType = register.getRegisterSpec().getRegisterType().getReadingType();
        for (MeterActivation meterActivation : this.getSortedMeterActivations(meter)) {
            Optional<com.elster.jupiter.metering.Channel> channel = this.getChannel(meterActivation.getChannelsContainer(), readingType);
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
        } else {
            return Optional.of(false);
        }
    }

    boolean hasData(Channel channel) {
        return this.hasData(this.findKoreChannels(channel));
    }

    boolean hasData(Register<?, ?> register) {
        return this.hasData(this.findKoreChannels(register));
    }

    private boolean hasData(List<com.elster.jupiter.metering.Channel> channels) {
        return channels
                .stream()
                .anyMatch(com.elster.jupiter.metering.Channel::hasData);
    }

    @Override
    public MeterActivation activate(Instant start) {
        return this.koreHelper.activateMeter(start);
    }

    @Override
    public MeterActivation activate(Instant start, UsagePoint usagePoint) {
        SynchDeviceWithKoreForUsagePointChange usagePointChange = new SynchDeviceWithKoreForUsagePointChange(this, start, usagePoint, meteringService, readingTypeUtilService, deviceConfigurationService, thesaurus, userPreferencesService, threadPrincipalService);
        //All actions to take to sync with Kore once a Device is created
        usagePointChange.syncWithKore(this);
        return getCurrentMeterActivation().get();
    }

    @Override
    public void deactivateNow() {
        this.deactivate(this.clock.instant());
    }

    @Override
    public void deactivate(Instant when) {
        this.koreHelper.deactivateMeter(when);
    }

    @Override
    public Optional<MeterActivation> getCurrentMeterActivation() {
        return this.koreHelper.getCurrentMeterActivation();
    }

    private <AT> Optional<AT> getOptionalMeterAspect(Function<Meter, Optional<AT>> aspectFunction) {
        if (meter.isPresent()) {
            return aspectFunction.apply(this.meter.get());
        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private <AT> List<AT> getListMeterAspect(Function<Meter, List<AT>> aspectFunction) {
        if (this.meter.isPresent()) {
            return aspectFunction.apply(this.meter.get());
        } else {
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

    @Override
    public List<MeterActivation> getMeterActivations(Range<Instant> range) {
        return getSortedMeterActivations(meter.get(), range);
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
        if (this.meter.isPresent() && this.meter.get().getMeterActivations().isEmpty()) {
            this.meter.get().activate(when);
        }
    }

    private Optional<MeterActivation> getMeterActivation(Instant when) {
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
            return Optional.ofNullable(getChannel(meterActivation.get().getChannelsContainer(), readingTypeSupplier.get()).orElse(null));
        } else {
            return Optional.empty();
        }
    }

    List<com.elster.jupiter.metering.Channel> findKoreChannels(Channel channel) {
        return findKoreChannels(channel::getReadingType);
    }

    List<com.elster.jupiter.metering.Channel> findKoreChannels(Register<?, ?> register) {
        return findKoreChannels(register::getReadingType);
    }

    private List<com.elster.jupiter.metering.Channel> findKoreChannels(Supplier<ReadingType> readingTypeSupplier) {
        return this.getListMeterAspect(meter -> this.findKoreChannels(readingTypeSupplier, meter));
    }

    com.elster.jupiter.metering.Channel findOrCreateKoreChannel(Instant when, Register<?, ?> register) {
        return findOrCreateKoreChannel(when, register.getReadingType());
    }

    com.elster.jupiter.metering.Channel findOrCreateKoreChannel(Instant when, Channel channel) {
        return findOrCreateKoreChannel(when, channel.getReadingType());
    }

    private com.elster.jupiter.metering.Channel findOrCreateKoreChannel(Instant when, ReadingType readingType) {
        Optional<MeterActivation> meterActivation = this.getMeterActivation(when);
        if (meterActivation.isPresent()) {
            return this.getChannel(meterActivation.get().getChannelsContainer(), readingType)
                    .orElseGet(() -> meterActivation.get().getChannelsContainer().createChannel(readingType));
        } else {
            throw this.noMeterActivationAt(when).get();
        }
    }

    private List<com.elster.jupiter.metering.Channel> findKoreChannels(Supplier<ReadingType> readingTypeSupplier, Meter meter) {
        return meter.getChannelsContainers()
                .stream()
                .map(channelContainer -> getChannel(channelContainer, readingTypeSupplier.get()))
                .flatMap(asStream())
                .collect(Collectors.toList());
    }

    private Optional<com.elster.jupiter.metering.Channel> getChannel(ChannelsContainer channelsContainer, ReadingType readingType) {
        return channelsContainer.getChannels()
                .stream()
                .filter(channel -> channel.getReadingTypes().contains(readingType))
                .findFirst();
    }

    /**
     * Sorts the {@link MeterActivation}s of the specified {@link Meter}
     * that overlap with the {@link Interval}, where the most recent activations are returned first.
     *
     * @param meter The Meter
     * @param interval The Interval
     * @return The List of MeterActivation
     */
    private List<MeterActivation> getSortedMeterActivations(Meter meter, Range<Instant> interval) {
        List<MeterActivation> overlapping = new ArrayList<>();
        overlapping.addAll(meter.getMeterActivations(interval));
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
        return this.getConnectionTaskImpls().collect(Collectors.toList());
    }

    private Stream<ConnectionTaskImpl<?, ?>> getConnectionTaskImpls() {
        return this.connectionTasks
                .stream()
                .filter(Predicates.not(ConnectionTask::isObsolete));
    }

    @Override
    public List<ScheduledConnectionTask> getScheduledConnectionTasks() {
        return this.getConnectionTaskImpls()
                .filter(ct -> ct instanceof ScheduledConnectionTask)
                .map(ScheduledConnectionTask.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConnectionInitiationTask> getConnectionInitiationTasks() {
        return this.getConnectionTaskImpls()
                .filter(ct -> ct instanceof ConnectionInitiationTask)
                .map(ConnectionInitiationTask.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<InboundConnectionTask> getInboundConnectionTasks() {
        return this.getConnectionTaskImpls()
                .filter(ct -> ct instanceof InboundConnectionTask)
                .map(InboundConnectionTask.class::cast)
                .collect(Collectors.toList());
    }

    private ConnectionTask add(ConnectionTaskImpl connectionTask) {
        Save.CREATE.validate(DeviceImpl.this.dataModel, connectionTask, Save.Create.class, Save.Update.class);
        DeviceImpl.this.connectionTasks.add(connectionTask);
        if (this.id != 0) {
            // saving the connection properties
            connectionTask.saveAllProperties();
            connectionTask.notifyCreated();
            this.dataModel.touch(DeviceImpl.this);
        }
        return connectionTask;
    }

    @Override
    public void removeConnectionTask(ConnectionTask<?, ?> connectionTask) {
        this.connectionTasks
                .stream()
                .filter(x -> x.getId() == connectionTask.getId())
                .findAny()
                .map(ServerConnectionTask.class::cast)
                .ifPresent(ServerConnectionTask::makeObsolete);
    }

    private Stream<ComTaskExecutionImpl> getComTaskExecutionImpls() {
        return this.comTaskExecutions
                .stream()
                .filter(Predicates.not(ComTaskExecution::isObsolete));
    }

    @Override
    public List<ComTaskExecution> getComTaskExecutions() {
        return comTaskExecutions.stream().filter(((Predicate<ComTaskExecution>) ComTaskExecution::isObsolete).negate()).collect(Collectors.toList());
    }

    private ComTaskExecution add(ComTaskExecutionImpl comTaskExecution) {
        Save.CREATE.validate(DeviceImpl.this.dataModel, comTaskExecution, Save.Create.class);
        Save.UPDATE.validate(DeviceImpl.this.dataModel, this, Save.Update.class);
        this.comTaskExecutions.add(comTaskExecution);
        if (this.id != 0) {
            comTaskExecution.notifyCreated();
            dataModel.touch(DeviceImpl.this);
        }
        return comTaskExecution;
    }

    @Override
    public void removeComTaskExecution(ComTaskExecution comTaskExecution) {
        this.comTaskExecutions
                .stream()
                .filter(x -> x.getId() == comTaskExecution.getId())
                .findAny()
                .map(ServerComTaskExecution.class::cast)
                .ifPresent(ServerComTaskExecution::makeObsolete);
    }

    @Override
    public ComTaskExecutionBuilder<ScheduledComTaskExecution> newScheduledComTaskExecution(ComSchedule comSchedule) {
        return new ScheduledComTaskExecutionBuilderForDevice(scheduledComTaskExecutionProvider, comSchedule);
    }

    @Override
    public AdHocComTaskExecutionBuilderForDevice newAdHocComTaskExecution(ComTaskEnablement comTaskEnablement) {
        return new AdHocComTaskExecutionBuilderForDevice(manuallyScheduledComTaskExecutionProvider, comTaskEnablement);
    }

    @Override
    public ComTaskExecutionBuilder<FirmwareComTaskExecution> newFirmwareComTaskExecution(ComTaskEnablement comTaskEnablement) {
        return new FirmwareComTaskExecutionBuilderForDevice(firmwareComTaskExecutionProvider, comTaskEnablement);
    }

    @Override
    public ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> newManuallyScheduledComTaskExecution(ComTaskEnablement comTaskEnablement, TemporalExpression temporalExpression) {
        return new ManuallyScheduledComTaskExecutionBuilderForDevice(
                this.manuallyScheduledComTaskExecutionProvider,
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
    public void removeComSchedule(ComSchedule comSchedule) {
        ComTaskExecution toRemove = getComTaskExecutionImpls().filter(x -> x.executesComSchedule(comSchedule)).findFirst().
                orElseThrow(() -> new CannotDeleteComScheduleFromDevice(comSchedule, this, this.thesaurus, MessageSeeds.COM_SCHEDULE_CANNOT_DELETE_IF_NOT_FROM_DEVICE));
        removeComTaskExecution(toRemove);
    }

    @Override
    public List<SecurityProperty> getSecurityProperties(SecurityPropertySet securityPropertySet) {
        return this.getSecurityProperties(clock.instant(), securityPropertySet);
    }

    @Override
    public List<ProtocolDialectConfigurationProperties> getProtocolDialects() {
        return this.getDeviceConfiguration().getProtocolDialectConfigurationPropertiesList();
    }

    private List<SecurityProperty> getSecurityProperties(Instant when, SecurityPropertySet securityPropertySet) {
        return this.securityPropertyService.getSecurityProperties(this, when, securityPropertySet);
    }

    @Override
    public boolean hasSecurityProperties(SecurityPropertySet securityPropertySet) {
        return this.hasSecurityProperties(clock.instant(), securityPropertySet);
    }

    private boolean hasSecurityProperties(Instant when, SecurityPropertySet securityPropertySet) {
        return this.securityPropertyService.hasSecurityProperties(this, when, securityPropertySet);
    }

    @Override
    public boolean securityPropertiesAreValid() {
        return this.securityPropertyService.securityPropertiesAreValid(this);
    }

    @Override
    public boolean securityPropertiesAreValid(SecurityPropertySet securityPropertySet) {
        return this.securityPropertyService.securityPropertiesAreValid(this, securityPropertySet);
    }

    @Override
    public DeviceValidation forValidation() {
        if (deviceValidation == null) {
            deviceValidation = new DeviceValidationImpl(getMdcAmrSystem(), this.validationService, this.clock, this.thesaurus, this);
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
    public GatewayType getConfigurationGatewayType() {
        DeviceConfiguration configuration = getDeviceConfiguration();
        if (configuration == null) {
            return GatewayType.NONE;
        }
        return configuration.getGatewayType();
    }

    @Override
    public DeviceMessageBuilder newDeviceMessage(DeviceMessageId deviceMessageId) {
        return this.newDeviceMessage(deviceMessageId, TrackingCategory.manual);
    }

    @Override
    public DeviceMessageBuilder newDeviceMessage(DeviceMessageId deviceMessageId, TrackingCategory trackingCategory) {
        return new InternalDeviceMessageBuilder(deviceMessageId, trackingCategory);
    }

    @Override
    public void addToGroup(EnumeratedEndDeviceGroup enumeratedEndDeviceGroup, Range<Instant> range) {
        enumeratedEndDeviceGroup.add(this.meter.get(), range);
    }

    @Override
    public boolean hasOpenIssues() {
        return !getOpenIssues().isEmpty();
    }

    @Override
    public List<OpenIssue> getOpenIssues() {
        return getListMeterAspect(this::getOpenIssuesForMeter);
    }

    private List<OpenIssue> getOpenIssuesForMeter(Meter meter) {
        return this.issueService.query(OpenIssue.class).select(where("device").isEqualTo(meter));
    }

    private List<HistoricalIssue> getAllHistoricalIssuesForMeter(Meter meter) {
        return this.issueService.query(HistoricalIssue.class).select(where("device").isEqualTo(meter));
    }

    @Override
    public State getState() {
        return this.getState(this.clock.instant()).get();
    }

    @Override
    public CalendarSupport calendars() {
        return new DeviceCalendarSupport(this, this.dataModel, this.clock);
    }

    TemporalReference<ServerActiveEffectiveCalendar> activeEffectiveCalendar() {
        return this.activeCalendar;
    }

    Optional<PassiveCalendar> getPassiveCalendar() {
        return this.passiveCalendar.getOptional();
    }

    void setPassiveCalendar(PassiveCalendar calendar) {
        this.clearPassiveCalendar();
        this.passiveCalendar.set(calendar);
    }

    void clearPassiveCalendar() {
        this.passiveCalendar.set(null);
        this.getPassiveCalendar().ifPresent(this.dataModel::remove);
    }

    Optional<PassiveCalendar> getPlannedPassiveCalendar() {
        return this.plannedPassiveCalendar.getOptional();
    }

    void setPlannedPassiveCalendar(PassiveCalendar calendar) {
        this.clearPlannedPassiveCalendar();
        this.plannedPassiveCalendar.set(calendar);
    }

    void clearPlannedPassiveCalendar() {
        this.plannedPassiveCalendar.set(null);
        this.getPlannedPassiveCalendar().ifPresent(this.dataModel::remove);
    }

    @Override
    public void runStatusInformationTask(Consumer<ComTaskExecution> requestedAction) {
        Optional<ComTaskExecution> comTaskExecution = Optional.empty();
        Optional<ComTaskExecution> bestComTaskExecution = getComTaskExecutions().stream()
                .filter(cte -> containsStatusInformationProtocolTask(cte.getProtocolTasks()))
                .sorted((cte1, cte2) -> compareProtocolTasks(cte1.getProtocolTasks(), cte2.getProtocolTasks()))
                .findFirst();

        Optional<ComTaskEnablement> bestComTaskEnablement = getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> containsStatusInformationProtocolTask(comTaskEnablement.getComTask().getProtocolTasks()))
                .sorted((cte1, cte2) -> compareProtocolTasks(cte1.getComTask().getProtocolTasks(), cte2.getComTask().getProtocolTasks()))
                .findFirst();

        if (bestComTaskExecution.isPresent() && bestComTaskEnablement.isPresent()) {
            if (bestComTaskExecution.get().getComTasks().contains(bestComTaskEnablement.get().getComTask())) {
                comTaskExecution = bestComTaskExecution;
            } else {
                comTaskExecution = createAdHocComTaskExecutionToRunNow(bestComTaskEnablement.get());
            }
        } else if (bestComTaskExecution.isPresent()) {
            comTaskExecution = bestComTaskExecution;
        } else if (bestComTaskEnablement.isPresent()) {
            comTaskExecution = createAdHocComTaskExecutionToRunNow(bestComTaskEnablement.get());
        }

        if (!comTaskExecution.isPresent()) {
            throw new NoStatusInformationTaskException();
        }

        requestedAction.accept(comTaskExecution.get());
    }

    private Optional<ComTaskExecution> createAdHocComTaskExecutionToRunNow(ComTaskEnablement enablement) {
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = newAdHocComTaskExecution(enablement);
        if (enablement.hasPartialConnectionTask()) {
            getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == enablement.getPartialConnectionTask().get().getId())
                    .forEach(comTaskExecutionBuilder::connectionTask);
        }
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        save();
        return Optional.of(comTaskExecution);
    }

    private boolean containsStatusInformationProtocolTask(List<ProtocolTask> protocolTasks) {
        return protocolTasks
                .stream()
                .anyMatch(protocolTask -> protocolTask instanceof StatusInformationTask);
    }

    private int compareProtocolTasks(List<ProtocolTask> protocolTasks1, List<ProtocolTask> protocolTasks2) {
        return compareScores(determineScore(protocolTasks1), determineScore(protocolTasks2));
    }

    private int compareScores(Integer[] scores1, Integer[] scores2) {
        for (int i = 0; i < Math.min(scores1.length, scores2.length); i++) {
            if (scores1[i] < scores2[i]) {
                return -1;
            } else if (scores1[i] > scores1[i]) {
                return 1;
            }
        }
        return 0;
    }

    private Integer[] determineScore(List<ProtocolTask> protocolTasks) {
        return protocolTasks.stream()
                .map(protocolTask -> score(protocolTask.getClass()))
                .sorted(Comparator.<Integer>naturalOrder().reversed())
                .toArray(Integer[]::new);
    }

    private int score(Class<? extends ProtocolTask> protocolTaskClass) {
        return getProtocolTasksScores()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().test(protocolTaskClass))
                .map(Map.Entry::getValue)
                .max(Comparator.naturalOrder())
                .orElse(0);
    }

    private Map<Predicate<Class<? extends ProtocolTask>>, Integer> getProtocolTasksScores() {
        if (scorePerProtocolTask == null) {
            scorePerProtocolTask = new HashMap<>();
            scorePerProtocolTask.put(StatusInformationTask.class::isAssignableFrom, 1);
            scorePerProtocolTask.put(BasicCheckTask.class::isAssignableFrom, 2);
            scorePerProtocolTask.put(ClockTask.class::isAssignableFrom, 2);
            scorePerProtocolTask.put(TopologyTask.class::isAssignableFrom, 3);
            scorePerProtocolTask.put(RegistersTask.class::isAssignableFrom, 4);
            scorePerProtocolTask.put(LogBooksTask.class::isAssignableFrom, 4);
            scorePerProtocolTask.put(LoadProfilesTask.class::isAssignableFrom, 4);
            scorePerProtocolTask.put(MessagesTask.class::isAssignableFrom, 5);
            scorePerProtocolTask.put(FirmwareManagementTask.class::isAssignableFrom, 6);
        }

        return scorePerProtocolTask;
    }

    @Override
    public Optional<State> getState(Instant instant) {
        if (this.id > 0) {
            if (this.meter.isPresent()) {
                return this.meter.get().getState(instant);
            } else {
                // Kore meter was not created yet
                throw new IllegalStateException("Kore meter was not created when this Device was created");
            }
        } else {
            return Optional.of(this.getDeviceType().getDeviceLifeCycle().getFiniteStateMachine().getInitialState());
        }
    }

    @Override
    public StateTimeline getStateTimeline() {
        return this.getOptionalMeterAspect(EndDevice::getStateTimeline).get();
    }

    @Override
    public CIMLifecycleDates getLifecycleDates() {
        if (this.meter.isPresent()) {
            return new CIMLifecycleDatesImpl(this.meter.get(), meter.get().getLifecycleDates());
        } else {
            return new NoCimLifecycleDates();
        }
    }

    @Override
    public List<DeviceLifeCycleChangeEvent> getDeviceLifeCycleChangeEvents() {
        // Merge the StateTimeline with the list of change events from my DeviceType.
        Deque<StateTimeSlice> stateTimeSlices = new LinkedList<>(this.getStateTimeline().getSlices());
        Deque<com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent> deviceTypeChangeEvents = new LinkedList<>(this.getDeviceTypeLifeCycleChangeEvents());
        List<DeviceLifeCycleChangeEvent> changeEvents = new ArrayList<>();
        boolean notReady;
        do {
            DeviceLifeCycleChangeEvent newEvent = this.newEventForMostRecent(stateTimeSlices, deviceTypeChangeEvents);
            changeEvents.add(newEvent);
            notReady = !stateTimeSlices.isEmpty() || !deviceTypeChangeEvents.isEmpty();
        } while (notReady);
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
        } else if (deviceTypeChangeEvents.isEmpty()) {
            return DeviceLifeCycleChangeEventImpl.from(stateTimeSlices.removeFirst());
        } else {
            // Compare both timestamps and create event from the most recent one
            StateTimeSlice stateTimeSlice = stateTimeSlices.peekFirst();
            com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent deviceLifeCycleChangeEvent = deviceTypeChangeEvents.peekFirst();
            if (stateTimeSlice.getPeriod().lowerEndpoint().equals(deviceLifeCycleChangeEvent.getTimestamp())) {
                // Give precedence to the device life cycle change but also consume the state change so the latter is ignored
                stateTimeSlices.removeFirst();
                return DeviceLifeCycleChangeEventImpl.from(deviceTypeChangeEvents.removeFirst());
            } else if (stateTimeSlice.getPeriod().lowerEndpoint().isBefore(deviceLifeCycleChangeEvent.getTimestamp())) {
                return DeviceLifeCycleChangeEventImpl.from(stateTimeSlices.removeFirst());
            } else {
                return DeviceLifeCycleChangeEventImpl.from(deviceTypeChangeEvents.removeFirst());
            }
        }
    }

    public Optional<ReadingTypeObisCodeUsage> getReadingTypeObisCodeUsage(ReadingType readingType) {
        if (readingType == null) {
            return Optional.empty();
        }
        for (ReadingTypeObisCodeUsageImpl readingTypeObisCodeUsage : readingTypeObisCodeUsages) {
            if (readingTypeObisCodeUsage.getReadingType().getMRID().equals(readingType.getMRID())) {
                return Optional.of(readingTypeObisCodeUsage);
            }
        }
        return Optional.empty();
    }

    void addReadingTypeObisCodeUsage(ReadingType readingType, ObisCode obisCode) {
        ReadingTypeObisCodeUsageImpl readingTypeObisCodeUsage = dataModel.getInstance(ReadingTypeObisCodeUsageImpl.class);
        readingTypeObisCodeUsage.initialize(this, readingType, obisCode);
        readingTypeObisCodeUsages.add(readingTypeObisCodeUsage);
    }

    void removeReadingTypeObisCodeUsage(ReadingType readingType) {
        for (java.util.Iterator<ReadingTypeObisCodeUsageImpl> iterator = readingTypeObisCodeUsages.iterator(); iterator.hasNext(); ) {
            ReadingTypeObisCodeUsageImpl readingTypeObisCodeUsage = iterator.next();
            if (readingTypeObisCodeUsage.getReadingType().getMRID().equals(readingType.getMRID())) {
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceImpl device = (DeviceImpl) o;
        return id == device.id;
    }

    private enum RegisterFactory {
        Text {
            @Override
            boolean appliesTo(RegisterSpec registerSpec) {
                return registerSpec.isTextual();
            }

            @Override
            RegisterImpl newRegister(DeviceImpl device, RegisterSpec registerSpec) {
                return new TextRegisterImpl(device, (TextualRegisterSpec) registerSpec);
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
                return new BillingRegisterImpl(device, (NumericalRegisterSpec) registerSpec);
            }
        },

        Flags {
            @Override
            boolean appliesTo(RegisterSpec registerSpec) {
                return this.getReadingType(registerSpec).getUnit().equals(ReadingTypeUnit.BOOLEANARRAY);
            }

            @Override
            RegisterImpl newRegister(DeviceImpl device, RegisterSpec registerSpec) {
                return new FlagsRegisterImpl(device, (NumericalRegisterSpec) registerSpec);
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
                return new NumericalRegisterImpl(device, (NumericalRegisterSpec) registerSpec);
            }
        };

        ReadingType getReadingType(RegisterSpec registerSpec) {
            return registerSpec.getRegisterType().getReadingType();
        }

        abstract boolean appliesTo(RegisterSpec registerSpec);

        abstract RegisterImpl newRegister(DeviceImpl device, RegisterSpec registerSpec);
    }

    private class InternalDeviceMessageBuilder implements DeviceMessageBuilder {

        private final DeviceMessageImpl deviceMessage;

        private InternalDeviceMessageBuilder(DeviceMessageId deviceMessageId, TrackingCategory trackingCategory) {
            deviceMessage = DeviceImpl.this.dataModel.getInstance(DeviceMessageImpl.class).initialize(DeviceImpl.this, deviceMessageId);
            deviceMessage.setTrackingCategory(trackingCategory);
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

    private class ConnectionInitiationTaskBuilderForDevice extends ConnectionInitiationTaskImpl.AbstractConnectionInitiationTaskBuilder {

        private ConnectionInitiationTaskBuilderForDevice(Device device, PartialConnectionInitiationTask partialConnectionInitiationTask) {
            super(connectionInitiationTaskProvider.get());
            this.getConnectionInitiationTask().initialize(device, partialConnectionInitiationTask, partialConnectionInitiationTask.getComPortPool());
        }

        @Override
        public ConnectionInitiationTaskBuilder setComPortPool(OutboundComPortPool comPortPool) {
            this.getConnectionInitiationTask().setComPortPool(comPortPool);
            return this;
        }

        @Override
        public ConnectionInitiationTaskBuilder setProperty(String propertyName, Object value) {
            this.getConnectionInitiationTask().setProperty(propertyName, value);
            return this;
        }

        @Override
        public ConnectionInitiationTask add() {
            return (ConnectionInitiationTask) DeviceImpl.this.add(this.getConnectionInitiationTask());
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
            return (InboundConnectionTask) DeviceImpl.this.add(this.getInboundConnectionTask());
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
            return (ScheduledConnectionTaskImpl) DeviceImpl.this.add(this.getScheduledConnectionTask());
        }
    }

    private class ScheduledComTaskExecutionBuilderForDevice
            extends ScheduledComTaskExecutionImpl.ScheduledComTaskExecutionBuilderImpl {

        private Set<ComTaskExecution> executionsToDelete;

        private ScheduledComTaskExecutionBuilderForDevice(Provider<ScheduledComTaskExecutionImpl> comTaskExecutionProvider, ComSchedule comSchedule) {
            super(comTaskExecutionProvider.get());
            this.initExecutionsToDelete(comSchedule);
            this.getComTaskExecution().initialize(DeviceImpl.this, comSchedule);
        }

        private void initExecutionsToDelete(ComSchedule comSchedule) {
            Set<Long> comScheduleComTasks = comSchedule.getComTasks().stream().map(ComTask::getId).collect(Collectors.toSet());
            this.executionsToDelete = DeviceImpl.this.getComTaskExecutionImpls()
                    .filter(Predicates.not(ComTaskExecution::usesSharedSchedule))
                    .filter(cte -> comScheduleComTasks.contains(cte.getComTasks().get(0).getId()))
                    .collect(Collectors.toSet());
        }

        @Override
        public ScheduledComTaskExecution add() {
            executionsToDelete.forEach(DeviceImpl.this::removeComTaskExecution);
            ScheduledComTaskExecution comTaskExecution = super.add();
            return (ScheduledComTaskExecution) DeviceImpl.this.add((ComTaskExecutionImpl) comTaskExecution);
        }
    }

    private class AdHocComTaskExecutionBuilderForDevice
            extends ManuallyScheduledComTaskExecutionImpl.ManuallyScheduledComTaskExecutionBuilderImpl {

        private AdHocComTaskExecutionBuilderForDevice(Provider<ManuallyScheduledComTaskExecutionImpl> comTaskExecutionProvider, ComTaskEnablement comTaskEnablement) {
            super(comTaskExecutionProvider.get());
            this.getComTaskExecution().initializeAdhoc(DeviceImpl.this, comTaskEnablement);
        }


        @Override
        public ManuallyScheduledComTaskExecution add() {
            ManuallyScheduledComTaskExecution comTaskExecution = super.add();
            return (ManuallyScheduledComTaskExecution) DeviceImpl.this.add((ComTaskExecutionImpl) comTaskExecution);
        }
    }

    private class FirmwareComTaskExecutionBuilderForDevice extends FirmwareComTaskExecutionImpl.FirmwareComTaskExecutionBuilderImpl {

        private FirmwareComTaskExecutionBuilderForDevice(Provider<FirmwareComTaskExecutionImpl> comTaskExecutionProvider, ComTaskEnablement comTaskEnablement) {
            super(comTaskExecutionProvider.get());
            this.getComTaskExecution().initializeFirmwareTask(DeviceImpl.this, comTaskEnablement);
        }

        @Override
        public FirmwareComTaskExecution add() {
            FirmwareComTaskExecution firmwareComTaskExecution = super.add();
            return (FirmwareComTaskExecution) DeviceImpl.this.add((ComTaskExecutionImpl) firmwareComTaskExecution);
        }
    }

    private class ManuallyScheduledComTaskExecutionBuilderForDevice
            extends ManuallyScheduledComTaskExecutionImpl.ManuallyScheduledComTaskExecutionBuilderImpl {

        private ManuallyScheduledComTaskExecutionBuilderForDevice(Provider<ManuallyScheduledComTaskExecutionImpl> comTaskExecutionProvider, ComTaskEnablement comTaskEnablement, TemporalExpression temporalExpression) {
            super(comTaskExecutionProvider.get());
            this.getComTaskExecution().initialize(DeviceImpl.this, comTaskEnablement, temporalExpression);
        }

        @Override
        public ManuallyScheduledComTaskExecution add() {
            ManuallyScheduledComTaskExecution comTaskExecution = super.add();
            return (ManuallyScheduledComTaskExecution) DeviceImpl.this.add((ComTaskExecutionImpl) comTaskExecution);
        }
    }

    @Override
    public Channel.ChannelUpdater getChannelUpdaterFor(Channel channel) {
        return new ChannelUpdaterImpl(this.meteringService, this.readingTypeUtilService, this.clock, channel);
    }

    @Override
    public Register.RegisterUpdater getRegisterUpdaterFor(Register register) {
        return new RegisterUpdaterImpl(this.meteringService, this.readingTypeUtilService, this.clock, register);
    }

    private class CIMLifecycleDatesImpl implements CIMLifecycleDates {
        private final EndDevice koreDevice;
        private final LifecycleDates koreLifecycleDates;

        private CIMLifecycleDatesImpl(EndDevice koreDevice, LifecycleDates koreLifecycleDates) {
            super();
            this.koreDevice = koreDevice;
            this.koreLifecycleDates = koreLifecycleDates;
        }

        @Override
        public Optional<Instant> getManufacturedDate() {
            return koreLifecycleDates.getManufacturedDate();
        }

        @Override
        public CIMLifecycleDates setManufacturedDate(Instant manufacturedDate) {
            koreLifecycleDates.setManufacturedDate(manufacturedDate);
            return this;
        }

        @Override
        public Optional<Instant> getPurchasedDate() {
            return koreLifecycleDates.getPurchasedDate();
        }

        @Override
        public CIMLifecycleDates setPurchasedDate(Instant purchasedDate) {
            koreLifecycleDates.setPurchasedDate(purchasedDate);
            return this;
        }

        @Override
        public Optional<Instant> getReceivedDate() {
            return koreLifecycleDates.getReceivedDate();
        }

        @Override
        public CIMLifecycleDates setReceivedDate(Instant receivedDate) {
            koreLifecycleDates.setReceivedDate(receivedDate);
            return this;
        }

        @Override
        public Optional<Instant> getInstalledDate() {
            return koreLifecycleDates.getInstalledDate();
        }

        @Override
        public CIMLifecycleDates setInstalledDate(Instant installedDate) {
            koreLifecycleDates.setInstalledDate(installedDate);
            return this;
        }

        @Override
        public Optional<Instant> getRemovedDate() {
            return koreLifecycleDates.getRemovedDate();
        }

        @Override
        public CIMLifecycleDates setRemovedDate(Instant removedDate) {
            koreLifecycleDates.setRemovedDate(removedDate);
            return this;
        }

        @Override
        public Optional<Instant> getRetiredDate() {
            return koreLifecycleDates.getRetiredDate();
        }

        @Override
        public CIMLifecycleDates setRetiredDate(Instant retiredDate) {
            koreLifecycleDates.setRetiredDate(retiredDate);
            return this;
        }

        @Override
        public void save() {
            this.koreDevice.update();
        }
    }

    private class NoCimLifecycleDates implements CIMLifecycleDates {
        @Override
        public Optional<Instant> getManufacturedDate() {
            return Optional.empty();
        }

        @Override
        public CIMLifecycleDates setManufacturedDate(Instant manufacturedDate) {
            // Ignore blissfully
            return this;
        }

        @Override
        public Optional<Instant> getPurchasedDate() {
            return Optional.empty();
        }

        @Override
        public CIMLifecycleDates setPurchasedDate(Instant purchasedDate) {
            // Ignore blissfully
            return this;
        }

        @Override
        public Optional<Instant> getReceivedDate() {
            return Optional.empty();
        }

        @Override
        public CIMLifecycleDates setReceivedDate(Instant receivedDate) {
            // Ignore blissfully
            return this;
        }

        @Override
        public Optional<Instant> getInstalledDate() {
            return Optional.empty();
        }

        @Override
        public CIMLifecycleDates setInstalledDate(Instant installedDate) {
            // Ignore blissfully
            return this;
        }

        @Override
        public Optional<Instant> getRemovedDate() {
            return Optional.empty();
        }

        @Override
        public CIMLifecycleDates setRemovedDate(Instant removedDate) {
            // Ignore blissfully
            return this;
        }

        @Override
        public Optional<Instant> getRetiredDate() {
            return Optional.empty();
        }

        @Override
        public CIMLifecycleDates setRetiredDate(Instant retiredDate) {
            // Ignore blissfully
            return this;
        }

        @Override
        public void save() {
            // Since there were no dates to start with, there is nothing to save
        }
    }

    private class CanUpdateMeterActivationLast implements Comparator<SyncDeviceWithKoreMeter> {
        @Override
        public int compare(SyncDeviceWithKoreMeter o1, SyncDeviceWithKoreMeter o2) {
            int a = o1.canUpdateCurrentMeterActivation() ? 1 : 0;
            int b = o2.canUpdateCurrentMeterActivation() ? 1 : 0;
            return new Integer(a).compareTo(b);
        }
    }

    @Target({java.lang.annotation.ElementType.TYPE, ElementType.FIELD})
    @Retention(RUNTIME)
    @Documented
    @Constraint(validatedBy = {ShipmentDateValidator.class})
    @interface HasValidShipmentDate {
        String message() default "";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    private static class ShipmentDateValidator implements ConstraintValidator<HasValidShipmentDate, DeviceImpl> {

        @Override
        public void initialize(HasValidShipmentDate constraintAnnotation) {

        }

        @Override
        public boolean isValid(DeviceImpl device, ConstraintValidatorContext context) {
            if (!device.koreHelper.getInitialMeterActivationStartDate().isPresent()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{" + MessageSeeds.FIELD_IS_REQUIRED.getKey() + "}")
                        .addPropertyNode("shipmentDate").addConstraintViolation();
                return false;
            }
            return true;
        }
    }
}
