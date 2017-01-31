/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests the {@link CustomPropertySetServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-12 (14:04)
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomPropertySetServiceImplIT {

    private BundleContext bundleContext;
    @Mock
    private Clock clock;
    @Mock
    private TimeService timeService;
    private UserService userService;
    private DataVaultService dataVaultService;
    private User principal;
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private ThreadPrincipalService threadPrincipalService;
    private InMemoryBootstrapModule bootstrapModule;

    private Injector injector;
    private CustomPropertySetServiceImpl testInstance;

    @Rule
    public ExpectedConstraintViolationRule rule = new ExpectedConstraintViolationRule();

    @Before
    public void initialize() {
        this.bootstrapModule = new InMemoryBootstrapModule();
        this.initializeMocks(CustomPropertySetServiceImplIT.class.getSimpleName());
        this.injector = Guice.createInjector(
                new MockModule(),
                this.bootstrapModule,
                new ThreadSecurityModule(principal),
                new NlsModule(),
                new PubSubModule(),
                new TransactionModule(),
                new OrmModule(),
                new BasicPropertiesModule(),
                new CustomPropertySetsModule(),
                new UtilModule(this.clock),
                new SearchModule());
        this.transactionService = this.injector.getInstance(TransactionService.class);
        this.createTestInstance();
    }

    private void initializeMocks(String testName) {
        when(this.clock.instant()).thenReturn(Instant.now());
        this.bundleContext = mock(BundleContext.class);
        this.eventAdmin = mock(EventAdmin.class);
        this.dataVaultService = mock(DataVaultService.class);
        this.userService = mock(UserService.class);
        this.principal = mock(User.class);
        when(this.principal.getName()).thenReturn(testName);
    }

    private void createTestInstance() {
        try (TransactionContext ctx = transactionService.getContext()) {
            this.testInstance = injector.getInstance(CustomPropertySetServiceImpl.class);
            ctx.commit();
        }
    }

    @After
    public void cleanUpDataBase() {
        this.bootstrapModule.deactivate();
    }

    @Before
    public void setThreadPrinciple() {
        this.threadPrincipalService = this.injector.getInstance(ThreadPrincipalService.class);
        this.threadPrincipalService.set(this.principal);
    }

    @After
    public void clearThreadPrinciple() {
        this.threadPrincipalService.clear();
    }

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(DataVaultService.class).toInstance(dataVaultService);
            bind(UserService.class).toInstance(userService);
            bind(Clock.class).toInstance(clock);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(FileSystem.class).toInstance(FileSystems.getDefault());
            bind(TimeService.class).toInstance(timeService);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void addNonVersionedCustomPropertySetWhenTestDomainIsNotRegisteredWithOrmService() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        List<DataModel> dataModelsBeforeAdd = ormService.getDataModels();

        // Business method
        this.testInstance.addCustomPropertySet(new CustomPropertySetForTestingPurposes(propertySpecService));

        // Asserts
        List<DataModel> dataModelsAfterAdd = ormService.getDataModels();
        assertThat(dataModelsAfterAdd.size()).isGreaterThan(dataModelsBeforeAdd.size());
    }

    @Test
    public void addNonVersionedCustomPropertySet() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            TestDomain.install(ormService);
            ctx.commit();   // Above will have done a DDL which is auto commit but just making the intention clear
        }

        List<DataModel> dataModelsBeforeAdd = ormService.getDataModels();

        // Business method
        this.testInstance.addCustomPropertySet(new CustomPropertySetForTestingPurposes(propertySpecService));

        // Asserts
        List<DataModel> dataModelsAfterAdd = ormService.getDataModels();
        assertThat(dataModelsAfterAdd.size()).isGreaterThan(dataModelsBeforeAdd.size());
        assertThat(this.testInstance.findActiveCustomPropertySets()).isNotEmpty();
    }

    @Test
    public void addNonVersionedCustomPropertySetAfterServerRestart() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            TestDomain.install(ormService);
            ctx.commit();   // Above will have done a DDL which is auto commit but just making the intention clear
        }
        this.testInstance.addCustomPropertySet(new CustomPropertySetForTestingPurposes(propertySpecService));
        List<DataModel> dataModelsBeforeAdd = ormService.getDataModels();

        // Simulate server restart by recreating the CustomPropertySetService instance
        this.testInstance = injector.getInstance(CustomPropertySetServiceImpl.class);

        // Business method
        this.testInstance.addCustomPropertySet(new CustomPropertySetForTestingPurposes(propertySpecService));

        // Asserts
        List<DataModel> dataModelsAfterAdd = ormService.getDataModels();
        assertThat(this.testInstance.findActiveCustomPropertySets()).isNotEmpty();
        assertThat(dataModelsAfterAdd).hasSameSizeAs(dataModelsBeforeAdd);
    }

    @Test
    public void addNonVersionedCustomPropertySetWithAdditionalPrimaryKeyColumns() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            TestDomain.install(ormService);
            ctx.commit();   // Above will have done a DDL which is auto commit but just making the intention clear
        }

        List<DataModel> dataModelsBeforeAdd = ormService.getDataModels();

        // Business method
        CustomPropertySetWithAdditionalPrimaryKeyColumnsForTestingPurposes customPropertySet = new CustomPropertySetWithAdditionalPrimaryKeyColumnsForTestingPurposes(propertySpecService);
        this.testInstance.addCustomPropertySet(customPropertySet);

        // Asserts
        List<DataModel> dataModelsAfterAdd = ormService.getDataModels();
        assertThat(dataModelsAfterAdd.size()).isGreaterThan(dataModelsBeforeAdd.size());
        assertThat(this.testInstance.findActiveCustomPropertySets()).isNotEmpty();
        Optional<DataModel> dataModel = ormService.getDataModel(customPropertySet.getPersistenceSupport().componentName());
        assertThat(dataModel).isPresent();
        Table<?> table = dataModel.get().getTable(customPropertySet.getPersistenceSupport().tableName());
        List<String> primaryKeyColumnNames = table.getPrimaryKeyColumns().stream().map(Column::getName).collect(Collectors.toList());
        assertThat(primaryKeyColumnNames).contains(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.databaseName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addVersionedCustomPropertySetWhenTestDomainIsNotRegisteredWithOrmService() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        List<DataModel> dataModelsBeforeAdd = ormService.getDataModels();

        // Business method
        this.testInstance.addCustomPropertySet(new VersionedCustomPropertySetForTestingPurposes(propertySpecService));

        // Asserts
        List<DataModel> dataModelsAfterAdd = ormService.getDataModels();
        assertThat(dataModelsAfterAdd.size()).isGreaterThan(dataModelsBeforeAdd.size());
    }

    @Test
    public void addVersionedCustomPropertySet() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            TestDomain.install(ormService);
            ctx.commit();   // Above will have done a DDL which is auto commit but just making the intention clear
        }

        List<DataModel> dataModelsBeforeAdd = ormService.getDataModels();

        // Business method
        this.testInstance.addCustomPropertySet(new VersionedCustomPropertySetForTestingPurposes(propertySpecService));

        // Asserts
        List<DataModel> dataModelsAfterAdd = ormService.getDataModels();
        assertThat(dataModelsAfterAdd.size()).isGreaterThan(dataModelsBeforeAdd.size());
        assertThat(this.testInstance.findActiveCustomPropertySets()).isNotEmpty();
    }

    @Test
    public void addVersionedCustomPropertySetAfterServerRestart() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            TestDomain.install(ormService);
            ctx.commit();   // Above will have done a DDL which is auto commit but just making the intention clear
        }
        this.testInstance.addCustomPropertySet(new VersionedCustomPropertySetForTestingPurposes(propertySpecService));
        List<DataModel> dataModelsBeforeAdd = ormService.getDataModels();

        // Simulate server restart by recreating the CustomPropertySetService instance
        this.testInstance = injector.getInstance(CustomPropertySetServiceImpl.class);

        // Business method
        this.testInstance.addCustomPropertySet(new VersionedCustomPropertySetForTestingPurposes(propertySpecService));

        // Asserts
        List<DataModel> dataModelsAfterAdd = ormService.getDataModels();
        assertThat(this.testInstance.findActiveCustomPropertySets()).isNotEmpty();
        assertThat(dataModelsAfterAdd).hasSameSizeAs(dataModelsBeforeAdd);
    }

    @Test
    public void addVersionedCustomPropertySetWithSamePersistenceSupportClass() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            TestDomain.install(ormService);
            ctx.commit();   // Above will have done a DDL which is auto commit but just making the intention clear
        }

        this.testInstance.addCustomPropertySet(new VersionedCustomPropertySetForTestingPurposes(propertySpecService));
        List<DataModel> dataModelsBeforeAdd = ormService.getDataModels();
        List<RegisteredCustomPropertySet> customPropertySetsBeforeAdd = this.testInstance.findActiveCustomPropertySets();

        // Business method
        this.testInstance.addCustomPropertySet(new VersionedCustomPropertySetForTestingPurposesWithSamePersistenceSupport(propertySpecService));

        // Asserts
        List<DataModel> dataModelsAfterAdd = ormService.getDataModels();
        assertThat(dataModelsAfterAdd).hasSameSizeAs(dataModelsBeforeAdd);
        List<RegisteredCustomPropertySet> customPropertySetsAfterAdd = this.testInstance.findActiveCustomPropertySets();
        assertThat(customPropertySetsAfterAdd.size()).isGreaterThan(customPropertySetsBeforeAdd.size());
    }

    @Test
    public void addVersionedCustomPropertySetWithAdditionalPrimaryKeyColumns() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            TestDomain.install(ormService);
            ctx.commit();   // Above will have done a DDL which is auto commit but just making the intention clear
        }

        List<DataModel> dataModelsBeforeAdd = ormService.getDataModels();

        // Business method
        VersionedCustomPropertySetWithAdditionalPrimaryKeyColumnsForTestingPurposes customPropertySet = new VersionedCustomPropertySetWithAdditionalPrimaryKeyColumnsForTestingPurposes(propertySpecService);
        this.testInstance.addCustomPropertySet(customPropertySet);

        // Asserts
        List<DataModel> dataModelsAfterAdd = ormService.getDataModels();
        assertThat(dataModelsAfterAdd.size()).isGreaterThan(dataModelsBeforeAdd.size());
        assertThat(this.testInstance.findActiveCustomPropertySets()).isNotEmpty();
        Optional<DataModel> dataModel = ormService.getDataModel(customPropertySet.getPersistenceSupport().componentName());
        assertThat(dataModel).isPresent();
        Table<?> table = dataModel.get().getTable(customPropertySet.getPersistenceSupport().tableName());
        List<String> primaryKeyColumnNames = table.getPrimaryKeyColumns().stream().map(Column::getName).collect(Collectors.toList());
        assertThat(primaryKeyColumnNames).contains(VersionedDomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.databaseName());
    }

    @Test(timeout = 10000)
    public void addCustomPropertySetsWhileActivating() throws InterruptedException {
        OrmService ormService = injector.getInstance(OrmService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            TestDomain.install(ormService);
            ctx.commit();   // Above will have done a DDL which is auto commit but just making the intention clear
        }
        List<DataModel> dataModelsBeforeAdd = ormService.getDataModels();
        CustomPropertySetServiceImpl service = new CustomPropertySetServiceImpl();
        service.setOrmService(ormService, true);
        service.setNlsService(this.injector.getInstance(NlsService.class));
        service.setTransactionService(this.transactionService);
        service.setSearchService(mock(SearchService.class));
        service.setUserService(userService);
        service.setUpgradeService(UpgradeModule.FakeUpgradeService.getInstance());

        /* Create 3 threads that will wait on CountdownLatch to start simultaneously
         *    1. activate the service
         *    2. Add non versioned CustomPropertySet
         *    3. Add versioned CustomPropertySet */
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch stopLatch = new CountDownLatch(3);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.execute(new Felix(this.transactionService, this.threadPrincipalService, this.principal, service, startLatch, stopLatch));
        executorService.execute(new AddCustomPropertySet(this.transactionService, this.threadPrincipalService, this.principal, service, startLatch, stopLatch));
        executorService.execute(new AddVersionedCustomPropertySet(this.transactionService, this.threadPrincipalService, this.principal, service, startLatch, stopLatch));

        // Here is where all the action will happen
        startLatch.countDown();

        // Now wait until all 3 threads have completed
        stopLatch.await();

        // Asserts
        List<DataModel> dataModelsAfterAdd = ormService.getDataModels();
        assertThat(dataModelsAfterAdd.size()).isEqualTo(dataModelsBeforeAdd.size() + 2);
        assertThat(service.findActiveCustomPropertySets()).hasSize(2);
    }

    @Test
    public void addSystemDefinedCustomPropertySet() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            TestDomain.install(ormService);
            ctx.commit();   // Above will have done a DDL which is auto commit but just making the intention clear
        }

        List<DataModel> dataModelsBeforeAdd = ormService.getDataModels();

        // Business method
        try (TransactionContext ctx = transactionService.getContext()) {
            this.testInstance.addSystemCustomPropertySet(new CustomPropertySetForTestingPurposes(propertySpecService));
            ctx.commit();   // Above will have done a DDL which is auto commit but just making the intention clear

            // Asserts
            List<DataModel> dataModelsAfterAdd = ormService.getDataModels();
            assertThat(dataModelsAfterAdd.size()).isGreaterThan(dataModelsBeforeAdd.size());
        }
    }

    @Test
    public void addSystemDefinedCustomPropertySetSecondTime() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            TestDomain.install(ormService);
            ctx.commit();   // Above will have done a DDL which is auto commit but just making the intention clear
        }

        try (TransactionContext ctx = transactionService.getContext()) {
            this.testInstance.addSystemCustomPropertySet(new CustomPropertySetForTestingPurposes(propertySpecService));
            ctx.commit();
        }
        List<DataModel> dataModelsBeforeAdd = ormService.getDataModels();

        // Business method
        try (TransactionContext ctx = transactionService.getContext()) {
            this.testInstance.addSystemCustomPropertySet(new CustomPropertySetForTestingPurposes(propertySpecService));
            ctx.commit();
        }

        // Asserts
        List<DataModel> dataModelsAfterAdd = ormService.getDataModels();
        assertThat(dataModelsAfterAdd).hasSameSizeAs(dataModelsBeforeAdd);
    }

    @Test
    public void systemDefinedCustomPropertySetIsNotReturnedByFindActive() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            TestDomain.install(ormService);
            this.testInstance.addSystemCustomPropertySet(new CustomPropertySetForTestingPurposes(propertySpecService));
            ctx.commit();
        }

        // Business method
        List<RegisteredCustomPropertySet> activeCustomPropertySets = this.testInstance.findActiveCustomPropertySets();

        // Asserts
        assertThat(activeCustomPropertySets).isEmpty();
    }

    @Test
    public void systemDefinedCustomPropertySetIsNotReturnedByFindActiveByDomainClass() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            TestDomain.install(ormService);
            this.testInstance.addSystemCustomPropertySet(new CustomPropertySetForTestingPurposes(propertySpecService));
            ctx.commit();
        }

        // Business method
        List<RegisteredCustomPropertySet> activeCustomPropertySets = this.testInstance.findActiveCustomPropertySets(TestDomain.class);

        // Asserts
        assertThat(activeCustomPropertySets).isEmpty();
    }

    @Test
    public void systemDefinedCustomPropertySetIstReturnedByFindById() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        OrmService ormService = injector.getInstance(OrmService.class);
        CustomPropertySetForTestingPurposes customPropertySet;
        try (TransactionContext ctx = transactionService.getContext()) {
            TestDomain.install(ormService);
            customPropertySet = new CustomPropertySetForTestingPurposes(propertySpecService);
            this.testInstance.addSystemCustomPropertySet(customPropertySet);
            ctx.commit();
        }

        // Business method
        Optional<RegisteredCustomPropertySet> activeCustomPropertySet = this.testInstance.findActiveCustomPropertySet(customPropertySet.getId());

        // Asserts
        assertThat(activeCustomPropertySet).isPresent();
    }

    @Test
    public void createNonVersionedValues() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        CustomPropertySetForTestingPurposes customPropertySet = new CustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("createNonVersionedValues");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();

        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
        BigDecimal expectedBillingCycle = BigDecimal.TEN;
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
        String expectedContractNumber = "createNonVersionedValues";
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), expectedContractNumber);

        try (TransactionContext ctx = transactionService.getContext()) {
            // Business method
            this.testInstance.setValuesFor(customPropertySet, testDomain, values);
            ctx.commit();   // Above will have done a DDL which is auto commit but just making the intention clear

            // Asserts: not expecting any exceptions
        }
    }

    @Test
    public void updateNonVersionedValues() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        CustomPropertySetForTestingPurposes customPropertySet = new CustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("updateNonVersionedValues");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();

        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
        BigDecimal initialBillingCycle = BigDecimal.ONE;
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), initialBillingCycle);
        String initialContractNumber = "initialValue";
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), initialContractNumber);
        BigDecimal expectedBillingCycle = BigDecimal.TEN;

        try (TransactionContext ctx = transactionService.getContext()) {
            // Set initial values
            this.testInstance.setValuesFor(customPropertySet, testDomain, values);
            ctx.commit();
        }

        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
        String expectedContractNumber = "updateNonVersionedValues";
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), expectedContractNumber);
        try (TransactionContext ctx = transactionService.getContext()) {
            // Business method
            this.testInstance.setValuesFor(customPropertySet, testDomain, values);
            ctx.commit();   // Above will have done a DDL which is auto commit but just making the intention clear

            // Asserts: not expecting any exceptions
        }
    }

    @Test(expected = CurrentUserIsNotAllowedToEditValuesOfCustomPropertySetException.class)
    public void updateNonVersionedValuesWithoutEditPrivileges() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        CustomPropertySetForTestingPurposes customPropertySet = new CustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("updateNonVersionedValues");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        BigDecimal initialBillingCycle = BigDecimal.ONE;
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), initialBillingCycle);
        String initialContractNumber = "initialValue";
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), initialContractNumber);
        BigDecimal expectedBillingCycle = BigDecimal.TEN;

        try (TransactionContext ctx = transactionService.getContext()) {
            // Set initial values
            this.testInstance.setValuesFor(customPropertySet, testDomain, values);
            ctx.commit();
        }

        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
        String expectedContractNumber = "updateNonVersionedValues";
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), expectedContractNumber);
        try (TransactionContext ctx = transactionService.getContext()) {
            // Business method
            this.testInstance.setValuesFor(customPropertySet, testDomain, values);
            ctx.commit();   // Above will have done a DDL which is auto commit but just making the intention clear

            // Asserts: not expecting any exceptions
        }
    }

    @Test
    public void verifyValuesAfterUpdateOfNonVersionedValues() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        CustomPropertySetForTestingPurposes customPropertySet = new CustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("verifyValuesAfterUpdateOfNonVersionedValues");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();

        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
        BigDecimal initialBillingCycle = BigDecimal.ONE;
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), initialBillingCycle);
        String initialContractNumber = "initialValue";
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), initialContractNumber);
        BigDecimal expectedBillingCycle = BigDecimal.TEN;

        try (TransactionContext ctx = transactionService.getContext()) {
            // Set initial values
            this.testInstance.setValuesFor(customPropertySet, testDomain, values);
            ctx.commit();
        }

        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
        String expectedContractNumber = "verifyValuesAfterUpdateOfNonVersionedValues";
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), expectedContractNumber);
        try (TransactionContext ctx = transactionService.getContext()) {
            // Business method
            this.testInstance.setValuesFor(customPropertySet, testDomain, values);

            CustomPropertySetValues valuesForVerifying = this.testInstance.getUniqueValuesFor(customPropertySet, testDomain);
            assertThat(valuesForVerifying.getProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName())).isEqualToComparingFieldByField(expectedBillingCycle);
            assertThat(valuesForVerifying.getProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName())).isEqualToComparingFieldByField(expectedContractNumber);
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createNonVersionedValuesWithVersionedApi() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        CustomPropertySetForTestingPurposes customPropertySet = new CustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("createNonVersionedValues");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        CustomPropertySetValues values = CustomPropertySetValues.empty();

        try (TransactionContext ctx = transactionService.getContext()) {
            // Business method
            this.testInstance.setValuesFor(customPropertySet, testDomain, values, Instant.now());

            // Asserts: see expected exception rule
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = "CannotBeNull", property = "billingCycle")
    public void createNonVersionedValuesWithMissingRequiredProperty() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        CustomPropertySetForTestingPurposes customPropertySet = new CustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("createNonVersionedValuesWithMissingRequiredProperty");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();

        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), "dontCareBecauseWillFailAnyway");

        try (TransactionContext ctx = transactionService.getContext()) {
            // Business method
            this.testInstance.setValuesFor(customPropertySet, testDomain, values);

            // Asserts: see expected contraint violation rule
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = "FieldTooLong", property = "contractNumber")
    public void createNonVersionedValuesWithTooLargeOptionalValue() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        CustomPropertySetForTestingPurposes customPropertySet = new CustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("createNonVersionedValuesWithTooLargeOptionalValue");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();

        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
        BigDecimal expectedBillingCycle = BigDecimal.TEN;
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), Strings.repeat("Too long", 100));

        try (TransactionContext ctx = transactionService.getContext()) {
            // Business method
            this.testInstance.setValuesFor(customPropertySet, testDomain, values);

            // Asserts: see expected contraint violation rule
        }
    }

    @Test
    public void removeNonVersionedValues() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        CustomPropertySetForTestingPurposes customPropertySet = new CustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("removeNonVersionedValues");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();

        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
        BigDecimal expectedBillingCycle = BigDecimal.TEN;
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
        String expectedContractNumber = "createNonVersionedValues";
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), expectedContractNumber);

        try (TransactionContext ctx = transactionService.getContext()) {
            this.testInstance.setValuesFor(customPropertySet, testDomain, values);

            // Business method
            this.testInstance.removeValuesFor(customPropertySet, testDomain);

            // Asserts
            CustomPropertySetValues valuesAfterRemove = this.testInstance.getUniqueValuesFor(customPropertySet, testDomain);
            assertThat(valuesAfterRemove.isEmpty()).isTrue();
        }
    }

    @Test
    public void removeNonVersionedValuesOfCustomPropertySetWithAdditionalPrimaryKeys() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        CustomPropertySetWithAdditionalPrimaryKeyColumnsForTestingPurposes customPropertySet = new CustomPropertySetWithAdditionalPrimaryKeyColumnsForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("removeNonVersionedValues");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();

        CustomPropertySetValues values = CustomPropertySetValues.empty();
        ServiceCategoryForTestingPurposes electricityAdditionalPK = ServiceCategoryForTestingPurposes.ELECTRICITY;
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), electricityAdditionalPK);
        BigDecimal expectedBillingCycle = BigDecimal.TEN;
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
        String expectedContractNumber = "createNonVersionedValues";
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), expectedContractNumber);

        try (TransactionContext ctx = transactionService.getContext()) {
            this.testInstance.setValuesFor(customPropertySet, testDomain, values, electricityAdditionalPK);

            // Business method
            this.testInstance.removeValuesFor(customPropertySet, testDomain, electricityAdditionalPK);

            // Asserts
            CustomPropertySetValues valuesAfterRemove = this.testInstance.getUniqueValuesFor(customPropertySet, testDomain, electricityAdditionalPK);
            assertThat(valuesAfterRemove.isEmpty()).isTrue();
        }
    }

    @Test
    public void createVersionedValues() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        VersionedCustomPropertySetForTestingPurposes customPropertySet = new VersionedCustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("createVersionedValues");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();

        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
        BigDecimal expectedBillingCycle = BigDecimal.TEN;
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
        String expectedContractNumber = "createVersionedValues";
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), expectedContractNumber);

        try (TransactionContext ctx = transactionService.getContext()) {
            // Business method
            this.testInstance.setValuesFor(customPropertySet, testDomain, values, Instant.now());

            // Asserts: not expecting any exceptions
        }
    }

    @Test
    public void updateVersionedValues() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        VersionedCustomPropertySetForTestingPurposes customPropertySet = new VersionedCustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("updateVersionedValues");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();

        CustomPropertySetValues initialValues = CustomPropertySetValues.empty();
        initialValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
        BigDecimal initialBillingCycle = BigDecimal.ONE;
        initialValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), initialBillingCycle);
        String initialContractNumber = "initialValue";
        initialValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), initialContractNumber);

        try (TransactionContext ctx = transactionService.getContext()) {
            this.testInstance.setValuesFor(customPropertySet, testDomain, initialValues, Instant.now());
            ctx.commit();
        }

        CustomPropertySetValues updateValues = CustomPropertySetValues.empty();
        updateValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
        BigDecimal expectedBillingCycle = BigDecimal.TEN;
        updateValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
        String expectedContractNumber = "updated!";
        updateValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), expectedContractNumber);

        try (TransactionContext ctx = transactionService.getContext()) {
            // Business method
            this.testInstance.setValuesFor(customPropertySet, testDomain, updateValues, Instant.now());

            // Asserts: not expecting any exceptions
        }
    }

    @Test(expected = CurrentUserIsNotAllowedToEditValuesOfCustomPropertySetException.class)
    public void updateVersionedValuesWithoutEditPrivileges() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        VersionedCustomPropertySetForTestingPurposes customPropertySet = new VersionedCustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("updateVersionedValues");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);

        CustomPropertySetValues initialValues = CustomPropertySetValues.empty();
        BigDecimal initialBillingCycle = BigDecimal.ONE;
        initialValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), initialBillingCycle);
        String initialContractNumber = "initialValue";
        initialValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), initialContractNumber);

        try (TransactionContext ctx = transactionService.getContext()) {
            this.testInstance.setValuesFor(customPropertySet, testDomain, initialValues, Instant.now());
            ctx.commit();
        }

        CustomPropertySetValues updateValues = CustomPropertySetValues.empty();
        BigDecimal expectedBillingCycle = BigDecimal.TEN;
        updateValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
        String expectedContractNumber = "updated!";
        updateValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), expectedContractNumber);

        try (TransactionContext ctx = transactionService.getContext()) {
            // Business method
            this.testInstance.setValuesFor(customPropertySet, testDomain, updateValues, Instant.now());

            // Asserts: not expecting any exceptions
        }
    }

    @Test
    public void verifyValuesAfterUpdateOfVersionedValues() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        VersionedCustomPropertySetForTestingPurposes customPropertySet = new VersionedCustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("verifyValuesAfterUpdateOfVersionedValues");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();

        CustomPropertySetValues initialValues = CustomPropertySetValues.empty();
        initialValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
        BigDecimal initialBillingCycle = BigDecimal.ONE;
        initialValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), initialBillingCycle);
        String initialContractNumber = "initialValue";
        initialValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), initialContractNumber);

        try (TransactionContext ctx = transactionService.getContext()) {
            this.testInstance.setValuesFor(customPropertySet, testDomain, initialValues, Instant.now());
            ctx.commit();
        }

        CustomPropertySetValues updateValues = CustomPropertySetValues.empty();
        updateValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
        BigDecimal expectedBillingCycle = BigDecimal.TEN;
        updateValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
        String expectedContractNumber = "updated!";
        updateValues.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), expectedContractNumber);

        try (TransactionContext ctx = transactionService.getContext()) {
            // Business method
            this.testInstance.setValuesFor(customPropertySet, testDomain, updateValues, Instant.now());

            // Asserts
            CustomPropertySetValues valuesForVerifying = this.testInstance.getUniqueValuesFor(customPropertySet, testDomain, Instant.now());
            assertThat(valuesForVerifying.getProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName())).isEqualToComparingFieldByField(expectedBillingCycle);
            assertThat(valuesForVerifying.getProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName())).isEqualToComparingFieldByField(expectedContractNumber);
        }

    }

    @Test(expected = UnsupportedOperationException.class)
    public void createVersionedValuesWithVersionedApi() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        VersionedCustomPropertySetForTestingPurposes customPropertySet = new VersionedCustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("createNonVersionedValues");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        CustomPropertySetValues values = CustomPropertySetValues.empty();

        try (TransactionContext ctx = transactionService.getContext()) {
            // Business method
            this.testInstance.setValuesFor(customPropertySet, testDomain, values);

            // Asserts: see expected exception rule
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = "CannotBeNull", property = "billingCycle")
    public void createVersionedValuesWithMissingRequiredProperty() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        VersionedCustomPropertySetForTestingPurposes customPropertySet = new VersionedCustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("createVersionedValuesWithMissingRequiredProperty");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), "dontCareBecauseWillFailAnyway");

        try (TransactionContext ctx = transactionService.getContext()) {
            // Business method
            this.testInstance.setValuesFor(customPropertySet, testDomain, values, Instant.now());

            // Asserts: see expected contraint violation rule
        }
    }

    @Test(expected = CurrentUserIsNotAllowedToEditValuesOfCustomPropertySetException.class)
    public void createVersionedValuesWithoutEditPrivileges() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        VersionedCustomPropertySetForTestingPurposes customPropertySet = new VersionedCustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("createNonVersionedValuesWithMissingRequiredProperty");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);

        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), BigDecimal.ONE);
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), "dontCareBecauseWillFailAnyway");

        try (TransactionContext ctx = transactionService.getContext()) {
            // Business method
            this.testInstance.setValuesFor(customPropertySet, testDomain, values, Instant.now());

            // Asserts: see expected contraint violation rule
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = "FieldTooLong", property = "contractNumber")
    public void createVersionedValuesWithTooLargeOptionalValue() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        VersionedCustomPropertySetForTestingPurposes customPropertySet = new VersionedCustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("createNonVersionedValuesWithTooLargeOptionalValue");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();

        CustomPropertySetValues values = CustomPropertySetValues.empty();
        BigDecimal expectedBillingCycle = BigDecimal.TEN;
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), Strings.repeat("Too long", 100));

        try (TransactionContext ctx = transactionService.getContext()) {
            // Business method
            this.testInstance.setValuesFor(customPropertySet, testDomain, values, Instant.now());

            // Asserts: see expected contraint violation rule
        }
    }

    @Test
    public void removeVersionedValues() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        VersionedCustomPropertySetForTestingPurposes customPropertySet = new VersionedCustomPropertySetForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("createVersionedValues");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();

        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
        BigDecimal expectedBillingCycle = BigDecimal.TEN;
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
        String expectedContractNumber = "createVersionedValues";
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), expectedContractNumber);

        try (TransactionContext ctx = transactionService.getContext()) {
            this.testInstance.setValuesFor(customPropertySet, testDomain, values, Instant.now());

            // Business method
            this.testInstance.removeValuesFor(customPropertySet, testDomain);

            // Asserts
            CustomPropertySetValues valuesAfterRemove = this.testInstance.getUniqueValuesFor(customPropertySet, testDomain, Instant.now());
            assertThat(valuesAfterRemove.isEmpty()).isTrue();
        }
    }

    @Test
    public void removeVersionedValuesOfCustomPropertySetWithAdditionalPrimaryKeys() {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        VersionedCustomPropertySetWithAdditionalPrimaryKeyColumnsForTestingPurposes customPropertySet = new VersionedCustomPropertySetWithAdditionalPrimaryKeyColumnsForTestingPurposes(propertySpecService);
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("createVersionedValues");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();

        CustomPropertySetValues values = CustomPropertySetValues.empty();
        ServiceCategoryForTestingPurposes electricityAdditionalPK = ServiceCategoryForTestingPurposes.ELECTRICITY;
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), electricityAdditionalPK);
        BigDecimal expectedBillingCycle = BigDecimal.TEN;
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
        String expectedContractNumber = "createVersionedValues";
        values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), expectedContractNumber);

        try (TransactionContext ctx = transactionService.getContext()) {
            this.testInstance.setValuesFor(customPropertySet, testDomain, values, Instant.now(), electricityAdditionalPK);

            // Business method
            this.testInstance.removeValuesFor(customPropertySet, testDomain, electricityAdditionalPK);

            // Asserts
            CustomPropertySetValues valuesAfterRemove = this.testInstance.getUniqueValuesFor(customPropertySet, testDomain, Instant.now(), electricityAdditionalPK);
            assertThat(valuesAfterRemove.isEmpty()).isTrue();
        }
    }

    @Test
    public void getRawValuesSql() throws SQLException {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        VersionedCustomPropertySetForTestingPurposes customPropertySet = new VersionedCustomPropertySetForTestingPurposes(propertySpecService);
        PropertySpec propertySpec =
                customPropertySet.getPropertySpecs()
                        .stream()
                        .filter(each -> each.getName().equals(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName()))
                        .findFirst()
                        .get();
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("getRawValuesSql");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();
        try (TransactionContext ctx = transactionService.getContext()) {
            CustomPropertySetValues values = CustomPropertySetValues.empty();
            values.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
            BigDecimal expectedBillingCycle = BigDecimal.TEN;
            values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
            values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), "createVersionedValues");
            this.testInstance.setValuesFor(customPropertySet, testDomain, values, Instant.now());
            ctx.commit();
        }

        // Business method
        SqlFragment rawValuesSql = this.testInstance.getRawValuesSql(customPropertySet, propertySpec, "value", testDomain, Range.all());
        System.out.println("rawValuesSql = " + rawValuesSql);

        // Asserts: execute the query first
        try (Connection connection = this.testInstance.getDataModel().getConnection(false)) {
            SqlBuilder sqlBuilder = new SqlBuilder("SELECT count(*) FROM (");
            sqlBuilder.add(rawValuesSql);
            sqlBuilder.append(")");
            try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    int count = resultSet.getInt(1);

                    // Asserts
                    assertThat(count).isEqualTo(1);
                }
            }
        }
    }

    @Test
    public void getRawValuesSqlWithAdditionalPrimaryKey() throws SQLException {
        PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
        VersionedCustomPropertySetWithAdditionalPrimaryKeyColumnsForTestingPurposes customPropertySet = new VersionedCustomPropertySetWithAdditionalPrimaryKeyColumnsForTestingPurposes(propertySpecService);
        PropertySpec propertySpec =
                customPropertySet.getPropertySpecs()
                    .stream()
                    .filter(each -> each.getName().equals(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName()))
                    .findFirst()
                    .get();
        OrmService ormService = injector.getInstance(OrmService.class);
        TestDomain testDomain = new TestDomain();
        try (TransactionContext ctx = transactionService.getContext()) {
            DataModel testDomainDataModel = TestDomain.install(ormService);
            testDomain.setName("getRawValuesSqlWithAdditionalPrimaryKey");
            testDomain.setDescription("for testing purposes only");
            testDomainDataModel.persist(testDomain);
            ctx.commit();
        }
        this.testInstance.addCustomPropertySet(customPropertySet);
        this.grantAllViewAndEditPrivilegesToPrincipal();
        try (TransactionContext ctx = transactionService.getContext()) {
            CustomPropertySetValues values = CustomPropertySetValues.empty();
            values.setProperty(DomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(), ServiceCategoryForTestingPurposes.ELECTRICITY);
            BigDecimal expectedBillingCycle = BigDecimal.TEN;
            values.setProperty(DomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName(), expectedBillingCycle);
            values.setProperty(DomainExtensionForTestingPurposes.FieldNames.CONTRACT_NUMBER.javaName(), "createVersionedValues");
            this.testInstance.setValuesFor(customPropertySet, testDomain, values, Instant.now(), ServiceCategoryForTestingPurposes.ELECTRICITY);
            ctx.commit();
        }

        // Business method
        SqlFragment rawValuesSql = this.testInstance.getRawValuesSql(customPropertySet, propertySpec, "value", testDomain, Range.all(), ServiceCategoryForTestingPurposes.ELECTRICITY);

        // Asserts: execute the query first
        try (Connection connection = this.testInstance.getDataModel().getConnection(false)) {
            SqlBuilder sqlBuilder = new SqlBuilder("SELECT count(*) FROM (");
            sqlBuilder.add(rawValuesSql);
            sqlBuilder.append(")");
            try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    int count = resultSet.getInt(1);

                    // Asserts
                    assertThat(count).isEqualTo(1);
                }
            }
        }
    }

    private void grantAllViewAndEditPrivilegesToPrincipal() {
        Set<Privilege> privileges = new HashSet<>();
        Privilege editPrivilege = mock(Privilege.class);
        when(editPrivilege.getName()).thenReturn(EditPrivilege.LEVEL_1.getPrivilege());
        privileges.add(editPrivilege);
        Privilege viewPrivilege = mock(Privilege.class);
        when(viewPrivilege.getName()).thenReturn(ViewPrivilege.LEVEL_1.getPrivilege());
        privileges.add(viewPrivilege);
        when(this.principal.getPrivileges()).thenReturn(privileges);
    }

    private abstract class LatchDrivenRunnable implements Runnable {

        private final TransactionService transactionService;
        private final ThreadPrincipalService threadPrincipalService;
        private final Principal principal;
        private final CustomPropertySetServiceImpl service;
        private final CountDownLatch startLatch;
        private final CountDownLatch stopLatch;

        LatchDrivenRunnable(TransactionService transactionService, ThreadPrincipalService threadPrincipalService, Principal principal, CustomPropertySetServiceImpl service, CountDownLatch startLatch, CountDownLatch stopLatch) {
            super();
            this.transactionService = transactionService;
            this.threadPrincipalService = threadPrincipalService;
            this.principal = principal;
            this.service = service;
            this.startLatch = startLatch;
            this.stopLatch = stopLatch;
        }

        @Override
        public void run() {
            try {
                this.startLatch.await();
                this.threadPrincipalService.set(this.principal);
                try (TransactionContext ctx = this.transactionService.getContext()) {
                    this.doRun(this.service);
                    ctx.commit();
                }
                this.stopLatch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                this.threadPrincipalService.clear();
            }
        }

        protected abstract void doRun(CustomPropertySetServiceImpl service);

    }

    private class Felix extends LatchDrivenRunnable {

        private Felix(TransactionService transactionService, ThreadPrincipalService threadPrincipalService, Principal principal, CustomPropertySetServiceImpl service, CountDownLatch startLatch, CountDownLatch stopLatch) {
            super(transactionService, threadPrincipalService, principal, service, startLatch, stopLatch);
        }

        @Override
        protected void doRun(CustomPropertySetServiceImpl service) {
            service.activate();
        }
    }

    private class AddCustomPropertySet extends LatchDrivenRunnable {

        AddCustomPropertySet(TransactionService transactionService, ThreadPrincipalService threadPrincipalService, Principal principal, CustomPropertySetServiceImpl service, CountDownLatch startLatch, CountDownLatch stopLatch) {
            super(transactionService, threadPrincipalService, principal, service, startLatch, stopLatch);
        }

        @Override
        protected void doRun(CustomPropertySetServiceImpl service) {
            PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
            CustomPropertySet customPropertySet = new CustomPropertySetForTestingPurposes(propertySpecService);
            service.addCustomPropertySet(customPropertySet);
        }
    }

    private class AddVersionedCustomPropertySet extends LatchDrivenRunnable {

        AddVersionedCustomPropertySet(TransactionService transactionService, ThreadPrincipalService threadPrincipalService, Principal principal, CustomPropertySetServiceImpl service, CountDownLatch startLatch, CountDownLatch stopLatch) {
            super(transactionService, threadPrincipalService, principal, service, startLatch, stopLatch);
        }

        @Override
        protected void doRun(CustomPropertySetServiceImpl service) {
            PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
            CustomPropertySet customPropertySet = new VersionedCustomPropertySetForTestingPurposes(propertySpecService);
            service.addCustomPropertySet(customPropertySet);
        }
    }

}