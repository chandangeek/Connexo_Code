package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.EngineModule;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
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
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;

import java.time.Clock;

import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.protocols.mdc.channels.serial.SerialComponentService;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import java.time.Instant;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
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
    public static void setupEnvironment() {
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
                new ThreadSecurityModule(principal),
                new PubSubModule(),
                new TransactionModule(),
                new DomainUtilModule(),
                new NlsModule(),
                new UserModule(),
                new MeteringModule(),
                new MeteringGroupsModule(),
                new OrmModule(),
                new MdcCommonModule(),
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
                new ProtocolsModule(),
                new PluggableModule(),
                new ValidationModule(),
                new DeviceConfigurationModule(),
                new DeviceDataModule(),
                new MasterDataModule(),
                new MdcReadingTypeUtilServiceModule(),
                new SchedulingModule(),
                new KpiModule(),
                new TaskModule(),
                new TasksModule(),
                new IssuesModule());
        initializeTopModuleInATransaction();
    }

    private static void initializeTopModuleInATransaction() {
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                injector.getInstance(MeteringService.class);
                injector.getInstance(MeteringGroupsService.class);
                injector.getInstance(EngineService.class);
                EventService eventService = injector.getInstance(EventService.class);
                makeSureJupiterEventsAreInstalled(eventService);
                meteringService = injector.getInstance(MeteringService.class);
                mdcReadingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
                masterDataService = injector.getInstance(MasterDataService.class);
            }
        });
    }

    private static void makeSureJupiterEventsAreInstalled(EventService eventService) {
        ((InstallService) eventService).install();
    }

    @AfterClass
    public static void tearDownEnvironment() {
        bootstrapModule.deactivate();
    }

    @Before
    public void resetClock () {
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
    protected Date freezeClock (int year, int month, int day) {
        return freezeClock(year, month, day, 0, 0, 0, 0);
    }

    protected Date freezeClock (int year, int month, int day, TimeZone timeZone) {
        return freezeClock(year, month, day, 0, 0, 0, 0, timeZone);
    }

    protected Date freezeClock (int year, int month, int day, int hour, int minute, int second, int millisecond) {
        return freezeClock(year, month, day, hour, minute, second, millisecond, utcTimeZone);
    }

    protected Date freezeClock(Date timeStamp) {
        when(clock.getZone()).thenReturn(utcTimeZone.toZoneId());
        when(clock.instant()).thenReturn(timeStamp.toInstant());
        return timeStamp;
    }
    protected Date freezeClock (int year, int month, int day, int hour, int minute, int second, int millisecond, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(year, month, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        when(clock.getZone()).thenReturn(timeZone.toZoneId());
        when(clock.instant()).thenReturn(calendar.getTime().toInstant());
        return calendar.getTime();
    }

    protected static Injector getInjector(){
        return injector;
    }

    protected <T> T executeInTransaction(Transaction<T> transaction) {
        return injector.getInstance(TransactionService.class).execute(transaction);
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
            bind(JsonService.class).toInstance(new JsonServiceImpl());
            bind(BeanService.class).toInstance(new BeanServiceImpl());
            bind(SerialComponentService.class).toInstance(serialComponentService);
            bind(Clock.class).toInstance(clock);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(CronExpressionParser.class).toInstance(mock(CronExpressionParser.class, RETURNS_DEEP_STUBS));
            bind(LogService.class).toInstance(mock(LogService.class));
        }

    }

}