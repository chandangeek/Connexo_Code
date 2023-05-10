/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.DateTimeFormatGenerator;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.fsm.impl.StageImpl;
import com.elster.jupiter.fsm.impl.StateImpl;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.JournaledChannelReadingRecord;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeterHasUnsatisfiedRequirements;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointHasMeterOnThisRole;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.impl.UsagePointImpl;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.metering.zone.ZoneType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.TemporalReference;
import com.elster.jupiter.orm.associations.Temporals;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.streams.Predicates;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.GatewayType;
import com.energyict.mdc.common.device.config.LoadProfileSpec;
import com.energyict.mdc.common.device.config.LogBookSpec;
import com.energyict.mdc.common.device.config.NumericalRegisterSpec;
import com.energyict.mdc.common.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.common.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.common.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.common.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.common.device.config.RegisterSpec;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.config.TextualRegisterSpec;
import com.energyict.mdc.common.device.data.Batch;
import com.energyict.mdc.common.device.data.CIMLifecycleDates;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.ChannelEstimationRuleOverriddenProperties;
import com.energyict.mdc.common.device.data.ChannelValidationRuleOverriddenProperties;
import com.energyict.mdc.common.device.data.ConnectionInitiationTask;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.DeviceEstimation;
import com.energyict.mdc.common.device.data.DeviceEstimationRuleSetActivation;
import com.energyict.mdc.common.device.data.DeviceLifeCycleChangeEvent;
import com.energyict.mdc.common.device.data.DeviceProtocolProperty;
import com.energyict.mdc.common.device.data.DeviceValidation;
import com.energyict.mdc.common.device.data.InboundConnectionTask;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.common.device.data.LoadProfileJournalReading;
import com.energyict.mdc.common.device.data.LoadProfileReading;
import com.energyict.mdc.common.device.data.LogBook;
import com.energyict.mdc.common.device.data.PassiveCalendar;
import com.energyict.mdc.common.device.data.ProtocolDialectProperties;
import com.energyict.mdc.common.device.data.ReadingTypeObisCodeUsage;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.protocol.TrackingCategory;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.scheduling.NextExecutionSpecs;
import com.energyict.mdc.common.tasks.BasicCheckTask;
import com.energyict.mdc.common.tasks.ClockTask;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.common.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.common.tasks.FirmwareManagementTask;
import com.energyict.mdc.common.tasks.LoadProfilesTask;
import com.energyict.mdc.common.tasks.LogBooksTask;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.common.tasks.RegistersTask;
import com.energyict.mdc.common.tasks.ServerComTaskExecution;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.common.tasks.TopologyTask;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.LockService;
import com.energyict.mdc.device.config.impl.DeviceConfigurationImpl;
import com.energyict.mdc.device.config.impl.DeviceTypeImpl;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.TypedPropertiesValueAdapter;
import com.energyict.mdc.device.data.exceptions.CannotChangeDeviceConfigStillUnresolvedConflicts;
import com.energyict.mdc.device.data.exceptions.CannotDeleteComScheduleFromDevice;
import com.energyict.mdc.device.data.exceptions.CannotSetMultipleComSchedulesWithSameComTask;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;
import com.energyict.mdc.device.data.exceptions.DeviceProtocolPropertyException;
import com.energyict.mdc.device.data.exceptions.MultiplierConfigurationException;
import com.energyict.mdc.device.data.exceptions.NoLifeCycleActiveAt;
import com.energyict.mdc.device.data.exceptions.NoMeterActivationAt;
import com.energyict.mdc.device.data.exceptions.NoStatusInformationTaskException;
import com.energyict.mdc.device.data.exceptions.ProtocolDialectConfigurationPropertiesIsRequiredException;
import com.energyict.mdc.device.data.exceptions.UnsatisfiedReadingTypeRequirementsOfUsagePointException;
import com.energyict.mdc.device.data.exceptions.UsagePointAlreadyLinkedToAnotherDeviceException;
import com.energyict.mdc.device.data.impl.configchange.ServerDeviceForConfigChange;
import com.energyict.mdc.device.data.impl.constraintvalidators.DeviceConfigurationIsPresentAndActive;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueComTaskScheduling;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueMrid;
import com.energyict.mdc.device.data.impl.constraintvalidators.UniqueName;
import com.energyict.mdc.device.data.impl.constraintvalidators.ValidOverruledAttributes;
import com.energyict.mdc.device.data.impl.pki.CentrallyManagedDeviceSecurityAccessor;
import com.energyict.mdc.device.data.impl.pki.CertificateAccessorImpl;
import com.energyict.mdc.device.data.impl.pki.HsmSymmetricKeyAccessorImpl;
import com.energyict.mdc.device.data.impl.pki.PassphraseAccessorImpl;
import com.energyict.mdc.device.data.impl.pki.PlainTextSymmetricKeyAccessorImpl;
import com.energyict.mdc.device.data.impl.pki.SymmetricKeyAccessorImpl;
import com.energyict.mdc.device.data.impl.pki.UnmanageableSecurityAccessorException;
import com.energyict.mdc.device.data.impl.sync.SyncDeviceWithKoreForInfo;
import com.energyict.mdc.device.data.impl.sync.SyncDeviceWithKoreForMultiplierChange;
import com.energyict.mdc.device.data.impl.sync.SyncDeviceWithKoreForRemoval;
import com.energyict.mdc.device.data.impl.sync.SyncDeviceWithKoreForSimpleUpdate;
import com.energyict.mdc.device.data.impl.sync.SynchDeviceWithKoreForConfigurationChange;
import com.energyict.mdc.device.data.impl.sync.SynchNewDeviceWithKore;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionInitiationTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTask;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import com.energyict.obis.ObisCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
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
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_DEVICE_NAME + "}")
@UniqueComTaskScheduling(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_COMTASK + "}")
@DeviceImpl.HasValidShipmentDate(groups = {Save.Create.class})
@ValidOverruledAttributes(groups = {Save.Update.class})
@XmlRootElement
public class DeviceImpl implements Device, ServerDeviceForConfigChange, ServerDevice {
    public static final String IP_V6_ADDRESS = "IPv6Address";
    static final String TIME_ZONE_PROPERTY_NAME = "TimeZone";
    private static final String DEVICE = "com.energyict.mdc.common.device.data.Device";
    private static final String HOST_PROPERTY_SPEC_NAME = "host";
    private static final BigDecimal maxMultiplier = BigDecimal.valueOf(Integer.MAX_VALUE);
    private static Map<Predicate<Class<? extends ProtocolTask>>, Integer> scorePerProtocolTask;
    private DataModel dataModel;
    private EventService eventService;
    private IssueService issueService;
    private Thesaurus thesaurus;
    private Clock clock;
    private MeteringService meteringService;
    private ValidationService validationService;
    private MeteringGroupsService meteringGroupsService;
    private CustomPropertySetService customPropertySetService;
    private ServerDeviceService deviceService;
    private LockService lockService;
    private SecurityManagementService securityManagementService;
    private MdcReadingTypeUtilService readingTypeUtilService;
    private ThreadPrincipalService threadPrincipalService;
    private UserPreferencesService userPreferencesService;
    private DeviceConfigurationService deviceConfigurationService;
    private DeviceMessageService deviceMessageService;
    private MessageService messageService;
    private JsonService jsonService;

    private final List<LoadProfile> loadProfiles = new ArrayList<>();
    private final List<LogBook> logBooks = new ArrayList<>();
    private final List<SecurityAccessor> keyAccessors = new ArrayList<>();
    private final Reference<DeviceType> deviceType = ValueReference.absent();
    @DeviceConfigurationIsPresentAndActive(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    private Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider;
    private Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider;
    private Provider<ConnectionInitiationTaskImpl> connectionInitiationTaskProvider;
    private Provider<ComTaskExecutionImpl> comTaskExecutionProvider;
    private Reference<Batch> batch = ValueReference.absent();
    private ConnectionTaskService connectionTaskService;
    private MeteringZoneService meteringZoneService;
    private CommunicationTaskService communicationTaskService;

    @SuppressWarnings("unused")
    private long id;
    @Valid
    private List<ReadingTypeObisCodeUsageImpl> readingTypeObisCodeUsages = new ArrayList<>();
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(blacklisted = {'%', '+', '/', ';', '?', '\\', '!', '*', '\'', '(', ')', ':', '@', '&', '=', '$', ',', '[', ']'},
            groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FORBIDDEN_CHARS + "}")
    private String name;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(blacklisted = {'<', '>', '/', ';', '?', '\\', '!', '*', '\'', '=', ','}, balcklistedCharRegEx = "")
    private String mRID;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(blacklisted = {'<', '>', '/', ';', '?', '\\', '!', '*', '\'', '=', ','}, balcklistedCharRegEx = "")
    private String serialNumber;
    @Size(max = 32, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String timeZoneId;
    private ZoneId zoneId;
    private Integer yearOfCertification;
    private boolean estimationActive;
    private List<DeviceEstimationRuleSetActivation> estimationRuleSetActivations = new ArrayList<>();
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
    private List<DeviceMessageImpl> deviceMessages = new ArrayList<>();
    private List<ProtocolDialectPropertiesImpl> dialectPropertiesList = new ArrayList<>();
    private List<ProtocolDialectPropertiesImpl> newDialectProperties = new ArrayList<>();
    private List<ProtocolDialectPropertiesImpl> dirtyDialectProperties = new ArrayList<>();
    private Reference<PassiveCalendar> passiveCalendar = ValueReference.absent();
    private Reference<PassiveCalendar> plannedPassiveCalendar = ValueReference.absent();
    private TemporalReference<ServerActiveEffectiveCalendar> activeCalendar = Temporals.absent();
    private transient DeviceValidationImpl deviceValidation;
    private transient AmrSystem amrSystem;
    // Next objects separate 'Kore' Specific Behaviour
    // Will help us with Kore specific stuff as there is Meter, MeterActivation, Multiplier....
    private transient SyncDeviceWithKoreForInfo koreHelper;
    // 'Synchronize with Kore' actions once this device is saved;
    private transient List<SyncDeviceWithKoreMeter> syncsWithKore = new ArrayList<>();

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DeviceImpl.class.getName());

    public DeviceImpl() {
        super();
    }

    @Inject
    public DeviceImpl(
            DataModel dataModel,
            EventService eventService,
            IssueService issueService,
            Thesaurus thesaurus,
            Clock clock,
            MeteringService meteringService,
            ValidationService validationService,
            Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider,
            Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider,
            Provider<ConnectionInitiationTaskImpl> connectionInitiationTaskProvider,
            Provider<ComTaskExecutionImpl> comTaskExecutionProvider,
            MeteringGroupsService meteringGroupsService,
            CustomPropertySetService customPropertySetService,
            MdcReadingTypeUtilService readingTypeUtilService,
            ThreadPrincipalService threadPrincipalService,
            UserPreferencesService userPreferencesService,
            DeviceConfigurationService deviceConfigurationService,
            DeviceMessageService deviceMessageService,
            ServerDeviceService deviceService,
            LockService lockService,
            SecurityManagementService securityManagementService,
            ConnectionTaskService connectionTaskService,
            MeteringZoneService meteringZoneService,
            MessageService messageService,
            JsonService jsonService,
            CommunicationTaskService communicationTaskService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.issueService = issueService;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.meteringService = meteringService;
        this.validationService = validationService;
        this.scheduledConnectionTaskProvider = scheduledConnectionTaskProvider;
        this.inboundConnectionTaskProvider = inboundConnectionTaskProvider;
        this.connectionInitiationTaskProvider = connectionInitiationTaskProvider;
        this.comTaskExecutionProvider = comTaskExecutionProvider;
        this.meteringGroupsService = meteringGroupsService;
        this.customPropertySetService = customPropertySetService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.threadPrincipalService = threadPrincipalService;
        this.userPreferencesService = userPreferencesService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceMessageService = deviceMessageService;
        this.deviceService = deviceService;
        this.lockService = lockService;
        // Helper to get activation info... from 'Kore'
        this.koreHelper = new SyncDeviceWithKoreForInfo(this, this.deviceService, this.readingTypeUtilService, clock, this.eventService);
        this.securityManagementService = securityManagementService;
        this.connectionTaskService = connectionTaskService;
        this.messageService = messageService;
        this.jsonService = jsonService;
        this.communicationTaskService = communicationTaskService;
        this.koreHelper.syncWithKore(this);
        this.meteringZoneService = meteringZoneService;
    }

    DeviceImpl initialize(DeviceConfiguration deviceConfiguration, String name, Instant startDate) {
        this.createTime = this.clock.instant();
        this.deviceConfiguration.set(deviceConfiguration);
        this.setDeviceTypeFromDeviceConfiguration();
        setName(name);
        this.mRID = generateMRID();
        this.koreHelper.setInitialMeterActivationStartDate(startDate);
        createLoadProfiles();
        createLogBooks();
        inheritDeviceConnTaskFromDeviceConfig(deviceConfiguration);
        return this;
    }

    private void inheritDeviceConnTaskFromDeviceConfig(DeviceConfiguration deviceConfiguration) {
        if (Objects.nonNull(deviceConfiguration)) {
            List<Long> inboundTaskIdList = this.getInboundConnectionTasks().stream().map(HasId::getId).collect(Collectors.toList());
            List<Long> outboundTaskIdList = this.getScheduledConnectionTasks().stream().map(HasId::getId).collect(Collectors.toList());
            deviceConfiguration.getPartialConnectionTasks().stream().forEach(partialConnectionTask -> {
                if (!connectionTaskService.findConnectionTaskForPartialOnDevice(partialConnectionTask, this).isPresent()) {
                    if (partialConnectionTask instanceof PartialInboundConnectionTask && !inboundTaskIdList.contains(partialConnectionTask.getId())) {
                        InboundConnectionTaskBuilder inboundConnectionTaskBuilder = this.getInboundConnectionTaskBuilder((PartialInboundConnectionTask) partialConnectionTask);
                        deactivateConnectionTaskIfPropsAreMissing(partialConnectionTask, inboundConnectionTaskBuilder);
                        inboundConnectionTaskBuilder.add();
                    } else if (!(partialConnectionTask instanceof PartialConnectionInitiationTask) && (partialConnectionTask instanceof PartialOutboundConnectionTask && !outboundTaskIdList.contains(partialConnectionTask
                            .getId()))) {
                        ScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = this.getScheduledConnectionTaskBuilder((PartialOutboundConnectionTask) partialConnectionTask);
                        NextExecutionSpecs nextExecutionSpecs = ((PartialOutboundConnectionTask) partialConnectionTask).getNextExecutionSpecs();
                        deactivateConnectionTaskIfPropsAreMissing(partialConnectionTask, scheduledConnectionTaskBuilder);
                        if (nextExecutionSpecs != null) {
                            scheduledConnectionTaskBuilder.setNextExecutionSpecsFrom(nextExecutionSpecs.getTemporalExpression());
                        }
                        scheduledConnectionTaskBuilder.add();
                    } else if (partialConnectionTask instanceof PartialConnectionInitiationTask) {
                        ConnectionInitiationTaskBuilder partialConnectionTaskBuilder = this.getConnectionInitiationTaskBuilder((PartialConnectionInitiationTask) partialConnectionTask);
                        deactivateConnectionTaskIfPropsAreMissing(partialConnectionTask, partialConnectionTaskBuilder);
                        partialConnectionTaskBuilder.add();
                    }
                }
            });
        }
    }

    private void deactivateConnectionTaskIfPropsAreMissing(PartialConnectionTask partialConnectionTask, InboundConnectionTaskBuilder inboundConnectionTaskBuilder) {
        if (Objects.isNull(partialConnectionTask.getComPortPool())) {
            inboundConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
        }
    }

    private void deactivateConnectionTaskIfPropsAreMissing(PartialConnectionTask scheduledConnectionTask, ScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder) {
        //it's wrong that the properties name are hardcoded but to add the properties from OutboundIpConnectionProperties.Fields this bundle will have a dependency on com.energyict.mdc.protocols.
        //also iterating over properties is also not a valid solution because if the property value is null, it isn't present in the list.....
        if (Objects.isNull(scheduledConnectionTask.getComPortPool()) || Objects.isNull(scheduledConnectionTask.getProperty("host")) || Objects.isNull(scheduledConnectionTask.getProperty("portNumber"))) {
            scheduledConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
        }
        if (scheduledConnectionTask.getPluggableClass().getName().equals("Outbound TLS") && Objects.isNull(scheduledConnectionTask.getProperty("ServerTLSCertificate"))) {
            scheduledConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
        }
    }

    private void deactivateConnectionTaskIfPropsAreMissing(PartialConnectionTask scheduledConnectionTask, ConnectionInitiationTaskBuilder scheduledConnectionTaskBuilder) {
        if (Objects.isNull(scheduledConnectionTask.getComPortPool()) || Objects.isNull(scheduledConnectionTask.getProperty("host")) || Objects.isNull(scheduledConnectionTask.getProperty("portNumber"))) {
            scheduledConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
        }
    }

    private String generateMRID() {
        return UUID.randomUUID().toString();
    }

    boolean syncWithKore(SyncDeviceWithKoreMeter syncDeviceWithKore) {
        return syncsWithKore.add(syncDeviceWithKore);
    }

    ValidationService getValidationService() {
        return validationService;
    }

    ServerDeviceService getDeviceService() {
        return deviceService;
    }

    DeviceConfigurationService getDeviceConfigurationService() {
        return deviceConfigurationService;
    }

    LockService getLockService() {
        return lockService;
    }

    EventService getEventService() {
        return eventService;
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
        if (this.meter.isPresent() && !getName().equals(this.meter.get().getName())) {
            this.meter.get().setName(getName());
            this.meter.get().update();
        }
        this.saveDirtyConnectionProperties();
        this.saveNewAndDirtyDialectProperties();
        this.notifyUpdated();
    }

    void validateForUpdate() {
        Save.UPDATE.save(dataModel, this);
    }

    void executeSyncs() {
        // We order the synchronisation actions: first those which create a meter activation, followed
        // by those which reuse the last created meter activation (and just update it).
        syncsWithKore.stream()
                .sorted(new CanUpdateMeterActivationLast())
                .forEach((x) -> x.syncWithKore(DeviceImpl.this));
        syncsWithKore.clear();
    }

    private Optional<Location> getUpdatedLocation() {
        Optional<SyncDeviceWithKoreForSimpleUpdate> currentKoreUpdater = getKoreMeterUpdater();
        if (currentKoreUpdater.isPresent()) {
            return currentKoreUpdater.get().getLocation();
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
        return this.syncsWithKore.stream()
                .filter(x -> x.getClass().isAssignableFrom(SyncDeviceWithKoreForSimpleUpdate.class))
                .map(SyncDeviceWithKoreForSimpleUpdate.class::cast)
                .findFirst();
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

    private void saveDialectProperties(List<ProtocolDialectPropertiesImpl> dialectProperties) {
        dialectProperties.forEach(ProtocolDialectPropertiesImpl::save);
    }

    private void notifyUpdated() {
        this.eventService.postEvent(UpdateEventType.DEVICE.topic(), this);
    }

    private void notifyCreated() {
        this.eventService.postEvent(CreateEventType.DEVICE.topic(), this);
        // In addition notify the creation of ConnectionTasks and ComTaskExecutions
        //this.getConnectionTaskImpls().forEach(ConnectionTaskImpl::notifyCreated);
        //this.getComTaskExecutionImpls().forEach(ComTaskExecutionImpl::notifyCreated);
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
        deleteReferenceOnCalendars();
        deleteDeviceMessages();
        deleteValidationProperties();
        deleteEstimationProperties();
        removeDeviceFromStaticGroups();
        removeZonesOnDevice();
        new SyncDeviceWithKoreForRemoval(this, deviceService, readingTypeUtilService, clock, eventService).syncWithKore(this);
        koreHelper.deactivateMeter(clock.instant());
        this.readingTypeObisCodeUsages.clear();
        this.getDataMapper().remove(this);
    }

    @SuppressWarnings("unchecked")
    private void removeCustomProperties() {
        this.getDeviceType().getCustomPropertySets().forEach(this::removeCustomPropertiesFor);
        this.getRegisters()
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
                this.meteringGroupsService
                        .findEnumeratedEndDeviceGroupsContaining(this.meter.get())
                        .forEach(enumeratedEndDeviceGroup -> removeDeviceFromGroup(enumeratedEndDeviceGroup, this.meter.get()));
            }
        }
    }

    @Override
    public void removeZonesOnDevice() {
        if (this.meter.isPresent()) {
            meteringZoneService.getByEndDevice(this.meter.get()).stream().forEach(endDeviceZone -> endDeviceZone.delete());
        }
    }

    @Override
    public void addZone(String zoneName, String zoneTypeName) {
        if (this.meter.isPresent()) {
            Optional<ZoneType> zoneType = meteringZoneService.getZoneType(zoneTypeName, "MDC");
            if (zoneType.isPresent()) {
                Optional<Zone> zone = meteringZoneService.getZoneByName(zoneName, zoneType.get().getId());
                if (zone.isPresent()) {
                    if (!meteringZoneService.getByEndDevice(meter.get()).stream().filter(endDeviceZone -> endDeviceZone.getZone().getId() == zone.get().getId()).findAny().isPresent()) {
                        meteringZoneService.newEndDeviceZoneBuilder()
                                .withEndDevice(meter.get())
                                .withZone(zone.get())
                                .create();
                    }
                }
            }
        }
    }

    @Override
    public Multimap<String, String> getZones() {
        Multimap<String, String> zones = ArrayListMultimap.create();
        if (this.meter.isPresent()) {
            meteringZoneService.getByEndDevice(this.meter.get()).stream().forEach(endDeviceZone -> zones.put(endDeviceZone.getZone().getName(), endDeviceZone.getZone().getZoneType().getName()));
        }
        return zones;
    }

    private void removeDeviceFromGroup(EnumeratedEndDeviceGroup group, EndDevice endDevice) {
        group
                .getEntries()
                .stream()
                .filter(each -> each.getMember().getId() == endDevice.getId())
                .findFirst()
                .ifPresent(group::remove);
    }

    private void deleteAllIssues() {
        this.issueService
                .findStatus(IssueStatus.WONT_FIX)
                .ifPresent(this::wontFixOpenIssues);
        getListMeterAspect(this::getAllHistoricalIssuesForMeter).forEach(Issue::delete);
    }

    private void wontFixOpenIssues(IssueStatus issueStatus) {
        this.getOpenIssues().forEach(openIssue -> openIssue.close(issueStatus));
    }

    private void deleteComTaskExecutions() {
        this.comTaskExecutions.clear();
    }

    private void deleteConnectionTasks() {
        this.connectionTasks.forEach(ServerConnectionTask::notifyDelete);
        this.connectionTasks.clear();
    }

    private void deleteDeviceMessages() {
        getMessages().forEach(DeviceMessage::delete);
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

    private void deleteValidationProperties() {
        forValidation().findAllOverriddenProperties().forEach(ChannelValidationRuleOverriddenProperties::delete);
    }

    private void deleteEstimationProperties() {
        forEstimation().findAllOverriddenProperties().forEach(ChannelEstimationRuleOverriddenProperties::delete);
    }

    private Meter meterVal;

    @Override
    public Meter getMeter() {
        if (this.meter.isPresent()) {
            meterVal = this.meter.get();
        }
        return meterVal;
    }

    public void setMeter(Meter meterVal) {
        this.meter.set(meterVal);
    }

    public Reference<Meter> getMeterReference() {
        return this.meter;
    }

    @Override
    public SyncDeviceWithKoreForInfo getKoreHelper() {
        return this.koreHelper;
    }

    public Map<MetrologyConfiguration, List<ReadingTypeRequirement>> getUnsatisfiedRequirements(UsagePoint usagePoint, Instant from, DeviceConfiguration deviceConfiguration) {
        List<UsagePointMetrologyConfiguration> effectiveMetrologyConfigurations = usagePoint.getEffectiveMetrologyConfigurations()
                .stream()
                .filter(emc -> !emc.getRange().intersection(Range.atLeast(from)).isEmpty())
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                .collect(Collectors.toList());
        if (effectiveMetrologyConfigurations.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ReadingType> supportedReadingTypes = getDeviceCapabilities(deviceConfiguration);
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

    @Override
    public void touch() {
        this.dataModel.touch(this);
    }

    @Override
    public void activateEstimation() {
        this.estimationActive = true;
    }

    @Override
    public void deactivateEstimation() {
        this.estimationActive = false;
    }

    @Override
    @XmlAttribute
    public long getId() {
        return id;
    }

    //  All 'EndDevice' fields
    private SyncDeviceWithKoreForSimpleUpdate findOrCreateKoreUpdater() {
        Optional<SyncDeviceWithKoreForSimpleUpdate> currentKoreUpdater = getKoreMeterUpdater();
        SyncDeviceWithKoreForSimpleUpdate koreUpdater = new SyncDeviceWithKoreForSimpleUpdate(this, deviceService, readingTypeUtilService, eventService);
        if (!currentKoreUpdater.isPresent()) {
            syncsWithKore.add(koreUpdater);
            return koreUpdater;
        } else {
            return currentKoreUpdater.get();
        }
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
            Optional<? extends MeterActivation> meterActivationAt = this.meter.get().getMeterActivation(startDateMultiplier.get());
            if (!meterActivationAt.isPresent()) {
                throw MultiplierConfigurationException.multiplierMustHaveMeterActivation(thesaurus);
            }
        }
    }

    @Override
    public List<Channel> getChannels() {
        return loadProfiles.stream().flatMap(lp -> lp.getChannels().stream()).collect(Collectors.toList());
    }

    @Override
    public String getSerialNumber() {
        Optional<SyncDeviceWithKoreForSimpleUpdate> currentKoreUpdater = getKoreMeterUpdater();
        if (currentKoreUpdater.isPresent()) {
            return currentKoreUpdater.get().getSerialNumber();
        } else if (meter.isPresent() && meter.get().getSerialNumber() != null && !meter.get().getSerialNumber().isEmpty()) {
            return meter.get().getSerialNumber();
        }
        return this.serialNumber;
    }

    @Override
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        if (deviceService != null && eventService != null && readingTypeUtilService != null) {
            findOrCreateKoreUpdater().setSerialNumber(serialNumber);
        }
    }

    @Override
    public void setMultiplier(BigDecimal multiplier, Instant from) {
        if (getMultiplier() == null || multiplier.compareTo(getMultiplier()) != 0) {
            validateMultiplierValue(multiplier);
            Instant now = clock.instant();
            Optional<Instant> startDateMultiplier = from == null ? Optional.of(now) : Optional.of(from);
            if (this.meter.isPresent()) {
                validateStartDateOfNewMultiplier(now, startDateMultiplier);  // should not be validated for device creation case
            }
            SyncDeviceWithKoreForMultiplierChange multiplierChange =
                    new SyncDeviceWithKoreForMultiplierChange(
                            this,
                            startDateMultiplier.get(),
                            multiplier,
                            deviceService, readingTypeUtilService, eventService);
            //All actions to take to sync with Kore once a Device is created
            syncsWithKore.add(multiplierChange);
        }
    }

    @XmlAttribute
    private BigDecimal multiplier;

    @Override
    public BigDecimal getMultiplier() {
        if (this.koreHelper != null) {
            this.multiplier = this.koreHelper.getMultiplier().orElse(BigDecimal.ONE);
        }
        return multiplier;
    }

    @Override
    public void setMultiplier(BigDecimal multiplier) {
        if (clock != null) {
            this.setMultiplier(multiplier, null);
        } else {
            validateMultiplierValue(multiplier);
            this.multiplier = multiplier;
        }
    }

    @Override
    public Optional<BigDecimal> getMultiplierAt(Instant multiplierEffectiveTimeStamp) {
        return this.koreHelper.getMultiplierAt(multiplierEffectiveTimeStamp);
    }

    @XmlAttribute
    private Instant multiplierEffectiveTimeStamp;

    @Override
    public Instant getMultiplierEffectiveTimeStamp() {
        if (this.koreHelper != null) {
            multiplierEffectiveTimeStamp = this.koreHelper.getMultiplierEffectiveTimeStamp();
        }
        return multiplierEffectiveTimeStamp;
    }

    @Override
    public Integer getYearOfCertification() {
        return this.yearOfCertification;
    }

    @Override
    public void setYearOfCertification(Integer yearOfCertification) {
        this.yearOfCertification = yearOfCertification;
    }

    public Instant getModificationDate() {
        return this.modTime;
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

    @Override
    public long getDeviceEventsCountByFilter(EndDeviceEventRecordFilterSpecification filter) {
        if (meter.isPresent()) {
            return meter.get().getDeviceEventsCountByFilter(filter);
        } else {
            return 0;
        }
    }

    public List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter) {
        return this.getListMeterAspect(meter -> meter.getDeviceEventsByFilter(filter));
    }

    @Override
    public List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter, Integer from, Integer to) {
        return this.getListMeterAspect(meter -> meter.getDeviceEventsByFilter(filter, from, to));
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

    @Override
    public void setProtocolProperty(String name, Object value) {
        PropertySpec propertySpec = getPropertySpecForProperty(name);
        ValueFactory valueFactory = propertySpec.getValueFactory();
        String propertyValue;
        if (valueFactory instanceof BigDecimalFactory && value instanceof Integer) {
            propertyValue = ((Integer) value).toString();
        } else {
            propertyValue = propertySpec.getValueFactory().toStringValue(value);
        }
        Optional<DeviceProtocolProperty> optionalProperty = getDeviceProtocolProperty(name);
        if (optionalProperty.isPresent()) {
            updateDeviceProperty(optionalProperty.get(), propertyValue);
        } else {
            addDeviceProperty(propertySpec, propertyValue);
        }
        if (getId() > 0) {
            dataModel.touch(this);
        }
    }

    @Override
    public void removeProtocolProperty(String name) {
        for (DeviceProtocolProperty deviceProtocolProperty : deviceProperties) {
            if (deviceProtocolProperty.getName().equals(name)) {
                this.deviceProperties.remove(deviceProtocolProperty);
                updateConnectionMethodProperty(deviceProtocolProperty, "");
                this.notifyUpdate(deviceProtocolProperty);
                dataModel.touch(this);
                break;
            }
        }
    }

    @Override
    public TypedProperties getSecurityProperties(SecurityPropertySet securityPropertySet) {
        TypedProperties securityProperties = TypedProperties.empty();
        for (ConfigurationSecurityProperty configurationSecurityProperty : securityPropertySet.getConfigurationSecurityProperties()) {
            Optional<SecurityAccessor> optionalKeyAccessor = getSecurityAccessors()
                    .stream()
                    .filter(keyAccessor -> keyAccessor.getSecurityAccessorType().getName().equals(configurationSecurityProperty.getSecurityAccessorType().getName()))
                    .findFirst();
            if (optionalKeyAccessor.isPresent() && optionalKeyAccessor.get().getActualValue().isPresent()) {
                Object actualValue = optionalKeyAccessor.get().getActualValue().get();
                Object adaptedValue = TypedPropertiesValueAdapter.adaptActualValueToUPLValue(actualValue, configurationSecurityProperty.getSecurityAccessorType());
                securityProperties.setProperty(configurationSecurityProperty.getName(), adaptedValue);
            }
        }
        return securityProperties;
    }

    @Override
    public Optional<SecurityAccessor> getSecurityAccessorByName(String securityAccessorName) {
        Optional<SecurityAccessor> optionalKeyAccessor = getSecurityAccessors()
                .stream()
                .filter(keyAccessor -> keyAccessor.getName().equals(securityAccessorName))
                .findFirst();

        return optionalKeyAccessor;
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
    public void store(MeterReading meterReading) {
        this.store(meterReading, null);
    }

    @Override
    public void store(MeterReading meterReading, Instant readingDate) {
        this.meter.getOptional().ifPresent(meter -> meter.store(QualityCodeSystem.MDC, meterReading, readingDate));
    }

    @Override
    public boolean hasData() {
        return this.getOptionalMeterAspect(this::hasData).get();
    }

    @Override
    public void touchDevice() {
        this.touch();
    }

    @Override
    public MeterActivation activate(Instant start, UsagePoint usagePoint, MeterRole meterRole) {
        if (start == null || usagePoint == null || meterRole == null) {
            throw new IllegalArgumentException("All arguments are mandatory and can't be null.");
        }
        try {
            usagePoint.linkMeters().activate(start, getMeterReference().get(), meterRole).throwingValidation().complete();
        } catch (MeterHasUnsatisfiedRequirements badRequirementsEx) {
            throw new UnsatisfiedReadingTypeRequirementsOfUsagePointException(this.thesaurus, badRequirementsEx.getUnsatisfiedRequirements());
        } catch (UsagePointHasMeterOnThisRole upActiveEx) {
            throw new UsagePointAlreadyLinkedToAnotherDeviceException(
                    this.thesaurus,
                    getLongDateFormatForCurrentUser(),
                    upActiveEx.getMeter().getMeterActivations(upActiveEx.getConflictActivationRange()).get(0));
        }
        this.koreHelper.reloadCurrentMeterActivation();
        return getCurrentMeterActivation().get();
    }

    @Override
    public void deactivate(Instant when) {
        this.koreHelper.deactivateMeter(when);
    }

    @Override
    public void deactivateNow() {
        this.deactivate(this.clock.instant());
    }

    @XmlAttribute
    private Optional<MeterActivation> currentMeterActivation;

    @Override
    public Optional<MeterActivation> getCurrentMeterActivation() {
        if (this.koreHelper != null) {
            currentMeterActivation = this.koreHelper.getCurrentMeterActivation();
        }
        return currentMeterActivation;
    }

    @Override
    public Optional<MeterActivation> getMeterActivation(Instant when) {
        return this.getOptionalMeterAspect(meter -> meter.getMeterActivation(when).map(MeterActivation.class::cast));
    }

    @Override
    public List<MeterActivation> getMeterActivationsMostRecentFirst() {
        return this.getListMeterAspect(this::getSortedMeterActivations);
    }

    @Override
    public List<MeterActivation> getMeterActivations(Range<Instant> range) {
        return getSortedMeterActivations(meter.get(), range);
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
    public List<ConnectionInitiationTask> getConnectionInitiationTasks() {
        return this.getConnectionTaskImpls()
                .filter(ct -> ct instanceof ConnectionInitiationTask)
                .map(ConnectionInitiationTask.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduledConnectionTask> getScheduledConnectionTasks() {
        return this.getConnectionTaskImpls()
                .filter(ct -> ct instanceof ScheduledConnectionTask)
                .map(ScheduledConnectionTask.class::cast)
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
    public void removePermanentlyConnectionTask(ConnectionTask<?, ?> connectionTask) {
        this.connectionTasks.remove(connectionTask);
    }

    @Override
    public List<ComTaskExecution> getComTaskExecutions() {
        return comTaskExecutions.stream()
                .filter(((Predicate<ComTaskExecution>) ComTaskExecution::isObsolete).negate())
                .collect(Collectors.toList());
    }

    @Override
    public ComTaskExecutionUpdater getComTaskExecutionUpdater(ComTaskExecution comTaskExecution) {
        return comTaskExecution.getUpdater();
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
    public ComTaskExecutionBuilder newScheduledComTaskExecution(ComSchedule comSchedule) {
        return new ScheduledComTaskExecutionBuilderForDevice(comTaskExecutionProvider, comSchedule);
    }

    @Override
    public ComTaskExecutionBuilder newManuallyScheduledComTaskExecution(ComTaskEnablement comTaskEnablement, TemporalExpression temporalExpression) {
        return new ManuallyScheduledComTaskExecutionBuilderForDevice(
                this.comTaskExecutionProvider,
                comTaskEnablement,
                temporalExpression);
    }

    @Override
    public AdHocComTaskExecutionBuilderForDevice newAdHocComTaskExecution(ComTaskEnablement comTaskEnablement) {
        return new AdHocComTaskExecutionBuilderForDevice(comTaskExecutionProvider, comTaskEnablement);
    }

    @Override
    public ComTaskExecutionBuilder newFirmwareComTaskExecution(ComTaskEnablement comTaskEnablement) {
        return new FirmwareComTaskExecutionBuilderForDevice(comTaskExecutionProvider, comTaskEnablement);
    }

    @Override
    public List<ProtocolDialectConfigurationProperties> getProtocolDialects() {
        return this.getDeviceConfiguration().getProtocolDialectConfigurationPropertiesList();
    }

    @Override
    public void removeComSchedule(ComSchedule comSchedule) {
        List<ComTaskExecutionImpl> comTasksWithSchedule =
                getComTaskExecutionImpls()
                        .filter(cte -> cte.getComSchedule().isPresent())
                        .filter(cte -> cte.getComSchedule().get().getId() == comSchedule.getId())
                        .collect(Collectors.toList());
        if (comTasksWithSchedule.size() == 0) {
            throw new CannotDeleteComScheduleFromDevice(comSchedule, this, this.thesaurus, MessageSeeds.COM_SCHEDULE_CANNOT_DELETE_IF_NOT_FROM_DEVICE);
        } else {
            LOGGER.info("CXO-11731: Update comtask execution from removeComSchedule" + comTasksWithSchedule);
            comTasksWithSchedule.forEach(comTaskExecution -> comTaskExecution.getUpdater().removeSchedule().update());
        }
    }

    @Override
    public void addToGroup(EnumeratedEndDeviceGroup enumeratedEndDeviceGroup, Range<Instant> range) {
        enumeratedEndDeviceGroup.add(this.meter.get(), range);
    }

    @Override
    public DeviceValidation forValidation() {
        if (deviceValidation == null) {
            deviceValidation = new DeviceValidationImpl(this.dataModel, this.validationService, this.thesaurus, this, clock);
        }
        return deviceValidation;
    }

    @Override
    public DeviceEstimation forEstimation() {
        return this.dataModel.getInstance(DeviceEstimationImpl.class).init(this, this.estimationActive, this.estimationRuleSetActivations);
    }

    @XmlElement(type = UsagePointImpl.class)
    private Optional<UsagePoint> usagePoint;

    @Override
    public Optional<UsagePoint> getUsagePoint() {
        if (koreHelper != null) {
            usagePoint = koreHelper.getUsagePoint();
        }
        return usagePoint;
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
    public boolean hasOpenIssues() {
        return !getOpenIssues().isEmpty();
    }

    @Override
    public List<OpenIssue> getOpenIssues() {
        return getListMeterAspect(this::getOpenIssuesForMeter);
    }

    @XmlElement(type = StateImpl.class)
    private State state;

    @Override
    public State getState() {
        if (clock != null) {
            state = this.getState(this.clock.instant()).get();
        }
        return state;
    }

    @XmlElement(type = StageImpl.class)
    private Stage stage;

    @Override
    public Stage getStage() {
        if (clock != null) {
            stage = this.getState(this.clock.instant()).get().getStage().orElseThrow(() -> new IllegalStateException("Device state does not have a stage"));
        }
        return stage;
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
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    private StateTimeline stateTimeline;

    @Override
    public StateTimeline getStateTimeline() {
        Optional<StateTimeline> optStateTimeline = this.getOptionalMeterAspect(EndDevice::getStateTimeline);
        if (optStateTimeline.isPresent()) {
            stateTimeline = optStateTimeline.get();
        }
        return stateTimeline;
    }

    @JsonIgnore
    @XmlTransient
    @Override
    public List<DeviceLifeCycleChangeEvent> getDeviceLifeCycleChangeEvents() {
        // Merge the StateTimeline with the list of change events from my DeviceType.
        List<DeviceLifeCycleChangeEvent> deviceLifeChangeEvents = new ArrayList<>();
        Deque<StateTimeSlice> stateTimeSlices = new LinkedList<>(this.getStateTimeline().getSlices());
        Deque<com.energyict.mdc.common.device.config.DeviceLifeCycleChangeEvent> deviceTypeChangeEvents = new LinkedList<>(this.getDeviceTypeLifeCycleChangeEvents());
        List<DeviceLifeCycleChangeEvent> changeEvents = new ArrayList<>();
        boolean notReady;
        do {
            DeviceLifeCycleChangeEvent newEvent = this.newEventForMostRecent(stateTimeSlices, deviceTypeChangeEvents);
            deviceLifeChangeEvents.add(newEvent);
            notReady = !stateTimeSlices.isEmpty() || !deviceTypeChangeEvents.isEmpty();
        } while (notReady);
        return deviceLifeChangeEvents;
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
    public CalendarSupport calendars() {
        return new DeviceCalendarSupport(this, this.dataModel, this.clock);
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

    @Override
    public Channel.ChannelUpdater getChannelUpdaterFor(Channel channel) {
        return new ChannelUpdaterImpl(this.deviceService, this.readingTypeUtilService, this.clock, channel, eventService);
    }

    @Override
    public Register.RegisterUpdater getRegisterUpdaterFor(Register register) {
        return new RegisterUpdaterImpl(this.deviceService, this.readingTypeUtilService, this.clock, eventService, register);
    }

    @Override
    public void runStatusInformationTask(Consumer<ComTaskExecution> requestedAction) {
        ComTaskExecution comTaskExecution = null;
        Optional<ComTaskExecution> bestComTaskExecution = getComTaskExecutions().stream()
                .filter(cte -> containsStatusInformationProtocolTask(cte.getProtocolTasks()))
                .min((cte1, cte2) -> compareProtocolTasks(cte1.getProtocolTasks(), cte2.getProtocolTasks()));
        Optional<ComTaskEnablement> bestComTaskEnablement = getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> containsStatusInformationProtocolTask(comTaskEnablement.getComTask().getProtocolTasks()))
                .min((cte1, cte2) -> compareProtocolTasks(cte1.getComTask().getProtocolTasks(), cte2.getComTask().getProtocolTasks()));
        if (bestComTaskExecution.isPresent()) {
            comTaskExecution = bestComTaskExecution.get();
        } else if (bestComTaskEnablement.isPresent()) {
            comTaskExecution = createAdHocComTaskExecutionToRunNow(bestComTaskEnablement.get());
        }
        if (comTaskExecution == null) {
            throw new NoStatusInformationTaskException();
        }
        connectionTaskService.findAndLockConnectionTaskById(comTaskExecution.getConnectionTaskId());
        comTaskExecution = communicationTaskService.findAndLockComTaskExecutionById(comTaskExecution.getId())
                .orElseThrow(NoStatusInformationTaskException::new);
        requestedAction.accept(comTaskExecution);
    }

    @Override
    public Optional<Device> getHistory(Instant when) {
        if (when.isAfter(modTime)) {
            return Optional.of(this); // current device, for sure
        }
        if (when.isBefore(this.getLifecycleDates().getReceivedDate().orElse(createTime))) {
            return Optional.empty(); // there was no device
        }
        return Optional.of(getFirstJournalEntryAfter(when)); // crutch for the case of gaps in journal table
    }

    private Device getFirstJournalEntryAfter(Instant when) {
        return dataModel.mapper(Device.class)
                .at(Instant.EPOCH)
                .find(Arrays.asList(Operator.EQUAL.compare("ID", getId()), Operator.GREATERTHAN.compare("JOURNALTIME", when.toEpochMilli())))
                .stream()
                .min(Comparator.comparing(JournalEntry::getJournalTime))
                .map(JournalEntry::get) // closest journal entry after "when"
                .orElse(this); // journal entries not found => current device
    }

    @Override
    public void addInBatch(Batch batch) {
        this.batch.set(batch);
        save();
    }

    @Override
    public void removeFromBatch(Batch batch) {
        if (this.batch.isPresent() && this.batch.get().getId() == batch.getId()) {
            this.batch.set(null);
            save();
        }
    }

    @Override
    public Optional<Batch> getBatch() {
        return this.batch.getOptional();
    }

    @Override
    public void setConnectionTaskForComTaskExecutions(ConnectionTask connectionTask) {
        List<ComTask> comTasksWithConnectionTask = this.getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> !comTaskEnablement.usesDefaultConnectionTask())
                .filter(comTaskEnablement -> !comTaskEnablement.getConnectionFunction().isPresent())
                .filter(comTaskEnablement -> comTaskEnablement.getPartialConnectionTask().isPresent())
                .filter(comTaskEnablement -> comTaskEnablement.getPartialConnectionTask().get().equals(connectionTask.getPartialConnectionTask()))
                .map(ComTaskEnablement::getComTask)
                .collect(Collectors.toList());
        this.getComTaskExecutions().stream()
                .filter(comTaskExecution -> comTasksWithConnectionTask.contains(comTaskExecution.getComTask()))
                .forEach((comTaskExecution) -> {
                    ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getUpdater();
                    comTaskExecutionUpdater.connectionTask(connectionTask);
                    LOGGER.info("CXO-11731: Update comtask execution from setConnectionTaskForComTaskExecutions" + connectionTask + " comtaskExec=" + comTaskExecution);
                    comTaskExecutionUpdater.update();
                });
    }

    @Override
    public String getManufacturer() {
        Optional<SyncDeviceWithKoreForSimpleUpdate> currentKoreUpdater = getKoreMeterUpdater();
        if (currentKoreUpdater.isPresent()) {
            return currentKoreUpdater.get().getManufacturer();
        } else if (meter.isPresent()) {
            return meter.get().getManufacturer();
        }
        return null;
    }

    @Override
    public void setManufacturer(String manufacturer) {
        findOrCreateKoreUpdater().setManufacturer(manufacturer);
    }

    @Override
    public String getModelNumber() {
        Optional<SyncDeviceWithKoreForSimpleUpdate> currentKoreUpdater = getKoreMeterUpdater();
        if (currentKoreUpdater.isPresent()) {
            return currentKoreUpdater.get().getModelNumber();
        } else if (meter.isPresent()) {
            return meter.get().getModelNumber();
        }
        return null;
    }

    @Override
    public void setModelNumber(String modelNumber) {
        findOrCreateKoreUpdater().setModelNumber(modelNumber);
    }

    @Override
    public String getModelVersion() {
        Optional<SyncDeviceWithKoreForSimpleUpdate> currentKoreUpdater = getKoreMeterUpdater();
        if (currentKoreUpdater.isPresent()) {
            return currentKoreUpdater.get().getModelVersion();
        } else if (meter.isPresent()) {
            return meter.get().getModelVersion();
        }
        return null;
    }

    @Override
    public void setModelVersion(String modelVersion) {
        findOrCreateKoreUpdater().setModelVersion(modelVersion);
    }

    /**
     * Only used for JSON serializing
     */
    @Override
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
    }

    @Override
    public List<SecurityAccessor> getSecurityAccessors() {
        List<SecurityAccessor> securityAccessorsManagedCentrally = getSecurityAccessorsManagedCentrally();
        List<SecurityAccessor> securityAccessors = new ArrayList<>(this.keyAccessors.size() + securityAccessorsManagedCentrally.size());
        securityAccessors.addAll(this.keyAccessors);
        securityAccessors.addAll(securityAccessorsManagedCentrally);
        return securityAccessors;
    }

    @XmlElements({
            @XmlElement(type = PassphraseAccessorImpl.class),
            @XmlElement(type = CertificateAccessorImpl.class),
            @XmlElement(type = PlainTextSymmetricKeyAccessorImpl.class),
            @XmlElement(type = HsmSymmetricKeyAccessorImpl.class),
    })
    public List<SecurityAccessor> getKeyAccessors() {
        return this.keyAccessors;
    }

    public void setKeyAccessors(List<SecurityAccessor> keyAccessors) {
        this.keyAccessors.addAll(keyAccessors);
    }

    @Override
    public Optional<SecurityAccessor> getSecurityAccessor(SecurityAccessorType securityAccessorType) {
        Optional<SecurityAccessor> securityAccessor = keyAccessors.stream()
                .filter(keyAccessor -> keyAccessor.getSecurityAccessorType().getId() == securityAccessorType.getId())
                .findAny();
        if (securityAccessor.isPresent()) {
            return securityAccessor;
        }
        return getDeviceType().getSecurityAccessorTypes().stream()
                .filter(sat -> sat.getId() == securityAccessorType.getId())
                .findAny()
                .flatMap(securityManagementService::getDefaultValues)
                .map(sa -> CentrallyManagedDeviceSecurityAccessor.of(thesaurus, this, sa));
    }

    @Override
    public SecurityAccessor newSecurityAccessor(SecurityAccessorType securityAccessorType) {
        validateManageable(securityAccessorType);
        CryptographicType cryptographicType = securityAccessorType.getKeyType().getCryptographicType();
        switch (cryptographicType) {
            case Certificate:
            case ClientCertificate:
            case TrustedCertificate:
                CertificateAccessorImpl certificateAccessor = dataModel.getInstance(CertificateAccessorImpl.class);
                certificateAccessor.init(securityAccessorType, this);
                this.keyAccessors.add(certificateAccessor);
                return certificateAccessor;
            case SymmetricKey:
                SymmetricKeyAccessorImpl symmetricKeyAccessor = dataModel.getInstance(PlainTextSymmetricKeyAccessorImpl.class);
                symmetricKeyAccessor.init(securityAccessorType, this);
                this.keyAccessors.add(symmetricKeyAccessor);
                return symmetricKeyAccessor;
            case Passphrase:
                PassphraseAccessorImpl passphraseAccessor = dataModel.getInstance(PassphraseAccessorImpl.class);
                passphraseAccessor.init(securityAccessorType, this);
                this.keyAccessors.add(passphraseAccessor);
                return passphraseAccessor;
            case AsymmetricKey:
                return null; // TODO implement? will this occur?
            case Hsm:
                symmetricKeyAccessor = dataModel.getInstance(HsmSymmetricKeyAccessorImpl.class);
                symmetricKeyAccessor.init(securityAccessorType, this);
                this.keyAccessors.add(symmetricKeyAccessor);
                return symmetricKeyAccessor;
            default:
                throw new IllegalArgumentException("Unknown cryptographic type " + cryptographicType.name());
        }
    }

    @Override
    public void removeSecurityAccessor(SecurityAccessor securityAccessor) {
        validateManageable(securityAccessor.getSecurityAccessorType());
        this.getSecurityAccessor(securityAccessor.getSecurityAccessorType()).ifPresent(keyAccessors::remove);
    }

    @Override
    public List<Register> getRegisters() {
        return new ArrayList<>(getDeviceConfiguration().getRegisterSpecs()
                .stream()
                .map(this::newRegisterFor)
                .collect(Collectors.toList()));
    }

    @Override
    public Optional<Register> getRegisterWithDeviceObisCode(ObisCode code) {
        for (RegisterSpec registerSpec : getDeviceConfiguration().getRegisterSpecs()) {
            if (registerSpec.getDeviceObisCode().equals(code)) {
                return Optional.of(this.newRegisterFor(registerSpec));
            }
        }
        return Optional.empty();
    }

    public List<LoadProfile> getLoadProfiles() {
        return Collections.unmodifiableList(this.loadProfiles);
    }

    @Override
    public boolean isLogicalSlave() {
        return getDeviceType().isLogicalSlave();
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
            //All actions to take to sync with Kore once a Device is created
            syncsWithKore.add(new SynchNewDeviceWithKore(this, koreHelper.getInitialMeterActivationStartDate(), deviceService, readingTypeUtilService, clock, eventService));
            this.saveNewDialectProperties();
            this.createComTaskExecutionsForEnablementsMarkedAsAlwaysExecuteForInbound();
            this.notifyCreated();
            sendMessageCreated();
        }
        executeSyncs();
    }

    @Override
    public void delete() {
        this.notifyDeviceIsGoingToBeDeleted();
        this.doDelete();
        this.notifyDeleted();
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
        findOrCreateKoreUpdater().setLocation(location);
    }

    @Override
    public Optional<SpatialCoordinates> getSpatialCoordinates() {
        Optional<SpatialCoordinates> updatedValue = getUpdatedSpatialCoordinates();
        if (updatedValue.isPresent()) {
            return updatedValue;
        }
        return getMeterReference().getOptional().map(EndDevice::getSpatialCoordinates).orElse(Optional.empty());
    }

    @Override
    public void setSpatialCoordinates(SpatialCoordinates spatialCoordinates) {
        Optional<SyncDeviceWithKoreForSimpleUpdate> currentKoreUpdater = getKoreMeterUpdater();
        SyncDeviceWithKoreForSimpleUpdate koreUpdater = new SyncDeviceWithKoreForSimpleUpdate(this, deviceService, readingTypeUtilService, eventService);
        if (!currentKoreUpdater.isPresent()) {
            syncsWithKore.add(koreUpdater);
        } else {
            koreUpdater = currentKoreUpdater.get();
        }
        koreUpdater.setSpatialCoordinates(spatialCoordinates);
    }

    @XmlAttribute
    @Override
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
    @XmlElement(type = DeviceTypeImpl.class, name = "deviceType")
    public DeviceType getDeviceType() {
        return this.deviceType.get();
    }

    @Override
    public void setDeviceType(DeviceType deviceType) {
        this.deviceType.set(deviceType);
    }

    @Override
    public List<DeviceMessage> getMessages() {
        return Collections.unmodifiableList(this.deviceMessages);
    }

    @Override
    public List<DeviceMessage> getFirmwareMessages() {
        return deviceMessageService.findDeviceFirmwareMessages(this);
    }

    @Override
    public List<DeviceMessage> getMessagesByState(DeviceMessageStatus status) {
        return this.deviceMessages.stream()
                .filter(deviceMessage -> deviceMessage.getStatus().equals(status))
                .collect(toList());
    }

    @Override
    public Optional<DeviceProtocolPluggableClass> getDeviceProtocolPluggableClass() {
        return getDeviceType().getDeviceProtocolPluggableClass();
    }

    @Override
    @XmlElement(type = DeviceConfigurationImpl.class, name = "deviceConfiguration")
    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration.orNull();
    }

    @Override
    public void setDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        this.deviceConfiguration.set(deviceConfiguration);
    }

    @Deprecated
    @Override
    @JsonIgnore
    @XmlTransient
    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(getZone());
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
    public ZoneId getZone() {
        if (this.zoneId == null) {
            if (!Checks.is(timeZoneId).empty() && ZoneId.getAvailableZoneIds().contains(this.timeZoneId)) {
                this.zoneId = ZoneId.of(timeZoneId);
            } else if (clock != null) {
                this.zoneId = clock.getZone();
            }
        }
        return this.zoneId;
    }

    @Override
    public void setZone(ZoneId zone) {
        this.zoneId = zone == null ? clock.getZone() : zone;
        this.timeZoneId = zoneId.getId();
        updateChannelsZoneId(zoneId);
    }

    private void updateChannelsZoneId(ZoneId zoneId) {
        if (this.meter.isPresent()) {
            this.meter.get().getChannelsContainers().forEach(channelsContainer -> channelsContainer.getChannels()
                    .forEach(channel -> channel.updateZoneId(zoneId)));
        }
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
    public void validateDeviceCanChangeConfig(DeviceConfiguration destinationDeviceConfiguration) {
        if (this.getDeviceConfiguration().getId() == destinationDeviceConfiguration.getId()) {
            throw DeviceConfigurationChangeException.cannotChangeToSameConfig(thesaurus, this);
        }
        if (destinationDeviceConfiguration.getDeviceType().getId() != getDeviceType().getId()) {
            throw DeviceConfigurationChangeException.cannotChangeToConfigOfOtherDeviceType(thesaurus);
        }
        if (getDeviceType().isMultiElementSlave()) {
            throw DeviceConfigurationChangeException.cannotChangeConfigOfMultiElementSubmeterDevice(thesaurus);
        }
        checkIfAllConflictsAreSolved(this.getDeviceConfiguration(), destinationDeviceConfiguration);
        validateMetrologyConfigRequirements(destinationDeviceConfiguration);
    }

    @Override
    public void setNewDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        this.deviceConfiguration.set(deviceConfiguration);
        SynchDeviceWithKoreForConfigurationChange multiplierChange = new SynchDeviceWithKoreForConfigurationChange(this, deviceService, readingTypeUtilService, clock, eventService);
        //All actions to take to sync with Kore once a Device is created
        syncsWithKore.add(multiplierChange);
    }

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

    void validateMetrologyConfigRequirements(DeviceConfiguration destinationDeviceConfiguration) {
        this.getCurrentMeterActivation()
                .ifPresent(meterActivation -> meterActivation.getUsagePoint()
                        .ifPresent(usagePoint -> {
                            Map<MetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements = getUnsatisfiedRequirements(usagePoint, clock.instant(), destinationDeviceConfiguration);
                            if (!unsatisfiedRequirements.isEmpty()) {
                                throw DeviceConfigurationChangeException.unsatisfiedRequirements(thesaurus, this, destinationDeviceConfiguration, unsatisfiedRequirements);
                            }
                        })
                );
    }

    private void checkIfAllConflictsAreSolved(DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration) {
        originDeviceConfiguration.getDeviceType().getDeviceConfigConflictMappings().stream()
                .filter(deviceConfigConflictMapping -> deviceConfigConflictMapping.getOriginDeviceConfiguration()
                        .getId() == originDeviceConfiguration.getId()
                        && deviceConfigConflictMapping.getDestinationDeviceConfiguration()
                        .getId() == destinationDeviceConfiguration.getId())
                .filter(Predicates.not(DeviceConfigConflictMapping::isSolved)).findFirst()
                .ifPresent(deviceConfigConflictMapping1 -> {
                    throw new CannotChangeDeviceConfigStillUnresolvedConflicts(thesaurus, this, destinationDeviceConfiguration);
                });
    }

    Optional<ReadingType> getCalculatedReadingTypeFromMeterConfiguration(ReadingType readingType, Instant timeStamp) {
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

    Optional<MeterReadingTypeConfiguration> getMeterReadingTypeConfigurationFor(ReadingType readingType) {
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

    private void addDeviceProperty(PropertySpec propertySpec, String propertyValue) {
        if (propertyValue != null) {
            DeviceProtocolPropertyImpl deviceProtocolProperty = this.dataModel.getInstance(DeviceProtocolPropertyImpl.class).initialize(this, propertySpec, propertyValue);
            Save.CREATE.validate(dataModel, deviceProtocolProperty);
            this.notifyUpdate(deviceProtocolProperty);
            this.deviceProperties.add(deviceProtocolProperty);
            updateConnectionMethodProperty(deviceProtocolProperty, propertyValue);
            if (deviceProtocolProperty.getName().equals(TIME_ZONE_PROPERTY_NAME)) {
                this.setZone(ZoneId.of(propertyValue));
            }
        }
    }

    private void updateDeviceProperty(DeviceProtocolProperty property, String value) {
        if (propertyChanged(property.getPropertyValue(), value)) {
            property.setValue(value);
            property.update();
            updateConnectionMethodProperty(property, value);
            if (property.getName().equals(TIME_ZONE_PROPERTY_NAME)) {
                this.setZone(ZoneId.of(value));
            }
        }
    }

    private void updateConnectionMethodProperty(DeviceProtocolProperty property, String value) {
        //update the host property of the outbound connections with the ipv6address value
        try {
            if (property.getName().equals(IP_V6_ADDRESS)) {
                for (ScheduledConnectionTask outboundTask : this.getScheduledConnectionTasks()) {
                    LOGGER.info("Save ipv6: '" + value + "' on outbound connection: '" + outboundTask.getName() + "'");
                    outboundTask.setProperty(HOST_PROPERTY_SPEC_NAME, value);
                    outboundTask.saveAllProperties();
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Could not save ipv6 to host property on outbound connection: " + e.getMessage());
        }
    }

    private boolean propertyChanged(String oldValue, String newValue) {
        return !oldValue.equals(newValue);
    }

    private Optional<DeviceProtocolProperty> getDeviceProtocolProperty(String name) {
        for (DeviceProtocolProperty deviceProperty : deviceProperties) {
            if (deviceProperty.getName().equals(name)) {
                return Optional.of(deviceProperty);
            }
        }
        return Optional.empty();
    }

    public List<DeviceProtocolProperty> getDeviceProperties() {
        return new ArrayList(deviceProperties);
    }

    private void notifyUpdate(DeviceProtocolProperty property) {
        DevicePropertyUpdateEventEnum
                .stream()
                .filter(enumValue -> enumValue.getName().equals(property.getName()))
                .findFirst()
                .ifPresent(enumValue -> {
                    eventService.postEvent(enumValue.getTopic(),
                            new DeviceProtocolPropertyEventSource(this.getmRID()));
                });
    }

    private PropertySpec getPropertySpecForProperty(String name) {
        DeviceProtocol deviceProtocol = this.getDeviceProtocolPluggableClass()
                .map(DeviceProtocolPluggableClass::getDeviceProtocol)
                .orElseThrow(() -> new UnsupportedOperationException("No DeviceProtocolPluggableClass to communicate with device type" + getDeviceType().getName()));
        return deviceProtocol
                .getPropertySpecs()
                .stream()
                .filter(spec -> spec.getName().equals(name))
                .findFirst()
                .orElseThrow(() ->
                        DeviceProtocolPropertyException.propertyDoesNotExistForDeviceProtocol(name, deviceProtocol,
                                this, thesaurus, MessageSeeds.DEVICE_PROPERTY_NOT_ON_DEVICE_PROTOCOL));

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

    private Supplier<RuntimeException> mdcAMRSystemDoesNotExist() {
        return () -> new RuntimeException("The MDC AMR system does not exist");
    }

    private Supplier<NoMeterActivationAt> noMeterActivationAt(Instant timestamp) {
        return () -> new NoMeterActivationAt(timestamp, thesaurus, MessageSeeds.NO_METER_ACTIVATION_AT);
    }

    private Meter createKoreMeter(AmrSystem amrSystem) {
        FiniteStateMachine stateMachine = this.getDeviceType().getDeviceLifeCycle().getFiniteStateMachine();
        Instant maximumPastEffectiveTimestamp = this.getDeviceType().getDeviceLifeCycle().getMaximumPastEffectiveTimestamp().atZone(this.clock.getZone())
                .truncatedTo(ChronoUnit.DAYS).toInstant();
        Instant maximumFutureEffectiveTimestamp = this.getDeviceType().getDeviceLifeCycle().getMaximumFutureEffectiveTimestamp();
        if (koreHelper.getInitialMeterActivationStartDate().get().isAfter(maximumFutureEffectiveTimestamp)) {
            throw new NoLifeCycleActiveAt(thesaurus, MessageSeeds.INVALID_SHIPMENT_DATE, koreHelper.getInitialMeterActivationStartDate()
                    .get(), maximumPastEffectiveTimestamp, maximumFutureEffectiveTimestamp);
        }
        return amrSystem.newMeter(String.valueOf(getId()), getName())
                .setMRID(getmRID())
                .setStateMachine(stateMachine)
                .setSerialNumber(getSerialNumber())
                .setReceivedDate(koreHelper.getInitialMeterActivationStartDate().get()) // date should be present
                .setManufacturer(getManufacturer())
                .setModelNumber(getModelNumber())
                .setModelVersion(getModelVersion())
                .create();
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

    List<? extends ReadingRecord> getHistoryReadingsFor(Register<?, ?> register, Range<Instant> interval) {
        return this.getListMeterAspect(meter -> this.getHistoryReadingsFor(register, interval, meter));
    }

    private List<ReadingRecord> getReadingsFor(Register<?, ?> register, Range<Instant> interval, Meter meter) {
        List<? extends BaseReadingRecord> readings = meter.getReadings(interval, register.getRegisterSpec().getRegisterType().getReadingType());
        return readings
                .stream()
                .map(ReadingRecord.class::cast)
                .collect(Collectors.toList());
    }

    private List<? extends ReadingRecord> getHistoryReadingsFor(Register<?, ?> register, Range<Instant> interval, Meter meter) {
        return meter.getJournaledReadings(interval, register.getRegisterSpec().getRegisterType().getReadingType())
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
            meterHasData = this.addChannelDataToMap(interval, meter.get(), channel, sortedLoadProfileReadingMap);
            if (meterHasData) {
                loadProfileReadings = new ArrayList<>(sortedLoadProfileReadingMap.values());
            }
        }
        return Lists.reverse(loadProfileReadings);
    }

    List<LoadProfileJournalReading> getChannelWithHistoryData(Channel channel, Range<Instant> interval, boolean changedDataOnly) {
        List<LoadProfileJournalReading> loadProfileReadings = new ArrayList<>();
        boolean meterHasData;
        if (this.meter.isPresent()) {
            Map<Instant, LoadProfileReadingImpl> sortedLoadProfileReadingMap = getPreFilledLoadProfileReadingMap(
                    channel.getLoadProfile(),
                    interval,
                    meter.get());
            Range<Instant> clipped = Ranges.openClosed(interval.lowerEndpoint(), lastReadingClipped(channel.getLoadProfile(), interval));
            Map<Instant, List<LoadProfileJournalReadingImpl>> sortedHistoryLoadProfileReadingMap = sortedLoadProfileReadingMap.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    instantLoadProfileReadingEntry -> {
                        List<LoadProfileJournalReadingImpl> listOfReadings = new ArrayList<LoadProfileJournalReadingImpl>();
                        LoadProfileJournalReadingImpl loadProfileJournalReadingImpl = new LoadProfileJournalReadingImpl();
                        loadProfileJournalReadingImpl.setRange(instantLoadProfileReadingEntry.getValue().getRange());
                        loadProfileJournalReadingImpl.setReadingTime(instantLoadProfileReadingEntry.getValue().getReadingTime());
                        instantLoadProfileReadingEntry.getValue().getChannelValues().entrySet().stream()
                                .forEach(channelIntervalReadingRecordEntry ->
                                        loadProfileJournalReadingImpl.setChannelData(channelIntervalReadingRecordEntry.getKey(), channelIntervalReadingRecordEntry.getValue()));
                        instantLoadProfileReadingEntry.getValue().getChannelValidationStates().entrySet().stream()
                                .forEach(channelDataValidationStatusEntry ->
                                        loadProfileJournalReadingImpl.setDataValidationStatus(channelDataValidationStatusEntry.getKey(), channelDataValidationStatusEntry.getValue()));
                        loadProfileJournalReadingImpl.setActive(false);
                        listOfReadings.add(loadProfileJournalReadingImpl);
                        return listOfReadings;
                    }
            ));
            meterHasData = this.addChannelWithHistoryDataToMap(clipped, meter.get(), channel, sortedHistoryLoadProfileReadingMap);
            if (meterHasData) {
                if (changedDataOnly) {
                    sortedHistoryLoadProfileReadingMap = sortedHistoryLoadProfileReadingMap.entrySet()
                            .stream()
                            .filter(r -> r.getValue().size() > 1)
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                }
                sortedHistoryLoadProfileReadingMap = sortedHistoryLoadProfileReadingMap.entrySet()
                        .stream()
                        .filter(r -> r.getValue().get(0).getReadingTime() != null)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                sortedHistoryLoadProfileReadingMap.forEach(
                        (instant, loadProfileReadings1) -> {
                            loadProfileReadings.addAll(loadProfileReadings1);
                        }
                );
            }
        }
        //return loadProfileReadings;
        return loadProfileReadings.stream().sorted(Comparator.comparing(LoadProfileJournalReading::getRange, (o11, o21) -> o21.lowerEndpoint().compareTo(o11.lowerEndpoint()))
                .thenComparing(LoadProfileJournalReading::getReadingTime, (o1, o2) -> o2.compareTo(o1)))
                .collect(Collectors.toList());
    }

    /**
     * Adds meter readings for a single channel to the timeslot-map.
     *
     * @param interval                    The interval over which meter readings are requested
     * @param meter                       The meter for which readings are requested
     * @param mdcChannel                  The meter's channel for which readings are requested
     * @param sortedLoadProfileReadingMap The map to add the readings to in the correct timeslot
     * @return true if any readings were added to the map, false otherwise
     */
    private boolean addChannelDataToMap(Range<Instant> interval, Meter meter, Channel mdcChannel, Map<Instant, LoadProfileReadingImpl> sortedLoadProfileReadingMap) {
        boolean meterHasData = false;
        List<MeterActivation> meterActivations = this.getSortedMeterActivations(meter, Ranges.closed(interval.lowerEndpoint(), interval.upperEndpoint()));
        for (MeterActivation meterActivation : meterActivations) {
            Range<Instant> meterActivationInterval = meterActivation.getInterval().toOpenClosedRange().intersection(interval);
            meterHasData |= meterActivationInterval.lowerEndpoint() != meterActivationInterval.upperEndpoint();
            ReadingType readingType = mdcChannel.getReadingType();
            List<IntervalReadingRecord> meterReadings = (List<IntervalReadingRecord>) meter.getReadings(meterActivationInterval, readingType);
            // To avoid to have to collect the readingqualities meter reading by meter reading (meterreading.getReadingQualities()
            // does a lazy load (database access) we collect all readingqualities here;
            List<? extends ReadingQualityRecord> readingQualities = meter.getReadingQualities(meterActivationInterval);
            for (IntervalReadingRecord meterReading : meterReadings) {
                List<ReadingType> channelReadingTypes = getChannelReadingTypes(mdcChannel, meterReading.getTimeStamp());
                LoadProfileReadingImpl loadProfileReading = sortedLoadProfileReadingMap.get(meterReading.getTimeStamp());
                if (loadProfileReading != null) {
                    loadProfileReading.setChannelData(mdcChannel, meterReading);
                    //Previously collected readingqualities are filtered and added to the loadProfile Reading
                    loadProfileReading.setReadingQualities(mdcChannel, readingQualities.stream().filter(rq -> rq.getReadingTimestamp().equals(meterReading.getTimeStamp()))
                            .filter(rq -> channelReadingTypes.contains(rq.getReadingType())).collect(Collectors.toList()));
                    loadProfileReading.setReadingTime(meterReading.getReportedDateTime());
                }
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

    private <R> boolean addChannelWithHistoryDataToMap(Range<Instant> interval, Meter meter, Channel mdcChannel, Map<Instant, List<LoadProfileJournalReadingImpl>> sortedHistoryLoadProfileReadingMap) {
        boolean meterHasData = false;
        List<MeterActivation> meterActivations = this.getSortedMeterActivations(meter, Ranges.closed(interval.lowerEndpoint(), interval.upperEndpoint()));
        for (MeterActivation meterActivation : meterActivations) {
            Range<Instant> meterActivationInterval = meterActivation.getInterval().toOpenClosedRange().intersection(interval);
            meterHasData |= meterActivationInterval.lowerEndpoint() != meterActivationInterval.upperEndpoint();
            ReadingType readingType = mdcChannel.getReadingType();
            List<JournaledChannelReadingRecord> meterReadings = (List<JournaledChannelReadingRecord>) meter.getJournaledReadings(meterActivationInterval, readingType);
            // To avoid to have to collect the readingqualities meter reading by meter reading (meterreading.getReadingQualities()
            // does a lazy load (database access) we collect all readingqualities here;
            List<? extends ReadingQualityRecord> readingQualities = meter.getReadingQualities(meterActivationInterval);
            List<JournalEntry<? extends ReadingQualityRecord>> readingQualitiesJournal = new ArrayList<>();
            if (readingQualities.size() > 0) {
                readingQualitiesJournal = meter.getReadingQualitiesJournal(meterActivationInterval, Collections.EMPTY_LIST,
                        meterReadings.stream().map(JournaledChannelReadingRecord::getChannel).distinct().collect(Collectors.toList()));
            }
            for (JournaledChannelReadingRecord meterReading : meterReadings) {
                List<LoadProfileJournalReadingImpl> loadProfileReadingList = sortedHistoryLoadProfileReadingMap.get(meterReading.getTimeStamp());
                setLoadProfileHistoryReading(loadProfileReadingList, meterReading, mdcChannel);
            }
            sortedHistoryLoadProfileReadingMap = sortedHistoryLoadProfileReadingMap.entrySet()
                    .stream()
                    .filter(r -> r.getValue().get(0).getReadingTime() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            setJournalReadingQualities(sortedHistoryLoadProfileReadingMap, readingQualitiesJournal, mdcChannel, readingQualities);
            Optional<com.elster.jupiter.metering.Channel> koreChannel = this.getChannel(meterActivation.getChannelsContainer(), readingType);
            if (koreChannel.isPresent()) {
                setJournalReadingValidationStatusAndJournalTime(sortedHistoryLoadProfileReadingMap, mdcChannel, meterActivationInterval);
            }
        }
        return meterHasData;
    }

    private void setLoadProfileHistoryReading(List<LoadProfileJournalReadingImpl> loadProfileReadingList, JournaledChannelReadingRecord meterReading, Channel mdcChannel) {
        LoadProfileJournalReadingImpl loadProfileHistoryReading;
        if (loadProfileReadingList.size() == 1 && loadProfileReadingList.get(0).getReadingTime() == null) {
            loadProfileHistoryReading = loadProfileReadingList.get(0);
        } else {
            loadProfileHistoryReading = new LoadProfileJournalReadingImpl();
            loadProfileReadingList.add(loadProfileHistoryReading);
        }
        loadProfileHistoryReading.setRange(loadProfileReadingList.get(0).getRange());
        loadProfileHistoryReading.setUserName(meterReading.getUserName());
        loadProfileHistoryReading.setJournalTime(meterReading.getJournalTime());
        loadProfileHistoryReading.setChannelData(mdcChannel, meterReading);
        loadProfileHistoryReading.setReadingTime(meterReading.getReportedDateTime());
        loadProfileHistoryReading.setReadingQualities(mdcChannel, new ArrayList<>());
    }

    private void setJournalReadingQualities(Map<Instant, List<LoadProfileJournalReadingImpl>> sortedHistoryLoadProfileReadingMap, List<JournalEntry<? extends ReadingQualityRecord>> readingQualitiesJournal, Channel mdcChannel, List<? extends ReadingQualityRecord> readingQualities) {
        final List<JournalEntry<? extends ReadingQualityRecord>> finalReadingQualitiesJournal = readingQualitiesJournal;
        sortedHistoryLoadProfileReadingMap.entrySet()
                .forEach(instantListEntry -> {
                    List<ReadingType> channelReadingTypes = getChannelReadingTypes(mdcChannel, instantListEntry.getKey());
                    List<? extends ReadingQualityRecord> readingQualityList = readingQualities.stream()
                            .filter(o -> instantListEntry.getKey().equals(o.getReadingTimestamp()))
                            .filter(o -> channelReadingTypes.contains(o.getReadingType()))
                            .collect(Collectors.toList());
                    List<? extends ReadingQualityRecord> readingQualityJournalList = finalReadingQualitiesJournal.stream()
                            .filter(o -> instantListEntry.getKey().equals(o.get().getReadingTimestamp()))
                            .filter(o -> channelReadingTypes.contains(o.get().getReadingType()))
                            .map(JournalEntry::get).collect(Collectors.toList());
                    List<ReadingQualityRecord> allReadingQuality = new ArrayList<>(readingQualityList);
                    allReadingQuality.addAll(readingQualityJournalList);
                    allReadingQuality.forEach(rqj -> {
                        Optional<LoadProfileJournalReadingImpl> journalReadingOptional;
                        if ((rqj.getTypeCode().compareTo("2.5.258") == 0) || (rqj.getTypeCode().compareTo("2.5.259") == 0)) {
                            journalReadingOptional = instantListEntry.getValue()
                                    .stream()
                                    .sorted((a, b) -> b.getReadingTime().compareTo(a.getReadingTime()))
                                    .filter(x -> x.getReadingTime().compareTo(rqj.getTimestamp()) <= 0)
                                    .findFirst();
                        } else {
                            journalReadingOptional = instantListEntry.getValue()
                                    .stream()
                                    .sorted(Comparator.comparing(LoadProfileReadingImpl::getReadingTime))
                                    .filter(x -> x.getReadingTime().compareTo(rqj.getTimestamp()) >= 0)
                                    .findFirst();
                        }
                        journalReadingOptional.ifPresent(journalReading -> {
                            Map<Channel, List<? extends ReadingQualityRecord>> readingQualitiesList = journalReading.getReadingQualities();
                            List<ReadingQualityRecord> original = new ArrayList<>(readingQualitiesList.get(mdcChannel));
                            original.add(rqj);
                            journalReading.setReadingQualities(mdcChannel, original);
                        });

                    });
                });
    }

    private void setJournalReadingValidationStatusAndJournalTime(Map<Instant, List<LoadProfileJournalReadingImpl>> sortedHistoryLoadProfileReadingMap, Channel mdcChannel, Range<Instant> meterActivationInterval) {
        sortedHistoryLoadProfileReadingMap.forEach((instant, journalReadingList) -> journalReadingList.forEach(journalReading -> {
            List<ReadingQualityRecord> readingQualitiesList = journalReading.getReadingQualities()
                    .entrySet().stream()
                    .map(Map.Entry::getValue)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            DataValidationStatus validationStatus = forValidation().getValidationStatus(mdcChannel, instant, readingQualitiesList, meterActivationInterval);
            journalReading.setDataValidationStatus(mdcChannel, validationStatus);
            //code below is the processing of removed readings
            validationStatus.getReadingQualities()
                    .stream()
                    .filter(rq -> rq.getType().qualityIndex().orElse(null) == QualityCodeIndex.REJECTED)
                    .findAny()
                    .map(ReadingQualityRecord.class::cast)
                    .ifPresent(readingQuality -> journalReading.setReadingTime(readingQuality.getTimestamp()));
        }));
    }

    private List<ReadingType> getChannelReadingTypes(Channel channel, Instant instant) {
        ArrayList<ReadingType> readingTypes = new ArrayList<>();
        readingTypes.add(channel.getReadingType());
        channel.getCalculatedReadingType(instant).ifPresent(readingTypes::add);
        return readingTypes;
    }

    /**
     * Creates a map of LoadProfileReadings (k,v -> timestamp of end of interval, placeholder for readings) (without a reading value),
     * just a list of placeholders for each reading interval within the requestedInterval for all timestamps
     * that occur with the bounds of a meter activation and load profile's last reading.
     *
     * @param loadProfile       The LoadProfile
     * @param requestedInterval interval over which user wants to see readings
     * @param meter             The Meter
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
                    loadProfile.getChannels().stream()
                            .map(Channel::getOffset)
                            .collect(Collectors.toSet())
                            .forEach(offset -> {
                                ZonedDateTime requestStart = this.prefilledIntervalStart(loadProfile, affectedChannelContainer.getZoneId(), requestedIntervalClippedToMeterActivation, offset);
                                ZonedDateTime requestEnd =
                                        ZonedDateTime.ofInstant(
                                                requestedInterval.upperEndpoint(),
                                                affectedChannelContainer.getZoneId());
                                if (!requestEnd.isBefore(requestStart)) {
                                    Range<Instant> channelContainerInterval = Range.closedOpen(requestStart.toInstant(), requestEnd.toInstant());
                                    while (channelContainerInterval.contains(requestStart.toInstant())) {
                                        ZonedDateTime readingTimestamp = requestStart.plus(intervalLength);
                                        // we should handle dst difference for all hours load profiles except 1-hour
                                        TimeDuration interval = loadProfile.getInterval();
                                        if (interval.getTimeUnit() == TimeDuration.TimeUnit.HOURS && interval.getCount() > 1) {
                                            readingTimestamp = readingTimestamp.plusSeconds(requestStart.getOffset().getTotalSeconds() - readingTimestamp.getOffset().getTotalSeconds());
                                        }

                                        if (requestedInterval.contains(readingTimestamp.toInstant())) {
                                            LoadProfileReadingImpl value = new LoadProfileReadingImpl();
                                            value.setRange(Ranges.openClosed(requestStart.toInstant(), readingTimestamp.toInstant()));
                                            loadProfileReadingMap.put(readingTimestamp.toInstant(), value);
                                        }
                                        requestStart = readingTimestamp;
                                    }
                                }
                            });
                });
        return loadProfileReadingMap;
    }

    private ZonedDateTime prefilledIntervalStart(LoadProfile loadProfile, ZoneId zoneId, Range<Instant> requestedIntervalClippedToMeterActivation, long offset) {
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
                        .truncatedTo(this.truncationUnit(loadProfile))
                        .plusHours(offset / 3600);    // round start time to interval boundary
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
        ZonedDateTime zonedLowerBoundary = ZonedDateTime.ofInstant(
                requestedIntervalClippedToMeterActivation.lowerEndpoint(),
                zoneId);
        ZonedDateTime nextAttempt = zonedLowerBoundary.truncatedTo(this.truncationUnit(loadProfile));    // round start time to interval boundary
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

    public void refreshMeter() {
        if (meter.isPresent()) {
            meter.set(meteringService.findMeterById(meter.get().getId()).get());
        }
    }

    private DateTimeFormatter getLongDateFormatForCurrentUser() {
        return DateTimeFormatGenerator.getDateFormatForUser(
                DateTimeFormatGenerator.Mode.LONG,
                DateTimeFormatGenerator.Mode.LONG,
                this.userPreferencesService,
                this.threadPrincipalService.getPrincipal());
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
                    .orElseGet(() -> meterActivation.get().getChannelsContainer().createChannel(getZone(), readingType));
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
     * @param meter    The Meter
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

    private DataMapper<DeviceImpl> getDataMapper() {
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
        Save.CREATE.validate(DeviceImpl.this.dataModel, comTaskExecution, Save.Create.class);
        Save.UPDATE.validate(DeviceImpl.this.dataModel, this, Save.Update.class);
        this.comTaskExecutions.add(comTaskExecution);
        if (this.id != 0) {
            comTaskExecution.notifyCreated();
            dataModel.touch(DeviceImpl.this);
        }
        return comTaskExecution;
    }

    private List<OpenIssue> getOpenIssuesForMeter(Meter meter) {
        return this.issueService.query(OpenIssue.class).select(where("device").isEqualTo(meter));
    }

    private List<HistoricalIssue> getAllHistoricalIssuesForMeter(Meter meter) {
        return this.issueService.query(HistoricalIssue.class).select(where("device").isEqualTo(meter));
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

    void deleteReferenceOnCalendars() {
        clearPassiveCalendar();
        clearPlannedPassiveCalendar();
        dataModel.update(this, "passiveCalendar", "plannedPassiveCalendar");
    }

    private ComTaskExecution createAdHocComTaskExecutionToRunNow(ComTaskEnablement enablement) {
        ComTaskExecutionBuilder comTaskExecutionBuilder = newAdHocComTaskExecution(enablement);
        if (enablement.hasPartialConnectionTask()) {
            getConnectionTasks()
                    .stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == enablement.getPartialConnectionTask().get().getId())
                    .map(connectionTask -> connectionTaskService.findAndLockConnectionTaskById(connectionTask.getId()))
                    .map(Optional::get)
                    .forEach(comTaskExecutionBuilder::connectionTask);
        }
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        save();
        return comTaskExecution;
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

    private List<com.energyict.mdc.common.device.config.DeviceLifeCycleChangeEvent> getDeviceTypeLifeCycleChangeEvents() {
        return this.getDeviceType()
                .getDeviceLifeCycleChangeEvents()
                .stream()
                .filter(each -> each.getTimestamp().isAfter(this.createTime))
                .collect(Collectors.toList());
    }

    private DeviceLifeCycleChangeEvent newEventForMostRecent(Deque<StateTimeSlice> stateTimeSlices, Deque<com.energyict.mdc.common.device.config.DeviceLifeCycleChangeEvent> deviceTypeChangeEvents) {
        if (stateTimeSlices.isEmpty()) {
            return DeviceLifeCycleChangeEventImpl.from(deviceTypeChangeEvents.removeFirst());
        } else if (deviceTypeChangeEvents.isEmpty()) {
            return DeviceLifeCycleChangeEventImpl.from(stateTimeSlices.removeFirst());
        } else {
            // Compare both timestamps and create event from the most recent one
            StateTimeSlice stateTimeSlice = stateTimeSlices.peekFirst();
            com.energyict.mdc.common.device.config.DeviceLifeCycleChangeEvent deviceLifeCycleChangeEvent = deviceTypeChangeEvents
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

    void addReadingTypeObisCodeUsage(ReadingType readingType, ObisCode obisCode) {
        ReadingTypeObisCodeUsageImpl readingTypeObisCodeUsage = dataModel.getInstance(ReadingTypeObisCodeUsageImpl.class);
        readingTypeObisCodeUsage.initialize(this, readingType, obisCode);
        readingTypeObisCodeUsages.add(readingTypeObisCodeUsage);
    }

    void removeReadingTypeObisCodeUsage(ReadingType readingType) {
        for (Iterator<ReadingTypeObisCodeUsageImpl> iterator = readingTypeObisCodeUsages.iterator(); iterator.hasNext(); ) {
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

    private List<ReadingType> getDeviceCapabilities(DeviceConfiguration config) {
        return deviceConfigurationService.getReadingTypesRelatedToConfiguration(config);
    }

    private List<SecurityAccessor> getSecurityAccessorsManagedCentrally() {
        List<SecurityAccessorType> securityAccessorTypes = getDeviceType().getSecurityAccessorTypes();
        if (securityManagementService != null) {
            return securityManagementService.getDefaultValues(securityAccessorTypes.toArray(new SecurityAccessorType[securityAccessorTypes.size()])).stream()
                    .map(sa -> CentrallyManagedDeviceSecurityAccessor.of(thesaurus, this, sa))
                    .collect(toList());
        }
        return new ArrayList<>();
    }

    private void validateManageable(SecurityAccessorType securityAccessorType) {
        if (securityAccessorType.isManagedCentrally()) {
            throw new UnmanageableSecurityAccessorException(thesaurus, securityAccessorType);
        }
    }

    private void sendMessageCreated() {
        // send the InitialStateActions message in order to execute on entry actions for default life cycle states
        // called only when the device is created
        messageService
                .getDestinationSpec("InitialStateActions")
                .ifPresent(destinationSpec -> {
                    Map<String, String> message = new HashMap<String, String>() {{
                        put("stateId", String.valueOf(getState().getId()));
                        put("sourceId", String.valueOf(getId()));
                        put("sourceType", DEVICE);
                    }};
                    destinationSpec.message(jsonService.serialize(message)).send();
                });
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

    @Target({ElementType.TYPE, ElementType.FIELD})
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

    static class LogBookUpdaterForDevice extends LogBookImpl.LogBookUpdater {
        LogBookUpdaterForDevice(LogBookImpl logBook) {
            super(logBook);
        }
    }

    static class LoadProfileUpdaterForDevice extends LoadProfileImpl.LoadProfileUpdater {

        LoadProfileUpdaterForDevice(LoadProfileImpl loadProfile) {
            super(loadProfile);
        }

        @Override
        public void update() {
            super.update();
        }
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
        public DeviceMessage add() {
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
            this.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
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
        public ScheduledConnectionTaskBuilder setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections) {
            this.getScheduledConnectionTask().setNumberOfSimultaneousConnections(numberOfSimultaneousConnections);
            return this;
        }

        @Override
        public ScheduledConnectionTask add() {
            return (ScheduledConnectionTaskImpl) DeviceImpl.this.add(this.getScheduledConnectionTask());
        }
    }

    private class ScheduledComTaskExecutionBuilderForDevice
            extends ComTaskExecutionImpl.AbstractComTaskExecutionBuilder {
        private final List<ComTaskExecutionImpl.SingleScheduledComTaskExecutionBuilder> comTaskExecutionsBuilders = new ArrayList<>();
        private final List<ComTaskExecutionUpdater> comTaskExecutionsUpdaters;

        private ScheduledComTaskExecutionBuilderForDevice(Provider<ComTaskExecutionImpl> comTaskExecutionProvider, ComSchedule comSchedule) {
            super(comTaskExecutionProvider.get());
            Set<ComTaskExecution> toUpdate = new TreeSet<>(Comparator.comparingLong(ComTaskExecution::getId));
            DeviceImpl.this.getDeviceConfiguration()
                    .getComTaskEnablements()
                    .stream()
                    .filter(comTaskEnablement -> comSchedule.getComTasks().contains(comTaskEnablement.getComTask()))
                    .forEach(comTaskEnablement -> {
                        Optional<ComTaskExecutionImpl> existingComTaskExecution = DeviceImpl.this.getComTaskExecutionImpls()
                                .filter(comTaskExecution -> comTaskExecution.getComTask().getId() == comTaskEnablement.getComTask().getId())
                                .findAny();
                        if (existingComTaskExecution.isPresent()) { //update
                            if (existingComTaskExecution.get().usesSharedSchedule() && existingComTaskExecution.get().getComSchedule().get().getId() != comSchedule.getId()) {
                                throw new CannotSetMultipleComSchedulesWithSameComTask(comSchedule, DeviceImpl.this, thesaurus);
                            }
                            toUpdate.add(existingComTaskExecution.get());
                        } else { // create
                            ComTaskExecutionImpl scheduledComTaskExecution = comTaskExecutionProvider.get();
                            scheduledComTaskExecution.initializeForScheduledComTask(DeviceImpl.this, comTaskEnablement, comSchedule);
                            comTaskExecutionsBuilders.add(new ComTaskExecutionImpl.SingleScheduledComTaskExecutionBuilder(scheduledComTaskExecution));
                        }
                    });
            comTaskExecutionsUpdaters = toUpdate.stream()
                    .map(this::lock)
                    .flatMap(Functions.asStream())
                    .map(ComTaskExecution::getUpdater)
                    .map(updater -> updater.addSchedule(comSchedule))
                    .collect(toList());
        }

        @Override
        public ComTaskExecution getComTaskExecution() {
            if (this.comTaskExecutionsBuilders.size() > 0) {
                return this.comTaskExecutionsBuilders.get(0).getComTaskExecution();
            } else {
                return this.comTaskExecutionsUpdaters.get(0).getComTaskExecution();
            }
        }

        @Override
        public ScheduledComTaskExecutionBuilderForDevice useDefaultConnectionTask(boolean useDefaultConnectionTask) {
            comTaskExecutionsBuilders
                    .forEach(builder -> builder.useDefaultConnectionTask(useDefaultConnectionTask));
            comTaskExecutionsUpdaters
                    .forEach(updater -> updater.useDefaultConnectionTask(useDefaultConnectionTask));
            return this;
        }

        @Override
        public ScheduledComTaskExecutionBuilderForDevice setConnectionFunction(ConnectionFunction connectionFunction) {
            comTaskExecutionsBuilders
                    .forEach(builder -> builder.setConnectionFunction(connectionFunction));
            comTaskExecutionsUpdaters
                    .forEach(updater -> updater.setConnectionFunction(connectionFunction));
            return this;
        }

        @Override
        public ScheduledComTaskExecutionBuilderForDevice connectionTask(ConnectionTask<?, ?> connectionTask) {
            comTaskExecutionsBuilders
                    .forEach(builder -> builder.connectionTask(connectionTask));
            comTaskExecutionsUpdaters
                    .forEach(updater -> updater.connectionTask(connectionTask));
            return this;
        }

        @Override
        public ScheduledComTaskExecutionBuilderForDevice priority(int priority) {
            comTaskExecutionsBuilders
                    .forEach(builder -> builder.priority(priority));
            comTaskExecutionsUpdaters
                    .forEach(updater -> updater.priority(priority));
            return this;
        }

        @Override
        public ScheduledComTaskExecutionBuilderForDevice ignoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound) {
            comTaskExecutionsBuilders
                    .forEach(builder -> builder.ignoreNextExecutionSpecForInbound(ignoreNextExecutionSpecsForInbound));
            comTaskExecutionsUpdaters
                    .forEach(updater -> updater.ignoreNextExecutionSpecForInbound(ignoreNextExecutionSpecsForInbound));
            return this;
        }

        @Override
        public ScheduledComTaskExecutionBuilderForDevice scheduleNow() {
            comTaskExecutionsBuilders
                    .forEach(ComTaskExecutionImpl.AbstractComTaskExecutionBuilder::scheduleNow);
            comTaskExecutionsUpdaters.stream()
                    .map(ComTaskExecutionUpdater::getComTaskExecution)
                    .forEach(ComTaskExecution::scheduleNow);
            return this;
        }

        @Override
        public ScheduledComTaskExecutionBuilderForDevice runNow() {
            comTaskExecutionsBuilders
                    .forEach(ComTaskExecutionImpl.AbstractComTaskExecutionBuilder::runNow);
            comTaskExecutionsUpdaters.stream()
                    .map(ComTaskExecutionUpdater::getComTaskExecution)
                    .forEach(ComTaskExecution::runNow);
            return this;
        }

        @Override
        public void putOnHold() {
            comTaskExecutionsBuilders
                    .forEach(ComTaskExecutionImpl.AbstractComTaskExecutionBuilder::putOnHold);
            comTaskExecutionsUpdaters.stream()
                    .map(ComTaskExecutionUpdater::getComTaskExecution)
                    .forEach(ComTaskExecution::putOnHold);
        }

        @Override
        public void resume() {
            comTaskExecutionsBuilders
                    .forEach(ComTaskExecutionImpl.AbstractComTaskExecutionBuilder::resume);
            comTaskExecutionsUpdaters.stream()
                    .map(ComTaskExecutionUpdater::getComTaskExecution)
                    .forEach(ComTaskExecution::resume);
        }

        @Override
        public ComTaskExecution add() {
            comTaskExecutionsBuilders
                    .forEach(builder -> {
                        ComTaskExecution execution = builder.add();
                        DeviceImpl.this.add((ComTaskExecutionImpl) execution);
                    });
            LOGGER.info("CXO-11731: Update comtask execution from DeviceImpl.");
            comTaskExecutionsUpdaters
                    .forEach(ComTaskExecutionUpdater::update);
            return getComTaskExecution();
        }

        private Optional<ComTaskExecution> lock(ComTaskExecution comTaskExecution) {
            long comTaskId = comTaskExecution.getId();
            connectionTaskService.findAndLockConnectionTaskById(comTaskExecution.getConnectionTaskId());
            return communicationTaskService.findAndLockComTaskExecutionById(comTaskId);
        }
    }

    private class AdHocComTaskExecutionBuilderForDevice
            extends ComTaskExecutionImpl.AbstractComTaskExecutionBuilder {

        private AdHocComTaskExecutionBuilderForDevice(Provider<ComTaskExecutionImpl> comTaskExecutionProvider, ComTaskEnablement comTaskEnablement) {
            super(comTaskExecutionProvider.get());
            ((ComTaskExecutionImpl) this.getComTaskExecution()).initializeAdhoc(DeviceImpl.this, comTaskEnablement);
        }


        @Override
        public ComTaskExecution add() {
            ComTaskExecution comTaskExecution = super.add();
            return DeviceImpl.this.add((ComTaskExecutionImpl) comTaskExecution);
        }
    }

    private class FirmwareComTaskExecutionBuilderForDevice extends ComTaskExecutionImpl.AbstractComTaskExecutionBuilder {

        private FirmwareComTaskExecutionBuilderForDevice(Provider<ComTaskExecutionImpl> comTaskExecutionProvider, ComTaskEnablement comTaskEnablement) {
            super(comTaskExecutionProvider.get());
            ((ComTaskExecutionImpl) this.getComTaskExecution()).initializeFirmwareTask(DeviceImpl.this, comTaskEnablement);
        }

        @Override
        public ComTaskExecution add() {
            ComTaskExecution firmwareComTaskExecution = super.add();
            return DeviceImpl.this.add((ComTaskExecutionImpl) firmwareComTaskExecution);
        }
    }

    private class ManuallyScheduledComTaskExecutionBuilderForDevice
            extends ComTaskExecutionImpl.AbstractComTaskExecutionBuilder {

        private ManuallyScheduledComTaskExecutionBuilderForDevice(Provider<ComTaskExecutionImpl> comTaskExecutionProvider, ComTaskEnablement comTaskEnablement, TemporalExpression temporalExpression) {
            super(comTaskExecutionProvider.get());
            ((ComTaskExecutionImpl) this.getComTaskExecution()).initializeManualScheduled(DeviceImpl.this, comTaskEnablement, temporalExpression);
        }

        @Override
        public ComTaskExecution add() {
            ComTaskExecution comTaskExecution = super.add();
            return DeviceImpl.this.add((ComTaskExecutionImpl) comTaskExecution);
        }
    }

    private static class CIMLifecycleDatesImpl implements CIMLifecycleDates {
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

    private static class NoCimLifecycleDates implements CIMLifecycleDates {
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

    private static class CanUpdateMeterActivationLast implements Comparator<SyncDeviceWithKoreMeter> {
        @Override
        public int compare(SyncDeviceWithKoreMeter o1, SyncDeviceWithKoreMeter o2) {
            int a = o1.canUpdateCurrentMeterActivation() ? 1 : 0;
            int b = o2.canUpdateCurrentMeterActivation() ? 1 : 0;
            return Integer.compare(a, b);
        }
    }
}
