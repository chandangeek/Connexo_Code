package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.config.events.PartialConnectionTaskUpdateDetails;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.impl.NextExecutionSpecsImpl;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.common.*;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.config.events.PartialConnectionTaskUpdateDetails;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigConflictMappingHandler;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.impl.NextExecutionSpecsImpl;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.fest.assertions.core.Condition;
import org.joda.time.DateTimeConstants;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PartialOutboundConnectionTaskCrudIT {

    private static final ComWindow COM_WINDOW = new ComWindow(DateTimeConstants.SECONDS_PER_HOUR, DateTimeConstants.SECONDS_PER_HOUR * 2);
    private static final TimeDuration SIXTY_SECONDS = TimeDuration.seconds(DateTimeConstants.SECONDS_PER_MINUTE);   // In other words: 1 minute
    private static final TimeDuration FIFTEEN_MINUTES = TimeDuration.minutes(15);
    private static final TimeDuration NINETY_MINUTES = TimeDuration.minutes(90);
    private static final TimeDuration TWELVE_HOURS = new TimeDuration(12, TimeDuration.TimeUnit.HOURS);
    private static final TimeDuration TWO_HOURS = new TimeDuration(2, TimeDuration.TimeUnit.HOURS);
    private static final TimeDuration ONE_HOUR_IN_MINUTES = TimeDuration.minutes(60);

    @Rule
    public final TestRule thereIsNOOOORuleNumber6 = new ExpectedConstraintViolationRule();
    @Rule
    public final TestRule transactional = new TransactionalRule(transactionService);

    private static InMemoryBootstrapModule bootstrapModule;
    private static EventAdmin eventAdmin;
    private static BundleContext bundleContext;
    private static LicenseService licenseService;
    private static TransactionService transactionService;
    private static EngineConfigurationService engineConfigurationService;
    private static ProtocolPluggableService protocolPluggableService;
    private static DeviceConfigurationServiceImpl deviceConfigurationService;
    private static ConnectionTypePluggableClass connectionTypePluggableClass, connectionTypePluggableClass2;
    private static LicensedProtocolService licensedProtocolService;
    private static ConnectionTypeService connectionTypeService;
    private static OutboundComPortPool outboundComPortPool, outboundComPortPool1;
    private static SpyEventService eventService;
    private static SchedulingService schedulingService;
    private static Injector injector = null;


    @Mock
    private MyDeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;

    @Mock
    private IdBusinessObjectFactory businessObjectFactory;

    private static class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
        }
    }

    @BeforeClass
    public static void initializeDatabase() throws SQLException {
        initializeStaticMocks();
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(PartialOutboundConnectionTaskCrudIT.class.getSimpleName());
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    bootstrapModule,
                    new ThreadSecurityModule(principal),
                    new PubSubModule(),
                    new TransactionModule(false),
                    new UtilModule(),
                    new NlsModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new PartyModule(),
                    new UserModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
                    new MeteringModule(),
                    new InMemoryMessagingModule(),
                    new OrmModule(),
                    new DataVaultModule(),
                    new MdcReadingTypeUtilServiceModule(),
                    new MasterDataModule(),
                    new BasicPropertiesModule(),
                    new MdcDynamicModule(),
                    new ProtocolApiModule(),
                    new TasksModule(),
                    new DeviceLifeCycleConfigurationModule(),
                    new DeviceConfigurationModule(),
                    new MdcIOModule(),
                    new EngineModelModule(),
                    new ProtocolPluggableModule(),
                    new ValidationModule(),
                    new EstimationModule(),
                    new MeteringGroupsModule(),
                    new TaskModule(),
                    new IssuesModule(),
                    new BasicPropertiesModule(),
                    new MdcDynamicModule(),
                    new PluggableModule(),
                    new SchedulingModule(),
                    new TimeModule());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            OrmService ormService = injector.getInstance(OrmService.class);
            eventService = new SpyEventService(injector.getInstance(EventService.class));
            NlsService nlsService = injector.getInstance(NlsService.class);
            PropertySpecServiceImpl propertySpecService = (PropertySpecServiceImpl) injector.getInstance(PropertySpecService.class);
            initializeConnectionTypes(propertySpecService);
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringService.class);
            injector.getInstance(MdcReadingTypeUtilService.class);
            engineConfigurationService = injector.getInstance(EngineConfigurationService.class);
            protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            protocolPluggableService.addLicensedProtocolService(licensedProtocolService);
            protocolPluggableService.addConnectionTypeService(connectionTypeService);
            injector.getInstance(PluggableService.class);
            injector.getInstance(MasterDataService.class);
            injector.getInstance(TaskService.class);
            injector.getInstance(ValidationService.class);
            schedulingService = injector.getInstance(SchedulingService.class);
            deviceConfigurationService = new DeviceConfigurationServiceImpl(
                    ormService,
                    injector.getInstance(Clock.class),
                    injector.getInstance(ThreadPrincipalService.class),
                    eventService,
                    nlsService,
                    injector.getInstance(MeteringService.class),
                    injector.getInstance(MdcReadingTypeUtilService.class),
                    injector.getInstance(UserService.class),
                    protocolPluggableService,
                    engineConfigurationService,
                    schedulingService,
                    injector.getInstance(ValidationService.class),
                    injector.getInstance(EstimationService.class),
                    injector.getInstance(MasterDataService.class),
                    injector.getInstance(DeviceLifeCycleConfigurationService.class));
            DataModel dataModel = deviceConfigurationService.getDataModel();
            createOracleAliases(dataModel.getConnection(true));
            ctx.commit();
        }
        setupMasterData();
    }

    private static void initializeStaticMocks() {
        eventAdmin = mock(EventAdmin.class);
        bundleContext = mock(BundleContext.class);
        licenseService = mock(LicenseService.class);
        when(licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.empty());
        bootstrapModule = new InMemoryBootstrapModule();
        licensedProtocolService = mock(LicensedProtocolService.class);
        when(licensedProtocolService.isValidJavaClassName(anyString(), any(License.class))).thenReturn(true);
    }

    private static void initializeConnectionTypes(PropertySpecServiceImpl propertySpecService) {
        propertySpecService.addFactoryProvider(() -> {
            List<CanFindByLongPrimaryKey<? extends HasId>> finders = new ArrayList<>();
            finders.add(new ConnectionTaskFinder());
            return finders;
        });
        connectionTypeService = mock(ConnectionTypeService.class);
        when(connectionTypeService.createConnectionType(OutboundNoParamsConnectionTypeImpl.class.getName())).thenReturn(new OutboundNoParamsConnectionTypeImpl());
        when(connectionTypeService.createConnectionType(InboundNoParamsConnectionTypeImpl.class.getName())).thenReturn(new InboundNoParamsConnectionTypeImpl());
        when(connectionTypeService.createConnectionType(IpConnectionType.class.getName())).thenReturn(new IpConnectionType(propertySpecService));
    }

    private static void setupMasterData() {
        try (TransactionContext context = transactionService.getContext()) {
            connectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType", OutboundNoParamsConnectionTypeImpl.class.getName());
            connectionTypePluggableClass.save();
            connectionTypePluggableClass2 = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType2", OutboundNoParamsConnectionTypeImpl.class.getName());
            connectionTypePluggableClass2.save();
            outboundComPortPool = engineConfigurationService.newOutboundComPortPool("inboundComPortPool", ComPortType.TCP, FIFTEEN_MINUTES);
            outboundComPortPool.setActive(true);
            outboundComPortPool.save();
            outboundComPortPool1 = engineConfigurationService.newOutboundComPortPool("inboundComPortPool2", ComPortType.TCP, TimeDuration.minutes(5));
            outboundComPortPool1.setActive(true);
            outboundComPortPool1.save();
            context.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        bootstrapModule.deactivate();
    }

    @Before
    public void initializeMocks() throws SQLException {
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.values()));
    }

    @Test
    @Transactional
    public void testCreate() {
        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(TimeDuration.days(1), NINETY_MINUTES).set()
                .asDefault(true).build();
        deviceConfiguration.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found.isPresent()).isTrue();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialScheduledConnectionTaskImpl.class);

        PartialScheduledConnectionTaskImpl partialOutboundConnectionTask = (PartialScheduledConnectionTaskImpl) partialConnectionTask;

        assertThat(partialOutboundConnectionTask.getComPortPool().getId()).isEqualTo(outboundComPortPool.getId());
        assertThat(partialOutboundConnectionTask.isDefault()).isTrue();
        assertThat(partialOutboundConnectionTask.getConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(partialOutboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass.getConnectionType());
        assertThat(partialOutboundConnectionTask.getCommunicationWindow()).isEqualTo(COM_WINDOW);
        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs().getTemporalExpression()).isEqualTo(new TemporalExpression(TimeDuration.days(1), NINETY_MINUTES));
        assertThat(partialOutboundConnectionTask.getName()).isEqualTo("MyOutbound");
        assertThat(partialOutboundConnectionTask.getRescheduleDelay()).isEqualTo(SIXTY_SECONDS);
        assertThat(partialOutboundConnectionTask.getConnectionStrategy()).isEqualTo(ConnectionStrategy.MINIMIZE_CONNECTIONS);

        verify(eventService.getSpy()).postEvent(EventType.PARTIAL_SCHEDULED_CONNECTION_TASK_CREATED.topic(), outboundConnectionTask);
    }

    @Test
    @Transactional
    public void createDefaultWithoutDefaultTest() {
        PartialScheduledConnectionTaskImpl notTheDefault;
        PartialScheduledConnectionTaskImpl theDefault;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        notTheDefault = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(TimeDuration.days(1), NINETY_MINUTES).set()
                .asDefault(false).build();
        theDefault = deviceConfiguration.newPartialScheduledConnectionTask("MyDefault", connectionTypePluggableClass2, TimeDuration.days(1), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(outboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.save();

        Optional<PartialConnectionTask> foundTheNotDefault = deviceConfigurationService.findPartialConnectionTask(notTheDefault.getId());
        Optional<PartialConnectionTask> foundTheDefault = deviceConfigurationService.findPartialConnectionTask(theDefault.getId());
        assertThat(foundTheNotDefault.isPresent()).isTrue();
        assertThat(foundTheDefault.isPresent()).isTrue();
        assertThat(foundTheNotDefault.get().isDefault()).isFalse();
        assertThat(foundTheDefault.get().isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void createDefaultWithAlreadyDefaultTest() {
        PartialScheduledConnectionTaskImpl notTheDefault;
        PartialScheduledConnectionTaskImpl theDefault;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        notTheDefault = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(TimeDuration.days(1), NINETY_MINUTES).set()
                .asDefault(true).build();
        theDefault = deviceConfiguration.newPartialScheduledConnectionTask("MyDefault", connectionTypePluggableClass2, TimeDuration.days(1), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(outboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.save();

        Optional<PartialConnectionTask> foundTheNotDefault = deviceConfigurationService.findPartialConnectionTask(notTheDefault.getId());
        Optional<PartialConnectionTask> foundTheDefault = deviceConfigurationService.findPartialConnectionTask(theDefault.getId());
        assertThat(foundTheNotDefault.isPresent()).isTrue();
        assertThat(foundTheDefault.isPresent()).isTrue();
        assertThat(foundTheNotDefault.get().isDefault()).isFalse();
        assertThat(foundTheDefault.get().isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void testUpdate() {
        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(new TimeDuration(6, TimeDuration.TimeUnit.HOURS), new TimeDuration(1, TimeDuration.TimeUnit.HOURS)).set()
                .asDefault(true).build();
        deviceConfiguration.save();
        reset(eventService.getSpy());

        ComWindow newComWindow = new ComWindow(DateTimeConstants.SECONDS_PER_HOUR * 2, DateTimeConstants.SECONDS_PER_HOUR * 3);
        PartialScheduledConnectionTask task;
        task = deviceConfiguration.getPartialOutboundConnectionTasks().get(0);
        task.setDefault(false);
        task.setComportPool(outboundComPortPool1);
        task.setConnectionTypePluggableClass(connectionTypePluggableClass2);
        task.setTemporalExpression(new TemporalExpression(TWELVE_HOURS, TWO_HOURS));
        task.setComWindow(newComWindow);
        task.setName("Changed");
        task.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found.isPresent()).isTrue();
        PartialConnectionTask partialConnectionTask = found.get();
        assertThat(partialConnectionTask).isInstanceOf(PartialScheduledConnectionTaskImpl.class);
        PartialScheduledConnectionTaskImpl reloadedPartialOutboundConnectionTask = (PartialScheduledConnectionTaskImpl) partialConnectionTask;
        assertThat(reloadedPartialOutboundConnectionTask.getComPortPool().getId()).isEqualTo(outboundComPortPool1.getId());
        assertThat(reloadedPartialOutboundConnectionTask.isDefault()).isFalse();
        assertThat(reloadedPartialOutboundConnectionTask.getConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(reloadedPartialOutboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass2.getConnectionType());
        assertThat(reloadedPartialOutboundConnectionTask.getCommunicationWindow()).isEqualTo(newComWindow);
        assertThat(reloadedPartialOutboundConnectionTask.getTemporalExpression().getEvery().getCount()).isEqualTo(12);
        assertThat(reloadedPartialOutboundConnectionTask.getTemporalExpression().getEvery().getTimeUnit()).isEqualTo(TimeDuration.TimeUnit.HOURS);
        assertThat(reloadedPartialOutboundConnectionTask.getName()).isEqualTo("Changed");

        ArgumentCaptor<PartialConnectionTaskUpdateDetails> updateDetailsArgumentCaptor = ArgumentCaptor.forClass(PartialConnectionTaskUpdateDetails.class);
        verify(eventService.getSpy()).postEvent(eq(EventType.PARTIAL_SCHEDULED_CONNECTION_TASK_UPDATED.topic()), updateDetailsArgumentCaptor.capture());
        PartialConnectionTaskUpdateDetails updateDetails = updateDetailsArgumentCaptor.getValue();
        assertThat(updateDetails.getPartialConnectionTask()).isEqualTo(task);
    }

    @Test
    @Transactional
    public void updateToDefaultWithoutCurrentDefaultTest() {
        DeviceConfiguration deviceConfiguration;
        final String connectionTaskName1 = "MyOutbound";
        final String connectionTaskName2 = "MyDefault";
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        deviceConfiguration.newPartialScheduledConnectionTask(connectionTaskName1, connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(TimeDuration.days(1), NINETY_MINUTES).set()
                .asDefault(false).build();
        deviceConfiguration.newPartialScheduledConnectionTask(connectionTaskName2, connectionTypePluggableClass2, TimeDuration.days(1), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(outboundComPortPool)
                .asDefault(false).build();
        deviceConfiguration.save();

        PartialScheduledConnectionTask task;
        task = getConnectionTaskWithName(deviceConfiguration, connectionTaskName2);
        task.setDefault(true);
        task.save();

        DeviceConfiguration reloadedDeviceConfig = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId()).get();
        PartialScheduledConnectionTask partialConnectionTask1 = getConnectionTaskWithName(reloadedDeviceConfig, connectionTaskName1);
        assertThat(partialConnectionTask1.isDefault()).isFalse();
        PartialScheduledConnectionTask partialConnectionTask2 = getConnectionTaskWithName(reloadedDeviceConfig, connectionTaskName2);
        assertThat(partialConnectionTask2.isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void updateToDefaultWithCurrentDefaultTest() {
        DeviceConfiguration deviceConfiguration;
        final String connectionTaskName1 = "MyOutbound";
        final String connectionTaskName2 = "MyDefault";
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        deviceConfiguration.newPartialScheduledConnectionTask(connectionTaskName1, connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(TimeDuration.days(1), NINETY_MINUTES).set()
                .asDefault(true).build();
        deviceConfiguration.newPartialScheduledConnectionTask(connectionTaskName2, connectionTypePluggableClass2, TimeDuration.days(1), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(outboundComPortPool)
                .asDefault(false).build();
        deviceConfiguration.save();

        PartialScheduledConnectionTask initialDefault = getConnectionTaskWithName(deviceConfiguration, connectionTaskName1);
        assertThat(initialDefault.isDefault()).isTrue();

        PartialScheduledConnectionTask task;
        task = getConnectionTaskWithName(deviceConfiguration, connectionTaskName2);
        task.setDefault(true);
        task.save();

        DeviceConfiguration reloadedDeviceConfig = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId()).get();
        PartialScheduledConnectionTask partialConnectionTask1 = getConnectionTaskWithName(reloadedDeviceConfig, connectionTaskName1);
        assertThat(partialConnectionTask1.isDefault()).isFalse();
        PartialScheduledConnectionTask partialConnectionTask2 = getConnectionTaskWithName(reloadedDeviceConfig, connectionTaskName2);
        assertThat(partialConnectionTask2.isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void testUpdateNextExecutionSpecsWithMinimizeConnections() {
        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .nextExecutionSpec().temporalExpression(TimeDuration.minutes(30)).set()
                .asDefault(true).build();
        deviceConfiguration.save();

        deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId()).get();

        PartialScheduledConnectionTask task;
        task = deviceConfiguration.getPartialOutboundConnectionTasks().get(0);
        NextExecutionSpecsImpl instance = (NextExecutionSpecsImpl) schedulingService.newNextExecutionSpecs(null);
        instance.setTemporalExpression(new TemporalExpression(ONE_HOUR_IN_MINUTES, ONE_HOUR_IN_MINUTES));
        instance.save();
        task.setNextExecutionSpecs(instance);
        task.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found.isPresent()).isTrue();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialScheduledConnectionTaskImpl.class);

        PartialScheduledConnectionTaskImpl partialOutboundConnectionTask = (PartialScheduledConnectionTaskImpl) partialConnectionTask;

        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs().getTemporalExpression()).isEqualTo(new TemporalExpression(ONE_HOUR_IN_MINUTES, ONE_HOUR_IN_MINUTES));
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_CONFIGURATION_IS_NOT_DIRECTLY_ADDRESSABLE + "}")
    public void testCanNotCreateConnectionTaskWhenConfigurationIsNotDirectlyAddressable() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(false);
        deviceConfiguration.save();

        deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .asDefault(true).build();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NEXT_EXECUTION_SPEC_NOT_ALLOWED_FOR_ASAP + "}", property = "nextExecutionSpecs")
    public void testCanNotCreateConnectionTaskWithNextExecutionSpecIfAsSoonAsPossible() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(outboundComPortPool)
                .nextExecutionSpec().temporalExpression(TimeDuration.hours(3)).set()
                .asDefault(true).build();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    public void testUpdateNextExecutionSpecsWithAsSoonAsPossible() {
        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .asDefault(true).build();
        deviceConfiguration.save();

        deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId()).get();

        PartialScheduledConnectionTask task;
        task = deviceConfiguration.getPartialOutboundConnectionTasks().get(0);
        NextExecutionSpecsImpl instance = (NextExecutionSpecsImpl) schedulingService.newNextExecutionSpecs(null);
        instance.setTemporalExpression(new TemporalExpression(ONE_HOUR_IN_MINUTES, ONE_HOUR_IN_MINUTES));
        instance.save();
        task.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found.isPresent()).isTrue();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialScheduledConnectionTaskImpl.class);

        PartialScheduledConnectionTaskImpl partialOutboundConnectionTask = (PartialScheduledConnectionTaskImpl) partialConnectionTask;

        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs()).isNull();
    }

    @Test
    @Transactional
    public void testDelete() {
        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .asDefault(true).build();
        deviceConfiguration.save();

        PartialScheduledConnectionTask partialOutboundConnectionTask;
        partialOutboundConnectionTask = deviceConfiguration.getPartialOutboundConnectionTasks().get(0);
        deviceConfiguration.remove(partialOutboundConnectionTask);
        deviceConfiguration.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found.isPresent()).isFalse();

        verify(eventService.getSpy()).postEvent(EventType.PARTIAL_SCHEDULED_CONNECTION_TASK_DELETED.topic(), partialOutboundConnectionTask);

    }

    @Test
    @Transactional
    public void testCreateReferencingInitiation() {
        PartialConnectionInitiationTaskImpl connectionInitiationTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType;
        deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        connectionInitiationTask = deviceConfiguration.newPartialConnectionInitiationTask("MyInitiation", connectionTypePluggableClass, SIXTY_SECONDS)
                .comPortPool(outboundComPortPool)
                .rescheduleDelay(SIXTY_SECONDS)
                .build();
        deviceConfiguration.save();

        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(TimeDuration.days(1), NINETY_MINUTES).set()
                .initiationTask(connectionInitiationTask)
                .asDefault(true).build();
        deviceConfiguration.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found.isPresent()).isTrue();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialScheduledConnectionTaskImpl.class);

        PartialScheduledConnectionTaskImpl partialOutboundConnectionTask = (PartialScheduledConnectionTaskImpl) partialConnectionTask;

        assertThat(partialOutboundConnectionTask.getComPortPool().getId()).isEqualTo(outboundComPortPool.getId());
        assertThat(partialOutboundConnectionTask.isDefault()).isTrue();
        assertThat(partialOutboundConnectionTask.getConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(partialOutboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass.getConnectionType());
        assertThat(partialOutboundConnectionTask.getCommunicationWindow()).isEqualTo(COM_WINDOW);
        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs().getTemporalExpression()).isEqualTo(new TemporalExpression(TimeDuration.days(1), NINETY_MINUTES));
        assertThat(partialOutboundConnectionTask.getInitiatorTask().getId()).isEqualTo(connectionInitiationTask.getId());
        assertThat(partialOutboundConnectionTask.getName()).isEqualTo("MyOutbound");

    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.CONNECTION_STRATEGY_REQUIRED + '}')
    public void testCreateWithoutConnectionStrategy() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, null)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(TimeDuration.days(1), NINETY_MINUTES).set()
                .asDefault(true).build();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.NEXT_EXECUTION_SPEC_REQUIRED_FOR_MINIMIZE_CONNECTIONS + '}')
    public void testCreateMinimizingConnectionsWithoutNextExecutionSpecs() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .asDefault(true).build();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.NEXT_EXECUTION_SPEC_INVALID_FOR_COM_WINDOW + '}')
    public void testCreateWithNextExecutionSpecsOffsetNotWithinComWindowTest() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(TimeDuration.hours(12), TimeDuration.hours(4)).set()
                .asDefault(true).build();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.UNDER_MINIMUM_RESCHEDULE_DELAY + '}')
    public void testCreateWithTooLowReschedulingRetryDelayTest() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(59), ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(TimeDuration.days(1), NINETY_MINUTES).set()
                .asDefault(true).build();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.PARTIAL_CONNECTION_TASK_PROPERTY_HAS_NO_SPEC + '}')
    public void testCreateWithNonSpecifiedProperty() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(TimeDuration.days(1), NINETY_MINUTES).set()
                .addProperty("NONSPECCED", 5)
                .asDefault(true).build();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.PARTIAL_CONNECTION_TASK_PROPERTY_VALUE_OF_WRONG_TYPE + '}')
    public void testCreateWithWrongValueForProperty() {
        DeviceConfiguration deviceConfiguration;
        connectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("IPConnectionType", IpConnectionType.class.getName());
        connectionTypePluggableClass.save();
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(TimeDuration.days(1), NINETY_MINUTES).set()
                .addProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, BigDecimal.valueOf(140024, 2))
                .asDefault(true).build();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    public void testCreateWithCorrectProperty() {
        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(TimeDuration.days(1), NINETY_MINUTES).set()
                .addProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, "127.0.0.1")
                .asDefault(true).build();
        deviceConfiguration.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found.isPresent()).isTrue();

        PartialConnectionTask partialConnectionTask = found.get();

        TypedProperties typedProperties = partialConnectionTask.getTypedProperties();
        assertThat(typedProperties.hasValueFor(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo("127.0.0.1");

    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.INCORRECT_CONNECTION_TYPE_FOR_CONNECTION_METHOD + "}")
    public void createOutboundConnectionMethodWithInboundConnectionTypeTest() {
        DeviceConfiguration deviceConfiguration;
        ConnectionTypePluggableClass inboundConnectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("Inboundtype", InboundNoParamsConnectionTypeImpl.class.getName());
        inboundConnectionTypePluggableClass.save();
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        deviceConfiguration.newPartialScheduledConnectionTask("MyOutboundWhichIsActuallyAnInbound", inboundConnectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(TimeDuration.days(1), NINETY_MINUTES).set()
                .asDefault(true).build();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    public void cloneTest() {
        PartialScheduledConnectionTaskImpl outboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceConfiguration clonedDeviceConfig;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("CloneCreate", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Original").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();
        clonedDeviceConfig = deviceType.newConfiguration("Clone").add();
        clonedDeviceConfig.setDirectlyAddressable(true);
        clonedDeviceConfig.save();

        outboundConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(TimeDuration.days(1), NINETY_MINUTES).set()
                .asDefault(true).build();
        deviceConfiguration.save();

        PartialConnectionTask clonedPartialConnectionTask = outboundConnectionTask.cloneForDeviceConfig(clonedDeviceConfig);

        PartialScheduledConnectionTaskImpl partialOutboundConnectionTask = (PartialScheduledConnectionTaskImpl) clonedPartialConnectionTask;

        assertThat(partialOutboundConnectionTask.getComPortPool().getId()).isEqualTo(outboundComPortPool.getId());
        assertThat(partialOutboundConnectionTask.isDefault()).isTrue();
        assertThat(partialOutboundConnectionTask.getConfiguration().getId()).isEqualTo(clonedDeviceConfig.getId());
        assertThat(partialOutboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass.getConnectionType());
        assertThat(partialOutboundConnectionTask.getCommunicationWindow()).isEqualTo(COM_WINDOW);
        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs().getTemporalExpression()).isEqualTo(new TemporalExpression(TimeDuration.days(1), NINETY_MINUTES));
        assertThat(partialOutboundConnectionTask.getName()).isEqualTo("MyOutbound");
        assertThat(partialOutboundConnectionTask.getRescheduleDelay()).isEqualTo(SIXTY_SECONDS);
        assertThat(partialOutboundConnectionTask.getConnectionStrategy()).isEqualTo(ConnectionStrategy.MINIMIZE_CONNECTIONS);
    }

    @Test
    @Transactional
    public void partialConnectionTaskConflictTest() {
        DeviceType deviceType = deviceConfigurationService.newDeviceType("ConflictTest", deviceProtocolPluggableClass);
        deviceType.save();

        DeviceConfiguration firstConfig = createDirectlyAddressableConfig(deviceType, "firstConfig");
        PartialScheduledConnectionTaskImpl outboundConnectionTask1 = createPartialConnectionTask(firstConfig, "FirstConnectionTask");

        DeviceConfiguration secondConfig = createDirectlyAddressableConfig(deviceType, "secondConfig");
        PartialScheduledConnectionTaskImpl outboundConnectionTask2 = createPartialConnectionTask(secondConfig, "OtherConnectionTask");

        assertThat(deviceType.getDeviceConfigConflictMappings()).isEmpty();
        ((ServerDeviceType) deviceType).updateConflictingMappings();
        assertThat(deviceType.getDeviceConfigConflictMappings()).hasSize(2);
        assertThat(deviceType.getDeviceConfigConflictMappings()).haveExactly(1, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                return matchConfigs(deviceConfigConflictMapping, firstConfig, secondConfig)
                        && deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().size() == 1
                        && matchPartialConnectionTasks(outboundConnectionTask1, outboundConnectionTask2, deviceConfigConflictMapping);
            }
        });
        assertThat(deviceType.getDeviceConfigConflictMappings()).haveExactly(1, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                return matchConfigs(deviceConfigConflictMapping, secondConfig, firstConfig)
                        && deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().size() == 1
                        && matchPartialConnectionTasks(outboundConnectionTask2, outboundConnectionTask1, deviceConfigConflictMapping);
            }
        });
    }

    @Test
    @Transactional
    public void resolveConflictsWhenDeviceConfigBecomesInactiveTest() {
        DeviceType deviceType = deviceConfigurationService.newDeviceType("ConflictTest", deviceProtocolPluggableClass);
        deviceType.save();

        DeviceConfiguration firstConfig = createDirectlyAddressableConfig(deviceType, "firstConfig");
        PartialScheduledConnectionTaskImpl outboundConnectionTask1 = createPartialConnectionTask(firstConfig, "FirstConnectionTask");

        DeviceConfiguration secondConfig = createDirectlyAddressableConfig(deviceType, "secondConfig");
        PartialScheduledConnectionTaskImpl outboundConnectionTask2 = createPartialConnectionTask(secondConfig, "OtherConnectionTask");
        ((ServerDeviceType) deviceType).updateConflictingMappings();
        assertThat(deviceType.getDeviceConfigConflictMappings()).hasSize(2); // what is in here is checked in another test

        secondConfig.deactivate();

        ((ServerDeviceType) deviceType).updateConflictingMappings();
        assertThat(deviceType.getDeviceConfigConflictMappings()).isEmpty();
    }

    @Test
    @Transactional
    public void resolveConnectionTaskConflictWhenRemovalOfConnectionTaskTest() {
        DeviceType deviceType = deviceConfigurationService.newDeviceType("ConflictTest", deviceProtocolPluggableClass);
        deviceType.save();

        DeviceConfiguration firstConfig = createDirectlyAddressableConfig(deviceType, "firstConfig");
        PartialScheduledConnectionTaskImpl outboundConnectionTask1 = createPartialConnectionTask(firstConfig, "FirstConnectionTask");

        DeviceConfiguration secondConfig = createDirectlyAddressableConfig(deviceType, "secondConfig");
        PartialScheduledConnectionTaskImpl outboundConnectionTask2 = createPartialConnectionTask(secondConfig, "OtherConnectionTask");

        ((ServerDeviceType) deviceType).updateConflictingMappings();
        assertThat(deviceType.getDeviceConfigConflictMappings()).hasSize(2); // what is in here is checked in another test

        deviceType.save();

        LocalEvent localEvent = mock(LocalEvent.class);
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(EventType.PARTIAL_SCHEDULED_CONNECTION_TASK_DELETED.topic());
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(outboundConnectionTask2);
        doAnswer(invocationOnMock -> {
            injector.getInstance(DeviceConfigConflictMappingHandler.class).onEvent(localEvent, outboundConnectionTask2);
            return null;
        }).when(eventService.getSpy()).postEvent(EventType.PARTIAL_SCHEDULED_CONNECTION_TASK_DELETED.topic(), outboundConnectionTask2);

        removeConnectionTaskFromConfig(secondConfig, outboundConnectionTask2);
        assertThat(deviceType.getDeviceConfigConflictMappings()).isEmpty();
    }

    @Test
    @Transactional
    public void solvedMappingsAreNotRemovedWhenNewConflictArisesTest() {
        DeviceType deviceType = deviceConfigurationService.newDeviceType("ConflictTest", deviceProtocolPluggableClass);
        deviceType.save();

        DeviceConfiguration firstConfig = createDirectlyAddressableConfig(deviceType, "firstConfig");
        PartialScheduledConnectionTaskImpl outboundConnectionTask1 = createPartialConnectionTask(firstConfig, "FirstConnectionTask");

        DeviceConfiguration secondConfig = createDirectlyAddressableConfig(deviceType, "secondConfig");
        PartialScheduledConnectionTaskImpl outboundConnectionTask2 = createPartialConnectionTask(secondConfig, "OtherConnectionTask");

        ((ServerDeviceType) deviceType).updateConflictingMappings();

        assertThat(deviceType.getDeviceConfigConflictMappings()).hasSize(2); // what is in here is checked in another test
        DeviceConfigConflictMapping deviceConfigConflictMapping1 = deviceType.getDeviceConfigConflictMappings().get(0);
        ConflictingConnectionMethodSolution conflictingConnectionMethodSolution1 = deviceConfigConflictMapping1.getConflictingConnectionMethodSolutions().get(0);
        conflictingConnectionMethodSolution1.setSolution(DeviceConfigConflictMapping.ConflictingMappingAction.ADD);

        DeviceConfigConflictMapping deviceConfigConflictMapping2 = deviceType.getDeviceConfigConflictMappings().get(1);
        ConflictingConnectionMethodSolution conflictingConnectionMethodSolution3 = deviceConfigConflictMapping2.getConflictingConnectionMethodSolutions().get(0);
        conflictingConnectionMethodSolution3.setSolution(DeviceConfigConflictMapping.ConflictingMappingAction.REMOVE);

        DeviceType reloadedDeviceType = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).hasSize(2);

        assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).areExactly(1, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                return deviceConfigConflictMapping.getConflictingConnectionMethodSolutions()
                        .get(0).getConflictingMappingAction().equals(DeviceConfigConflictMapping.ConflictingMappingAction.ADD)
                        && deviceConfigConflictMapping.isSolved();
            }
        });

        assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).areExactly(1, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                return deviceConfigConflictMapping.getConflictingConnectionMethodSolutions()
                        .get(0).getConflictingMappingAction().equals(DeviceConfigConflictMapping.ConflictingMappingAction.REMOVE)
                        && deviceConfigConflictMapping.isSolved();
            }
        });

        // Logic that we want to test: if new ConnectionMethod is added, new conflicts will be calculated. Existing solved conflicts should still remain
        DeviceConfiguration thirdConfig = createDirectlyAddressableConfig(deviceType, "ThirdConfig");
        PartialScheduledConnectionTaskImpl outboundConnectionTask3 = createPartialConnectionTask(thirdConfig, "ThirdConnectionTask");
        ((ServerDeviceType) deviceType).updateConflictingMappings(); // we call the method ourselves because we don't have the eventhandler in place

        DeviceType finalDeviceType = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        assertThat(finalDeviceType.getDeviceConfigConflictMappings()).hasSize(6);

        assertThat(finalDeviceType.getDeviceConfigConflictMappings()).areExactly(1, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                return deviceConfigConflictMapping.getConflictingConnectionMethodSolutions()
                        .get(0).getConflictingMappingAction().equals(DeviceConfigConflictMapping.ConflictingMappingAction.ADD)
                        && deviceConfigConflictMapping.isSolved();
            }
        });

        assertThat(finalDeviceType.getDeviceConfigConflictMappings()).areExactly(1, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                return deviceConfigConflictMapping.getConflictingConnectionMethodSolutions()
                        .get(0).getConflictingMappingAction().equals(DeviceConfigConflictMapping.ConflictingMappingAction.REMOVE)
                        && deviceConfigConflictMapping.isSolved();
            }
        });

        assertThat(finalDeviceType.getDeviceConfigConflictMappings()).areExactly(2, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                return deviceConfigConflictMapping.isSolved();
            }
        });

        assertThat(finalDeviceType.getDeviceConfigConflictMappings()).areExactly(4, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                return !deviceConfigConflictMapping.isSolved();
            }
        });
    }

    private void removeConnectionTaskFromConfig(DeviceConfiguration secondConfig, PartialScheduledConnectionTaskImpl outboundConnectionTask2) {
        secondConfig.remove(outboundConnectionTask2);
    }

    private DeviceConfiguration createDirectlyAddressableConfig(DeviceType deviceType, String name) {
        DeviceConfiguration firstConfig = deviceType.newConfiguration(name).add();
        firstConfig.setDirectlyAddressable(true);
        return firstConfig;
    }

    private PartialScheduledConnectionTaskImpl createPartialConnectionTask(DeviceConfiguration firstConfig, String name) {
        PartialScheduledConnectionTaskImpl outboundConnectionTask1 = firstConfig.newPartialScheduledConnectionTask(name, connectionTypePluggableClass, SIXTY_SECONDS, ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .nextExecutionSpec().temporalExpression(TimeDuration.days(1), NINETY_MINUTES).set()
                .asDefault(true).build();
        firstConfig.save();
        firstConfig.activate();
        return outboundConnectionTask1;
    }

    private boolean matchPartialConnectionTasks(PartialScheduledConnectionTaskImpl originPartialConnectionTask, PartialScheduledConnectionTaskImpl destinationPartialConnectionTask, DeviceConfigConflictMapping deviceConfigConflictMapping) {
        return deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().get(0).getOriginDataSource().getId() == originPartialConnectionTask.getId()
                && deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().get(0).getDestinationDataSource().getId() == destinationPartialConnectionTask.getId();
    }

    private boolean matchConfigs(DeviceConfigConflictMapping deviceConfigConflictMapping, DeviceConfiguration originConfig, DeviceConfiguration destinationConfig) {
        return deviceConfigConflictMapping.getOriginDeviceConfiguration().getId() == originConfig.getId()
                && deviceConfigConflictMapping.getDestinationDeviceConfiguration().getId() == destinationConfig.getId();
    }

    private PartialScheduledConnectionTask getConnectionTaskWithName(DeviceConfiguration deviceConfiguration, String connectionTaskName) {
        for (PartialScheduledConnectionTask partialScheduledConnectionTask : deviceConfiguration.getPartialOutboundConnectionTasks()) {
            if (partialScheduledConnectionTask.getName().equals(connectionTaskName)) {
                return partialScheduledConnectionTask;
            }
        }
        return null;
    }

    public interface MyDeviceProtocolPluggableClass extends DeviceProtocolPluggableClass {

    }

    public static class ConnectionTaskFinder implements CanFindByLongPrimaryKey {

        @Override
        public FactoryIds factoryId() {
            return FactoryIds.CONNECTION_TASK;
        }

        @Override
        public Class valueDomain() {
            return null;
        }

        @Override
        public Optional findByPrimaryKey(long id) {
            return null;
        }

    }

    private static void createOracleAliases(Connection connection) {
        try {
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "CREATE VIEW IF NOT EXISTS USER_TABLES AS select table_name from INFORMATION_SCHEMA.TABLES where table_schema = 'PUBLIC'"
            )) {
                preparedStatement.execute();
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "CREATE VIEW IF NOT EXISTS USER_IND_COLUMNS AS select index_name, table_name, column_name, ordinal_position AS column_position from INFORMATION_SCHEMA.INDEXES where table_schema = 'PUBLIC'"
            )) {
                preparedStatement.execute();
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS USER_SEQUENCES ( SEQUENCE_NAME VARCHAR2 (30) NOT NULL, MIN_VALUE NUMBER, MAX_VALUE NUMBER, INCREMENT_BY NUMBER NOT NULL, CYCLE_FLAG VARCHAR2 (1), ORDER_FLAG VARCHAR2 (1), CACHE_SIZE NUMBER NOT NULL, LAST_NUMBER NUMBER NOT NULL)"
            )) {
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }
}
