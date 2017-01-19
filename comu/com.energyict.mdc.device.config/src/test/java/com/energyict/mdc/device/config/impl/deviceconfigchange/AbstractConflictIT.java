package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
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
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.config.impl.DeviceConfigurationServiceImpl;
import com.energyict.mdc.device.config.impl.InboundNoParamsConnectionTypeImpl;
import com.energyict.mdc.device.config.impl.IpConnectionType;
import com.energyict.mdc.device.config.impl.OutboundNoParamsConnectionTypeImpl;
import com.energyict.mdc.device.config.impl.PartialOutboundConnectionTaskCrudIT;
import com.energyict.mdc.device.config.impl.ServerDeviceType;
import com.energyict.mdc.device.config.impl.SpyEventService;
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
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.SQLException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 15.09.15
 * Time: 10:16
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractConflictIT {
    public static final TimeDuration FIFTEEN_MINUTES = TimeDuration.minutes(15);

    @Rule
    public final TestRule thereIsNOOOORuleNumber6 = new ExpectedConstraintViolationRule();
    @Rule
    public final TestRule transactional = new TransactionalRule(transactionService);

    static InMemoryBootstrapModule bootstrapModule;
    static EventAdmin eventAdmin;
    static BundleContext bundleContext;
    static LicenseService licenseService;
    static TransactionService transactionService;
    static EngineConfigurationService engineConfigurationService;
    static ProtocolPluggableService protocolPluggableService;
    static DeviceConfigurationServiceImpl deviceConfigurationService;
    static ConnectionTypePluggableClass connectionTypePluggableClass, connectionTypePluggableClass2;
    static LicensedProtocolService licensedProtocolService;
    static ConnectionTypeService connectionTypeService;
    static OutboundComPortPool outboundComPortPool, outboundComPortPool1;
    static SpyEventService eventService;
    static SchedulingService schedulingService;
    static Injector injector = null;

    @Mock
    DeviceProtocol deviceProtocol;

    @Mock
    DeviceProtocolPluggableClass deviceProtocolPluggableClass;

    public DeviceType getReloadedDeviceType(ServerDeviceType deviceType) {
        return deviceConfigurationService.findDeviceType(deviceType.getId()).get();
    }

    private static class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @BeforeClass
    public static void initializeDatabase() {
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
                    new KpiModule(),
                    new ValidationModule(),
                    new EstimationModule(),
                    new MeteringGroupsModule(),
                    new SearchModule(),
                    new TaskModule(),
                    new IssuesModule(),
                    new BasicPropertiesModule(),
                    new MdcDynamicModule(),
                    new PluggableModule(),
                    new SchedulingModule(),
                    new TimeModule(),
                    new CustomPropertySetsModule(),
                    new CalendarModule());
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
            FiniteStateMachineService finiteStateMachineService = injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringService.class);
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(MdcReadingTypeUtilService.class);
            engineConfigurationService = injector.getInstance(EngineConfigurationService.class);
            protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            protocolPluggableService.addLicensedProtocolService(licensedProtocolService);
            protocolPluggableService.addConnectionTypeService(connectionTypeService);
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
                    injector.getInstance(com.elster.jupiter.properties.PropertySpecService.class),
                    injector.getInstance(MeteringService.class),
                    injector.getInstance(MdcReadingTypeUtilService.class),
                    injector.getInstance(UserService.class),
                    injector.getInstance(PluggableService.class),
                    protocolPluggableService,
                    engineConfigurationService,
                    schedulingService,
                    injector.getInstance(ValidationService.class),
                    injector.getInstance(EstimationService.class),
                    injector.getInstance(MasterDataService.class),
                    finiteStateMachineService,
                    injector.getInstance(DeviceLifeCycleConfigurationService.class),
                    injector.getInstance(CalendarService.class),
                    injector.getInstance(CustomPropertySetService.class),
                    UpgradeModule.FakeUpgradeService.getInstance());
            ctx.commit();
        }
        setupMasterData();
        enhanceEventServiceForConflictCalculation();
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
            outboundComPortPool.update();
            outboundComPortPool1 = engineConfigurationService.newOutboundComPortPool("inboundComPortPool2", ComPortType.TCP, TimeDuration.minutes(5));
            outboundComPortPool1.setActive(true);
            outboundComPortPool1.update();
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

    private static void enhanceEventServiceForConflictCalculation() {
        doAnswer(invocationOnMock -> {
            LocalEvent localEvent = mock(LocalEvent.class);
            com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
            when(eventType.getTopic()).thenReturn((String) invocationOnMock.getArguments()[0]);
            when(localEvent.getType()).thenReturn(eventType);
            when(localEvent.getSource()).thenReturn(invocationOnMock.getArguments()[1]);
            injector.getInstance(DeviceConfigConflictMappingHandler.class).onEvent(localEvent);
            return null;
        }).when(eventService.getSpy()).postEvent(any(), any());
    }


    void verifyConflictValidation(VerificationMode mode) {
        final DeviceConfigConflictMappingImpl deviceConfigConflictMapping = mock(DeviceConfigConflictMappingImpl.class);
        verifyConflictValidation(mode, deviceConfigConflictMapping);
    }

    void verifyConflictValidation(VerificationMode mode, DeviceConfigConflictMapping deviceConfigConflictMapping) {
        verify(eventService.getSpy(), mode).postEvent(EventType.DEVICE_CONFIG_CONFLICT_VALIDATE_CREATE.topic(), deviceConfigConflictMapping);
    }

}