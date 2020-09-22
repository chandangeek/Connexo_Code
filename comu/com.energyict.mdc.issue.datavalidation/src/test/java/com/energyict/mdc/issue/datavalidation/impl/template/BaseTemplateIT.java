package com.energyict.mdc.issue.datavalidation.impl.template;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.impl.AppServiceConsoleService;
import com.elster.jupiter.appserver.impl.MessageHandlerLauncherService;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.h2.impl.TransientMessage;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Counter;
import com.elster.jupiter.util.Counters;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.cron.impl.DefaultCronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validators.impl.DefaultValidatorFactory;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.masterdata.RegisterType;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceConfigValidationRuleSetResolver;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.impl.BareMinimumDeviceProtocol;
import com.energyict.mdc.issue.datavalidation.impl.InMemoryIntegrationPersistence;
import com.energyict.mdc.issue.datavalidation.impl.event.DataValidationEventDescription;
import com.energyict.mdc.issue.datavalidation.impl.event.DataValidationEventHandlerFactory;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.energyict.obis.ObisCode;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseTemplateIT {
    private static final String APP_SERVER_NAME = "IssueIT";
    private static final Semaphore SEMAPHORE = new Semaphore(0);
    private static final Set<String> ISSUE_EVENT_CORRELATION_IDS = Arrays.stream(DataValidationEventDescription.values())
            .map(DataValidationEventDescription::getTopic)
            .collect(Collectors.toSet());
    protected static final InMemoryIntegrationPersistence IN_MEMORY_PERSISTENCE = new InMemoryIntegrationPersistence();
    protected static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");
    protected static final Instant TIME = LocalDateTime.of(2015, 6, 16, 0, 0).toInstant(ZoneOffset.UTC);

    protected static IssueService issueService;
    protected static IssueCreationService issueCreationService;
    protected static IssueDataValidationService issueDataValidationService;
    protected static MeteringService meteringService;
    protected static ValidationService validationService;
    protected static MasterDataService masterDataService;
    protected static AppService appService;
    protected static SuspectCreatedIssueCreationRuleTemplate suspectCreatedIssueCreationRuleTemplate;
    protected static DataValidationIssueCreationRuleTemplate dataValidationIssueCreationRuleTemplate;

    @BeforeClass
    public static void setUpClass() throws SQLException {
        initializeClock();
        IN_MEMORY_PERSISTENCE.initializeDatabase(false);

        try (TransactionContext transactionContext = IN_MEMORY_PERSISTENCE.getTransactionService().getContext()) {
            IN_MEMORY_PERSISTENCE.getService(FiniteStateMachineService.class);
            IN_MEMORY_PERSISTENCE.getService(IssueDataValidationService.class);
            transactionContext.commit();
        }
        issueService = IN_MEMORY_PERSISTENCE.getService(IssueService.class);
        issueCreationService = issueService.getIssueCreationService();
        issueDataValidationService = IN_MEMORY_PERSISTENCE.getService(IssueDataValidationService.class);
        meteringService = IN_MEMORY_PERSISTENCE.getService(MeteringService.class);
        validationService = IN_MEMORY_PERSISTENCE.getService(ValidationService.class);
        masterDataService = IN_MEMORY_PERSISTENCE.getService(MasterDataService.class);
        appService = IN_MEMORY_PERSISTENCE.getService(AppService.class);
        suspectCreatedIssueCreationRuleTemplate = IN_MEMORY_PERSISTENCE.getService(SuspectCreatedIssueCreationRuleTemplate.class);
        dataValidationIssueCreationRuleTemplate = IN_MEMORY_PERSISTENCE.getService(DataValidationIssueCreationRuleTemplate.class);

        // Registering
        validationService.addValidatorFactory(IN_MEMORY_PERSISTENCE.getService(DefaultValidatorFactory.class));
        validationService.addValidationRuleSetResolver(IN_MEMORY_PERSISTENCE.getService(DeviceConfigValidationRuleSetResolver.class));
        issueService.addCreationRuleTemplate(suspectCreatedIssueCreationRuleTemplate);
        issueService.addCreationRuleTemplate(dataValidationIssueCreationRuleTemplate);
        setupAppServerInfrastructure();
    }

    @AfterClass
    public static void tearDownClass() throws SQLException {
        IN_MEMORY_PERSISTENCE.cleanUpDataBase();
    }

    private static void setupAppServerInfrastructure() {
        try (TransactionContext transactionContext = IN_MEMORY_PERSISTENCE.getTransactionService().getContext()) {
            String destinationSpecName = EventService.JUPITER_EVENTS;
            String subscriberName = DataValidationEventHandlerFactory.AQ_DATA_VALIDATION_EVENT_SUBSCRIBER;
            SubscriberSpec subscriberSpec = IN_MEMORY_PERSISTENCE
                    .getService(MessageService.class)
                    .getSubscriberSpec(destinationSpecName, subscriberName)
                    .get();
            when(IN_MEMORY_PERSISTENCE.getService(BundleContext.class).getProperty(AppService.SERVER_NAME_PROPERTY_NAME))
                    .thenReturn(APP_SERVER_NAME);
            MessageHandlerLauncherService launcherService = IN_MEMORY_PERSISTENCE.getService(MessageHandlerLauncherService.class);
            launcherService.activate();
            launcherService.addResource(
                    new SemaphoreDrivenMessageHandlerFactory(IN_MEMORY_PERSISTENCE.getService(DataValidationEventHandlerFactory.class),
                            SEMAPHORE,
                            message -> ISSUE_EVENT_CORRELATION_IDS.contains(((TransientMessage) message).getCorrelationId())),
                    ImmutableMap.of("destination", destinationSpecName, "subscriber", subscriberName));
            AppServer appServer = appService
                    .createAppServer(APP_SERVER_NAME, new DefaultCronExpressionParser().parse("0 0 * * * ? *").get());
            appServer.createSubscriberExecutionSpec(subscriberSpec, 1);
            appServer.activate();
            transactionContext.commit();
        }
        IN_MEMORY_PERSISTENCE.getService(AppServiceConsoleService.class).become(APP_SERVER_NAME);
    }

    private static void initializeClock() {
        when(IN_MEMORY_PERSISTENCE.getClock().getZone()).thenReturn(TIME_ZONE.toZoneId());
        when(IN_MEMORY_PERSISTENCE.getClock().instant()).thenReturn(TIME);
    }

    protected static void waitWhileIssueEventIsBeingProcessed() {
        try {
            SEMAPHORE.acquire();
        } catch (InterruptedException e) {
            throw new IllegalThreadStateException("Current test thread is interrupted while waiting for issue creation.");
        }
    }

    protected static <T> T getService(Class<T> serviceClass) {
        return IN_MEMORY_PERSISTENCE.getService(serviceClass);
    }

    protected Message mockCannotEstimateDataMessage(Instant start, Instant end, Channel channel, ReadingType readingType) {
        Message message = mock(Message.class);
        Map<String, Object> map = new HashMap<>();
        map.put("event.topics", "com/elster/jupiter/estimation/estimationblock/FAILURE");
        map.put("startTime", start.toEpochMilli());
        map.put("endTime", end.toEpochMilli());
        map.put("channelId", channel.getId());
        map.put("readingType", readingType.getMRID());
        String payload = IN_MEMORY_PERSISTENCE.getService(JsonService.class).serialize(map);
        when(message.getPayload()).thenReturn(payload.getBytes());
        return message;
    }

    protected Message mockSuspectDeletedMessage(Instant timeStamp, Channel channel, String readingQuality, ReadingType readingType) {
        Message message = mock(Message.class);
        Map<String, Object> map = new HashMap<>();
        map.put("event.topics", "com/elster/jupiter/metering/readingquality/DELETED");
        map.put("readingTimestamp", timeStamp.toEpochMilli());
        map.put("channelId", channel.getId());
        map.put("readingQualityTypeCode", readingQuality);
        map.put("readingType", readingType.getMRID());
        String payload = IN_MEMORY_PERSISTENCE.getService(JsonService.class).serialize(map);
        when(message.getPayload()).thenReturn(payload.getBytes());
        return message;
    }

    protected static DeviceType createDeviceType(ReadingType... readingTypes) {
        DeviceConfigurationService deviceConfigurationService = IN_MEMORY_PERSISTENCE.getService(DeviceConfigurationService.class);
        ProtocolPluggableService protocolPluggableService = IN_MEMORY_PERSISTENCE.getService(ProtocolPluggableService.class);
        protocolPluggableService.addDeviceProtocolService(new BareMinimumDeviceProtocolService());
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass("DVALIDATION", BareMinimumDeviceProtocol.class.getName());
        deviceProtocolPluggableClass.save();
        DeviceType deviceType = deviceConfigurationService.newDeviceType("DeviceType", deviceProtocolPluggableClass);
        Counter counter = Counters.newLenientCounter();
        Arrays.stream(readingTypes)
                .forEach(rt -> {
                    counter.increment();
                    RegisterType registerType = masterDataService.newRegisterType(rt, ObisCode.fromString("1.0.1.8.0." + counter.getValue()));
                    registerType.save();
                    deviceType.addRegisterType(registerType);
                });
        return deviceType;
    }

    protected static DeviceConfiguration createDeviceConfiguration(DeviceType deviceType, String name) {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(name);
        deviceType.getRegisterTypes().forEach(rt -> deviceConfigurationBuilder.newNumericalRegisterSpec(rt)
                .numberOfFractionDigits(3));
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();
        deviceConfiguration.save();
        return deviceConfiguration;
    }

    protected static Meter createMeter(DeviceConfiguration deviceConfiguration, String name, Instant creationTime) {
        DeviceService deviceService = IN_MEMORY_PERSISTENCE.getService(DeviceService.class);
        Device device = deviceService.newDevice(deviceConfiguration, name, name, creationTime.minusMillis(1));
        device.save();
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.ACTIVATED.findOrCreate(IN_MEMORY_PERSISTENCE.getFiniteStateMachineService());

        User user = mock(User.class);
        when(user.getName()).thenReturn("name");
        when(user.getLocale()).thenReturn(Optional.empty());
        IN_MEMORY_PERSISTENCE.getThreadPrincipalService().set(user);
        when(user.hasPrivilege(anyString(), Matchers.any(Privilege.class))).thenReturn(true);

        ExecutableAction installAndActivateAction = IN_MEMORY_PERSISTENCE.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();
        // Business method
        installAndActivateAction.execute(creationTime, Collections.emptyList());

        MeteringService meteringService = IN_MEMORY_PERSISTENCE.getService(MeteringService.class);
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        return amrSystem.findMeter(String.valueOf(device.getId())).get();
    }

    protected static ValidationRule createValidationRule(String name) {
        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(name, QualityCodeSystem.MDC);
        ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion(name, null);
        return validationRuleSetVersion.addRule(ValidationAction.FAIL, DefaultValidatorFactory.REGISTER_INCREASE_VALIDATOR, name)
                .active(true)
                .withReadingType(InMemoryIntegrationPersistence.READING_TYPE_MRID)
                .havingProperty("failEqualData")
                .withValue(false)
                .create();
    }

    protected static CreationRule createRuleForDeviceConfiguration(String name, String template, DeviceType deviceType, List<DeviceConfiguration> deviceConfigurations,
                                                                   List<ValidationRule> validationRules, RelativePeriod relativePeriod, Integer threshold) {
        IssueCreationService.CreationRuleBuilder ruleBuilder = issueCreationService.newCreationRule();
        Map<String, Object> props = new HashMap<>();
        List<HasIdAndName> value = new ArrayList<>();
        for (DeviceConfiguration config : deviceConfigurations) {
            HasIdAndName deviceConfig = mock(HasIdAndName.class);
            when(deviceConfig.getId()).thenReturn(config.getId());
            value.add(deviceConfig);
        }
        List<HasIdAndName> value2 = new ArrayList<>();
        HasIdAndName deviceLife = mock(HasIdAndName.class);
        String deviceLifeId = deviceType.getId() + ":" + deviceType.getDeviceLifeCycle().getId() + ":" + deviceType.getDeviceLifeCycle()
                .getFiniteStateMachine()
                .getStates()
                .stream()
                .sorted(Comparator.comparing(State::getId))
                .map(HasId::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        when(deviceLife.getId()).thenReturn(deviceLifeId);
        value2.add(deviceLife);
        props.put(SuspectCreatedIssueCreationRuleTemplate.NAME.equals(template) ?
                        SuspectCreatedIssueCreationRuleTemplate.DEVICE_CONFIGURATIONS :
                        DataValidationIssueCreationRuleTemplate.DEVICE_CONFIGURATIONS,
                value);
        props.put(DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES, value2);
        if (validationRules != null && !validationRules.isEmpty()) {
            props.put(SuspectCreatedIssueCreationRuleTemplate.VALIDATION_RULES, validationRules.stream()
                    .map(SuspectCreatedIssueCreationRuleTemplate.ValidationRuleInfo::new)
                    .collect(Collectors.toList()));
        }
        if (relativePeriod != null && threshold != null) {
            props.put(SuspectCreatedIssueCreationRuleTemplate.THRESHOLD,
                    new SuspectCreatedIssueCreationRuleTemplate.RelativePeriodWithCountInfo(threshold, relativePeriod));
        }
        return ruleBuilder.setTemplate(template)
                .setName(name)
                .setIssueType(issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get())
                .setReason(issueService.findReason(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON).get())
                .setPriority(Priority.DEFAULT)
                .activate()
                .setDueInTime(DueInType.YEAR, 5)
                .setProperties(props)
                .complete();
    }

    private static class BareMinimumDeviceProtocolService implements DeviceProtocolService {
        @Override
        public Object createProtocol(String className) {
            if (BareMinimumDeviceProtocol.class.getName().equals(className)) {
                return new BareMinimumDeviceProtocol();
            } else {
                return null;
            }
        }
    }

    private static class SemaphoreDrivenMessageHandler implements MessageHandler {
        private final MessageHandler messageHandler;
        private final Semaphore semaphore;
        private final Predicate<Message> messageFilter;

        private SemaphoreDrivenMessageHandler(MessageHandler messageHandler, Semaphore semaphore, Predicate<Message> messageFilter) {
            this.messageHandler = messageHandler;
            this.semaphore = semaphore;
            this.messageFilter = messageFilter;
        }

        @Override
        public void process(Message message) {
            if (messageFilter.test(message)) {
                messageHandler.process(message);
            }
        }

        @Override
        public void onMessageDelete(Message message) {
            if (messageFilter.test(message)) {
                messageHandler.onMessageDelete(message);
                semaphore.release();
            }
        }
    }

    private static class SemaphoreDrivenMessageHandlerFactory implements MessageHandlerFactory {
        private final MessageHandlerFactory messageHandlerFactory;
        private final Semaphore semaphore;
        private final Predicate<Message> messageFilter;

        private SemaphoreDrivenMessageHandlerFactory(MessageHandlerFactory messageHandlerFactory, Semaphore semaphore) {
            this(messageHandlerFactory, semaphore, any -> true);
        }

        private SemaphoreDrivenMessageHandlerFactory(MessageHandlerFactory messageHandlerFactory, Semaphore semaphore, Predicate<Message> messageFilter) {
            this.messageHandlerFactory = messageHandlerFactory;
            this.semaphore = semaphore;
            this.messageFilter = messageFilter;
        }

        @Override
        public MessageHandler newMessageHandler() {
            return new SemaphoreDrivenMessageHandler(messageHandlerFactory.newMessageHandler(), semaphore, messageFilter);
        }
    }
}
