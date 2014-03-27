package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.TemporalExpression;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.protocols.mdc.inbound.dlms.DlmsSerialNumberDiscover;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PartialOutboundConnectiontaskCrudIT {

    private static final ComWindow COM_WINDOW = new ComWindow(3600, 7200);
    private OutboundComPortPool outboundComPortPool, outboundComPortPool1;
    private ConnectionTypePluggableClass connectionTypePluggableClass, connectionTypePluggableClass2;

    @Rule
    public final TestRule thereIsNOOOORuleNumber6 = new ExpectedConstraintViolationRule();

    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    MyDeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    DeviceProtocol deviceProtocol;

    public static final String JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME = "jupiter.bootstrap.module";

    @Mock
    private BundleContext bundleContext;
    @Mock
    private Principal principal;
    @Mock
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private OrmService ormService;
    private EventService eventService;
    private NlsService nlsService;
    private DeviceConfigurationServiceImpl deviceConfigurationService;
    private MeteringService meteringService;
    private DataModel dataModel;
    private Injector injector;
    @Mock
    private ApplicationContext applicationContext;
    private ProtocolPluggableService protocolPluggableService;
    private MdcReadingTypeUtilService readingTypeUtilService;
    private EngineModelService engineModelService;
    private InMemoryBootstrapModule bootstrapModule;
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    private InboundDeviceProtocolPluggableClass discoveryPluggable;
    @Mock
    private IdBusinessObjectFactory businessObjectFactory;

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(DataModel.class).toProvider(new Provider<DataModel>() {
                @Override
                public DataModel get() {
                    return dataModel;
                }
            });
        }

    }

    public void initializeDatabase(boolean showSqlLogging, boolean createMasterData) {
        bootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(principal),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new MeteringModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new OrmModule(),
                new MdcReadingTypeUtilServiceModule(),
                new DeviceConfigurationModule(),
                new MdcCommonModule(),
                new EngineModelModule(),
                new ProtocolPluggableModule(),
                new IssuesModule(),
                new ProtocolsModule(),
                new MdcDynamicModule(),
                new PluggableModule());
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            ormService = injector.getInstance(OrmService.class);
            eventService = injector.getInstance(EventService.class);
            nlsService = injector.getInstance(NlsService.class);
            meteringService = injector.getInstance(MeteringService.class);
            readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
            engineModelService = injector.getInstance(EngineModelService.class);
            protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            inboundDeviceProtocolService = injector.getInstance(InboundDeviceProtocolService.class);
            injector.getInstance(PluggableService.class);
            deviceConfigurationService = (DeviceConfigurationServiceImpl) injector.getInstance(DeviceConfigurationService.class);
            ctx.commit();
        }
        Environment environment = injector.getInstance(Environment.class);
        environment.put(InMemoryPersistence.JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME, bootstrapModule, true);
        environment.setApplicationContext(applicationContext);
    }

    @Before
    public void setUp() {
        when(principal.getName()).thenReturn("test");
        Translator translator = mock(Translator.class);
        when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(applicationContext.getTranslator()).thenReturn(translator);
        when(applicationContext.findFactory(5011)).thenReturn(businessObjectFactory);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);

        initializeDatabase(false, false);
        protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
        engineModelService = injector.getInstance(EngineModelService.class);

        try (TransactionContext context = transactionService.getContext()) {
            connectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType", NoParamsConnectionType.class.getName());
            connectionTypePluggableClass.save();
            connectionTypePluggableClass2 = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType2", NoParamsConnectionType.class.getName());
            connectionTypePluggableClass2.save();
            discoveryPluggable = protocolPluggableService.newInboundDeviceProtocolPluggableClass("MyDiscoveryName", DlmsSerialNumberDiscover.class.getName());
            discoveryPluggable.save();
            outboundComPortPool = engineModelService.newOutboundComPortPool();
            outboundComPortPool.setActive(true);
            outboundComPortPool.setComPortType(ComPortType.TCP);
            outboundComPortPool.setName("inboundComPortPool");
            outboundComPortPool.setTaskExecutionTimeout(TimeDuration.minutes(15));
            outboundComPortPool.save();
            outboundComPortPool1 = engineModelService.newOutboundComPortPool();
            outboundComPortPool1.setActive(true);
            outboundComPortPool1.setComPortType(ComPortType.TCP);
            outboundComPortPool1.setName("inboundComPortPool2");
            outboundComPortPool1.setTaskExecutionTimeout(TimeDuration.minutes(5));
            outboundComPortPool1.save();
            context.commit();
        }
    }

    @After
    public void tearDown() {
        bootstrapModule.deactivate();
    }

    @Test
    public void testCreate() {

        PartialOutboundConnectionTask outboundConnectionTask;
        DeviceCommunicationConfiguration communicationConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(deviceConfiguration);
            communicationConfiguration.save();

            outboundConnectionTask = communicationConfiguration.createPartialOutboundConnectionTask()
                    .name("MyOutbound")
                    .comPortPool(outboundComPortPool)
                    .pluggableClass(connectionTypePluggableClass)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)).set()
                    .rescheduleDelay(TimeDuration.seconds(60))
                    .connectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .asDefault(true).build();
            communicationConfiguration.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found).isPresent();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialOutboundConnectionTask.class);

        PartialOutboundConnectionTask partialOutboundConnectionTask = (PartialOutboundConnectionTask) partialConnectionTask;

        assertThat(partialOutboundConnectionTask.getComPortPool().getId()).isEqualTo(outboundComPortPool.getId());
        assertThat(partialOutboundConnectionTask.isDefault()).isTrue();
        assertThat(partialOutboundConnectionTask.getConfiguration().getId()).isEqualTo(communicationConfiguration.getId());
        assertThat(partialOutboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass.getConnectionType());
        assertThat(partialOutboundConnectionTask.getCommunicationWindow()).isEqualTo(COM_WINDOW);
        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs().getTemporalExpression()).isEqualTo(new TemporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)));
        assertThat(partialOutboundConnectionTask.getName()).isEqualTo("MyOutbound");
        assertThat(partialOutboundConnectionTask.getRescheduleDelay()).isEqualTo(TimeDuration.seconds(60));
        assertThat(partialOutboundConnectionTask.getConnectionStrategy()).isEqualTo(ConnectionStrategy.MINIMIZE_CONNECTIONS);

    }

//            partialInboundConnectionTask.setName("Changed");
    @Test
    public void testUpdate() {

        PartialOutboundConnectionTask outboundConnectionTask;
        DeviceCommunicationConfiguration communicationConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(deviceConfiguration);
            communicationConfiguration.save();

            outboundConnectionTask = communicationConfiguration.createPartialOutboundConnectionTask()
                    .name("MyOutbound")
                    .comPortPool(outboundComPortPool)
                    .pluggableClass(connectionTypePluggableClass)
                    .comWindow(COM_WINDOW)
                    .rescheduleDelay(TimeDuration.seconds(60))
                    .connectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                    .asDefault(true).build();
            communicationConfiguration.save();

            context.commit();
        }

        ComWindow newComWindow = new ComWindow(7200, 10800);
        try (TransactionContext context = transactionService.getContext()) {
            DeviceCommunicationConfiguration configuration = deviceConfigurationService.findDeviceConfigurationService(communicationConfiguration.getId());
            PartialOutboundConnectionTask partialOutboundConnectionTask = configuration.getPartialOutboundConnectionTasks().get(0);
            partialOutboundConnectionTask.setDefault(false);
            partialOutboundConnectionTask.setComportPool(outboundComPortPool1);
            partialOutboundConnectionTask.setConnectionTypePluggableClass(connectionTypePluggableClass2);
            partialOutboundConnectionTask.setComWindow(newComWindow);
            partialOutboundConnectionTask.setName("Changed");
            partialOutboundConnectionTask.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found).isPresent();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialOutboundConnectionTask.class);

        PartialOutboundConnectionTask partialOutboundConnectionTask = (PartialOutboundConnectionTask) partialConnectionTask;

        assertThat(partialOutboundConnectionTask.getComPortPool().getId()).isEqualTo(outboundComPortPool1.getId());
        assertThat(partialOutboundConnectionTask.isDefault()).isFalse();
        assertThat(partialOutboundConnectionTask.getConfiguration().getId()).isEqualTo(communicationConfiguration.getId());
        assertThat(partialOutboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass2.getConnectionType());
        assertThat(partialOutboundConnectionTask.getCommunicationWindow()).isEqualTo(newComWindow);
        assertThat(partialOutboundConnectionTask.getName()).isEqualTo("Changed");

    }

    @Test
    public void testDelete() {
        PartialOutboundConnectionTask outboundConnectionTask;
        DeviceCommunicationConfiguration communicationConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(deviceConfiguration);
            communicationConfiguration.save();

            outboundConnectionTask = communicationConfiguration.createPartialOutboundConnectionTask()
                    .name("MyOutbound")
                    .comPortPool(outboundComPortPool)
                    .pluggableClass(connectionTypePluggableClass)
                    .comWindow(COM_WINDOW)
                    .rescheduleDelay(TimeDuration.seconds(60))
                    .connectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                    .asDefault(true).build();
            communicationConfiguration.save();

            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            DeviceCommunicationConfiguration configuration = deviceConfigurationService.findDeviceConfigurationService(communicationConfiguration.getId());
            PartialOutboundConnectionTask partialOutboundConnectionTask = configuration.getPartialOutboundConnectionTasks().get(0);
            configuration.remove(partialOutboundConnectionTask);
            configuration.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found).isAbsent();

    }

    @Test
    public void testCreateReferencingInitiation() {

        PartialConnectionInitiationTask connectionInitiationTask;
        DeviceCommunicationConfiguration communicationConfiguration;
        DeviceType deviceType;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(deviceConfiguration);
            communicationConfiguration.save();

            connectionInitiationTask = communicationConfiguration.createPartialConnectionInitiationTask()
                    .name("MyInitiation")
                    .comPortPool(outboundComPortPool)
                    .pluggableClass(connectionTypePluggableClass)
                    .rescheduleDelay(TimeDuration.seconds(60))
                    .build();
            communicationConfiguration.save();

            context.commit();
        }

        PartialOutboundConnectionTask outboundConnectionTask;
        try (TransactionContext context = transactionService.getContext()) {
            communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(deviceConfiguration);
            communicationConfiguration.save();

            outboundConnectionTask = communicationConfiguration.createPartialOutboundConnectionTask()
                    .name("MyOutbound")
                    .comPortPool(outboundComPortPool)
                    .pluggableClass(connectionTypePluggableClass)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)).set()
                    .initiatonTask(connectionInitiationTask)
                    .connectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .rescheduleDelay(TimeDuration.seconds(60))
                    .asDefault(true).build();
            communicationConfiguration.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(outboundConnectionTask.getId());
        assertThat(found).isPresent();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialOutboundConnectionTask.class);

        PartialOutboundConnectionTask partialOutboundConnectionTask = (PartialOutboundConnectionTask) partialConnectionTask;

        assertThat(partialOutboundConnectionTask.getComPortPool().getId()).isEqualTo(outboundComPortPool.getId());
        assertThat(partialOutboundConnectionTask.isDefault()).isTrue();
        assertThat(partialOutboundConnectionTask.getConfiguration().getId()).isEqualTo(communicationConfiguration.getId());
        assertThat(partialOutboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass.getConnectionType());
        assertThat(partialOutboundConnectionTask.getCommunicationWindow()).isEqualTo(COM_WINDOW);
        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(partialOutboundConnectionTask.getNextExecutionSpecs().getTemporalExpression()).isEqualTo(new TemporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)));
        assertThat(partialOutboundConnectionTask.getInitiatorTask().getId()).isEqualTo(connectionInitiationTask.getId());
        assertThat(partialOutboundConnectionTask.getName()).isEqualTo("MyOutbound");

    }

    @Test
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Constants.CONNECTION_STRATEGY_REQUIRED_KEY + '}')
    public void testCreateWithoutConnectionStrategy() {

        PartialOutboundConnectionTask outboundConnectionTask;
        DeviceCommunicationConfiguration communicationConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(deviceConfiguration);
            communicationConfiguration.save();

            outboundConnectionTask = communicationConfiguration.createPartialOutboundConnectionTask()
                    .name("MyOutbound")
                    .comPortPool(outboundComPortPool)
                    .pluggableClass(connectionTypePluggableClass)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)).set()
                    .rescheduleDelay(TimeDuration.seconds(60))
                    .asDefault(true).build();
            communicationConfiguration.save();

            context.commit();
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Constants.NEXT_EXECUTION_SPEC_REQUIRED_FOR_MINIMIZE_CONNECTIONS_KEY + '}')
    public void testCreateMinimizingConnectionsWithoutNextExecutionSpecs() {

        PartialOutboundConnectionTask outboundConnectionTask;
        DeviceCommunicationConfiguration communicationConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(deviceConfiguration);
            communicationConfiguration.save();

            outboundConnectionTask = communicationConfiguration.createPartialOutboundConnectionTask()
                    .name("MyOutbound")
                    .comPortPool(outboundComPortPool)
                    .pluggableClass(connectionTypePluggableClass)
                    .comWindow(COM_WINDOW)
                    .rescheduleDelay(TimeDuration.seconds(60))
                    .connectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .asDefault(true).build();
            communicationConfiguration.save();

            context.commit();
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Constants.NEXT_EXECUTION_SPEC_INVALID_FOR_COM_WINDOW_KEY + '}')
    public void testCreateWithNextExecutionSpecsOffsetNotWithinComWindowTest() {

        PartialOutboundConnectionTask outboundConnectionTask;
        DeviceCommunicationConfiguration communicationConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(deviceConfiguration);
            communicationConfiguration.save();

            outboundConnectionTask = communicationConfiguration.createPartialOutboundConnectionTask()
                    .name("MyOutbound")
                    .comPortPool(outboundComPortPool)
                    .pluggableClass(connectionTypePluggableClass)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.hours(15), TimeDuration.hours(4)).set()
                    .rescheduleDelay(TimeDuration.seconds(60))
                    .connectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .asDefault(true).build();
            communicationConfiguration.save();

            context.commit();
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Constants.UNDER_MINIMUM_RESCHEDULE_DELAY_KEY + '}')
    public void testCreateWithTooLowReschedulingRetryDelayTest() {

        PartialOutboundConnectionTask outboundConnectionTask;
        DeviceCommunicationConfiguration communicationConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(deviceConfiguration);
            communicationConfiguration.save();

            outboundConnectionTask = communicationConfiguration.createPartialOutboundConnectionTask()
                    .name("MyOutbound")
                    .comPortPool(outboundComPortPool)
                    .pluggableClass(connectionTypePluggableClass)
                    .comWindow(COM_WINDOW)
                    .nextExecutionSpec().temporalExpression(TimeDuration.days(1), TimeDuration.minutes(90)).set()
                    .rescheduleDelay(TimeDuration.seconds(59))
                    .connectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS)
                    .asDefault(true).build();
            communicationConfiguration.save();

            context.commit();
        }
    }


    public interface MyDeviceProtocolPluggableClass extends DeviceProtocolPluggableClass {
    }

}
