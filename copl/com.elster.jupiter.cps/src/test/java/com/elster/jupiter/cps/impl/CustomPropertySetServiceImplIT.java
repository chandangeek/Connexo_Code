package com.elster.jupiter.cps.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
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
import java.util.List;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
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

    private BundleContext bundleContext;
    private Principal principal;
    private EventAdmin eventAdmin;
    private Clock clock;
    private TransactionService transactionService;
    private InMemoryBootstrapModule bootstrapModule;

    private Injector injector;
    private CustomPropertySetService testInstance;

    @Before
    public void initialize() {
        this.bootstrapModule = new InMemoryBootstrapModule();
        initializeMocks(CustomPropertySetServiceImplIT.class.getSimpleName());
        this.injector = Guice.createInjector(
                new MockModule(),
                this.bootstrapModule,
                new ThreadSecurityModule(principal),
                new NlsModule(),
                new PubSubModule(),
                new TransactionModule(),
                new OrmModule(),
                new BasicPropertiesModule(),
                new CustomPropertySetsModule());
        this.transactionService = this.injector.getInstance(TransactionService.class);
        this.createTestInstance();
    }

    private void initializeMocks(String testName) {
        this.clock = mock(Clock.class);
        when(this.clock.instant()).thenReturn(Instant.now());
        this.bundleContext = mock(BundleContext.class);
        this.eventAdmin = mock(EventAdmin.class);
        this.principal = mock(Principal.class, withSettings().extraInterfaces(User.class));
        when(this.principal.getName()).thenReturn(testName);
    }

    private void createTestInstance() {
        try (TransactionContext ctx = transactionService.getContext()) {
            this.testInstance = injector.getInstance(CustomPropertySetService.class);
            ctx.commit();
        }
    }

    @After
    public void cleanUpDataBase() {
        this.bootstrapModule.deactivate();
    }

    private class MockModule extends AbstractModule {
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

    @Test(expected = IllegalArgumentException.class)
    public void addNonVersionedCustomPropertySetWhenTestDomainIsNotRegisteredWithOrmService() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        List<? extends DataModel> dataModelsBeforeAdd = ormService.getDataModels();

        // Business method
        this.testInstance.addCustomPropertySet(new CustomPropertySetForTestingPurposes(propertySpecService));

        // Asserts
        List<? extends DataModel> dataModelsAfterAdd = ormService.getDataModels();
        assertThat(dataModelsAfterAdd.size()).isGreaterThan(dataModelsBeforeAdd.size());
    }

    @Test
    public void addNonVersionedCustomPropertySet() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            TestDomain.install(ormService);
        }

        List<? extends DataModel> dataModelsBeforeAdd = ormService.getDataModels();

        // Business method
        this.testInstance.addCustomPropertySet(new CustomPropertySetForTestingPurposes(propertySpecService));

        // Asserts
        List<? extends DataModel> dataModelsAfterAdd = ormService.getDataModels();
        assertThat(dataModelsAfterAdd.size()).isGreaterThan(dataModelsBeforeAdd.size());
        assertThat(this.testInstance.findActiveCustomPropertySets()).isNotEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void addVersionedCustomPropertySetWhenTestDomainIsNotRegisteredWithOrmService() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        List<? extends DataModel> dataModelsBeforeAdd = ormService.getDataModels();

        // Business method
        this.testInstance.addCustomPropertySet(new VersionedCustomPropertySetForTestingPurposes(propertySpecService));

        // Asserts
        List<? extends DataModel> dataModelsAfterAdd = ormService.getDataModels();
        assertThat(dataModelsAfterAdd.size()).isGreaterThan(dataModelsBeforeAdd.size());
    }

    @Test
    public void addVersionedCustomPropertySet() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            TestDomain.install(ormService);
        }

        List<? extends DataModel> dataModelsBeforeAdd = ormService.getDataModels();

        // Business method
        this.testInstance.addCustomPropertySet(new VersionedCustomPropertySetForTestingPurposes(propertySpecService));

        // Asserts
        List<? extends DataModel> dataModelsAfterAdd = ormService.getDataModels();
        assertThat(dataModelsAfterAdd.size()).isGreaterThan(dataModelsBeforeAdd.size());
        assertThat(this.testInstance.findActiveCustomPropertySets()).isNotEmpty();
    }

}