/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.rest.ObjectMapperProvider;
import com.elster.jupiter.issue.rest.response.issue.IssueInfoFactoryService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionMapper;
import com.elster.jupiter.rest.util.ConcurrentModificationInfo;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.JsonMappingExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.RestValidationExceptionMapper;
import com.elster.jupiter.rest.util.TransactionWrapper;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.energyict.mdc.common.services.ObisCodeDescriptor;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.impl.PartialScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo;
import com.energyict.mdc.device.data.rest.DeviceStateAccessFeature;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask.ConnectionTaskLifecycleStatus;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.topology.impl.ServerTopologyService;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.obis.ObisCode;
import com.jayway.jsonpath.JsonModel;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Alright, this is a JerseyTest coupled with the real-deal back end service (or at least most of them), running in in-memory h2
 * Setup is crappy, but I couldn't find a way to avoid it.
 * We really needed end2end testing for connection method properties, as bugs kept piling up.
 */
public class ConnectionMethodResourceIntegrationTest extends JerseyTest {

    private static final String AS_1440_INCOMPLETE = "AS1440";
    private static final String AS_1440_COMPLETED = "AS1440Completed";
    private static final String IP_ADDRESS_FROM_PARTIAL = "192.168.1.1";
    private static final BigDecimal PORT_FROM_PARTIAL = BigDecimal.valueOf(4096);
    private static final String DEVICE_TYPE_NAME = ConnectionMethodResourceIntegrationTest.class.getSimpleName() + "Type";
    private static final String DEVICE_CONFIGURATION_NAME = ConnectionMethodResourceIntegrationTest.class.getSimpleName() + "Config";
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    private static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
    private static InMemoryIntegrationPersistence inMemoryPersistence;
    private static DeviceProtocol deviceProtocol;
    private static DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    private static ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties;
    private static ConnectionTypePluggableClass outboundIpConnectionTypePluggableClass;
    private static List<DeviceMessageSpec> deviceMessageIds;
    private static PartialScheduledConnectionTaskImpl as1440WithoutProperties;
    private static PartialScheduledConnectionTaskImpl as1440WithProperties;
    private static OutboundComPortPool whirlpool;

    private static DeviceType deviceType;
    private static DeviceConfiguration deviceConfiguration;

    private static YellowfinGroupsService yellowfinGroupsService;
    private static FirmwareService firmwareService;
    private static AppService appService;
    private static DeviceMessageService deviceMessageService;
    private static LoadProfileService loadProfileService;
    private static SearchService searchService;
    private static MessageService messageService;
    private static IssueDataValidationService issueDataValidationService;
    private static DeviceAlarmService deviceAlarmService;
    private static UserService userService;
    private static RestQueryService restQueryService;
    private static FavoritesService favoritesService;
    private static DeviceLifeCycleService deviceLifecycleService;
    private static ServerTopologyService topologyService;
    private static MultiElementDeviceService multiElementDeviceService;
    private static ServiceCallService serviceCallService;
    private static BpmService bpmService;
    private static ThreadPrincipalService threadPrincipalService;
    private static SecurityManagementService securityManagementService;
    private static MdcPropertyUtils mdcPropertyUtils;
    private static OrmService ormService;
    private static IssueInfoFactoryService issueInfoFactoryService;
    private static RegisteredDevicesKpiService registeredDevicesKpiService;
    private static TaskService tskService;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryPersistence.getTransactionService());

    static ObisCodeDescriptor obisCodeDescriptor;

    @BeforeClass
    public static void initialize() throws SQLException {
        yellowfinGroupsService = mock(YellowfinGroupsService.class);
        firmwareService = mock(FirmwareService.class);
        appService = mock(AppService.class);
        deviceMessageService = mock(DeviceMessageService.class);
        loadProfileService = mock(LoadProfileService.class);
        searchService = mock(SearchService.class);
        messageService = mock(MessageService.class);
        issueDataValidationService = mock(IssueDataValidationService.class);
        deviceAlarmService = mock(DeviceAlarmService.class);
        restQueryService = mock(RestQueryService.class);
        favoritesService = mock(FavoritesService.class);
        deviceLifecycleService = mock(DeviceLifeCycleService.class);
        topologyService = mock(ServerTopologyService.class);
        multiElementDeviceService = mock(MultiElementDeviceService.class);
        serviceCallService = mock(ServiceCallService.class);
        bpmService = mock(BpmService.class);
        threadPrincipalService = mock(ThreadPrincipalService.class);
        registeredDevicesKpiService = mock(RegisteredDevicesKpiService.class);
        userService = mock(UserService.class);
        securityManagementService = mock(SecurityManagementService.class);
        ormService = mock(OrmService.class);
        issueInfoFactoryService = mock(IssueInfoFactoryService.class);
        obisCodeDescriptor = mock(ObisCodeDescriptor.class);
        tskService = mock(TaskService.class);
        when(obisCodeDescriptor.describe(any(ObisCode.class))).thenReturn("obisCodeDescription");
        inMemoryPersistence = new InMemoryIntegrationPersistence();
        initializeClock();
        inMemoryPersistence.initializeDatabase("PersistenceIntegrationTest.mdc.device.data", false);
        deviceProtocol = mock(DeviceProtocol.class);
        deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        registerConnectionTypePluggableClasses();
        initializeMocks();
    }

    private static void registerConnectionTypePluggableClasses() {
        try (TransactionContext transactionContext = inMemoryPersistence.getTransactionService().getContext()) {
            outboundIpConnectionTypePluggableClass = registerConnectionTypePluggableClass(OutboundIpConnectionTypeImpl.class);
            transactionContext.commit();
        }
    }

    private static <T extends ConnectionType> ConnectionTypePluggableClass registerConnectionTypePluggableClass(Class<T> connectionTypeClass) {
        return inMemoryPersistence.getProtocolPluggableService()
                .newConnectionTypePluggableClass(connectionTypeClass.getSimpleName(), connectionTypeClass.getName());
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static void initializeMocks() {
        try (TransactionContext context = inMemoryPersistence.getTransactionService().getContext()) {
            PartialConnectionTaskProtocolDialect protocolDialect = new PartialConnectionTaskProtocolDialect();
            when(deviceProtocol.getDeviceProtocolDialects()).thenReturn(Collections.singletonList(protocolDialect));


            deviceMessageIds = new ArrayList<>();
            com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec0 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
            when(deviceMessageSpec0.getId()).thenReturn(DeviceMessageId.CONTACTOR_CLOSE.dbValue());
            deviceMessageIds.add(deviceMessageSpec0);
            com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec1 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
            when(deviceMessageSpec1.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN.dbValue());
            deviceMessageIds.add(deviceMessageSpec1);
            com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec2 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
            when(deviceMessageSpec2.getId()).thenReturn(DeviceMessageId.CONTACTOR_ARM.dbValue());
            deviceMessageIds.add(deviceMessageSpec2);
            com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec3 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
            when(deviceMessageSpec3.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT.dbValue());
            deviceMessageIds.add(deviceMessageSpec3);
            com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec4 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
            when(deviceMessageSpec4.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.dbValue());
            deviceMessageIds.add(deviceMessageSpec4);
            com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec5 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
            when(deviceMessageSpec5.getId()).thenReturn(DeviceMessageId.DISPLAY_SET_MESSAGE_WITH_OPTIONS.dbValue());
            deviceMessageIds.add(deviceMessageSpec5);


            when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageIds);
            com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel.class);
            int anySecurityLevel = 0;
            when(authenticationAccessLevel.getId()).thenReturn(anySecurityLevel);
            when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Collections.singletonList(authenticationAccessLevel));
            com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel encryptionAccessLevel = mock(com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel.class);
            when(encryptionAccessLevel.getId()).thenReturn(anySecurityLevel);
            when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Collections.singletonList(encryptionAccessLevel));
            when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.values()));
            freezeClock(2014, Calendar.JANUARY, 1); // Experiencing timing issues in tests that set clock back in time and the respective devices need their device life cycle
            deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
            //      when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
            DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
            deviceConfigurationBuilder.isDirectlyAddressable(true);
            deviceConfiguration = deviceConfigurationBuilder.add();

            protocolDialectConfigurationProperties = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(protocolDialect);

            as1440WithoutProperties = deviceConfiguration.newPartialScheduledConnectionTask(AS_1440_INCOMPLETE, outboundIpConnectionTypePluggableClass, TimeDuration.hours(1), ConnectionStrategy.AS_SOON_AS_POSSIBLE, protocolDialectConfigurationProperties).build();
            as1440WithProperties = deviceConfiguration.newPartialScheduledConnectionTask(AS_1440_COMPLETED, outboundIpConnectionTypePluggableClass, TimeDuration.hours(1), ConnectionStrategy.AS_SOON_AS_POSSIBLE, protocolDialectConfigurationProperties).
                    addProperty("ipAddress", IP_ADDRESS_FROM_PARTIAL).
                    addProperty("port", PORT_FROM_PARTIAL).
                    build();
            deviceMessageIds.stream().map(DeviceMessageSpec::getId).map(DeviceMessageId::from).forEach(deviceConfiguration::createDeviceMessageEnablement);
            deviceConfiguration.activate();
            whirlpool = inMemoryPersistence.getEngineConfigurationService().newOutboundComPortPool("Whirlpool", ComPortType.TCP, TimeDuration.minutes(1));
            resetClock();
            context.commit();
        }
    }

    @AfterClass
    public static void resetClock() {
        initializeClock();
    }

    protected static Instant freezeClock(int year, int month, int day) {
        return freezeClock(year, month, day, 0, 0, 0, 0);
    }

    protected static Instant freezeClock(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        return freezeClock(year, month, day, hour, minute, second, millisecond, utcTimeZone);
    }

    protected static Instant freezeClock(int year, int month, int day, int hour, int minute, int second, int millisecond, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(year, month, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        when(inMemoryPersistence.getClock().getZone()).thenReturn(timeZone.toZoneId());
        Instant frozenClockValue = calendar.getTime().toInstant();
        when(inMemoryPersistence.getClock().instant()).thenReturn(frozenClockValue);
        return frozenClockValue;
    }


    private static void initializeClock() {
        when(inMemoryPersistence.getClock().getZone()).thenReturn(utcTimeZone.toZoneId());
        when(inMemoryPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());
    }


    protected boolean disableDeviceConstraintsBasedOnDeviceState() {
        return true;
    }

    @Override
    protected final Application configure() {
        MockitoAnnotations.initMocks(this);
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        Application application = getApplication();
        ResourceConfig resourceConfig = new ResourceConfig(application.getClasses());
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(ObjectMapperProvider.class);
        resourceConfig.register(LocalizedFieldValidationExceptionMapper.class);
        resourceConfig.register(LocalizedExceptionMapper.class);
        resourceConfig.register(ConstraintViolationExceptionMapper.class);
        resourceConfig.register(JsonMappingExceptionMapper.class);
        resourceConfig.register(RestValidationExceptionMapper.class);
        resourceConfig.register(ConcurrentModificationExceptionMapper.class);
        resourceConfig.register(TransactionWrapper.class);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(ConcurrentModificationInfo.class).to(ConcurrentModificationInfo.class);
                bind(ConcurrentModificationExceptionFactory.class).to(ConcurrentModificationExceptionFactory.class);
                bind(inMemoryPersistence.getTransactionService()).to(TransactionService.class);
            }
        });
        application.getSingletons().stream().filter(s -> s instanceof AbstractBinder).forEach(resourceConfig::register);
        return resourceConfig;
    }

    @Override
    protected final void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class); // client side JSON processing
        config.register(ObjectMapperProvider.class);
        config.register(MultiPartFeature.class);
        config.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);  // Makes DELETE accept an entity
        super.configureClient(config);
    }

    protected Application getApplication() {
        DeviceApplication application = new DeviceApplication() {
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> classes = new HashSet<>(super.getClasses());
                if (disableDeviceConstraintsBasedOnDeviceState()) {
                    classes.remove(DeviceStateAccessFeature.class);
                }
                return classes;
            }
        };
        application.setNlsService(inMemoryPersistence.getNlsService());
        application.setTransactionService(inMemoryPersistence.getTransactionService());
        application.setMasterDataService(inMemoryPersistence.getMasterDataService());
        application.setDeviceConfigurationService(inMemoryPersistence.getDeviceConfigurationService());
        application.setJsonService(inMemoryPersistence.getJsonService());
        application.setProtocolPluggableService(inMemoryPersistence.getProtocolPluggableService());
        application.setClockService(inMemoryPersistence.getClock());
        application.setConnectionTaskService(inMemoryPersistence.getConnectionTaskService());
        application.setDeviceService(inMemoryPersistence.getDeviceService());
        application.setTopologyService(topologyService);
        application.setMultiElementDeviceService(multiElementDeviceService);
        application.setBatchService(inMemoryPersistence.getBatchService());
        application.setEngineConfigurationService(inMemoryPersistence.getEngineConfigurationService());
        application.setIssueService(inMemoryPersistence.getIssueService());
        application.setIssueDataValidationService(issueDataValidationService);
        application.setDeviceAlarmService(deviceAlarmService);
        application.setUserService(userService);
        application.setMeteringGroupsService(inMemoryPersistence.getMeteringGroupsService());
        application.setMeteringService(inMemoryPersistence.getMeteringService());
        application.setLocationService(inMemoryPersistence.getLocationService());
        application.setSchedulingService(inMemoryPersistence.getSchedulingService());
        application.setValidationService(inMemoryPersistence.getValidationService());
        application.setEstimationService(inMemoryPersistence.getEstimationService());
        application.setRestQueryService(restQueryService);
        application.setTaskService(inMemoryPersistence.getTaskService());
        application.setCommunicationTaskService(inMemoryPersistence.getCommunicationTaskService());
        application.setDeviceMessageSpecificationService(inMemoryPersistence.getDeviceMessageSpecificationService());
        application.setFavoritesService(favoritesService);
        application.setDataCollectionKpiService(inMemoryPersistence.getDataCollectionKpiService());
        application.setYellowfinGroupsService(yellowfinGroupsService);
        application.setFirmwareService(firmwareService);
        application.setDeviceLifeCycleService(deviceLifecycleService);
        application.setAppService(appService);
        application.setMessageService(messageService);
        application.setSearchService(searchService);
        application.setLoadProfileService(loadProfileService);
        application.setDeviceMessageService(deviceMessageService);
        application.setCustomPropertySetService(inMemoryPersistence.getCustomPropertySetService());
        application.setServiceCallService(inMemoryPersistence.getServiceCallService());
        application.setBpmService(bpmService);
        application.setServiceCallInfoFactory(inMemoryPersistence.getServiceCallInfoFactory());
        application.setCalendarInfoFactory(inMemoryPersistence.getCalendarInfoFactory());
        application.setCalendarService(inMemoryPersistence.getCalendarService());
        application.setThreadPrincipalService(inMemoryPersistence.getThreadPrincipalService());
        application.setPropertyValueInfoService(inMemoryPersistence.getPropertyValueInfoService());
        application.setMeteringTranslationService(inMemoryPersistence.getMeteringTranslationService());
        application.setDeviceLifeCycleConfigurationService(inMemoryPersistence.getDeviceLifeCycleConfigurationService());
        application.setObisCodeDescriptor(obisCodeDescriptor);
        application.setSecurityManagementService(securityManagementService);
        application.setMdcPropertyUtils(inMemoryPersistence.getMdcPropertyUtils());
        application.setIssueInfoFactoryService(issueInfoFactoryService);
        application.setOrmService(ormService);
        application.setRegisteredDevicesKpiService(registeredDevicesKpiService);
        application.setTskService(tskService);
        return application;
    }

    @Test
    public void testCreateScheduledConnectionMethodWithoutPropertiesFromIncompleteConfig() {
        Device device;
        try (TransactionContext transactionContext = inMemoryPersistence.getTransactionService().getContext()) {
            device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "AGENT007", Instant.now());
            device.save();
            transactionContext.commit();
        }
        ScheduledConnectionMethodInfo info = updateInfo(null);
        info.status = ConnectionTaskLifecycleStatus.INCOMPLETE;

        Response response = target("/devices/AGENT007/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        Optional<Device> agent = inMemoryPersistence.getDeviceService().findDeviceByName(device.getName());
        assertThat(agent).isPresent();
        assertThat(agent.get().getConnectionTasks()).hasSize(1);
        ConnectionTask connectionTask = agent.get().getConnectionTasks().get(0);
        assertThat(connectionTask.getStatus()).isEqualTo(ConnectionTaskLifecycleStatus.INCOMPLETE);
    }

    @Test
    public void testCreateScheduledConnectionMethodActiveWithoutProperties() throws IOException {
        Device device;
        try (TransactionContext transactionContext = inMemoryPersistence.getTransactionService().getContext()) {
            device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "AGENT009", Instant.now());
            device.save();
            transactionContext.commit();
        }

        ScheduledConnectionMethodInfo info = updateInfo(null);

        Response response = target("/devices/AGENT009/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("connectionTaskProperty.required");
    }

    @Test
    public void testCreateScheduledConnectionMethodActiveWithOnlyRequiredPropertiesFromIncompleteConfig() throws IOException {
        Device device;
        try (TransactionContext transactionContext = inMemoryPersistence.getTransactionService().getContext()) {
            device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "AGENT010", Instant.now());
            device.save();
            transactionContext.commit();
        }

        ScheduledConnectionMethodInfo info = updateInfo(null);
        info.properties = new ArrayList<>();
        info.properties.add(new PropertyInfo("ipAddress", "ipAddress", new PropertyValueInfo<Object>("10.10.10.1", true, null), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.TEXT, null, null, null), true));
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());

        Response response = target("/devices/AGENT010/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        Optional<Device> agent = inMemoryPersistence.getDeviceService().findDeviceByName(device.getName());
        assertThat(agent).isPresent();
        assertThat(agent.get().getConnectionTasks()).hasSize(1);
        ConnectionTask connectionTask = agent.get().getConnectionTasks().get(0);
        assertThat(connectionTask.getStatus()).isEqualTo(ConnectionTaskLifecycleStatus.ACTIVE);
    }

    @Test
    public void testCreateScheduledConnectionMethodWithPropertiesFromIncompleteConfig() {
        Device device;
        try (TransactionContext transactionContext = inMemoryPersistence.getTransactionService().getContext()) {
            device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "AGENT008", Instant.now());
            device.save();
            transactionContext.commit();
        }

        ScheduledConnectionMethodInfo info = updateInfo(null);
        info.properties = new ArrayList<>();
        info.properties.add(new PropertyInfo("ipAddress", "ipAddress", new PropertyValueInfo<Object>("10.10.10.1", true, null), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.TEXT, null, null, null), true));
        info.properties.add(new PropertyInfo("port", "port", new PropertyValueInfo<Object>(4096, true, null), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.NUMBER, null, null, null), true));

        Response response = target("/devices/AGENT008/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        Optional<Device> agent = inMemoryPersistence.getDeviceService().findDeviceByName(device.getName());
        assertThat(agent).isPresent();
        assertThat(agent.get().getConnectionTasks()).hasSize(1);
        ConnectionTask connectionTask = agent.get().getConnectionTasks().get(0);
        assertThat(connectionTask.getStatus()).isEqualTo(ConnectionTaskLifecycleStatus.ACTIVE);
    }

    @Test
    public void testUpdateAlreadyCompletedScheduledConnectionMethodWithRequiredPropertiesFromIncompleteConfig() {
        Device device;
        ScheduledConnectionTask scheduledConnectionTask;
        try (TransactionContext transactionContext = inMemoryPersistence.getTransactionService().getContext()) {
            device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "AGENT011", Instant.now());
            device.save();
            scheduledConnectionTask = device.getScheduledConnectionTaskBuilder(as1440WithoutProperties).
                    setComPortPool(whirlpool).
                    setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties).
                    setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE).
                    setConnectionTaskLifecycleStatus(ConnectionTaskLifecycleStatus.ACTIVE).
                    setProperty("ipAddress", "1.1.1.256"). // <--- the only required property
                    setNextExecutionSpecsFrom(new TemporalExpression(TimeDuration.days(1))).
                    add();
            transactionContext.commit();
        }

        ScheduledConnectionMethodInfo info = updateInfo(scheduledConnectionTask);
        info.properties = new ArrayList<>();
        info.properties.add(new PropertyInfo("ipAddress", "ipAddress", new PropertyValueInfo<Object>("10.10.10.1", true, null), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.TEXT, null, null, null), true));

        Response response = target("/devices/AGENT011/connectionmethods/" + scheduledConnectionTask.getId()).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        Optional<Device> agent = inMemoryPersistence.getDeviceService().findDeviceByName(device.getName());
        assertThat(agent).isPresent();
        assertThat(agent.get().getConnectionTasks()).hasSize(1);
        ConnectionTask connectionTask = agent.get().getConnectionTasks().get(0);
        assertThat(connectionTask.getStatus()).isEqualTo(ConnectionTaskLifecycleStatus.ACTIVE);
        assertThat(connectionTask.getProperty("port")).isNull();
        assertThat(connectionTask.getProperty("ipAddress").getValue()).isEqualTo("10.10.10.1");
    }

    @Test
    public void testUpdateAlreadyCompletedScheduledConnectionMethodWithAllPropertiesFromIncompleteConfig() {
        ScheduledConnectionTask scheduledConnectionTask;
        try (TransactionContext transactionContext = inMemoryPersistence.getTransactionService().getContext()) {
            scheduledConnectionTask = addScheduledConnectionTask(createDevice("AGENT012"), as1440WithoutProperties, ConnectionTaskLifecycleStatus.ACTIVE, true, BigDecimal.valueOf(9998), "1.1.1.256");
            transactionContext.commit();
        }
        ScheduledConnectionMethodInfo info = updateInfo(scheduledConnectionTask);
        info.properties = new ArrayList<>();
        info.properties.add(new PropertyInfo("ipAddress", "ipAddress", new PropertyValueInfo<Object>("10.10.10.1", true, null),
                new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.TEXT, null, null, null), true));

        Response response = target("/devices/AGENT012/connectionmethods/" + scheduledConnectionTask.getId()).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        Optional<Device> agent = inMemoryPersistence.getDeviceService().findDeviceByName("AGENT012");
        assertThat(agent).isPresent();
        assertThat(agent.get().getConnectionTasks()).hasSize(1);
        ConnectionTask connectionTask = agent.get().getConnectionTasks().get(0);
        assertThat(connectionTask.getStatus()).isEqualTo(ConnectionTaskLifecycleStatus.ACTIVE);
        assertThat(connectionTask.getProperty("ipAddress").getValue()).isEqualTo("10.10.10.1");
    }

    @Test
    public void testUpdateIncompleteScheduledConnectionMethodWithAllPropertiesFromIncompleteConfig() {
        ScheduledConnectionTask scheduledConnectionTask;
        try (TransactionContext transactionContext = inMemoryPersistence.getTransactionService().getContext()) {
            scheduledConnectionTask = addScheduledConnectionTask(createDevice("AGENT013"), as1440WithoutProperties, ConnectionTaskLifecycleStatus.INCOMPLETE, false, null, null);
            transactionContext.commit();
        }
        ScheduledConnectionMethodInfo info = updateInfo(scheduledConnectionTask);
        info.properties = new ArrayList<>();
        info.properties.add(new PropertyInfo("ipAddress", "ipAddress", new PropertyValueInfo<Object>("10.10.10.1", true, null),
                new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.TEXT, null, null, null), true));

        Response response = target("/devices/AGENT013/connectionmethods/" + scheduledConnectionTask.getId()).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        Optional<Device> agent = inMemoryPersistence.getDeviceService().findDeviceByName("AGENT013");
        assertThat(agent).isPresent();
        assertThat(agent.get().getConnectionTasks()).hasSize(1);
        ConnectionTask connectionTask = agent.get().getConnectionTasks().get(0);
        assertThat(connectionTask.getStatus()).isEqualTo(ConnectionTaskLifecycleStatus.ACTIVE);
        assertThat(connectionTask.getProperty("ipAddress").getValue()).isEqualTo("10.10.10.1");
    }

    @Test
    public void testCreateScheduledConnectionMethodActiveWithNullRequiredPropertyFromCompleteConfig() throws IOException {
        Device device;
        try (TransactionContext transactionContext = inMemoryPersistence.getTransactionService().getContext()) {
            device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "AGENT014", Instant.now());
            device.save();
            transactionContext.commit();
        }
        ScheduledConnectionMethodInfo info = updateInfo(null);
        info.name = AS_1440_COMPLETED; // <-- we inherit value for ip address and port
        info.properties = new ArrayList<>();
        info.properties.add(new PropertyInfo("ipAddress", "ipAddress", new PropertyValueInfo<Object>(null, true, null), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.TEXT, null, null, null), true));
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());

        Response response = target("/devices/AGENT014/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        Optional<Device> agent = inMemoryPersistence.getDeviceService().findDeviceByName(device.getName());
        assertThat(agent).isPresent();
        assertThat(agent.get().getConnectionTasks()).hasSize(1);
        ConnectionTask connectionTask = agent.get().getConnectionTasks().get(0);
        assertThat(connectionTask.getStatus()).isEqualTo(ConnectionTaskLifecycleStatus.ACTIVE);
        assertThat(connectionTask.getProperty("port")).isNull();
        assertThat(connectionTask.getProperty("ipAddress")).isNull();

    }

    @Test
    public void testUpdateScheduledConnectionMethodUndoPreviouslySetRequiredPropertyFromCompleteConfig() throws IOException {
        ScheduledConnectionTask scheduledConnectionTask;
        try (TransactionContext transactionContext = inMemoryPersistence.getTransactionService().getContext()) {
            scheduledConnectionTask = addScheduledConnectionTask(createDevice("AGENT015"), as1440WithProperties, ConnectionTaskLifecycleStatus.INCOMPLETE, true, PORT_FROM_PARTIAL, null);
            transactionContext.commit();
        }
        ScheduledConnectionMethodInfo info = updateInfo(scheduledConnectionTask);
        info.properties = new ArrayList<>();
        info.properties.add(new PropertyInfo("ipAddress", "ipAddress", new PropertyValueInfo<Object>(null, true, null), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.TEXT, null, null, null), true));

        Response response = target("/devices/AGENT015/connectionmethods/" + scheduledConnectionTask.getId()).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        Optional<Device> agent = inMemoryPersistence.getDeviceService().findDeviceByName("AGENT015");
        assertThat(agent).isPresent();
        assertThat(agent.get().getConnectionTasks()).hasSize(1);
        ConnectionTask connectionTask = agent.get().getConnectionTasks().get(0);
        assertThat(connectionTask.getStatus()).isEqualTo(ConnectionTaskLifecycleStatus.ACTIVE);
        assertThat(connectionTask.getProperty("port")).isNull();
        assertThat(connectionTask.getProperty("ipAddress").getValue()).isNull();
    }

    @Test
    public void testUpdateIncompleteScheduledConnectionMethodWithMissingRequiredPropertiesFromIncompleteConfig() throws IOException {
        ScheduledConnectionTask scheduledConnectionTask;
        try (TransactionContext transactionContext = inMemoryPersistence.getTransactionService().getContext()) {
            scheduledConnectionTask = addScheduledConnectionTask(createDevice("AGENT016"), as1440WithoutProperties, ConnectionTaskLifecycleStatus.INCOMPLETE, false, null, null);
            transactionContext.commit();
        }
        ScheduledConnectionMethodInfo info = updateInfo(scheduledConnectionTask);
        info.properties = new ArrayList<>();
        info.properties.add(new PropertyInfo("port", "port", new PropertyValueInfo<Object>(4096, true, null), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.NUMBER, null, null, null), true));

        Response response = target("/devices/AGENT016/connectionmethods/" + scheduledConnectionTask.getId()).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("connectionTaskProperty.required");
        // logged on 'status' because task was incomplete at time of update
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("status");
    }

    @Test
    public void testUpdateCompletedScheduledConnectionMethodWithMissingRequiredPropertiesFromIncompleteConfig() throws IOException {
        ScheduledConnectionTask scheduledConnectionTask;
        try (TransactionContext transactionContext = inMemoryPersistence.getTransactionService().getContext()) {
            scheduledConnectionTask = addScheduledConnectionTask(createDevice("AGENT017"), as1440WithoutProperties, ConnectionTaskLifecycleStatus.ACTIVE, true, null, null);
            transactionContext.commit();
        }
        ScheduledConnectionMethodInfo info = updateInfo(scheduledConnectionTask);
        info.properties = new ArrayList<>();
        info.properties.add(new PropertyInfo("port", "port", new PropertyValueInfo<Object>(4096, true, null), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.NUMBER, null, null, null), true));

        Response response = target("/devices/AGENT017/connectionmethods/" + scheduledConnectionTask.getId()).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("connectionTaskProperty.required");
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("properties.ipAddress");
    }

    @Test
    public void testUpdateCompletedScheduledConnectionMethodWithNullRequiredPropertiesFromIncompleteConfig() throws IOException {
        ScheduledConnectionTask scheduledConnectionTask;
        try (TransactionContext transactionContext = inMemoryPersistence.getTransactionService().getContext()) {
            scheduledConnectionTask = addScheduledConnectionTask(createDevice("AGENT018"), as1440WithoutProperties, ConnectionTaskLifecycleStatus.ACTIVE, true, null, null);
            transactionContext.commit();
        }
        ScheduledConnectionMethodInfo info = updateInfo(scheduledConnectionTask);
        info.properties = new ArrayList<>();
        info.properties.add(new PropertyInfo("ipAddress", "ipAddress", new PropertyValueInfo<>(null, null, null), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.TEXT, null, null, null), true));
        info.properties.add(new PropertyInfo("port", "port", new PropertyValueInfo<Object>(4096, null, null), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.NUMBER, null, null, null), true));

        Response response = target("/devices/AGENT018/connectionmethods/" + scheduledConnectionTask.getId()).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("connectionTaskProperty.required");
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("properties.ipAddress");
    }

    @Test
    public void testUpdateCompletedScheduledConnectionMethodWithNullOptionalPropertiesFromIncompleteConfig() throws IOException {
        Device device;
        ScheduledConnectionTask scheduledConnectionTask;
        try (TransactionContext transactionContext = inMemoryPersistence.getTransactionService().getContext()) {
            device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "AGENT019", Instant.now());
            device.save();
            scheduledConnectionTask = device.getScheduledConnectionTaskBuilder(as1440WithoutProperties).
                    setComPortPool(whirlpool).
                    setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties).
                    setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE).
                    setConnectionTaskLifecycleStatus(ConnectionTaskLifecycleStatus.ACTIVE).
                    setNextExecutionSpecsFrom(new TemporalExpression(TimeDuration.days(1))).
                    setProperty("ipAddress", "6.6.6.6").
                    setProperty("port", BigDecimal.valueOf(666)).
                    add();
            transactionContext.commit();
        }
        ScheduledConnectionMethodInfo info = new ScheduledConnectionMethodInfo();
        info.name = AS_1440_INCOMPLETE;
        info.status = ConnectionTaskLifecycleStatus.ACTIVE;
        info.nextExecutionSpecs = new TemporalExpressionInfo();
        info.nextExecutionSpecs.every = new TimeDurationInfo();
        info.nextExecutionSpecs.every.count = 15;
        info.nextExecutionSpecs.every.timeUnit = "minutes";
        info.version = scheduledConnectionTask.getVersion();
        DeviceConnectionTaskInfo.ConnectionStrategyInfo strategyInfo = new DeviceConnectionTaskInfo.ConnectionStrategyInfo();
        strategyInfo.connectionStrategy = "AS_SOON_AS_POSSIBLE";
        strategyInfo.localizedValue = "As soon as Possible";
        info.connectionStrategyInfo = strategyInfo;
        info.comPortPool = "Whirlpool";
        info.protocolDialect = protocolDialectConfigurationProperties.getDeviceProtocolDialectName();
        info.protocolDialectDisplayName = protocolDialectConfigurationProperties.getDeviceProtocolDialect().getDeviceProtocolDialectDisplayName();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());
        info.properties = new ArrayList<>();
        info.properties.add(new PropertyInfo("ipAddress", "ipAddress", new PropertyValueInfo<Object>("10.10.10.1", null, null), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.TEXT, null, null, null), true));
        info.properties.add(new PropertyInfo("port", "port", new PropertyValueInfo<>(null, null, null), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.NUMBER, null, null, null), true));

        Response response = target("/devices/AGENT019/connectionmethods/" + scheduledConnectionTask.getId()).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        Optional<Device> agent = inMemoryPersistence.getDeviceService().findDeviceByName(device.getName());
        assertThat(agent).isPresent();
        assertThat(agent.get().getConnectionTasks()).hasSize(1);
        ConnectionTask connectionTask = agent.get().getConnectionTasks().get(0);
        assertThat(connectionTask.getStatus()).isEqualTo(ConnectionTaskLifecycleStatus.ACTIVE);
        assertThat(connectionTask.getProperty("port")).isNull();
        assertThat(connectionTask.getProperty("ipAddress").getValue()).isEqualTo("10.10.10.1");
    }

    private ScheduledConnectionMethodInfo updateInfo(ConnectionTask task) {
        ScheduledConnectionMethodInfo info = new ScheduledConnectionMethodInfo();
        info.name = AS_1440_INCOMPLETE;
        info.status = ConnectionTaskLifecycleStatus.ACTIVE;
        info.nextExecutionSpecs = new TemporalExpressionInfo();
        info.nextExecutionSpecs.every = new TimeDurationInfo();
        info.nextExecutionSpecs.every.count = 15;
        info.nextExecutionSpecs.every.timeUnit = "minutes";
        if (task != null) {
            info.version = task.getVersion();
        } else {
            info.version = 0L;
        }
        DeviceConnectionTaskInfo.ConnectionStrategyInfo strategyInfo = new DeviceConnectionTaskInfo.ConnectionStrategyInfo();
        strategyInfo.connectionStrategy = "AS_SOON_AS_POSSIBLE";
        strategyInfo.localizedValue = "As soon as Possible";
        info.connectionStrategyInfo = strategyInfo;
        info.comPortPool = "Whirlpool";
        info.protocolDialect = protocolDialectConfigurationProperties.getDeviceProtocolDialectName();
        info.protocolDialectDisplayName = protocolDialectConfigurationProperties.getDeviceProtocolDialect().getDeviceProtocolDialectDisplayName();
        if (task != null) {
            info.parent = new VersionInfo<>(task.getDevice().getName(), task.getDevice().getVersion());
        }
        return info;
    }

    private Device createDevice(String name) {
        return inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, Instant.now());
    }

    private ScheduledConnectionTask addScheduledConnectionTask(Device device, PartialOutboundConnectionTask partialConnectionTask, ConnectionTaskLifecycleStatus status, boolean complete, BigDecimal port, String ipAddress) {
        Device.ScheduledConnectionTaskBuilder taskbuilder = device.getScheduledConnectionTaskBuilder(partialConnectionTask);
        taskbuilder.setComPortPool(whirlpool).
                setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties).
                setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE).
                setConnectionTaskLifecycleStatus(status).
                setNextExecutionSpecsFrom(new TemporalExpression(TimeDuration.days(1)));
        if (complete) {
            taskbuilder.setProperty("port", port == null ? BigDecimal.valueOf(666) : port)
                    .setProperty("ipAddress", ipAddress == null ? "6.6.6.6" : ipAddress);
        }
        return taskbuilder.add();
    }

    private static class PartialConnectionTaskProtocolDialect implements DeviceProtocolDialect {

        @Override
        public List<PropertySpec> getUPLPropertySpecs() {
            return Collections.emptyList();
        }

        @Override
        public String getDeviceProtocolDialectName() {
            return "dialect";
        }

        @Override
        public String getDeviceProtocolDialectDisplayName() {
            return "It's a Dell Display";
        }

        @Override
        public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
            return Optional.empty();
        }

    }


}
