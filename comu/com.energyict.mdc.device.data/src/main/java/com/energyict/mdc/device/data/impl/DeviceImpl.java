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
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.GeoCoordinates;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.ami.EndDeviceCapabilities;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.ReadingQuality;
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
import com.elster.jupiter.users.FormatKey;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreference;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.streams.Predicates;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.DateTimeFormatGenerator;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceConfiguration;
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
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
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
import com.energyict.mdc.device.data.PassiveEffectiveCalendar;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.ReadingTypeObisCodeUsage;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exceptions.CannotChangeDeviceConfigStillUnresolvedConflicts;
import com.energyict.mdc.device.data.exceptions.CannotDeleteComScheduleFromDevice;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;
import com.energyict.mdc.device.data.exceptions.DeviceProtocolPropertyException;
import com.energyict.mdc.device.data.exceptions.MultiplierConfigurationException;
import com.energyict.mdc.device.data.exceptions.NoMeterActivationAt;
import com.energyict.mdc.device.data.exceptions.ProtocolDialectConfigurationPropertiesIsRequiredException;
import com.energyict.mdc.device.data.exceptions.UnsatisfiedReadingTypeRequirementsOfUsagePointException;
import com.energyict.mdc.device.data.exceptions.UsagePointAlreadyLinkedToAnotherDeviceException;
import com.energyict.mdc.device.data.impl.configchange.ServerDeviceForConfigChange;
import com.energyict.mdc.device.data.impl.configchange.ServerSecurityPropertyServiceForConfigChange;
import com.energyict.mdc.device.data.impl.constraintvalidators.DeviceConfigurationIsPresentAndActive;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueComTaskScheduling;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueMrid;
import com.energyict.mdc.device.data.impl.constraintvalidators.ValidOverruledAttributes;
import com.energyict.mdc.device.data.impl.constraintvalidators.ValidSecurityProperties;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.security.ServerDeviceForValidation;
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
import com.energyict.mdc.tasks.ComTask;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.stream.Collectors.toList;

@LiteralSql
@UniqueMrid(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_DEVICE_MRID + "}")
@UniqueComTaskScheduling(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_COMTASK + "}")
@ValidSecurityProperties(groups = {Save.Update.class})
@ValidOverruledAttributes(groups = {Save.Update.class})
public class DeviceImpl implements Device, ServerDeviceForConfigChange, ServerDeviceForValidation {

    private static final BigDecimal maxMultiplier = BigDecimal.valueOf(Integer.MAX_VALUE);
    private static final String MULTIPLIER_TYPE = "Default";
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
    private final List<LoadProfile> loadProfiles = new ArrayList<>();
    private final List<LogBook> logBooks = new ArrayList<>();
    private List<ReadingTypeObisCodeUsageImpl> readingTypeObisCodeUsages = new ArrayList<>();
    private final BigDecimal MULTIPLIER_ONE = BigDecimal.ONE;
    private final Reference<DeviceType> deviceType = ValueReference.absent();
    @DeviceConfigurationIsPresentAndActive(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    private final Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider;
    private final Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider;
    private final Provider<ConnectionInitiationTaskImpl> connectionInitiationTaskProvider;
    private final Provider<ScheduledComTaskExecutionImpl> scheduledComTaskExecutionProvider;
    private final Provider<ManuallyScheduledComTaskExecutionImpl> manuallyScheduledComTaskExecutionProvider;
    private final Provider<FirmwareComTaskExecutionImpl> firmwareComTaskExecutionProvider;
    private final Reference<DeviceEstimation> deviceEstimation = ValueReference.absent();
    @SuppressWarnings("unused")
    private long id;
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
    private List<PassiveEffectiveCalendar> passiveCalendars = new ArrayList<>();
    private TemporalReference<ActiveEffectiveCalendar> activeCalendar = Temporals.absent();

    private Map<SecurityPropertySet, TypedProperties> dirtySecurityProperties = new HashMap<>();
    private transient DeviceValidationImpl deviceValidation;
    private transient Optional<Meter> meter = Optional.empty();
    private transient AmrSystem amrSystem;
    private transient Optional<MeterActivation> currentMeterActivation = Optional.empty();
    private transient MultiplierType multiplierType;
    private transient BigDecimal multiplier;

    private Optional<Location> location = Optional.empty();
    private Optional<GeoCoordinates> geoCoordinates = Optional.empty();
    private boolean dirtyMeter = false;


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
            UserPreferencesService userPreferencesService) {
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
    }

    DeviceImpl initialize(DeviceConfiguration deviceConfiguration, String name, String mRID) {
        this.createTime = this.clock.instant();
        this.deviceConfiguration.set(deviceConfiguration);
        this.setDeviceTypeFromDeviceConfiguration();
        setName(name);
        this.setmRID(mRID);
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
            if (this.dirtyMeter) {
                findKoreMeter(getMdcAmrSystem()).ifPresent(EndDevice::update);
            }

            this.saveDirtySecurityProperties();
            this.saveDirtyConnectionProperties();
            this.saveNewAndDirtyDialectProperties();
            this.notifyUpdated();
        } else {
            Save.CREATE.save(dataModel, this);
            Meter meter = this.createKoreMeter();
            if (this.location.isPresent()) {
                meter.setLocation(location.get());
            }
            if (this.geoCoordinates.isPresent()) {
                meter.setGeoCoordinates(geoCoordinates.get());
            }
            this.createMeterConfiguration(meter, this.clock.instant(), false);
            this.saveNewDialectProperties();
            this.createComTaskExecutionsForEnablementsMarkedAsAlwaysExecuteForInbound();
            this.notifyCreated();
        }
    }

    @Override
    public void delete() {
        this.notifyDeviceIsGoingToBeDeleted();
        this.doDelete();
        this.notifyDeleted();
    }

    @Override
    public Optional<Location> getLocation() {
        Optional<Meter> meter = findKoreMeter(getMdcAmrSystem());
        if (meter.isPresent()) {
            return meter.get().getLocation();
        }
        return this.location;
    }

    @Override
    public void setLocation(Location location) {
        Optional<Meter> meter = findKoreMeter(getMdcAmrSystem());
        if (meter.isPresent()) {
            meter.get().setLocation(location);
            this.dirtyMeter = true;
        } else {
            this.location = Optional.ofNullable(location);
        }
    }

    @Override
    public Optional<GeoCoordinates> getGeoCoordinates() {
        Optional<Meter> meter = findKoreMeter(getMdcAmrSystem());
        if (meter.isPresent()) {
            return meter.get().getGeoCoordinates();
        }
        return this.geoCoordinates;
    }

    @Override
    public void setGeoCoordinates(GeoCoordinates geoCoordinates) {
        Optional<Meter> meter = findKoreMeter(getMdcAmrSystem());
        if (meter.isPresent()) {
            meter.get().setGeoCoordinates(geoCoordinates);
            this.dirtyMeter = true;
        } else {
            this.geoCoordinates = Optional.ofNullable(geoCoordinates);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = null;
        if (name != null) {
            this.name = name.trim();
        }
    }

    @Override
    public DeviceType getDeviceType() {
        return this.getDeviceConfiguration().getDeviceType();
    }

    @Override
    public List<DeviceMessage<Device>> getMessages() {
        return Collections.unmodifiableList(this.deviceMessages);
    }

    @Override
    public List<DeviceMessage<Device>> getMessagesByState(DeviceMessageStatus status) {
        return this.deviceMessages.stream()
                .filter(deviceMessage -> deviceMessage.getStatus().equals(status))
                .collect(toList());
    }

    @Override
    public DeviceProtocolPluggableClass getDeviceProtocolPluggableClass() {
        return getDeviceType().getDeviceProtocolPluggableClass();
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration.orNull();
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

    @Override
    public void setZone(ZoneId zone) {
        if (zone != null) {
            this.timeZoneId = zone.getId();
        } else {
            this.timeZoneId = "";
        }
        this.zoneId = zone;
    }

    /**
     * @deprecated use setZone
     */
    @Deprecated
    @Override
    public void setTimeZone(TimeZone timeZone) {
        setZone(timeZone.toZoneId());
    }

    private void saveDirtyConnectionProperties() {
        this.getConnectionTaskImpls()
                .filter(ConnectionTaskImpl::hasDirtyProperties)
                .forEach(ConnectionTaskPropertyProvider::saveAllProperties);
    }

    private void createMeterConfiguration(Meter meter, Instant timeStamp, boolean validMultiplierSet) {
        meter.getConfiguration(timeStamp).ifPresent(meterConfiguration -> meterConfiguration.endAt(timeStamp));
        if (getDeviceConfiguration().getChannelSpecs().size() > 0 || getDeviceConfiguration().getRegisterSpecs().size() > 0) {
            MultiplierType defaultMultiplierType = getDefaultMultiplierType();
            Meter.MeterConfigurationBuilder meterConfigurationBuilder = meter.startingConfigurationOn(timeStamp);
            createMeterConfigurationsForChannelSpecs(defaultMultiplierType, meterConfigurationBuilder, validMultiplierSet);
            createMeterConfigurationsForRegisterSpecs(defaultMultiplierType, meterConfigurationBuilder, validMultiplierSet);
            meterConfigurationBuilder.create();
        }
    }

    private void createMeterConfigurationsForChannelSpecs(MultiplierType defaultMultiplierType, Meter.MeterConfigurationBuilder meterConfigurationBuilder, boolean addCalculatedReadingType) {
        getDeviceConfiguration().getChannelSpecs().forEach(channelSpec -> {
            Meter.MeterReadingTypeConfigurationBuilder meterReadingTypeConfigurationBuilder = meterConfigurationBuilder
                    .configureReadingType(channelSpec.getReadingType())
                    .withNumberOfFractionDigits(channelSpec.getNbrOfFractionDigits());
            channelSpec.getOverflow().ifPresent(meterReadingTypeConfigurationBuilder::withOverflowValue);
            if (addCalculatedReadingType && channelSpec.isUseMultiplier()) {
                meterReadingTypeConfigurationBuilder
                        .withMultiplierOfType(defaultMultiplierType)
                        .calculating(getMultipliedReadingTypeForChannelSpec(channelSpec));
            }
        });
    }

    private void createMeterConfigurationsForRegisterSpecs(MultiplierType defaultMultiplierType, Meter.MeterConfigurationBuilder meterConfigurationBuilder, boolean addCalculatedReadingType) {
        getDeviceConfiguration().getRegisterSpecs().stream().filter(registerSpec -> !registerSpec.isTextual())
                .map(registerSpec1 -> ((NumericalRegisterSpec) registerSpec1)).forEach(registerSpec -> {
            Meter.MeterReadingTypeConfigurationBuilder meterReadingTypeConfigurationBuilder = meterConfigurationBuilder
                    .configureReadingType(registerSpec.getReadingType())
                    .withNumberOfFractionDigits(registerSpec.getNumberOfFractionDigits());
            registerSpec.getOverflowValue().ifPresent(meterReadingTypeConfigurationBuilder::withOverflowValue);
            if (addCalculatedReadingType && registerSpec.isUseMultiplier()) {
                meterReadingTypeConfigurationBuilder
                        .withMultiplierOfType(defaultMultiplierType)
                        .calculating(registerSpec.getCalculatedReadingType().get());
            }
        });
    }

    private ReadingType getMultipliedReadingTypeForChannelSpec(ChannelSpec channelSpec) {
        ReadingType calculatedReadingType = channelSpec.getCalculatedReadingType().get();
        if (channelSpec.getReadingType().isCumulative()) {
            String code = readingTypeUtilService.createReadingTypeCodeBuilderFrom(calculatedReadingType)
                    .accumulate(channelSpec.getReadingType().getAccumulation()).code();
            return readingTypeUtilService.findOrCreateReadingType(code, calculatedReadingType.getAliasName());
        } else {
            return calculatedReadingType;
        }
    }

    private void createComTaskExecutionsForEnablementsMarkedAsAlwaysExecuteForInbound() {
        List<ComTaskEnablement> comTaskEnablements = getDeviceConfiguration().getComTaskEnablements();
        comTaskEnablements.stream().filter(ComTaskEnablement::isIgnoreNextExecutionSpecsForInbound).forEach(cte -> newManuallyScheduledComTaskExecution(cte, null).add());
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
        closeCurrentMeterActivation();
        this.obsoleteKoreDevice();
        this.getDataMapper().remove(this);
    }

    private void removeCustomProperties() {
        this.getDeviceType().getCustomPropertySets().forEach(this::removeCustomPropertiesFor);
        this.getRegisters().stream()
                .forEach(register ->
                        this.getDeviceType().getRegisterTypeTypeCustomPropertySet(register.getRegisterSpec().getRegisterType())
                                .ifPresent(set -> this.customPropertySetService.removeValuesFor(set.getCustomPropertySet(), register.getRegisterSpec(), this.getId()))
                );

        this.getLoadProfiles().stream().flatMap(lp -> lp.getChannels().stream())
                .forEach(channel ->
                        this.getDeviceType().getLoadProfileTypeCustomPropertySet(channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType())
                                .ifPresent(set -> this.customPropertySetService.removeValuesFor(set.getCustomPropertySet(), channel.getChannelSpec(), this.getId()))
                );
    }

    private void removeCustomPropertiesFor(RegisteredCustomPropertySet customPropertySet) {
        this.removeCustomPropertiesFor(customPropertySet.getCustomPropertySet());
    }

    @SuppressWarnings("unchecked")
    private void removeCustomPropertiesFor(CustomPropertySet customPropertySet) {
        this.customPropertySetService.removeValuesFor(customPropertySet, this);
    }

    private void obsoleteKoreDevice() {
        findMdcAmrSystem().ifPresent(this::obsoleteKoreDevice);
    }

    private void obsoleteKoreDevice(AmrSystem amrSystem) {
        this.findKoreMeter(amrSystem).ifPresent(Meter::makeObsolete);
    }

    private void removeDeviceFromStaticGroups() {
        findMdcAmrSystem().ifPresent(amrSystem ->
                findKoreMeter(amrSystem).ifPresent(meter ->
                        this.meteringGroupsService.findEnumeratedEndDeviceGroupsContaining(meter).stream()
                                .forEach(enumeratedEndDeviceGroup -> removeDeviceFromGroup(enumeratedEndDeviceGroup, meter))));
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

    @Override
    public void setmRID(String mRID) {
        this.mRID = null;
        Optional.ofNullable(mRID)
                .map(String::trim)
                .ifPresent(trimmed -> this.mRID = trimmed);

        Optional<Meter> meter = findKoreMeter(getMdcAmrSystem());
        meter.ifPresent(foundMeter -> {
            if (foundMeter.getMRID() == null ||
                    !foundMeter.getMRID().equals(this.getmRID())) {
                foundMeter.setMRID(this.mRID);
                this.dirtyMeter = true;
            }
        });
    }

    private void closeCurrentMeterActivation() {
        getCurrentMeterActivation().ifPresent(meterActivation -> meterActivation.endAt(clock.instant()));
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

    public long getId() {
        return id;
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
    public void setYearOfCertification(Integer yearOfCertification) {
        this.yearOfCertification = yearOfCertification;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public BigDecimal getMultiplier() {
        if (this.multiplier == null) {
            Optional<MeterActivation> optionalCurrentMeterActivation = getCurrentMeterActivation();
            if (optionalCurrentMeterActivation.isPresent()) {
                this.multiplier = optionalCurrentMeterActivation.get().getMultiplier(getDefaultMultiplierType()).orElse(MULTIPLIER_ONE);
            } else {
                this.multiplier = MULTIPLIER_ONE;
            }
        }
        return this.multiplier;
    }

    @Override
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public Optional<BigDecimal> getMultiplierAt(Instant multiplierEffectiveTimeStamp) {
        List<MeterActivation> meterActivationsMostRecentFirst = getMeterActivationsMostRecentFirst();
        Optional<MeterActivation> meterActivationForEffectiveTimeStamp = meterActivationsMostRecentFirst.stream()
                .filter(meterActivation -> meterActivation.getInterval().toOpenClosedRange().contains(multiplierEffectiveTimeStamp))
                .findAny();
        if (meterActivationForEffectiveTimeStamp.isPresent()) {
            return meterActivationForEffectiveTimeStamp.get().getMultiplier(getDefaultMultiplierType());
        } else {
            return Optional.empty();
        }
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
    public Instant getMultiplierEffectiveTimeStamp() {
        List<MeterActivation> meterActivationsMostRecentFirst = getMeterActivationsMostRecentFirst();
        Instant effectiveTimeStamp = clock.instant();
        BigDecimal currentMultiplier = getMultiplier();
        for (MeterActivation meterActivation : meterActivationsMostRecentFirst) {
            Optional<BigDecimal> multiplier = meterActivation.getMultiplier(getDefaultMultiplierType());
            if (multiplier.isPresent()) {
                if (currentMultiplier.compareTo(multiplier.get()) == 0) {
                    effectiveTimeStamp = meterActivation.getStart();
                } else {
                    break;
                }
            }
        }
        return effectiveTimeStamp;
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

    public List<LoadProfile> getLoadProfiles() {
        return Collections.unmodifiableList(this.loadProfiles);
    }

    @Override
    public boolean isLogicalSlave() {
        return getDeviceType().isLogicalSlave();
    }

    @Override
    public void setMultiplier(BigDecimal multiplier) {
        this.setMultiplier(multiplier, null);
    }

    private MultiplierType getDefaultMultiplierType() {
        if (this.multiplierType == null) {
            Optional<MultiplierType> multiplierType = meteringService.getMultiplierType(MULTIPLIER_TYPE);
            if (multiplierType.isPresent()) {
                this.multiplierType = multiplierType.get();
            } else {
                this.multiplierType = createDefaultMultiplierType();
            }
        }
        return this.multiplierType;
    }

    @Override
    public void setMultiplier(BigDecimal multiplier, Instant from) {
        if (multiplier.compareTo(getMultiplier()) != 0) {
            validateMultiplierValue(multiplier);
            Instant now = clock.instant();
            Optional<Instant> startDateMultiplier = Optional.ofNullable(from);
            validateStartDateOfNewMultiplier(now, startDateMultiplier);
            boolean validMultiplierValue = multiplier.compareTo(BigDecimal.ONE) == 1;
            MeterActivation newMeterActivation = activate(startDateMultiplier.orElse(now), validMultiplierValue);
            if (validMultiplierValue) {
                newMeterActivation.setMultiplier(getDefaultMultiplierType(), multiplier);
            }
            createMeterConfiguration(findOrCreateKoreMeter(getMdcAmrSystem()), startDateMultiplier.orElse(now), validMultiplierValue);
            this.multiplier = multiplier;
        }
    }

    private MultiplierType createDefaultMultiplierType() {
        return meteringService.createMultiplierType(MULTIPLIER_TYPE);
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
            Meter meter = findOrCreateKoreMeter(getMdcAmrSystem());
            if (meter.hasData() && startDateMultiplier.get().isBefore(now)) {
                throw MultiplierConfigurationException.canNotConfigureMultiplierInPastWhenYouAlreadyHaveData(thesaurus);
            }
            if (meter.getCurrentMeterActivation().isPresent()) {
                if (!meter.getCurrentMeterActivation().get().getRange().contains(startDateMultiplier.get())) {
                    throw MultiplierConfigurationException.canNotConfigureMultiplierWithStartDateOutOfCurrentMeterActivation(thesaurus);
                }
            }
        }
    }

    @Override
    public Integer getYearOfCertification() {
        return this.yearOfCertification;
    }

    private RegisterImpl newRegisterFor(RegisterSpec registerSpec) {
        for (RegisterFactory factory : RegisterFactory.values()) {
            if (factory.appliesTo(registerSpec)) {
                return factory.newRegister(this, registerSpec);
            }
        }
        return RegisterFactory.Numerical.newRegister(this, registerSpec);
    }

    public Instant getModificationDate() {
        return this.modTime;
    }

    @Override
    public void validateDeviceCanChangeConfig(DeviceConfiguration destinationDeviceConfiguration) {
        if (this.getDeviceConfiguration().getId() == destinationDeviceConfiguration.getId()) {
            throw DeviceConfigurationChangeException.cannotChangeToSameConfig(thesaurus, this);
        }
        if (destinationDeviceConfiguration.getDeviceType().getId() != getDeviceType().getId()) {
            throw DeviceConfigurationChangeException.cannotChangeToConfigOfOtherDeviceType(thesaurus);
        }
        checkIfAllConflictsAreSolved(this.getDeviceConfiguration(), destinationDeviceConfiguration);
    }

    @Override
    public void setNewDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        this.deviceConfiguration.set(deviceConfiguration);
    }

    @Override
    public void createNewMeterActivation(Instant meterActivationStartTime) {
        activate(meterActivationStartTime);
    }

    @Override
    public void removeLoadProfiles(List<LoadProfile> loadProfiles) {
        this.loadProfiles.removeAll(loadProfiles);
    }

    @Override
    public void addLoadProfiles(List<LoadProfileSpec> loadProfileSpecs) {
        this.loadProfiles.addAll(
                loadProfileSpecs.stream()
                        .map(loadProfileSpec -> this.dataModel.getInstance(LoadProfileImpl.class)
                                .initialize(loadProfileSpec, this))
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

    @Override
    public List<LogBook> getLogBooks() {
        return Collections.unmodifiableList(this.logBooks);
    }

    @Override
    public void updateMeterConfiguration(Instant updateTimeStamp) {
        createMeterConfiguration(findKoreMeter(getMdcAmrSystem()).get(), updateTimeStamp, getMultiplier().compareTo(BigDecimal.ONE) == 1);
    }

    @Override
    public LogBook.LogBookUpdater getLogBookUpdaterFor(LogBook logBook) {
        return new LogBookUpdaterForDevice((LogBookImpl) logBook);
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

    public Optional<ReadingType> getCalculatedReadingTypeFromMeterConfiguration(ReadingType readingType, Instant timeStamp) {
        Optional<MeterConfiguration> configuration = findKoreMeter(getMdcAmrSystem()).get().getConfiguration(timeStamp);
        if (configuration.isPresent()) {
            Optional<MeterReadingTypeConfiguration> mrtConfiguration = configuration.get()
                    .getReadingTypeConfigs()
                    .stream()
                    .filter(meterReadingTypeConfiguration -> meterReadingTypeConfiguration.getMeasured()
                            .equals(readingType))
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
        Optional<Meter> koreMeter = findKoreMeter(getMdcAmrSystem());
        if (koreMeter.isPresent()) {
            Optional<MeterConfiguration> configuration = koreMeter.get().getConfiguration(clock.instant());
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

    private ProtocolDialectProperties createNewLocalDialectProperties(String dialectName) {
        ProtocolDialectPropertiesImpl dialectProperties;
        ProtocolDialectConfigurationProperties configurationProperties = this.getProtocolDialectConfigurationProperties(dialectName);
        if (configurationProperties != null) {
            dialectProperties = this.dataModel.getInstance(ProtocolDialectPropertiesImpl.class)
                    .initialize(this, configurationProperties);
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

    private void addDeviceProperty(Optional<PropertySpec> propertySpec, String propertyValue) {
        if (propertyValue != null) {
            DeviceProtocolPropertyImpl deviceProtocolProperty = this.dataModel.getInstance(DeviceProtocolPropertyImpl.class)
                    .initialize(this, propertySpec, propertyValue);
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

    private Optional<PropertySpec> getPropertySpecForProperty(String name) {
        return this.getDeviceProtocolPluggableClass()
                .getDeviceProtocol()
                .getPropertySpecs()
                .stream()
                .filter(spec -> spec.getName().equals(name))
                .findFirst();
    }

    private void addLocalProperties(TypedProperties properties, List<PropertySpec> propertySpecs) {
        for (PropertySpec propertySpec : propertySpecs) {
            DeviceProtocolProperty deviceProtocolProperty = findDevicePropertyFor(propertySpec);
            if (deviceProtocolProperty != null) {
                properties.setProperty(deviceProtocolProperty.getName(), propertySpec.getValueFactory()
                        .fromStringValue(deviceProtocolProperty.getPropertyValue()));
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

    private Optional<UsagePoint> getUsagePointFromMeterActivation(Meter meter) {
        Optional<? extends MeterActivation> currentMeterActivation = meter.getCurrentMeterActivation();
        if (currentMeterActivation.isPresent()) {
            return currentMeterActivation.get().getUsagePoint();
        } else {
            return Optional.empty();
        }
    }

    Optional<Meter> findKoreMeter(AmrSystem amrSystem) {
        if (!this.meter.isPresent()) {
            this.meter = amrSystem.findMeter(String.valueOf(getId()));
        }
        return this.meter;
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
        return this.createKoreMeter(getMdcAmrSystem());
    }

    private Supplier<RuntimeException> mdcAMRSystemDoesNotExist() {
        return () -> new RuntimeException("The MDC AMR system does not exist");
    }

    @Override
    public LoadProfile.LoadProfileUpdater getLoadProfileUpdaterFor(LoadProfile loadProfile) {
        return new LoadProfileUpdaterForDevice((LoadProfileImpl) loadProfile);
    }

    private Supplier<NoMeterActivationAt> noMeterActivationAt(Instant timestamp) {
        return () -> new NoMeterActivationAt(timestamp, thesaurus, MessageSeeds.NO_METER_ACTIVATION_AT);
    }

    Meter createKoreMeter(AmrSystem amrSystem) {
        FiniteStateMachine stateMachine = this.getDeviceType().getDeviceLifeCycle().getFiniteStateMachine();
        Meter newMeter = amrSystem.newMeter(String.valueOf(getId()))
                .setMRID(getmRID())
                .setStateMachine(stateMachine)
                .setSerialNumber(getSerialNumber())
                .create();
        newMeter.getLifecycleDates().setReceivedDate(this.clock.instant());
        newMeter.update();
        this.meter = Optional.of(newMeter);
        return newMeter;
    }

    @Override
    public List<ProtocolDialectProperties> getProtocolDialectPropertiesList() {
        List<ProtocolDialectProperties> all = new ArrayList<>(this.dialectPropertiesList.size() + this.newDialectProperties.size());
        all.addAll(this.dialectPropertiesList);
        all.addAll(this.newDialectProperties);
        return all;
    }

    private Optional<AmrSystem> findMdcAmrSystem() {
        return this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
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

    private AmrSystem getMdcAmrSystem() {
        if (this.amrSystem == null) {
            this.amrSystem = findMdcAmrSystem().orElseThrow(mdcAMRSystemDoesNotExist());
        }
        return this.amrSystem;
    }

    List<ReadingRecord> getReadingsFor(Register<?, ?> register, Range<Instant> interval) {
        return this.getListMeterAspect(meter -> this.getReadingsFor(register, interval, meter));
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

    private List<ReadingRecord> getReadingsFor(Register<?, ?> register, Range<Instant> interval, Meter meter) {
        List<? extends BaseReadingRecord> readings = meter.getReadings(interval, register.getRegisterSpec()
                .getRegisterType()
                .getReadingType());
        return readings
                .stream()
                .map(ReadingRecord.class::cast)
                .collect(Collectors.toList());
    }

    List<LoadProfileReading> getChannelData(LoadProfile loadProfile, Range<Instant> interval) {
        List<LoadProfileReading> loadProfileReadings = Collections.emptyList();
        boolean meterHasData = false;
        Optional<Meter> meter = this.findKoreMeter(getMdcAmrSystem());
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

        return Lists.reverse(loadProfileReadings);
    }

    List<LoadProfileReading> getChannelData(Channel channel, Range<Instant> interval) {
        List<LoadProfileReading> loadProfileReadings = Collections.emptyList();
        boolean meterHasData;
        Optional<Meter> meter = this.findKoreMeter(getMdcAmrSystem());
        if (meter.isPresent()) {
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

    /**
     * Adds meter readings for a single channel to the timeslot-map.
     *
     * @param interval The interval over which meter readings are requested
     * @param meter The meter for which readings are requested
     * @param mdcChannel The meter's channel for which readings are requested
     * @param sortedLoadProfileReadingMap The map to add the readings too in the correct timeslot
     * @return true if any readings were added to the map, false otherwise
     */
    private boolean addChannelDataToMap(Range<Instant> interval, Meter meter, Channel mdcChannel, Map<Instant, LoadProfileReadingImpl> sortedLoadProfileReadingMap) {
        boolean meterHasData = false;
        List<MeterActivation> meterActivations = this.getSortedMeterActivations(meter, Ranges.closed(interval.lowerEndpoint(), interval
                .upperEndpoint()));
        for (MeterActivation meterActivation : meterActivations) {
            Range<Instant> meterActivationInterval = meterActivation.getInterval()
                    .toOpenClosedRange()
                    .intersection(interval);
            meterHasData |= meterActivationInterval.lowerEndpoint() != meterActivationInterval.upperEndpoint();
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
                                        .filter(rq -> rq.getType()
                                                .equals(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.REJECTED)))
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
            throw DeviceProtocolPropertyException.propertyDoesNotExistForDeviceProtocol(name, this.getDeviceProtocolPluggableClass()
                    .getDeviceProtocol(), this, thesaurus, MessageSeeds.DEVICE_PROPERTY_NOT_ON_DEVICE_PROTOCOL);
        }
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

    @Override
    public void
    setSecurityProperties(SecurityPropertySet securityPropertySet, TypedProperties typedProperties) {
        dirtySecurityProperties.put(securityPropertySet, typedProperties);
        //Don't persist yet, need to be validated (done in the save step of this device)
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
        List<MeterActivation> allMeterActivations = new ArrayList<>(meter.getMeterActivations());
        allMeterActivations
                .stream()
                .filter(ma -> ma.overlaps(requestedInterval))
                .forEach(affectedMeterActivation -> {
                    Range<Instant> requestedIntervalClippedToMeterActivation = requestedInterval.intersection(affectedMeterActivation
                            .getRange());
                    ZonedDateTime requestStart = this.prefilledIntervalStart(loadProfile, affectedMeterActivation.getZoneId(), requestedIntervalClippedToMeterActivation);
                    ZonedDateTime requestEnd =
                            ZonedDateTime.ofInstant(
                                    this.lastReadingClipped(loadProfile, requestedInterval),
                                    affectedMeterActivation.getZoneId());
                    if (!requestEnd.isBefore(requestStart)) {
                        Range<Instant> meterActivationInterval = Range.closedOpen(requestStart.toInstant(), requestEnd.toInstant());
                        while (meterActivationInterval.contains(requestStart.toInstant())) {
                            ZonedDateTime readingTimestamp = requestStart.plus(intervalLength);
                            LoadProfileReadingImpl value = new LoadProfileReadingImpl();
                            value.setRange(Ranges.openClosed(requestStart.toInstant(), readingTimestamp.toInstant()));
                            loadProfileReadingMap.put(readingTimestamp.toInstant(), value);
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
                throw new IllegalArgumentException("Unsupported load profile interval length unit " + loadProfile.getInterval()
                        .getTimeUnit());
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

        while (nextAttempt.toInstant()
                .isBefore(requestedIntervalClippedToMeterActivation.lowerEndpoint()) || nextAttempt.toInstant()
                .equals(requestedIntervalClippedToMeterActivation.lowerEndpoint())) {
            latestAttemptBefore = nextAttempt;
            nextAttempt = nextAttempt.plus(this.intervalLength(loadProfile));
        }
        return latestAttemptBefore;
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

        while (nextAttempt.toInstant().isAfter(requestedIntervalClippedToMeterActivation.lowerEndpoint()) || nextAttempt
                .toInstant()
                .equals(requestedIntervalClippedToMeterActivation.lowerEndpoint())) {
            nextAttempt = nextAttempt.minus(this.intervalLength(loadProfile));
        }
        return nextAttempt;
    }

    @Override
    public TypedProperties getDeviceProtocolProperties() {
        TypedProperties properties = TypedProperties.inheritingFrom(this.getDeviceConfiguration().getDeviceProtocolProperties().getTypedProperties());
        this.addLocalProperties(properties, this.getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs());
        return properties;
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
                throw new IllegalArgumentException("Unsupported load profile interval length unit " + loadProfile.getInterval()
                        .getTimeUnit());
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
                throw new IllegalArgumentException("Unsupported load profile interval length unit " + loadProfile.getInterval()
                        .getTimeUnit());
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
            } else if (interval.upperEndpoint().isBefore(dataUntil)) {
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
        Meter meter = findOrCreateKoreMeter(getMdcAmrSystem());
        meter.store(meterReading);
    }

    private Optional<ReadingRecord> getLastReadingsFor(Register register, Meter meter) {
        ReadingType readingType = register.getRegisterSpec().getRegisterType().getReadingType();
        for (MeterActivation meterActivation : this.getSortedMeterActivations(meter)) {
            Optional<com.elster.jupiter.metering.Channel> channel = this.getChannel(meterActivation, readingType);
            if (channel.isPresent()) {
                Instant lastReadingDate = channel.get().getLastDateTime();
                if (lastReadingDate != null) {
                    return this.getLast(channel.get()
                            .getRegisterReadings(Interval.of(lastReadingDate, lastReadingDate).toClosedRange()));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<UsagePoint> getUsagePoint() {
        return this.getOptionalMeterAspect(this::getUsagePointFromMeterActivation);
    }

    private Optional<Boolean> hasData(Meter meter) {
        if (meter.hasData()) {
            return Optional.of(true);
        } else {
            return Optional.of(false);
        }
    }

    @Override
    public void setUsagePoint(UsagePoint usagePoint) {
        findKoreMeter(getMdcAmrSystem()).ifPresent(meter -> {
            Optional<UsagePoint> currentUsagePoint = getUsagePoint();
            Optional<Provider<MeterActivation>> activateOperation = Optional.empty();
            Instant startTime = this.clock.instant();
            if (usagePoint == null) {
                // remove the old usage point
                activateOperation = Optional.of(() -> meter.activate(startTime));
            } else if (!currentUsagePoint.isPresent() || usagePoint.getId() != currentUsagePoint.get().getId()) {
                // set the new usage point
                activateOperation = Optional.of(() -> meter.activate(usagePoint, startTime));
            }
            activateOperation.ifPresent(activator -> {
                getCurrentMeterActivation().ifPresent(ma -> ma.endAt(startTime));
                MeterActivation meterActivation = activator.get();
                meterActivation.setMultiplier(getDefaultMultiplierType(), getMultiplier());
                if (usagePoint != null) {
                    copyLocationAndGeoCoordinates(usagePoint);
                }
            });
        });
    }

    private void copyLocationAndGeoCoordinates(UsagePoint usagePoint) {
        usagePoint.getLocation().ifPresent(loc -> {
            if (!location.isPresent()) {
                setLocation(loc);
            }
        });
        usagePoint.getGeoCoordinates().ifPresent(geo -> {
            if (!geoCoordinates.isPresent()) {
                setGeoCoordinates(geo);
            }
        });
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

    /**
     * Activate the meter. Either end the current MeterActivation and create a new one, or just create a new one.
     * Depending on 'copyMultiplier' also copy the multiplierValue
     *
     * @param start start of the meterActivation
     * @param copyMultiplier indication to copy the multiplier
     * @return the new meterActivation
     */
    private MeterActivation activate(Instant start, boolean copyMultiplier) {
        Optional<MeterActivation> currentMeterActivation = getCurrentMeterActivation();
        AmrSystem amrSystem = getMdcAmrSystem();
        Meter meter = this.findOrCreateKoreMeter(amrSystem);
        if (currentMeterActivation.isPresent()) {
            this.currentMeterActivation = Optional.of(endMeterActivationAndCreateNewWithCurrentAsTemplate(start, meter, currentMeterActivation.get(), copyMultiplier));
        } else {
            this.currentMeterActivation = Optional.of(meter.activate(start));
        }
        return this.currentMeterActivation.get();
    }

    private MeterActivation endMeterActivationAndCreateNewWithCurrentAsTemplate(Instant start, Meter meter, MeterActivation meterActivation, boolean copyMultiplier) {
        Optional<UsagePoint> usagePoint = meter.getUsagePoint(start);
        meterActivation.endAt(start);
        MeterActivation newMeterActivation;
        if (usagePoint.isPresent()) {
            newMeterActivation = meter.activate(usagePoint.get(), start);
        } else {
            newMeterActivation = meter.activate(start);
        }
        if (copyMultiplier) {
            meterActivation.getMultiplier(getDefaultMultiplierType())
                    .ifPresent(multiplier -> newMeterActivation.setMultiplier(getDefaultMultiplierType(), multiplier));
        }
        return newMeterActivation;
    }

    private void removeCachedCurrentMeterActivation() {
        this.currentMeterActivation = Optional.empty();
    }

    private <AT> Optional<AT> getOptionalMeterAspect(Function<Meter, Optional<AT>> aspectFunction) {
        Optional<Meter> meter = this.findKoreMeter(getMdcAmrSystem());
        if (meter.isPresent()) {
            return aspectFunction.apply(meter.get());
        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private <AT> List<AT> getListMeterAspect(Function<Meter, List<AT>> aspectFunction) {
        Optional<Meter> meter = this.findKoreMeter(getMdcAmrSystem());
        if (meter.isPresent()) {
            return aspectFunction.apply(meter.get());
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
        Meter meter = this.findKoreMeter(getMdcAmrSystem()).get();
        if (meter.getMeterActivations().isEmpty()) {
            meter.activate(when);
        }
    }

    Optional<MeterActivation> getMeterActivation(Instant when) {
        return this.getOptionalMeterAspect(meter -> meter.getMeterActivation(when).map(MeterActivation.class::cast));
    }

    Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Channel channel, Instant when) {
        return findKoreChannel(channel::getReadingType, when);
    }

    public List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter) {
        return this.getListMeterAspect(meter -> meter.getDeviceEventsByFilter(filter));
    }

    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Supplier<ReadingType> readingTypeSupplier, Instant when) {
        return this.getOptionalMeterAspect(meter -> this.findKoreChannel(meter, readingTypeSupplier, when));
    }

    private Optional<com.elster.jupiter.metering.Channel> findKoreChannel(Meter meter, Supplier<ReadingType> readingTypeSupplier, Instant when) {
        Optional<? extends MeterActivation> meterActivation = meter.getMeterActivation(when);
        if (meterActivation.isPresent()) {
            return Optional.ofNullable(getChannel(meterActivation.get(), readingTypeSupplier.get()).orElse(null));
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

    List<com.elster.jupiter.metering.Channel> findKoreChannels(Supplier<ReadingType> readingTypeSupplier) {
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
            return this.getChannel(meterActivation.get(), readingType)
                    .orElseGet(() -> meterActivation.get().createChannel(readingType));
        } else {
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
        return meterActivation.getChannels()
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

    @Override
    public boolean hasData() {
        return this.getOptionalMeterAspect(this::hasData).get();
    }

    public DataMapper<DeviceImpl> getDataMapper() {
        return this.dataModel.mapper(DeviceImpl.class);
    }

    private Stream<ConnectionTaskImpl<?, ?>> getConnectionTaskImpls() {
        return this.connectionTasks
                .stream()
                .filter(Predicates.not(ConnectionTask::isObsolete));
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

    private Stream<ComTaskExecutionImpl> getComTaskExecutionImpls() {
        return this.comTaskExecutions
                .stream()
                .filter(Predicates.not(ComTaskExecution::isObsolete));
    }

    private ComTaskExecution add(ComTaskExecutionImpl comTaskExecution) {
        Save.CREATE.validate(DeviceImpl.this.dataModel, comTaskExecution, Save.Create.class, Save.Update.class);
        Save.UPDATE.validate(DeviceImpl.this.dataModel, this, Save.Create.class, Save.Update.class);
        this.comTaskExecutions.add(comTaskExecution);
        if (this.id != 0) {
            comTaskExecution.notifyCreated();
            dataModel.touch(DeviceImpl.this);
        }
        return comTaskExecution;
    }

    @Override
    public MeterActivation activate(Instant start) {
        return activate(start, true);
    }

    private List<SecurityProperty> getSecurityProperties(Instant when, SecurityPropertySet securityPropertySet) {
        return this.securityPropertyService.getSecurityProperties(this, when, securityPropertySet);
    }

    private boolean hasSecurityProperties(Instant when, SecurityPropertySet securityPropertySet) {
        return this.securityPropertyService.hasSecurityProperties(this, when, securityPropertySet);
    }

    private List<OpenIssue> getOpenIssuesForMeter(Meter meter) {
        return this.issueService.query(OpenIssue.class).select(where("device").isEqualTo(meter));
    }

    @Override
    public void deactivate(Instant when) {
        this.getCurrentMeterActivation().ifPresent(meterActivation -> meterActivation.endAt(when));
        removeCachedCurrentMeterActivation();
    }

    private List<HistoricalIssue> getAllHistoricalIssuesForMeter(Meter meter) {
        return this.issueService.query(HistoricalIssue.class).select(where("device").isEqualTo(meter));
    }


    @Override
    public List<PassiveEffectiveCalendar> getPassiveCalendars() {
        return this.passiveCalendars;
    }

    @Override
    public void setPassiveCalendars(List<PassiveEffectiveCalendar> passiveCalendars) {
        this.passiveCalendars = passiveCalendars;
    }

    @Override
    public Optional<ActiveEffectiveCalendar> getActiveCalendar() {
        return activeCalendar.effective(this.clock.instant());
    }

    public void setActiveCalendar(AllowedCalendar allowedCalendar, Instant effective, Instant lastVerified) {
        Interval effectivityInterval = Interval.of(Range.atLeast(effective));
        this.activeCalendar.add(
                this.dataModel.getInstance(ActiveEffectiveCalendarImpl.class)
                        .initialize(effectivityInterval, this, allowedCalendar, lastVerified)
        );
    }

    private List<com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent> getDeviceTypeLifeCycleChangeEvents() {
        return this.getDeviceType()
                .getDeviceLifeCycleChangeEvents()
                .stream()
                .filter(each -> each.getTimestamp().isAfter(this.createTime))
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateNow() {
        this.deactivate(this.clock.instant());
    }

    private DeviceLifeCycleChangeEvent newEventForMostRecent(Deque<StateTimeSlice> stateTimeSlices, Deque<com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent> deviceTypeChangeEvents) {
        if (stateTimeSlices.isEmpty()) {
            return DeviceLifeCycleChangeEventImpl.from(deviceTypeChangeEvents.removeFirst());
        } else if (deviceTypeChangeEvents.isEmpty()) {
            return DeviceLifeCycleChangeEventImpl.from(stateTimeSlices.removeFirst());
        } else {
            // Compare both timestamps and create event from the most recent one
            StateTimeSlice stateTimeSlice = stateTimeSlices.peekFirst();
            com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent deviceLifeCycleChangeEvent = deviceTypeChangeEvents
                    .peekFirst();
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

    private void addReadingTypeObisCodeUsage(ReadingType readingType, ObisCode obisCode) {
        ReadingTypeObisCodeUsageImpl readingTypeObisCodeUsage = dataModel.getInstance(ReadingTypeObisCodeUsageImpl.class);
        readingTypeObisCodeUsage.initialize(this, readingType, obisCode);
        readingTypeObisCodeUsages.add(readingTypeObisCodeUsage);
    }

    private void removeReadingTypeObisCodeUsage(ReadingType readingType) {
        for (java.util.Iterator<ReadingTypeObisCodeUsageImpl> iterator = readingTypeObisCodeUsages.iterator(); iterator.hasNext(); ) {
            ReadingTypeObisCodeUsageImpl readingTypeObisCodeUsage = iterator.next();
            if (readingTypeObisCodeUsage.getReadingType().getMRID().equals(readingType.getMRID())) {
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public Optional<MeterActivation> getCurrentMeterActivation() {
        if (!this.currentMeterActivation.isPresent()) {
            this.currentMeterActivation = this.getOptionalMeterAspect(m -> m.getCurrentMeterActivation().map(Function.<MeterActivation>identity()));
        }
        return this.currentMeterActivation;
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
    public List<MeterActivation> getMeterActivationsMostRecentFirst() {
        return this.getListMeterAspect(this::getSortedMeterActivations);
    }

    private class InternalDeviceMessageBuilder implements DeviceMessageBuilder {

        private final DeviceMessageImpl deviceMessage;

        private InternalDeviceMessageBuilder(DeviceMessageId deviceMessageId, TrackingCategory trackingCategory) {
            deviceMessage = DeviceImpl.this.dataModel.getInstance(DeviceMessageImpl.class)
                    .initialize(DeviceImpl.this, deviceMessageId);
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
            this.getConnectionInitiationTask()
                    .initialize(device, partialConnectionInitiationTask, partialConnectionInitiationTask.getComPortPool());
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
            this.getInboundConnectionTask()
                    .initialize(device, partialInboundConnectionTask, partialInboundConnectionTask.getComPortPool());
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
            this.getScheduledConnectionTask()
                    .initialize(device, (PartialScheduledConnectionTask) partialOutboundConnectionTask, partialOutboundConnectionTask
                            .getComPortPool());
            if (partialOutboundConnectionTask.getNextExecutionSpecs() != null) {
                this.getScheduledConnectionTask()
                        .setNextExecutionSpecsFrom(partialOutboundConnectionTask.getNextExecutionSpecs()
                                .getTemporalExpression());
            }
            this.getScheduledConnectionTask()
                    .setConnectionStrategy(((PartialScheduledConnectionTask) partialOutboundConnectionTask).getConnectionStrategy());
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

    public class ScheduledComTaskExecutionBuilderForDevice
            extends ScheduledComTaskExecutionImpl.ScheduledComTaskExecutionBuilderImpl {

        private Set<ComTaskExecution> executionsToDelete;

        private ScheduledComTaskExecutionBuilderForDevice(Provider<ScheduledComTaskExecutionImpl> comTaskExecutionProvider, ComSchedule comSchedule) {
            super(comTaskExecutionProvider.get());
            this.initExecutionsToDelete(comSchedule);
            this.getComTaskExecution().initialize(DeviceImpl.this, comSchedule);
        }

        private void initExecutionsToDelete(ComSchedule comSchedule) {
            Set<Long> comScheduleComTasks = comSchedule.getComTasks()
                    .stream()
                    .map(ComTask::getId)
                    .collect(Collectors.toSet());
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

    public class AdHocComTaskExecutionBuilderForDevice
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

    public class FirmwareComTaskExecutionBuilderForDevice extends FirmwareComTaskExecutionImpl.FirmwareComTaskExecutionBuilderImpl {

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

    public class ManuallyScheduledComTaskExecutionBuilderForDevice
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
        return new ChannelUpdaterImpl(channel);
    }

    @Override
    public Register.RegisterUpdater getRegisterUpdaterFor(Register register) {
        return new RegisterUpdaterImpl(register);
    }

    private class RegisterUpdaterImpl implements Register.RegisterUpdater {

        private final Register register;
        private Integer overruledNbrOfFractionDigits;
        private BigDecimal overruledOverflowValue;
        private ObisCode overruledObisCode;

        public RegisterUpdaterImpl(Register register) {
            this.register = register;
        }

        @Override
        public Register.RegisterUpdater setNumberOfFractionDigits(Integer overruledNbrOfFractionDigits) {
            this.overruledNbrOfFractionDigits = overruledNbrOfFractionDigits;
            return this;
        }

        @Override
        public Register.RegisterUpdater setOverflowValue(BigDecimal overruledOverflowValue) {
            this.overruledOverflowValue = overruledOverflowValue;
            return this;
        }

        @Override
        public Register.RegisterUpdater setObisCode(ObisCode overruledObisCode) {
            this.overruledObisCode = overruledObisCode;
            return this;
        }

        @Override
        public void update() {
            Instant updateInstant = DeviceImpl.this.clock.instant();
            if (register.getRegisterSpec() instanceof NumericalRegisterSpec) { //textRegisters don't have fraction digits and overflow values
                DeviceImpl.this.findKoreMeter(getMdcAmrSystem()).ifPresent(koreMeter -> {
                    Optional<MeterConfiguration> currentMeterConfiguration = koreMeter.getConfiguration(updateInstant);
                    if (requiredToCreateNewMeterConfiguration(currentMeterConfiguration, register.getReadingType(), overruledOverflowValue, overruledNbrOfFractionDigits)) { // if we need to update it
                        if (currentMeterConfiguration.isPresent()) {
                            MeterConfiguration meterConfiguration = currentMeterConfiguration.get();
                            meterConfiguration.endAt(updateInstant);
                            Meter.MeterConfigurationBuilder newMeterConfigBuilder = koreMeter.startingConfigurationOn(updateInstant);
                            NumericalRegisterSpec registerSpec = (NumericalRegisterSpec) register.getRegisterSpec();
                            meterConfiguration.getReadingTypeConfigs().stream().forEach(meterReadingTypeConfiguration -> {
                                Meter.MeterReadingTypeConfigurationBuilder meterReadingTypeConfigurationBuilder = newMeterConfigBuilder.configureReadingType(meterReadingTypeConfiguration
                                        .getMeasured());
                                if (registerWeNeedToUpdate(meterReadingTypeConfiguration)) {
                                    if (overruledOverflowValue == null) {
                                        meterReadingTypeConfigurationBuilder.withOverflowValue(registerSpec.getOverflowValue().orElse(null));
                                    } else {
                                        meterReadingTypeConfigurationBuilder.withOverflowValue(overruledOverflowValue);
                                    }
                                } else if (meterReadingTypeConfiguration.getOverflowValue().isPresent()) {
                                    meterReadingTypeConfigurationBuilder.withOverflowValue(meterReadingTypeConfiguration.getOverflowValue().get());
                                }
                                if (registerWeNeedToUpdate(meterReadingTypeConfiguration)) {
                                    if (overruledNbrOfFractionDigits == null) {
                                        meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(registerSpec.getNumberOfFractionDigits());
                                    } else {
                                        meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(overruledNbrOfFractionDigits);
                                    }
                                } else if (meterReadingTypeConfiguration.getNumberOfFractionDigits().isPresent()) {
                                    meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(meterReadingTypeConfiguration.getNumberOfFractionDigits().getAsInt());
                                }
                                meterReadingTypeConfiguration.getCalculated()
                                        .ifPresent(readingType -> meterReadingTypeConfigurationBuilder.withMultiplierOfType(getDefaultMultiplierType()).calculating(readingType));
                            });
                            newMeterConfigBuilder.create();
                        } else {
                            Meter.MeterConfigurationBuilder newMeterConfigBuilder = koreMeter.startingConfigurationOn(updateInstant);
                            getDeviceConfiguration().getRegisterSpecs().stream().filter(spec -> spec instanceof NumericalRegisterSpec)
                                    .map(numericalRegisterSpec -> ((NumericalRegisterSpec) numericalRegisterSpec)).forEach(registerSpec -> {
                                Meter.MeterReadingTypeConfigurationBuilder meterReadingTypeConfigurationBuilder = newMeterConfigBuilder.configureReadingType(registerSpec.getReadingType());
                                if (registerWeNeedToUpdate(registerSpec) && overruledOverflowValue != null) {
                                    meterReadingTypeConfigurationBuilder.withOverflowValue(overruledOverflowValue);
                                } else if (registerSpec.getOverflowValue().isPresent()) {
                                    meterReadingTypeConfigurationBuilder.withOverflowValue(registerSpec.getOverflowValue().get());
                                }
                                if (registerWeNeedToUpdate(registerSpec) && overruledNbrOfFractionDigits != null) {
                                    meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(overruledNbrOfFractionDigits);
                                } else {
                                    meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(registerSpec.getNumberOfFractionDigits());
                                }
                                if (getMultiplier().compareTo(BigDecimal.ONE) == 1 && registerSpec.isUseMultiplier()) {
                                    meterReadingTypeConfigurationBuilder.withMultiplierOfType(getDefaultMultiplierType())
                                            .calculating((registerSpec.getCalculatedReadingType().get()));
                                }
                            });
                            newMeterConfigBuilder.create();
                        }
                    }
                });
            }

            Optional<ReadingTypeObisCodeUsage> readingTypeObisCodeUsageOptional = DeviceImpl.this.getReadingTypeObisCodeUsage(register.getReadingType());
            boolean currentlyNoOverruledObisCodeAlthoughRequested = overruledObisCode != null && // obiscode overruling requested...
                    !readingTypeObisCodeUsageOptional.isPresent(), // ...while currently there is none
                    currentOverruledObisCodeIsNotTheCorrectOne = overruledObisCode != null && // obiscode overruling requested and...
                            readingTypeObisCodeUsageOptional.isPresent() && // ...the currently present one...
                            !readingTypeObisCodeUsageOptional.get().getObisCode().equals(overruledObisCode), // ...is different
                    currentOverruledObisCodeIsNotNeeded = overruledObisCode == null && // no obiscode overruling requested...
                            readingTypeObisCodeUsageOptional.isPresent(); // ...however, currently present

            if (currentOverruledObisCodeIsNotTheCorrectOne || currentOverruledObisCodeIsNotNeeded) {
                DeviceImpl.this.removeReadingTypeObisCodeUsage(register.getReadingType());
            }
            if (currentOverruledObisCodeIsNotTheCorrectOne || currentlyNoOverruledObisCodeAlthoughRequested) {
                DeviceImpl.this.addReadingTypeObisCodeUsage(register.getReadingType(), overruledObisCode);
            }

            DeviceImpl.this.save(); //do validation and make sure the version increases
        }

        private boolean registerWeNeedToUpdate(RegisterSpec registerSpec) {
            return registerSpec.getReadingType().equals(register.getReadingType());
        }

        private boolean registerWeNeedToUpdate(MeterReadingTypeConfiguration meterReadingTypeConfiguration) {
            return meterReadingTypeConfiguration.getMeasured().equals(register.getReadingType());
        }
    }

    private class ChannelUpdaterImpl implements Channel.ChannelUpdater {

        private final Channel channel;
        private Integer overruledNbrOfFractionDigits;
        private BigDecimal overruledOverflowValue;
        private ObisCode overruledObisCode;

        public ChannelUpdaterImpl(Channel channel) {
            this.channel = channel;
        }

        @Override
        public Channel.ChannelUpdater setNumberOfFractionDigits(Integer overruledNbrOfFractionDigits) {
            this.overruledNbrOfFractionDigits = overruledNbrOfFractionDigits;
            return this;
        }

        @Override
        public Channel.ChannelUpdater setOverflowValue(BigDecimal overruledOverflowValue) {
            this.overruledOverflowValue = overruledOverflowValue;
            return this;
        }

        @Override
        public Channel.ChannelUpdater setObisCode(ObisCode overruledObisCode) {
            this.overruledObisCode = overruledObisCode;
            return this;
        }

        @Override
        public void update() {
            Instant updateInstant = DeviceImpl.this.clock.instant();
            DeviceImpl.this.findKoreMeter(getMdcAmrSystem()).ifPresent(koreMeter -> {
                Optional<MeterConfiguration> currentMeterConfiguration = koreMeter.getConfiguration(updateInstant);
                if (requiredToCreateNewMeterConfiguration(currentMeterConfiguration, channel.getReadingType(), overruledOverflowValue, overruledNbrOfFractionDigits)) { // if we need to update it
                    if (currentMeterConfiguration.isPresent()) {
                        MeterConfiguration meterConfiguration = currentMeterConfiguration.get();
                        meterConfiguration.endAt(updateInstant);
                        Meter.MeterConfigurationBuilder newMeterConfigBuilder = koreMeter.startingConfigurationOn(updateInstant);
                        meterConfiguration.getReadingTypeConfigs().stream().forEach(meterReadingTypeConfiguration -> {
                            Meter.MeterReadingTypeConfigurationBuilder meterReadingTypeConfigurationBuilder = newMeterConfigBuilder.configureReadingType(meterReadingTypeConfiguration
                                    .getMeasured());
                            if (channelWeNeedToUpdate(meterReadingTypeConfiguration)) {
                                if (overruledOverflowValue == null) {
                                    meterReadingTypeConfigurationBuilder.withOverflowValue(channel.getChannelSpec().getOverflow().orElse(null));
                                } else {
                                    meterReadingTypeConfigurationBuilder.withOverflowValue(overruledOverflowValue);
                                }
                            } else if (meterReadingTypeConfiguration.getOverflowValue().isPresent()) {
                                meterReadingTypeConfigurationBuilder.withOverflowValue(meterReadingTypeConfiguration.getOverflowValue().get());
                            }
                            if (channelWeNeedToUpdate(meterReadingTypeConfiguration)) {
                                if (overruledNbrOfFractionDigits == null) {
                                    meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(channel.getChannelSpec().getNbrOfFractionDigits());
                                } else {
                                    meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(overruledNbrOfFractionDigits);
                                }
                            } else if (meterReadingTypeConfiguration.getNumberOfFractionDigits().isPresent()) {
                                meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(meterReadingTypeConfiguration.getNumberOfFractionDigits().getAsInt());
                            }
                            meterReadingTypeConfiguration.getCalculated()
                                    .ifPresent(readingType -> meterReadingTypeConfigurationBuilder.withMultiplierOfType(getDefaultMultiplierType()).calculating(readingType));
                        });
                        newMeterConfigBuilder.create();
                    } else {
                        Meter.MeterConfigurationBuilder newMeterConfigBuilder = koreMeter.startingConfigurationOn(updateInstant);
                        getDeviceConfiguration().getChannelSpecs().forEach(channelSpec -> {
                            Meter.MeterReadingTypeConfigurationBuilder meterReadingTypeConfigurationBuilder = newMeterConfigBuilder.configureReadingType(channelSpec.getReadingType());
                            if (channelWeNeedToUpdate(channelSpec) && overruledOverflowValue != null) {
                                meterReadingTypeConfigurationBuilder.withOverflowValue(overruledOverflowValue);
                            } else if (channelSpec.getOverflow().isPresent()) {
                                meterReadingTypeConfigurationBuilder.withOverflowValue(channelSpec.getOverflow().get());
                            }
                            if (channelWeNeedToUpdate(channelSpec) && overruledNbrOfFractionDigits != null) {
                                meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(overruledNbrOfFractionDigits);
                            } else {
                                meterReadingTypeConfigurationBuilder.withNumberOfFractionDigits(channelSpec.getNbrOfFractionDigits());
                            }
                            if (getMultiplier().compareTo(BigDecimal.ONE) == 1 && channelSpec.isUseMultiplier()) {
                                meterReadingTypeConfigurationBuilder.withMultiplierOfType(getDefaultMultiplierType())
                                        .calculating(getMultipliedReadingTypeForChannelSpec(channelSpec));
                            }
                        });
                        newMeterConfigBuilder.create();
                    }
                }
            });
            Optional<ReadingTypeObisCodeUsage> readingTypeObisCodeUsageOptional = DeviceImpl.this.getReadingTypeObisCodeUsage(channel.getReadingType());
            boolean currentlyNoOverruledObisCodeAlthoughRequested = overruledObisCode != null && // obiscode overruling requested...
                    !readingTypeObisCodeUsageOptional.isPresent(), // ...while currently there is none
                    currentOverruledObisCodeIsNotTheCorrectOne = overruledObisCode != null && // obiscode overruling requested and...
                            readingTypeObisCodeUsageOptional.isPresent() && // ...the currently present one...
                            !readingTypeObisCodeUsageOptional.get().getObisCode().equals(overruledObisCode), // ...is different
                    currentOverruledObisCodeIsNotNeeded = overruledObisCode == null && // no obiscode overruling requested...
                            readingTypeObisCodeUsageOptional.isPresent(); // ...however, currently present

            if (currentOverruledObisCodeIsNotTheCorrectOne || currentOverruledObisCodeIsNotNeeded) {
                DeviceImpl.this.removeReadingTypeObisCodeUsage(channel.getReadingType());
            }
            if (currentOverruledObisCodeIsNotTheCorrectOne || currentlyNoOverruledObisCodeAlthoughRequested) {
                DeviceImpl.this.addReadingTypeObisCodeUsage(channel.getReadingType(), overruledObisCode);
            }
            DeviceImpl.this.save(); //do validation and make sure version increases
        }

        private boolean channelWeNeedToUpdate(ChannelSpec channelSpec) {
            return channelSpec.getReadingType().equals(channel.getReadingType());
        }

        private boolean channelWeNeedToUpdate(MeterReadingTypeConfiguration meterReadingTypeConfiguration) {
            return meterReadingTypeConfiguration.getMeasured().equals(channel.getReadingType());
        }

        private ReadingType getMultipliedReadingTypeForChannelSpec(ChannelSpec channelSpec) {
            ReadingType calculatedReadingType = channelSpec.getCalculatedReadingType().get();
            if (channelSpec.getReadingType().isCumulative()) {
                String code = readingTypeUtilService.createReadingTypeCodeBuilderFrom(calculatedReadingType)
                        .accumulate(channelSpec.getReadingType().getAccumulation()).code();
                return readingTypeUtilService.findOrCreateReadingType(code, calculatedReadingType.getAliasName());
            } else {
                return calculatedReadingType;
            }
        }
    }

    private boolean requiredToCreateNewMeterConfiguration(Optional<MeterConfiguration> meterConfiguration, ReadingType readingType, BigDecimal overruledOverflowValue, Integer overruledNbrOfFractionDigits) {
        if (meterConfiguration.isPresent()) {
            boolean required;
            Optional<MeterReadingTypeConfiguration> readingTypeConfiguration = meterConfiguration.get().getReadingTypeConfiguration(readingType);
            if (overruledOverflowValue == null) {
                required = readingTypeConfiguration.isPresent() && readingTypeConfiguration.get().getOverflowValue().isPresent();
            } else {
                required = !readingTypeConfiguration.isPresent() || !readingTypeConfiguration.get().getOverflowValue().isPresent() || !readingTypeConfiguration.get()
                        .getOverflowValue()
                        .get()
                        .equals(overruledOverflowValue);
            }

            if (overruledNbrOfFractionDigits == null) {
                required |= readingTypeConfiguration.isPresent() && readingTypeConfiguration.get().getNumberOfFractionDigits().isPresent();
            } else {
                required |= !readingTypeConfiguration.isPresent() || readingTypeConfiguration.get().getNumberOfFractionDigits().getAsInt() != overruledNbrOfFractionDigits;
            }
            return required;
        } else {
            return true;
        }
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


    @Override
    public void removeConnectionTask(ConnectionTask<?, ?> connectionTask) {
        this.connectionTasks
                .stream()
                .filter(x -> x.getId() == connectionTask.getId())
                .findAny()
                .map(ServerConnectionTask.class::cast)
                .ifPresent(ServerConnectionTask::makeObsolete);
    }


    @Override
    public List<ComTaskExecution> getComTaskExecutions() {
        return comTaskExecutions.stream().filter(((Predicate<ComTaskExecution>) ComTaskExecution::isObsolete).negate()).collect(Collectors.toList());
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


    @Override
    public boolean hasSecurityProperties(SecurityPropertySet securityPropertySet) {
        return this.hasSecurityProperties(clock.instant(), securityPropertySet);
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
        enumeratedEndDeviceGroup.add(this.findOrCreateKoreMeter(getMdcAmrSystem()), range);
    }

    @Override
    public boolean hasOpenIssues() {
        return !getOpenIssues().isEmpty();
    }

    @Override
    public List<OpenIssue> getOpenIssues() {
        return getListMeterAspect(this::getOpenIssuesForMeter);
    }


    @Override
    public State getState() {
        return this.getState(this.clock.instant()).get();
    }

    @Override
    public Optional<State> getState(Instant instant) {
        if (this.id > 0) {
            Optional<Meter> meter = this.findKoreMeter(getMdcAmrSystem());
            if (meter.isPresent()) {
                return meter.get().getState(instant);
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
        Optional<Meter> meter = this.findKoreMeter(getMdcAmrSystem());
        if (meter.isPresent()) {
            return new CIMLifecycleDatesImpl(meter.get(), meter.get().getLifecycleDates());
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


    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public MeterActivation activate(Instant start, UsagePoint usagePoint) {
        Optional<MeterActivation> meterMeterActivation = getMeterActivation(start);
        Optional<BigDecimal> multiplier = Optional.empty();
        Optional<MeterActivation> usagePointMeterActivation = usagePoint.getMeterActivations(start).stream().findFirst();//for MultiSense we expect the only one MeterActivation

        //end meter's MeterActivation if exists
        if (meterMeterActivation.isPresent()) {
            if (meterMeterActivation.get().getEnd() != null) {
                throw new RuntimeException("Meter already active");
            }
            multiplier = meterMeterActivation.flatMap(ma -> ma.getMultiplier(getDefaultMultiplierType()));
            meterMeterActivation.get().endAt(start);
        }

        //end usage point's MeterActivation if exists
        Meter meter = findOrCreateKoreMeter(getMdcAmrSystem());
        if (usagePointMeterActivation.isPresent()) {
            Optional<Meter> meterLinkedToUsagePoint = usagePointMeterActivation.get().getMeter();
            if (meterLinkedToUsagePoint.isPresent() && !meterLinkedToUsagePoint.get().equals(meter)) {
                throw new UsagePointAlreadyLinkedToAnotherDeviceException(thesaurus, getLongDateFormatForCurrentUser(), usagePointMeterActivation.get());
            }
            if (usagePointMeterActivation.get().getEnd() != null) {
                throw new RuntimeException("Overlaps detected");//Should never happen?
            }
            usagePointMeterActivation.get().endAt(start);
        }
        validateReadingTypeRequirements(meter, usagePoint, start);
        MeterActivation newMeterActivation = meter.activate(usagePoint, getDefaultMeterRole(), start);
        //copy multiplier
        multiplier.ifPresent(m -> newMeterActivation.setMultiplier(getDefaultMultiplierType(), m));
        //copy location
        copyLocationAndGeoCoordinates(usagePoint);
        meter.update();
        return newMeterActivation;
    }

    private MeterRole getDefaultMeterRole() {
        return metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey()).orElseThrow(defaultMeterRoleDoesNotExist());
    }

    private Supplier<RuntimeException> defaultMeterRoleDoesNotExist() {
        return () -> new RuntimeException("Default meter role does not exist");
    }

    private void validateReadingTypeRequirements(Meter meter, UsagePoint usagePoint, Instant from) {
        Map<MetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements = getUnsatisfiedRequirements(meter, usagePoint, from);
        if (!unsatisfiedRequirements.isEmpty()) {
            throw new UnsatisfiedReadingTypeRequirementsOfUsagePointException(thesaurus, unsatisfiedRequirements);
        }
    }

    private Map<MetrologyConfiguration, List<ReadingTypeRequirement>> getUnsatisfiedRequirements(Meter meter, UsagePoint usagePoint, Instant from) {
        List<MetrologyConfiguration> effectiveMetrologyConfigurations = usagePoint.getMetrologyConfigurations(Range.atLeast(from));
        if (effectiveMetrologyConfigurations.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ReadingType> supportedReadingTypes = getDeviceCapabilities(meter);
        Map<MetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements = new HashMap<>();
        for (MetrologyConfiguration metrologyConfiguration : effectiveMetrologyConfigurations) {
            List<ReadingTypeRequirement> unsatisfied = metrologyConfiguration.getMandatoryReadingTypeRequirements()
                    .stream()
                    .filter(requirement -> supportedReadingTypes.stream().noneMatch(requirement::matches))
                    .collect(Collectors.toList());
            if (!unsatisfied.isEmpty()) {
                unsatisfiedRequirements.put(metrologyConfiguration, unsatisfied);
            }
        }
        return unsatisfiedRequirements;
    }

    private List<ReadingType> getDeviceCapabilities(Meter meter) {
        //Or may be just use deviceConfigurationService.getReadingTypesRelatedToConfiguration(findDeviceForEndDevice(endDevice).getDeviceConfiguration()); ?
        HeadEndInterface headEndInterface = meter.getHeadEndInterface().orElseThrow(() -> new RuntimeException("Multisense HeadEndInterface is not found"));
        EndDeviceCapabilities capabilities = headEndInterface.getCapabilities(meter);
        return capabilities.getConfiguredReadingTypes();
    }

    //copied from com.energyict.mdc.device.lifecycle.impl.DeviceLifeCycleServiceImpl.getLongDateFormatForCurrentUser()
    //TODO reuse?
    private DateTimeFormatter getLongDateFormatForCurrentUser() {
        Principal principal = threadPrincipalService.getPrincipal();
        String dateTimeFormat = "HH:mm:ss EEE dd MMM ''yy"; // default backend date format
        Locale locale = Locale.ENGLISH;
        if (principal instanceof User) {
            Optional<UserPreference> dateFormatPref = userPreferencesService.getPreferenceByKey((User) principal, FormatKey.LONG_DATE);
            Optional<UserPreference> timeFormatPref = userPreferencesService.getPreferenceByKey((User) principal, FormatKey.LONG_TIME);
            Optional<UserPreference> orderFormatPref = userPreferencesService.getPreferenceByKey((User) principal, FormatKey.DATETIME_ORDER);
            Optional<UserPreference> separatorFormatPref = userPreferencesService.getPreferenceByKey((User) principal, FormatKey.DATETIME_SEPARATOR);
            if (dateFormatPref.isPresent() && timeFormatPref.isPresent() && orderFormatPref.isPresent() && separatorFormatPref.isPresent()) {
                dateTimeFormat = DateTimeFormatGenerator.getDateTimeFormat(
                        dateFormatPref.get().getFormatBE(),
                        timeFormatPref.get().getFormatBE(),
                        orderFormatPref.get().getFormatBE(),
                        separatorFormatPref.get().getFormatBE()
                );
            }
            locale = ((User) principal).getLocale().orElse(locale);
        }
        DateTimeFormatterBuilder formatterBuilder = new DateTimeFormatterBuilder();
        return formatterBuilder.appendPattern(dateTimeFormat).toFormatter(locale);
    }
}
