package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.impl.DefaultClock;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.BusinessEventManager;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import java.security.Principal;
import java.sql.SQLException;
import java.util.List;
import javax.inject.Inject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 25/03/14
 * Time: 10:42
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceImplDoSomethingWithEventsTest {

    private static EventInMemoryPersistence inMemoryPersistence = new EventInMemoryPersistence();

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

    private static final String DEVICE_TYPE_NAME = DeviceImplDoSomethingWithEventsTest.class.getName() + "Type";
    private static final String DEVICE_CONFIGURATION_NAME = DeviceImplDoSomethingWithEventsTest.class.getName() + "Config";
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    private static final String DEVICENAME = "deviceName";
    private static final String MRID = "MyUniquemRID";

    private DeviceType deviceType;
    private DeviceConfiguration deviceConfiguration;

    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    DeviceProtocol deviceProtocol;
    private static Injector injector;
    private static Environment environment;

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = new EventInMemoryPersistence();
        inMemoryPersistence.initializeDatabase("PersistenceTest.mdc.device.data", false);
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @Before
    public void initializeMocks() {
        when(deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceType.save();
    }

    @After
    public void initAfter() {
        reset(((EventInMemoryPersistence.SpyEventService) inMemoryPersistence.getEventService()).getSpyEventService());
    }

    private Device createSimpleDevice() {
        return createSimpleDeviceWithName(DEVICENAME);
    }

    private Device createSimpleDeviceWithName(String name) {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, MRID);
        device.save();
        return device;
    }

    private Device getReloadedDevice(Device device) {
        return inMemoryPersistence.getDeviceService().findDeviceById(device.getId());
    }

    @Test
    @Transactional
    public void createEventTest() {
        EventInMemoryPersistence.SpyEventService eventService = (EventInMemoryPersistence.SpyEventService) inMemoryPersistence.getEventService();
        Device simpleDevice = createSimpleDevice();

        verify(eventService.getSpyEventService(), times(1)).postEvent(CreateEventType.DEVICE.topic(), simpleDevice);
        verify(eventService.getSpyEventService(), never()).postEvent(UpdateEventType.DEVICE.topic(), simpleDevice);
    }

    @Test
    @Transactional
    public void updateEventTest() {
        EventInMemoryPersistence.SpyEventService eventService = (EventInMemoryPersistence.SpyEventService) inMemoryPersistence.getEventService();
        Device simpleDevice = createSimpleDevice();
        Device reloadedDevice = getReloadedDevice(simpleDevice);
        reloadedDevice.setName("MyOtherName");
        reloadedDevice.save();

        verify(eventService.getSpyEventService(), times(1)).postEvent(CreateEventType.DEVICE.topic(), simpleDevice);
        verify(eventService.getSpyEventService(), times(1)).postEvent(UpdateEventType.DEVICE.topic(), reloadedDevice);
    }

    @Test
    @Transactional
    public void deleteEventTest() {
        EventInMemoryPersistence.SpyEventService eventService = (EventInMemoryPersistence.SpyEventService) inMemoryPersistence.getEventService();
        Device simpleDevice = createSimpleDevice();
        simpleDevice.delete();

        verify(eventService.getSpyEventService(), times(1)).postEvent(CreateEventType.DEVICE.topic(), simpleDevice);
        verify(eventService.getSpyEventService(), never()).postEvent(UpdateEventType.DEVICE.topic(), simpleDevice);
        verify(eventService.getSpyEventService(), times(1)).postEvent(DeleteEventType.DEVICE.topic(), simpleDevice);
    }

    private static class EventInMemoryPersistence {

        public static final String JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME = "jupiter.bootstrap.module";

        private InMemoryBootstrapModule bootstrapModule;
        private BundleContext bundleContext;
        private Principal principal;
        private EventAdmin eventAdmin;
        private TransactionService transactionService;
        private OrmService ormService;
        private EventService eventService;
        private NlsService nlsService;
        private DeviceConfigurationService deviceConfigurationService;
        private MeteringService meteringService;
        private DataModel dataModel;
        private ApplicationContext applicationContext;
        private ProtocolPluggableService protocolPluggableService;
        private MdcReadingTypeUtilService readingTypeUtilService;
        private DeviceDataServiceImpl deviceService;
        private Clock clock = new DefaultClock();
        private RelationService relationService;
        private EngineModelService engineModelService;
        private Environment environment;
        private TaskService taskService;

        public void initializeDatabase(String testName, boolean showSqlLogging) {
            this.initializeMocks(testName);
            this.bootstrapModule = new InMemoryBootstrapModule();
            Injector injector = Guice.createInjector(
                    new MockModule(),
                    this.bootstrapModule,
                    new ThreadSecurityModule(this.principal),
                    new PubSubModule(),
                    new TransactionModule(showSqlLogging),
                    new NlsModule(),
                    new DomainUtilModule(),
                    new PartyModule(),
                    new UserModule(),
                    new IdsModule(),
                    new MeteringModule(),
                    new InMemoryMessagingModule(),
                    new OrmModule(),
                    new IssuesModule(),
                    new ProtocolsModule(),
                    new MdcReadingTypeUtilServiceModule(),
                    new MdcDynamicModule(),
                    new PluggableModule(),
                    new ProtocolPluggableModule(),
                    new EngineModelModule(),
                    new MasterDataModule(),
                    new DeviceConfigurationModule(),
                    new MdcCommonModule(),
                    new TasksModule(),
                    new DeviceDataModule());
            BusinessEventManager eventManager = mock(BusinessEventManager.class);
            when(this.applicationContext.createEventManager()).thenReturn(eventManager);
            this.transactionService = injector.getInstance(TransactionService.class);
            this.environment = injector.getInstance(Environment.class);
            this.environment.put(InMemoryIntegrationPersistence.JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME, this.bootstrapModule, true);
            this.environment.setApplicationContext(this.applicationContext);
            try (TransactionContext ctx = this.transactionService.getContext()) {
                this.ormService = injector.getInstance(OrmService.class);
                this.transactionService = injector.getInstance(TransactionService.class);
                this.eventService = injector.getInstance(EventService.class);
                this.nlsService = injector.getInstance(NlsService.class);
                this.meteringService = injector.getInstance(MeteringService.class);
                this.readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
                injector.getInstance(MasterDataService.class);
                this.taskService = injector.getInstance(TaskService.class);
                this.deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
                this.engineModelService = injector.getInstance(EngineModelService.class);
                this.relationService = injector.getInstance(RelationService.class);
                this.protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
                this.dataModel = this.createNewDeviceDataService();
                ctx.commit();
            }
        }

        private DataModel createNewDeviceDataService() {
            this.deviceService = new DeviceDataServiceImpl(this.ormService, this.eventService, this.nlsService, this.clock, environment, relationService, protocolPluggableService, engineModelService, this.deviceConfigurationService, meteringService);
            return this.deviceService.getDataModel();
        }

        public void run(DataModelInitializer... dataModelInitializers) {
            try (TransactionContext ctx = this.transactionService.getContext()) {
                for (DataModelInitializer initializer : dataModelInitializers) {
                    initializer.initializeDataModel(this.dataModel);
                }
                ctx.commit();
            }
        }

        private void initializeMocks(String testName) {
            this.bundleContext = mock(BundleContext.class);
            this.eventAdmin = mock(EventAdmin.class);
            this.principal = mock(Principal.class);
            when(this.principal.getName()).thenReturn(testName);
            this.protocolPluggableService = mock(ProtocolPluggableService.class);
            this.applicationContext = mock(ApplicationContext.class);
            Translator translator = mock(Translator.class);
            when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
            when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
            when(this.applicationContext.getTranslator()).thenReturn(translator);
        }

        public void cleanUpDataBase() throws SQLException {
            this.bootstrapModule.deactivate();
        }

        public MeteringService getMeteringService() {
            return meteringService;
        }

        public DeviceConfigurationService getDeviceConfigurationService() {
            return deviceConfigurationService;
        }

        public TransactionService getTransactionService() {
            return transactionService;
        }

        public ProtocolPluggableService getProtocolPluggableService() {
            return protocolPluggableService;
        }

        public MdcReadingTypeUtilService getReadingTypeUtilService() {
            return readingTypeUtilService;
        }

        public ApplicationContext getApplicationContext() {
            return applicationContext;
        }

        public DeviceDataServiceImpl getDeviceService() {
            return deviceService;
        }

        public EventService getEventService() {
            return eventService;
        }

        private class MockModule extends AbstractModule {

            @Override
            protected void configure() {
                bind(JsonService.class).toInstance(new JsonServiceImpl());
                bind(BeanService.class).toInstance(new BeanServiceImpl());
                bind(Clock.class).toInstance(clock);
                bind(EventAdmin.class).toInstance(eventAdmin);
                bind(BundleContext.class).toInstance(bundleContext);
//                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(EventService.class).to(SpyEventService.class).in(Scopes.SINGLETON);
                bind(DataModel.class).toProvider(new Provider<DataModel>() {
                    @Override
                    public DataModel get() {
                        return dataModel;
                    }
                });
            }

        }

        public static class SpyEventService implements EventService {

            private final EventService eventService;

            public EventService getSpyEventService() {
                return eventService;
            }

            @Inject
            private SpyEventService(Clock clock, JsonService jsonService, Publisher publisher, BeanService beanService, OrmService ormService1, MessageService messageService, BundleContext bundleContext1, EventAdmin eventAdmin1, NlsService nlsService1) {
                this.eventService = spy(new EventServiceImpl(clock, jsonService, publisher, beanService, ormService1, messageService, bundleContext1, eventAdmin1, nlsService1));
            }

            @Override
            public void postEvent(String topic, Object source) {
                eventService.postEvent(topic, source);
            }

            @Override
            @TransactionRequired
            public EventTypeBuilder buildEventTypeWithTopic(String topic) {
                return eventService.buildEventTypeWithTopic(topic);
            }

            @Override
            public List<com.elster.jupiter.events.EventType> getEventTypes() {
                return eventService.getEventTypes();
            }

            @Override
            public Optional<EventType> getEventType(String topic) {
                return eventService.getEventType(topic);
            }
        }

    }

}
