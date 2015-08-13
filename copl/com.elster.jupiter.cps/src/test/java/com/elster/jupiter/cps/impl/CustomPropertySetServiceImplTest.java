package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.PrimaryKeyConstraint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CustomPropertySetServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-12 (08:45)
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomPropertySetServiceImplTest {

    private static final String CUSTOM_PROPERTY_SET_COMPONENT_ID = "T01";
    private static final String CUSTOM_PROPERTY_SET_ID = "TEST";
    private static final String TABLE_NAME = "TST_TEST";
    private static final String DOMAIN_COLUMN_NAME = "testDomain";
    private static final String DOMAIN_FK_NAME = "FK_EXT_TESTDOMAIN";
    private static final String VERSIONED_CUSTOM_PROPERTY_SET_COMPONENT_ID = "T02";
    private static final String VERSIONED_CUSTOM_PROPERTY_SET_ID = CUSTOM_PROPERTY_SET_ID + "_VERSIONED";
    private static final String VERSIONED_TABLE_NAME = TABLE_NAME + "_VERSIONED";

    @Mock
    private TransactionService transactionService;
    @Mock
    private TransactionContext transactionContext;
    @Mock
    private OrmService ormService;
    @Mock
    private DataModel serviceDataModel;
    @Mock
    private DataModel customPropertySetDataModel;
    @Mock
    private DataModel versionedCustomPropertySetDataModel;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private CustomPropertySet<TestDomain, DomainExtensionForTestingPurposes> customPropertySet;
    @Mock
    private PersistenceSupport<TestDomain, DomainExtensionForTestingPurposes> persistenceSupport;
    @Mock
    private Table<DomainExtensionForTestingPurposes> table;
    @Mock
    private CustomPropertySet<TestDomain, VersionedDomainExtensionForTestingPurposes> versionedCustomPropertySet;
    @Mock
    private PersistenceSupport<TestDomain, VersionedDomainExtensionForTestingPurposes> versionedPersistenceSupport;
    @Mock
    private Table<VersionedDomainExtensionForTestingPurposes> versionedTable;
    @Mock
    private Column.Builder domainColumnBuilder;
    @Mock
    private Column domainColumn;
    @Mock
    private ForeignKeyConstraint.Builder domainForeignKeyConstraintBuilder;
    @Mock
    private Column.Builder customPropertySetColumnBuilder;
    @Mock
    private Column customPropertySetColumn;
    @Mock
    private ForeignKeyConstraint.Builder customPropertySetForeignKeyConstraintBuilder;
    @Mock
    private PrimaryKeyConstraint.Builder primaryKeyConstraintBuilder;
    @Mock
    private DataMapper<RegisteredCustomPropertySet> registeredCustomPropertySetMapper;
    @Mock
    private RegisteredCustomPropertySet registeredCustomPropertySet;

    @Before
    public void initializeMocks() {
        when(this.transactionService.getContext()).thenReturn(this.transactionContext);
        when(this.ormService.newDataModel(eq(CustomPropertySetService.COMPONENT_NAME), anyString())).thenReturn(this.serviceDataModel);
        when(this.ormService.newDataModel(eq(CUSTOM_PROPERTY_SET_COMPONENT_ID), anyString())).thenReturn(this.customPropertySetDataModel);
        when(this.ormService.newDataModel(eq(VERSIONED_CUSTOM_PROPERTY_SET_COMPONENT_ID), anyString())).thenReturn(this.versionedCustomPropertySetDataModel);
        when(this.nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(this.thesaurus);
        when(this.customPropertySet.isVersioned()).thenReturn(false);
        when(this.customPropertySet.componentName()).thenReturn(CUSTOM_PROPERTY_SET_COMPONENT_ID);
        when(this.customPropertySet.getId()).thenReturn(CUSTOM_PROPERTY_SET_ID);
        when(this.customPropertySet.getName()).thenReturn("For testing purposes only");
        when(this.customPropertySet.getPersistenceSupport()).thenReturn(this.persistenceSupport);
        when(this.customPropertySet.getDomainClass()).thenReturn(TestDomain.class);
        when(this.customPropertySetDataModel.addTable(TABLE_NAME, DomainExtensionForTestingPurposes.class)).thenReturn(this.table);
        when(this.persistenceSupport.tableName()).thenReturn(TABLE_NAME);
        when(this.persistenceSupport.domainColumnName()).thenReturn(DOMAIN_COLUMN_NAME);
        when(this.persistenceSupport.domainForeignKeyName()).thenReturn(DOMAIN_FK_NAME);
        when(this.persistenceSupport.getPersistenceClass()).thenReturn(DomainExtensionForTestingPurposes.class);
        when(this.table.column(DOMAIN_COLUMN_NAME)).thenReturn(this.domainColumnBuilder);
        when(this.table.foreignKey(DOMAIN_FK_NAME)).thenReturn(this.domainForeignKeyConstraintBuilder);
        when(this.table.column(HardCodedFieldNames.CUSTOM_PROPERTY_SET.databaseName())).thenReturn(this.customPropertySetColumnBuilder);
        when(this.table.foreignKey(startsWith("FK_CPS_"))).thenReturn(this.customPropertySetForeignKeyConstraintBuilder);
        when(this.table.primaryKey(startsWith("PK_CPS_"))).thenReturn(this.primaryKeyConstraintBuilder);
        when(this.versionedCustomPropertySet.isVersioned()).thenReturn(true);
        when(this.versionedCustomPropertySet.componentName()).thenReturn(VERSIONED_CUSTOM_PROPERTY_SET_COMPONENT_ID);
        when(this.versionedCustomPropertySet.getId()).thenReturn(VERSIONED_CUSTOM_PROPERTY_SET_ID);
        when(this.versionedCustomPropertySet.getName()).thenReturn(null);   // Will be ackward for UI but backend should not worry about that
        when(this.versionedCustomPropertySet.getDomainClass()).thenReturn(TestDomain.class);
        when(this.versionedCustomPropertySet.getPersistenceSupport()).thenReturn(this.versionedPersistenceSupport);
        when(this.versionedCustomPropertySetDataModel.addTable(VERSIONED_TABLE_NAME, VersionedDomainExtensionForTestingPurposes.class)).thenReturn(this.versionedTable);
        when(this.versionedTable.column(DOMAIN_COLUMN_NAME)).thenReturn(this.domainColumnBuilder);
        when(this.versionedPersistenceSupport.tableName()).thenReturn(VERSIONED_TABLE_NAME);
        when(this.versionedPersistenceSupport.domainColumnName()).thenReturn(DOMAIN_COLUMN_NAME);
        when(this.versionedPersistenceSupport.domainForeignKeyName()).thenReturn(DOMAIN_FK_NAME);
        when(this.versionedPersistenceSupport.getPersistenceClass()).thenReturn(VersionedDomainExtensionForTestingPurposes.class);
        when(this.versionedTable.column(DOMAIN_COLUMN_NAME)).thenReturn(this.domainColumnBuilder);
        when(this.versionedTable.foreignKey(DOMAIN_FK_NAME)).thenReturn(this.domainForeignKeyConstraintBuilder);
        when(this.versionedTable.column(HardCodedFieldNames.CUSTOM_PROPERTY_SET.databaseName())).thenReturn(this.customPropertySetColumnBuilder);
        when(this.versionedTable.foreignKey(startsWith("FK_CPS_"))).thenReturn(this.customPropertySetForeignKeyConstraintBuilder);
        when(this.versionedTable.primaryKey(startsWith("PK_CPS_"))).thenReturn(this.primaryKeyConstraintBuilder);
        Column intervalStartColumn = mock(Column.class);
        Column intervalEndColumn = mock(Column.class);
        when(this.versionedTable.addIntervalColumns(anyString())).thenReturn(Arrays.asList(intervalStartColumn, intervalEndColumn));
        when(this.domainColumnBuilder.notNull()).thenReturn(this.domainColumnBuilder);
        when(this.domainColumnBuilder.map(anyString())).thenReturn(this.domainColumnBuilder);
        when(this.domainColumnBuilder.number()).thenReturn(this.domainColumnBuilder);
        when(this.domainColumnBuilder.conversion(any(ColumnConversion.class))).thenReturn(this.domainColumnBuilder);
        when(this.domainColumnBuilder.skipOnUpdate()).thenReturn(this.domainColumnBuilder);
        when(this.domainColumnBuilder.add()).thenReturn(this.domainColumn);
        when(this.domainForeignKeyConstraintBuilder.on(anyVararg())).thenReturn(this.domainForeignKeyConstraintBuilder);
        when(this.domainForeignKeyConstraintBuilder.references(TestDomain.class)).thenReturn(this.domainForeignKeyConstraintBuilder);
        when(this.domainForeignKeyConstraintBuilder.map(anyString())).thenReturn(this.domainForeignKeyConstraintBuilder);
        when(this.customPropertySetColumnBuilder.notNull()).thenReturn(this.customPropertySetColumnBuilder);
        when(this.customPropertySetColumnBuilder.map(anyString())).thenReturn(this.customPropertySetColumnBuilder);
        when(this.customPropertySetColumnBuilder.number()).thenReturn(this.customPropertySetColumnBuilder);
        when(this.customPropertySetColumnBuilder.conversion(any(ColumnConversion.class))).thenReturn(this.customPropertySetColumnBuilder);
        when(this.customPropertySetColumnBuilder.skipOnUpdate()).thenReturn(this.customPropertySetColumnBuilder);
        when(this.customPropertySetColumnBuilder.add()).thenReturn(this.customPropertySetColumn);
        when(this.customPropertySetForeignKeyConstraintBuilder.on(anyVararg())).thenReturn(this.customPropertySetForeignKeyConstraintBuilder);
        when(this.customPropertySetForeignKeyConstraintBuilder.references(RegisteredCustomPropertySet.class)).thenReturn(this.customPropertySetForeignKeyConstraintBuilder);
        when(this.customPropertySetForeignKeyConstraintBuilder.map(anyString())).thenReturn(this.customPropertySetForeignKeyConstraintBuilder);
        when(this.primaryKeyConstraintBuilder.on(anyVararg())).thenReturn(this.primaryKeyConstraintBuilder);
        when(this.primaryKeyConstraintBuilder.allowZero()).thenReturn(this.primaryKeyConstraintBuilder);
    }

    @Test
    public void addNonVersionedCustomPropertySetAfterInstallation() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        // Avoid creating the RegisteredCustomPropertySet
        when(this.serviceDataModel.mapper(RegisteredCustomPropertySet.class)).thenReturn(this.registeredCustomPropertySetMapper);
        when(this.registeredCustomPropertySetMapper
                .getUnique(
                        eq(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName()),
                        anyString()))
                .thenReturn(Optional.of(this.registeredCustomPropertySet));

        // Business method
        service.addCustomPropertySet(this.customPropertySet);

        // Asserts
        verify(this.customPropertySet).componentName();
        verify(this.customPropertySet, atLeastOnce()).getId();
        verify(this.customPropertySet).isVersioned();
        verify(this.customPropertySet, never()).defaultViewPrivileges();    // Since we avoid to create the RegisteredCustomPropertySet
        verify(this.customPropertySet, never()).defaultEditPrivileges();    // Since we avoid to create the RegisteredCustomPropertySet
        verify(this.ormService).newDataModel(eq(CUSTOM_PROPERTY_SET_COMPONENT_ID), anyString());
        verify(this.persistenceSupport).tableName();
        verify(this.customPropertySetDataModel).addTable(TABLE_NAME, DomainExtensionForTestingPurposes.class);
        verify(this.persistenceSupport).domainColumnName();
        verify(this.table).column(DOMAIN_COLUMN_NAME);
        verify(this.domainColumnBuilder).add();
        verify(this.persistenceSupport).domainForeignKeyName();
        verify(this.table).foreignKey(DOMAIN_FK_NAME);
        verify(this.domainForeignKeyConstraintBuilder).add();
        verify(this.customPropertySetColumnBuilder).add();
        verify(this.table).foreignKey(startsWith("FK_CPS_"));
        verify(this.customPropertySetForeignKeyConstraintBuilder).add();
        verify(this.persistenceSupport).addCustomPropertyColumnsTo(this.table);
        verify(this.table).primaryKey(startsWith("PK_CPS_"));
        verify(this.primaryKeyConstraintBuilder).add();
        verify(this.customPropertySetDataModel).register(anyVararg());
        verify(this.customPropertySetDataModel).install(eq(true), anyBoolean());
    }

    @Test
    public void addNonVersionedCustomPropertySetBeforeInstallation() {
        when(this.serviceDataModel.isInstalled()).thenReturn(false);
        CustomPropertySetServiceImpl service = this.testInstance();

        // Business method
        service.addCustomPropertySet(this.customPropertySet);

        // Asserts
        verify(this.ormService, never()).newDataModel(eq(CUSTOM_PROPERTY_SET_ID), anyString());
    }

    @Test
    public void addNonVersionedCustomPropertySetBeforeActivation() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        // Avoid creating the RegisteredCustomPropertySet
        when(this.serviceDataModel.mapper(RegisteredCustomPropertySet.class)).thenReturn(this.registeredCustomPropertySetMapper);
        when(this.registeredCustomPropertySetMapper
                .getUnique(
                        eq(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName()),
                        anyString()))
                .thenReturn(Optional.of(this.registeredCustomPropertySet));
        CustomPropertySetServiceImpl testInstance = new CustomPropertySetServiceImpl();
        testInstance.setOrmService(this.ormService, false);
        testInstance.setNlsService(this.nlsService);
        testInstance.setTransactionService(this.transactionService);
        testInstance.addCustomPropertySet(this.customPropertySet);

        // Busines method
        testInstance.activate();

        // Asserts
        verify(this.customPropertySet).componentName();
        verify(this.customPropertySet, atLeastOnce()).getId();
        verify(this.customPropertySet).isVersioned();
        verify(this.customPropertySet, never()).defaultViewPrivileges();    // Since we avoid to create the RegisteredCustomPropertySet
        verify(this.customPropertySet, never()).defaultEditPrivileges();    // Since we avoid to create the RegisteredCustomPropertySet
        verify(this.ormService).newDataModel(eq(CUSTOM_PROPERTY_SET_COMPONENT_ID), anyString());
        verify(this.persistenceSupport).tableName();
        verify(this.customPropertySetDataModel).addTable(TABLE_NAME, DomainExtensionForTestingPurposes.class);
        verify(this.persistenceSupport).domainColumnName();
        verify(this.table).column(DOMAIN_COLUMN_NAME);
        verify(this.domainColumnBuilder).add();
        verify(this.persistenceSupport).domainForeignKeyName();
        verify(this.table).foreignKey(DOMAIN_FK_NAME);
        verify(this.domainForeignKeyConstraintBuilder).add();
        verify(this.customPropertySetColumnBuilder).add();
        verify(this.table).foreignKey(startsWith("FK_CPS_"));
        verify(this.customPropertySetForeignKeyConstraintBuilder).add();
        verify(this.persistenceSupport).addCustomPropertyColumnsTo(this.table);
        verify(this.table).primaryKey(startsWith("PK_CPS_"));
        verify(this.primaryKeyConstraintBuilder).add();
        verify(this.customPropertySetDataModel).register(anyVararg());
        verify(this.customPropertySetDataModel).install(eq(true), anyBoolean());
    }

    @Test
    public void addVersionedCustomPropertySetAfterInstallation() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        // Avoid creating the RegisteredCustomPropertySet
        when(this.serviceDataModel.mapper(RegisteredCustomPropertySet.class)).thenReturn(this.registeredCustomPropertySetMapper);
        when(this.registeredCustomPropertySetMapper
                .getUnique(
                        eq(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName()),
                        anyString()))
                .thenReturn(Optional.of(this.registeredCustomPropertySet));

        // Business method
        service.addCustomPropertySet(this.versionedCustomPropertySet);

        // Asserts
        verify(this.versionedCustomPropertySet).componentName();
        verify(this.versionedCustomPropertySet, atLeastOnce()).getId();
        verify(this.versionedCustomPropertySet).isVersioned();
        verify(this.versionedCustomPropertySet, never()).defaultViewPrivileges();    // Since we avoid to create the RegisteredCustomPropertySet
        verify(this.versionedCustomPropertySet, never()).defaultEditPrivileges();    // Since we avoid to create the RegisteredCustomPropertySet
        verify(this.ormService).newDataModel(eq(VERSIONED_CUSTOM_PROPERTY_SET_COMPONENT_ID), anyString());
        verify(this.versionedPersistenceSupport, atLeastOnce()).tableName();
        verify(this.versionedCustomPropertySetDataModel).addTable(VERSIONED_TABLE_NAME, VersionedDomainExtensionForTestingPurposes.class);
        verify(this.versionedPersistenceSupport).domainColumnName();
        verify(this.versionedTable).setJournalTableName(anyString());
        verify(this.versionedTable).column(DOMAIN_COLUMN_NAME);
        verify(this.domainColumnBuilder).add();
        verify(this.versionedPersistenceSupport).domainForeignKeyName();
        verify(this.versionedTable).foreignKey(DOMAIN_FK_NAME);
        verify(this.domainForeignKeyConstraintBuilder).add();
        verify(this.customPropertySetColumnBuilder).add();
        verify(this.versionedTable).foreignKey(startsWith("FK_CPS_"));
        verify(this.customPropertySetForeignKeyConstraintBuilder).add();
        verify(this.versionedPersistenceSupport).addCustomPropertyColumnsTo(this.versionedTable);
        verify(this.versionedTable).primaryKey(startsWith("PK_CPS_"));
        verify(this.primaryKeyConstraintBuilder).add();
        verify(this.versionedTable).addIntervalColumns(HardCodedFieldNames.INTERVAL.javaName());
        verify(this.versionedCustomPropertySetDataModel).register(anyVararg());
        verify(this.versionedCustomPropertySetDataModel).install(eq(true), anyBoolean());
    }

    @Test
    public void addVersionedCustomPropertySetBeforeActivation() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        // Avoid creating the RegisteredCustomPropertySet
        when(this.serviceDataModel.mapper(RegisteredCustomPropertySet.class)).thenReturn(this.registeredCustomPropertySetMapper);
        when(this.registeredCustomPropertySetMapper
                .getUnique(
                        eq(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName()),
                        anyString()))
                .thenReturn(Optional.of(this.registeredCustomPropertySet));
        CustomPropertySetServiceImpl testInstance = new CustomPropertySetServiceImpl();
        testInstance.setOrmService(this.ormService, false);
        testInstance.setNlsService(this.nlsService);
        testInstance.setTransactionService(this.transactionService);
        testInstance.addCustomPropertySet(this.versionedCustomPropertySet);

        // Busines method
        testInstance.activate();

        // Asserts
        verify(this.versionedCustomPropertySet).componentName();
        verify(this.versionedCustomPropertySet, atLeastOnce()).getId();
        verify(this.versionedCustomPropertySet).isVersioned();
        verify(this.versionedCustomPropertySet, never()).defaultViewPrivileges();    // Since we avoid to create the RegisteredCustomPropertySet
        verify(this.versionedCustomPropertySet, never()).defaultEditPrivileges();    // Since we avoid to create the RegisteredCustomPropertySet
        verify(this.ormService).newDataModel(eq(VERSIONED_CUSTOM_PROPERTY_SET_COMPONENT_ID), anyString());
        verify(this.versionedPersistenceSupport, atLeastOnce()).tableName();
        verify(this.versionedCustomPropertySetDataModel).addTable(VERSIONED_TABLE_NAME, VersionedDomainExtensionForTestingPurposes.class);
        verify(this.versionedPersistenceSupport).domainColumnName();
        verify(this.versionedTable).setJournalTableName(anyString());
        verify(this.versionedTable).column(DOMAIN_COLUMN_NAME);
        verify(this.domainColumnBuilder).add();
        verify(this.versionedPersistenceSupport).domainForeignKeyName();
        verify(this.versionedTable).foreignKey(DOMAIN_FK_NAME);
        verify(this.domainForeignKeyConstraintBuilder).add();
        verify(this.customPropertySetColumnBuilder).add();
        verify(this.versionedTable).foreignKey(startsWith("FK_CPS_"));
        verify(this.customPropertySetForeignKeyConstraintBuilder).add();
        verify(this.versionedPersistenceSupport).addCustomPropertyColumnsTo(this.versionedTable);
        verify(this.versionedTable).primaryKey(startsWith("PK_CPS_"));
        verify(this.primaryKeyConstraintBuilder).add();
        verify(this.versionedTable).addIntervalColumns(HardCodedFieldNames.INTERVAL.javaName());
        verify(this.versionedCustomPropertySetDataModel).register(anyVararg());
        verify(this.versionedCustomPropertySetDataModel).install(eq(true), anyBoolean());
    }

    @Test
    public void addVersionedCustomPropertySetBeforeInstallation() {
        when(this.serviceDataModel.isInstalled()).thenReturn(false);
        CustomPropertySetServiceImpl service = this.testInstance();

        // Business method
        service.addCustomPropertySet(this.versionedCustomPropertySet);

        // Asserts
        verify(this.ormService, never()).newDataModel(eq(VERSIONED_CUSTOM_PROPERTY_SET_ID), anyString());
    }

    @Test(timeout = 5000)
    public void addCustomPropertySetsWhileActivating() throws InterruptedException {
        when(this.serviceDataModel.isInstalled()).thenReturn(false);
        CustomPropertySetServiceImpl testInstance = new CustomPropertySetServiceImpl();
        testInstance.setOrmService(this.ormService, false);
        testInstance.setNlsService(this.nlsService);
        testInstance.setTransactionService(this.transactionService);

        /* Create 3 threads that will wait on CountdownLatch to start simultaneously
         *    1. activate the service
         *    2. Add non versioned CustomPropertySet
         *    3. Add versioned CustomPropertySet */
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch stopLatch = new CountDownLatch(3);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.execute(new Felix(testInstance, startLatch, stopLatch));
        executorService.execute(new AddCustomPropertySet(testInstance, startLatch, stopLatch));
        executorService.execute(new AddVersionedCustomPropertySet(testInstance, startLatch, stopLatch));

        // Here is where all the action will happen
        startLatch.countDown();

        // Now wait until all 3 threads have completed
        stopLatch.await();

        // Asserts
        verify(this.ormService, never()).newDataModel(eq(CUSTOM_PROPERTY_SET_ID), anyString());
        verify(this.ormService, never()).newDataModel(eq(VERSIONED_CUSTOM_PROPERTY_SET_ID), anyString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCustomPropertiesWhenSetIsNotRegisteredYet() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        // Avoid creating the RegisteredCustomPropertySet
        when(this.serviceDataModel.mapper(RegisteredCustomPropertySet.class)).thenReturn(this.registeredCustomPropertySetMapper);
        when(this.registeredCustomPropertySetMapper
                .getUnique(
                        eq(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName()),
                        anyString()))
                .thenReturn(Optional.of(this.registeredCustomPropertySet));

        // Business method
        service.getValuesFor(this.customPropertySet, new TestDomain(1L));

        // Asserts: see expected exception rule
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getNonVersionedCustomPropertiesForVersionedSet() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        // Avoid creating the RegisteredCustomPropertySet
        when(this.serviceDataModel.mapper(RegisteredCustomPropertySet.class)).thenReturn(this.registeredCustomPropertySetMapper);
        when(this.registeredCustomPropertySetMapper
                .getUnique(
                        eq(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName()),
                        anyString()))
                .thenReturn(Optional.of(this.registeredCustomPropertySet));
        service.addCustomPropertySet(this.versionedCustomPropertySet);

        // Business method
        service.getValuesFor(this.versionedCustomPropertySet, new TestDomain(1L));

        // Asserts: see expected exception rule
    }

    @Test
    public void getCustomPropertiesThatDoNotExistYet() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        // Avoid creating the RegisteredCustomPropertySet
        when(this.serviceDataModel.mapper(RegisteredCustomPropertySet.class)).thenReturn(this.registeredCustomPropertySetMapper);
        when(this.registeredCustomPropertySetMapper
                .getUnique(
                        eq(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName()),
                        anyString()))
                .thenReturn(Optional.of(this.registeredCustomPropertySet));
        service.addCustomPropertySet(this.customPropertySet);
        DataMapper<DomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.getOptional(anyVararg())).thenReturn(Optional.<DomainExtensionForTestingPurposes>empty());
        when(this.customPropertySetDataModel.mapper(DomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);

        // Business method
        CustomPropertySetValues properties = service.getValuesFor(this.customPropertySet, new TestDomain(1L));

        // Asserts
        assertThat(properties.isEmpty()).isTrue();
    }

    @Test
    public void getCustomProperties() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        // Avoid creating the RegisteredCustomPropertySet
        when(this.serviceDataModel.mapper(RegisteredCustomPropertySet.class)).thenReturn(this.registeredCustomPropertySetMapper);
        when(this.registeredCustomPropertySetMapper
                .getUnique(
                        eq(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName()),
                        anyString()))
                .thenReturn(Optional.of(this.registeredCustomPropertySet));
        service.addCustomPropertySet(this.customPropertySet);
        DomainExtensionForTestingPurposes extension = mock(DomainExtensionForTestingPurposes.class);
        DataMapper<DomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.getOptional(anyVararg())).thenReturn(Optional.of(extension));
        when(this.customPropertySetDataModel.mapper(DomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);

        // Business method
        CustomPropertySetValues properties = service.getValuesFor(this.customPropertySet, new TestDomain(1L));

        // Asserts
        verify(extension).copyTo(properties);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getVersionedCustomPropertiesWhenSetIsNotRegisteredYet() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        // Avoid creating the RegisteredCustomPropertySet
        when(this.serviceDataModel.mapper(RegisteredCustomPropertySet.class)).thenReturn(this.registeredCustomPropertySetMapper);
        when(this.registeredCustomPropertySetMapper
                .getUnique(
                        eq(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName()),
                        anyString()))
                .thenReturn(Optional.of(this.registeredCustomPropertySet));

        // Business method
        service.getValuesFor(this.versionedCustomPropertySet, new TestDomain(1L), Instant.now());

        // Asserts: see expected exception rule
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getVersionedCustomPropertiesForNonVersionedSet() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        // Avoid creating the RegisteredCustomPropertySet
        when(this.serviceDataModel.mapper(RegisteredCustomPropertySet.class)).thenReturn(this.registeredCustomPropertySetMapper);
        when(this.registeredCustomPropertySetMapper
                .getUnique(
                        eq(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName()),
                        anyString()))
                .thenReturn(Optional.of(this.registeredCustomPropertySet));
        service.addCustomPropertySet(this.customPropertySet);

        // Business method
        service.getValuesFor(this.customPropertySet, new TestDomain(1L), Instant.now());

        // Asserts: see expected exception rule
    }

    @Test
    public void getVersionedCustomPropertiesThatDoNotExistYet() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        // Avoid creating the RegisteredCustomPropertySet
        when(this.serviceDataModel.mapper(RegisteredCustomPropertySet.class)).thenReturn(this.registeredCustomPropertySetMapper);
        when(this.registeredCustomPropertySetMapper
                .getUnique(
                        eq(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName()),
                        anyString()))
                .thenReturn(Optional.of(this.registeredCustomPropertySet));
        service.addCustomPropertySet(this.versionedCustomPropertySet);
        DataMapper<VersionedDomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.getOptional(anyVararg())).thenReturn(Optional.<VersionedDomainExtensionForTestingPurposes>empty());
        when(this.versionedCustomPropertySetDataModel.mapper(VersionedDomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);

        // Business method
        CustomPropertySetValues properties = service.getValuesFor(this.versionedCustomPropertySet, new TestDomain(1L), Instant.now());

        // Asserts
        assertThat(properties.isEmpty()).isTrue();
    }

    @Test
    public void getVersionedCustomProperties() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        // Avoid creating the RegisteredCustomPropertySet
        when(this.serviceDataModel.mapper(RegisteredCustomPropertySet.class)).thenReturn(this.registeredCustomPropertySetMapper);
        when(this.registeredCustomPropertySetMapper
                .getUnique(
                        eq(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName()),
                        anyString()))
                .thenReturn(Optional.of(this.registeredCustomPropertySet));
        service.addCustomPropertySet(this.versionedCustomPropertySet);
        VersionedDomainExtensionForTestingPurposes extension = mock(VersionedDomainExtensionForTestingPurposes.class);
        DataMapper<VersionedDomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.getOptional(anyVararg())).thenReturn(Optional.of(extension));
        when(this.versionedCustomPropertySetDataModel.mapper(VersionedDomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);

        // Business method
        CustomPropertySetValues properties = service.getValuesFor(this.versionedCustomPropertySet, new TestDomain(1L), Instant.now());

        // Asserts
        verify(extension).copyTo(properties);
    }

    private CustomPropertySetServiceImpl testInstance() {
        CustomPropertySetServiceImpl testInstance = new CustomPropertySetServiceImpl();
        testInstance.setOrmService(this.ormService, false);
        testInstance.setNlsService(this.nlsService);
        testInstance.setTransactionService(this.transactionService);
        testInstance.activate();
        return testInstance;
    }

    private abstract class LatchDrivenRunnable implements Runnable {
        private final CustomPropertySetServiceImpl service;
        private final CountDownLatch startLatch;
        private final CountDownLatch stopLatch;

        protected LatchDrivenRunnable(CustomPropertySetServiceImpl service, CountDownLatch startLatch, CountDownLatch stopLatch) {
            super();
            this.service = service;
            this.startLatch = startLatch;
            this.stopLatch = stopLatch;
        }

        @Override
        public void run() {
            try {
                this.startLatch.await();
                this.doRun(this.service);
                this.stopLatch.countDown();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        protected abstract void doRun(CustomPropertySetServiceImpl service);

    }
    private class Felix extends LatchDrivenRunnable {
        private Felix(CustomPropertySetServiceImpl service, CountDownLatch startLatch, CountDownLatch stopLatch) {
            super(service, startLatch, stopLatch);
        }

        @Override
        protected void doRun(CustomPropertySetServiceImpl service) {
            service.activate();
        }
    }

    private class AddCustomPropertySet extends LatchDrivenRunnable {

        protected AddCustomPropertySet(CustomPropertySetServiceImpl service, CountDownLatch startLatch, CountDownLatch stopLatch) {
            super(service, startLatch, stopLatch);
        }

        @Override
        protected void doRun(CustomPropertySetServiceImpl service) {
            service.addCustomPropertySet(CustomPropertySetServiceImplTest.this.customPropertySet);
        }
    }

    private class AddVersionedCustomPropertySet extends LatchDrivenRunnable {

        protected AddVersionedCustomPropertySet(CustomPropertySetServiceImpl service, CountDownLatch startLatch, CountDownLatch stopLatch) {
            super(service, startLatch, stopLatch);
        }

        @Override
        protected void doRun(CustomPropertySetServiceImpl service) {
            service.addCustomPropertySet(CustomPropertySetServiceImplTest.this.versionedCustomPropertySet);
        }
    }

}