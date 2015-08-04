package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.device.topology.impl.TopologyModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.engine.impl.EngineModule;
import com.energyict.mdc.firmware.impl.FirmwareModule;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.impl.MdcIOModule;
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

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
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
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
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

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

    private static Injector injector;
    private static InMemoryBootstrapModule bootstrapModule;
    private static MeteringService meteringService;
    private static Clock clock = mock(Clock.class);
    private static MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private static MasterDataService masterDataService;

    @Mock
    private DeviceFactory deviceFactory;
    private static TransactionService transactionService;

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
                new UserModule(),
                new FiniteStateMachineModule(),
                new MeteringModule(),
                new MeteringGroupsModule(),
                new OrmModule(),
                new DataVaultModule(),
                new MdcIOModule(),
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
                new ValidationModule(),
                new EstimationModule(),
                new TimeModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceConfigurationModule(),
                new DeviceDataModule(),
                new MasterDataModule(),
                new MdcReadingTypeUtilServiceModule(),
                new SchedulingModule(),
                new KpiModule(),
                new TaskModule(),
                new TasksModule(),
                new IssuesModule(),
                new TopologyModule(),
                new FirmwareModule());
        initializeTopModuleInATransaction();
    }

    private static void initializeTopModuleInATransaction() {
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                injector.getInstance(FiniteStateMachineService.class);
                injector.getInstance(MeteringService.class);
                injector.getInstance(MasterDataService.class);
                injector.getInstance(MeteringGroupsService.class);
                injector.getInstance(EngineService.class);
                EventService eventService = injector.getInstance(EventService.class);
                meteringService = injector.getInstance(MeteringService.class);
                mdcReadingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
                masterDataService = injector.getInstance(MasterDataService.class);
            }
        });
    }

    @AfterClass
    public static void tearDown() {
        bootstrapModule.deactivate();
    }

    @Before
    public void resetClock() {
        clock = mock(Clock.class);
        initializeClock();
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

    protected Date freezeClock(Date timeStamp) {
        when(clock.getZone()).thenReturn(utcTimeZone.toZoneId());
        when(clock.instant()).thenReturn(timeStamp.toInstant());
        return timeStamp;
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

    MeteringService getMeteringService() {
        return meteringService;
    }

    public MdcReadingTypeUtilService getMdcReadingTypeUtilService() {
        return mdcReadingTypeUtilService;
    }

    public MasterDataService getMasterDataService() {
        return masterDataService;
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
            SerialComponentService serialComponentService = mock(SerialComponentService.class);
            bind(SerialComponentService.class).toInstance(serialComponentService);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(Thesaurus.class).toInstance(mock(Thesaurus.class));
            bind(com.elster.jupiter.issue.share.service.IssueService.class).toInstance(mock(com.elster.jupiter.issue.share.service.IssueService.class));
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
    }

}