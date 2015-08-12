package com.elster.jupiter.cps.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;

import org.junit.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Integration tests the {@link CustomPropertySetServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-12 (14:04)
 */
public class CustomPropertySetServiceImplIT {

    private static BundleContext bundleContext;
    private static Principal principal;
    private static EventAdmin eventAdmin;
    private static Clock clock;
    private static TransactionService transactionService;
    private static InMemoryBootstrapModule bootstrapModule;

    private static Injector injector;
    private CustomPropertySetService testInstance;

    @BeforeClass
    public static void initialize() {
        bootstrapModule = new InMemoryBootstrapModule();
        initializeMocks(CustomPropertySetServiceImplIT.class.getSimpleName());
        injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(principal),
                new NlsModule(),
                new PubSubModule(),
                new TransactionModule(),
                new OrmModule(),
                new CustomPropertySetsModule());
        transactionService = injector.getInstance(TransactionService.class);
    }

    private static void initializeMocks(String testName) {
        clock = mock(Clock.class);
        when(clock.instant()).thenReturn(Instant.now());
        bundleContext = mock(BundleContext.class);
        eventAdmin = mock(EventAdmin.class);
        principal = mock(Principal.class, withSettings().extraInterfaces(User.class));
        when(principal.getName()).thenReturn(testName);
    }

    @AfterClass
    public static void cleanUpDataBase() {
        bootstrapModule.deactivate();
    }

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Clock.class).toInstance(clock);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(JsonService.class).toInstance(new JsonServiceImpl());
            bind(BeanService.class).toInstance(new BeanServiceImpl());
            bind(FileSystem.class).toInstance(FileSystems.getDefault());
        }
    }

    @Before
    public void createTestInstance() {
        try (TransactionContext ctx = transactionService.getContext()) {
            this.testInstance = injector.getInstance(CustomPropertySetService.class);
            ctx.commit();
        }
    }

    @Test
    public void setTestInstance() {

    }

}