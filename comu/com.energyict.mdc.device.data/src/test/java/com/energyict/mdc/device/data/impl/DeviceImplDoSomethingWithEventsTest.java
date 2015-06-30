package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiServiceImpl;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.CommunicationTaskServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskServiceImpl;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Scopes;
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
import org.osgi.service.log.LogService;

import java.security.Principal;
import java.sql.SQLException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
    private static final String DEVICE_NAME = "deviceName";
    private static final String MRID = "MyUniquemRID";

    private DeviceType deviceType;
    private DeviceConfiguration deviceConfiguration;

    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    DeviceProtocol deviceProtocol;

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
        deviceConfiguration.activate();
    }

    @After
    public void initAfter() {
        reset(((EventInMemoryPersistence.SpyEventService) inMemoryPersistence.getEventService()).getSpyEventService());
    }

    private Device createSimpleDevice() {
        return createSimpleDeviceWithName(DEVICE_NAME);
    }

    private Device createSimpleDeviceWithName(String name) {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, MRID);
        device.save();
        return device;
    }

    private Device getReloadedDevice(Device device) {
        return inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();
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

        private InMemoryBootstrapModule bootstrapModule;
        private BundleContext bundleContext;
        private Principal principal;
        private EventAdmin eventAdmin;
        private TransactionService transactionService;
        private OrmService ormService;
        private EventService eventService;
        private NlsService nlsService;
        private ValidationService validationService;
        private EstimationService estimationService;
        private DeviceConfigurationService deviceConfigurationService;
        private MeteringService meteringService;
        private DataModel dataModel;
        private ProtocolPluggableService protocolPluggableService;
        private MdcReadingTypeUtilService readingTypeUtilService;
        private DeviceDataModelService deviceDataModelService;
        private IdentificationServiceImpl identificationService;
        private Clock clock = Clock.systemDefaultZone();
        private RelationService relationService;
        private EngineConfigurationService engineConfigurationService;
        private SchedulingService schedulingService;
        private LicenseService licenseService;

        public void initializeDatabase(String testName, boolean showSqlLogging) {
            this.initializeMocks(testName);
            this.bootstrapModule = new InMemoryBootstrapModule();
            Injector injector = Guice.createInjector(
                    new MockModule(),
                    this.bootstrapModule,
                    new ThreadSecurityModule(this.principal),
                    new PubSubModule(),
                    new TransactionModule(showSqlLogging),
                    new EventsModule(),
                    new NlsModule(),
                    new DomainUtilModule(),
                    new UtilModule(clock),
                    new PartyModule(),
                    new UserModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
                    new MeteringModule(false),
                    new InMemoryMessagingModule(),
                    new OrmModule(),
                    new DataVaultModule(),
                    new IssuesModule(),
                    new MdcReadingTypeUtilServiceModule(),
                    new BasicPropertiesModule(),
                    new MdcDynamicModule(),
                    new PluggableModule(),
                    new ProtocolPluggableModule(),
                    new EngineModelModule(),
                    new MasterDataModule(),
                    new ValidationModule(),
                    new EstimationModule(),
                    new TimeModule(),
                    new DeviceLifeCycleConfigurationModule(),
                    new DeviceConfigurationModule(),
                    new MdcIOModule(),
                    new ProtocolApiModule(),
                    new KpiModule(),
                    new MeteringGroupsModule(),
                    new TaskModule(),
                    new TasksModule(),
                    new SchedulingModule());
            this.transactionService = injector.getInstance(TransactionService.class);
            try (TransactionContext ctx = this.transactionService.getContext()) {
                this.ormService = injector.getInstance(OrmService.class);
                this.transactionService = injector.getInstance(TransactionService.class);
                this.eventService = new SpyEventService(injector.getInstance(EventService.class));
                this.nlsService = injector.getInstance(NlsService.class);
                injector.getInstance(FiniteStateMachineService.class);
                this.meteringService = injector.getInstance(MeteringService.class);
                injector.getInstance(MeteringGroupsService.class);
                this.readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
                injector.getInstance(MasterDataService.class);
                this.validationService = injector.getInstance(ValidationService.class);
                this.estimationService = injector.getInstance(EstimationService.class);
                this.deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
                this.engineConfigurationService = injector.getInstance(EngineConfigurationService.class);
                this.relationService = injector.getInstance(RelationService.class);
                this.protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
                this.schedulingService = injector.getInstance(SchedulingService.class);
                this.deviceDataModelService =
                        new DeviceDataModelServiceImpl(
                                this.bundleContext,
                                this.ormService, this.eventService,
                                this.nlsService, this.clock,
                                injector.getInstance(KpiService.class),
                                injector.getInstance(TaskService.class),
                                mock(IssueService.class),
                                this.relationService, this.protocolPluggableService, this.engineConfigurationService,
                                this.deviceConfigurationService, this.meteringService, this.validationService, this.estimationService, this.schedulingService,
                                injector.getInstance(MessageService.class),
                                injector.getInstance(SecurityPropertyService.class),
                                injector.getInstance(UserService.class),
                                injector.getInstance(DeviceMessageSpecificationService.class));
                this.dataModel = this.deviceDataModelService.dataModel();
                ctx.commit();
            }
        }

        private void initializeMocks(String testName) {
            this.bundleContext = mock(BundleContext.class);
            this.eventAdmin = mock(EventAdmin.class);
            this.principal = mock(Principal.class);
            when(this.principal.getName()).thenReturn(testName);
            this.protocolPluggableService = mock(ProtocolPluggableService.class);
            this.licenseService = mock(LicenseService.class);
            when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.<License>empty());
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

        public ServerDeviceService getDeviceService() {
            return deviceDataModelService.deviceService();
        }

        public EventService getEventService() {
            return eventService;
        }

        private class MockModule extends AbstractModule {

            @Override
            protected void configure() {
                bind(BpmService.class).toInstance(mock(BpmService.class));
                bind(com.elster.jupiter.issue.share.service.IssueService.class).toInstance(mock(com.elster.jupiter.issue.share.service.IssueService.class));
                bind(EventAdmin.class).toInstance(eventAdmin);
                bind(BundleContext.class).toInstance(bundleContext);
                bind(LicenseService.class).toInstance(licenseService);
                bind(LogService.class).toInstance(mock(LogService.class));

                bind(SecurityPropertyService.class).to(SecurityPropertyServiceImpl.class).in(Scopes.SINGLETON);
                bind(ConnectionTaskService.class).to(ConnectionTaskServiceImpl.class).in(Scopes.SINGLETON);
                bind(CommunicationTaskService.class).to(CommunicationTaskServiceImpl.class).in(Scopes.SINGLETON);
                bind(LoadProfileService.class).to(LoadProfileServiceImpl.class).in(Scopes.SINGLETON);
                bind(LogBookService.class).to(LogBookServiceImpl.class).in(Scopes.SINGLETON);
                bind(DataCollectionKpiService.class).to(DataCollectionKpiServiceImpl.class).in(Scopes.SINGLETON);
                bind(DeviceDataModelService.class).toProvider(new Provider<DeviceDataModelService>() {
                    @Override
                    public DeviceDataModelService get() {
                        return deviceDataModelService;
                    }
                });
                bind(IdentificationServiceImpl.class).toProvider(new Provider<IdentificationServiceImpl>() {
                    @Override
                    public IdentificationServiceImpl get() {
                        return identificationService;
                    }
                });
                bind(DataModel.class).toProvider(new Provider<DataModel>() {
                    @Override
                    public DataModel get() {
                        return dataModel;
                    }
                });
                bind(IdentificationService.class).to(IdentificationServiceImpl.class).in(Scopes.SINGLETON);
            }

        }

        public static class SpyEventService implements EventService {

            private final EventService eventService;

            public EventService getSpyEventService() {
                return eventService;
            }

            private SpyEventService(EventService realEventService) {
                this.eventService = spy(realEventService);
            }

            @Override
            public List<EventType> getEventTypesForComponent(String component) {
                return this.eventService.getEventTypesForComponent(component);
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
