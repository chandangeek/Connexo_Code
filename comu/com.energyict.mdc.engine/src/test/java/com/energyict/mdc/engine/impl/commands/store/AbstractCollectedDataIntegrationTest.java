package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.impl.ServiceCallModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.impl.TopologyModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.engine.impl.EngineModule;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.firmware.impl.FirmwareModule;
import com.energyict.mdc.io.serial.SerialComponentService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.device.DeviceFactory;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 15/01/14
 * Time: 15:02
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractCollectedDataIntegrationTest {

    private static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
    private static final Clock clock = mock(Clock.class);
    private static Injector injector;
    private static InMemoryBootstrapModule bootstrapModule;
    private static MeteringService meteringService;
    private static MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private static MasterDataService masterDataService;
    private static TopologyService topologyService;
    private static TransactionService transactionService;
    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Mock
    private DeviceFactory deviceFactory;

    @BeforeClass
    public static void initializeDatabase() {
        initializeClock();
        BundleContext bundleContext = mock(BundleContext.class);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("InMemoryPersistence");
        LicenseService licenseService = mock(LicenseService.class);
        bootstrapModule = new InMemoryBootstrapModule();
        EventAdmin eventAdmin = mock(EventAdmin.class);
        injector = Guice.createInjector(
                new MockModule(bundleContext, eventAdmin, licenseService),
                bootstrapModule,
                new UtilModule(clock),
                new ThreadSecurityModule(principal),
                new PubSubModule(),
                new TransactionModule(),
                new DomainUtilModule(),
                new NlsModule(),
                new WebServicesModule(),
                new AppServiceModule(),
                new UserModule(),
                new FiniteStateMachineModule(),
                new ServiceCallModule(),
                new CustomPropertySetsModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new MeteringModule(
                        "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0",

                        "0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0",

                        "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0",
                        "0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0"

                ),
                new MeteringGroupsModule(),
                new SearchModule(),
                new OrmModule(),
                new DataVaultModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new IdsModule(),
                new EventsModule(),
                new PartyModule(),
                new InMemoryMessagingModule(),
                new ProtocolApiModule(),
                new ProtocolPluggableModule(),
                new EngineModelModule(),
                new EngineModule(),
                new ProtocolApiModule(),
                new PluggableModule(),
                new KpiModule(),
                new ValidationModule(),
                new EstimationModule(),
                new TimeModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceConfigurationModule(),
                new DeviceDataModule(),
                new MasterDataModule(),
                new MdcReadingTypeUtilServiceModule(),
                new SchedulingModule(),
                new TaskModule(),
                new TasksModule(),
                new IssuesModule(),
                new TopologyModule(),
                new FirmwareModule(),
                new CalendarModule(),
                new TopologyModule());
        initializeTopModuleInATransaction();
    }

    private static void initializeTopModuleInATransaction() {
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                UserService userService = injector.getInstance(UserService.class);
                userService.findOrCreateGroup(UserService.BATCH_EXECUTOR_ROLE);
                injector.getInstance(ServiceCallService.class);
                injector.getInstance(CustomPropertySetService.class);
                injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommandCustomPropertySet());
                injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CompletionOptionsCustomPropertySet());
                injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new OnDemandReadServiceCallCustomPropertySet());
                injector.getInstance(FiniteStateMachineService.class);
                injector.getInstance(MeteringService.class);
                injector.getInstance(MasterDataService.class);
                injector.getInstance(MeteringGroupsService.class);
//                injector.getInstance(EngineService.class);
                injector.getInstance(TopologyService.class);
                injector.getInstance(EventService.class);
                meteringService = injector.getInstance(MeteringService.class);
                mdcReadingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
                masterDataService = injector.getInstance(MasterDataService.class);
                topologyService = injector.getInstance(TopologyService.class);
            }
        });
    }

    @AfterClass
    public static void tearDown() {
        bootstrapModule.deactivate();
    }

    private static void initializeClock() {
        when(clock.getZone()).thenReturn(utcTimeZone.toZoneId());
        when(clock.instant()).thenAnswer(new Answer<Instant>() {
            @Override
            public Instant answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Instant.now();
            }
        });
    }

    protected static Injector getInjector() {
        return injector;
    }

    static Clock getClock() {
        return clock;
    }

    static TransactionService getTransactionService() {
        return transactionService;
    }

    @Before
    public void resetClock() {
        initializeClock();
    }

    protected Date freezeClock(Date timeStamp) {
        when(clock.getZone()).thenReturn(utcTimeZone.toZoneId());
        when(clock.instant()).thenReturn(timeStamp.toInstant());
        return timeStamp;
    }

    MeteringService getMeteringService() {
        return meteringService;
    }

    public MdcReadingTypeUtilService getMdcReadingTypeUtilService() {
        return mdcReadingTypeUtilService;
    }

    public MasterDataService getMasterDataService() {
        return masterDataService;
    }

    public TopologyService getTopologyService() {
        return topologyService;
    }

    private static class MockModule extends AbstractModule {

        private final BundleContext bundleContext;
        private final EventAdmin eventAdmin;
        private final LicenseService licenseService;

        private MockModule(BundleContext bundleContext, EventAdmin eventAdmin, LicenseService licenseService) {
            super();
            this.bundleContext = bundleContext;
            this.eventAdmin = eventAdmin;
            this.licenseService = licenseService;
        }

        @Override
        protected void configure() {
            bind(Clock.class).toInstance(clock);
            SerialComponentService serialComponentService = mock(SerialComponentService.class);
            bind(SerialComponentService.class).toInstance(serialComponentService);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(Thesaurus.class).toInstance(mock(Thesaurus.class));
            bind(com.elster.jupiter.issue.share.service.IssueService.class).toInstance(mock(com.elster.jupiter.issue.share.service.IssueService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(FileImportService.class).toInstance(mock(FileImportService.class));
            bind(HttpService.class).toInstance(mock(HttpService.class));
        }

    }

    protected class MdcReadingTypeUtilServiceAndClock implements DeviceCommand.ServiceProvider {

        @Override
        public EventService eventService() {
            return null;
        }

        @Override
        public IssueService issueService() {
            return null;
        }

        @Override
        public Clock clock() {
            return clock;
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return getMdcReadingTypeUtilService();
        }

        @Override
        public EngineService engineService() {
            return null;
        }

        @Override
        public NlsService nlsService() {
            return null;
        }

        @Override
        public EventPublisher eventPublisher() {
            return null;
        }

        @Override
        public DeviceMessageService deviceMessageService() {
            return null;
        }
    }

}