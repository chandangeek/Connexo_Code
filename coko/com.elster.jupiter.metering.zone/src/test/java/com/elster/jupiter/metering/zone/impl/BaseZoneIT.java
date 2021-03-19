package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;

import java.util.Dictionary;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseZoneIT {
    private static BundleContext bundleContext = mock(BundleContext.class);

    private static ServiceRegistration serviceRegistration = mock(ServiceRegistration.class);
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    protected static Injector injector;


    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TestRule transactionRule = new TransactionalRule(injector.getInstance(TransactionService.class));
    protected MeteringZoneService meteringZoneService;


    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
        }
    }

    @BeforeClass
    public static void setUp() {
        when(bundleContext.registerService(any(Class.class), any(Class.class), any(Dictionary.class))).thenReturn(serviceRegistration);
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new TransactionModule(),
                    new InMemoryMessagingModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new MeteringZoneModule(),
                    new DomainUtilModule(),
                    new H2OrmModule(),
                    new UtilModule(),
                    new MeteringModule(),
                    new NlsModule(),
                    new CalendarModule(),
                    new CustomPropertySetsModule(),
                    new EventsModule(),
                    new FiniteStateMachineModule(),
                    new IdsModule(),
                    new PartyModule(),
                    new BasicPropertiesModule(),
                    new SearchModule(),
                    new TimeModule(),
                    new BpmModule(),
                    new TaskModule(),
                    new WebServicesModule(),
                    new AuditServiceModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new UserModule(),
                    new DataVaultModule()

            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(AuditService.class);
                    injector.getInstance(MeteringZoneService.class);
                    return null;
                }
        );


    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Before
    public void init() {
        meteringZoneService = injector.getInstance(MeteringZoneService.class);
    }

}
