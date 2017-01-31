/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.PrimaryKeyConstraint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.RangeComparatorFactory;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.security.Principal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
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
    private static final String ADDITIONAL_COLUMN_NAME = "adiitionalColumn";
    private static final String DOMAIN_FK_NAME = "FK_EXT_TESTDOMAIN";
    private static final String VERSIONED_CUSTOM_PROPERTY_SET_COMPONENT_ID = "T02";
    private static final String VERSIONED_CUSTOM_PROPERTY_SET_ADD_COMPONENT_ID = "T03";
    private static final String VERSIONED_CUSTOM_PROPERTY_SET_ID = CUSTOM_PROPERTY_SET_ID + "_VERSIONED";
    private static final String VERSIONED_TABLE_NAME = TABLE_NAME + "_VERSIONED";
    private static final String VERSIONED_WITH_ADDITIONAL_KEY__TABLE_NAME = TABLE_NAME + "_ADD_VERSIONED";

    @Mock
    private TransactionService transactionService;
    @Mock
    private TransactionContext transactionContext;
    @Mock
    private OrmService ormService;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;
    @Mock
    private DataModel serviceDataModel;
    @Mock
    private DataModel customPropertySetDataModel;
    @Mock
    private DataModel versionedCustomPropertySetDataModel;
    @Mock
    private DataModel versionedCustomPropertySetWithAdditionalKeyDataModel;
    @Mock
    private NlsService nlsService;
    @Mock
    private UserService userService;
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
    private CustomPropertySet<TestDomain, VersionedDomainExtensionWithAdditionalPrimaryKeyForTestingPurposes> versionedCustomPropertySetWithAdditionalPrimaryKey;
    @Mock
    private PersistenceSupport<TestDomain, VersionedDomainExtensionWithAdditionalPrimaryKeyForTestingPurposes> versionedPersistenceSupportWithAdditionalPrimaryKey;
    @Mock
    private Table<VersionedDomainExtensionForTestingPurposes> versionedTable;
    @Mock
    private Table<VersionedDomainExtensionWithAdditionalPrimaryKeyForTestingPurposes> versionedWithAdditionalPrimaryKeyTable;
    @Mock
    private Column.Builder domainColumnBuilder;
    @Mock
    private Column domainColumn;
    @Mock
    private Column additionalPrimaryKeyColumn;
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
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private SearchService searchService;
    @Mock
    private UpgradeService upgradeService;

    @Before
    public void initializeMocks() {
        when(this.transactionService.getContext()).thenReturn(this.transactionContext);
        when(this.ormService.newDataModel(eq(CustomPropertySetService.COMPONENT_NAME), anyString())).thenReturn(this.serviceDataModel);
        when(this.ormService.newDataModel(eq(CUSTOM_PROPERTY_SET_COMPONENT_ID), anyString())).thenReturn(this.customPropertySetDataModel);
        when(this.ormService.newDataModel(eq(VERSIONED_CUSTOM_PROPERTY_SET_COMPONENT_ID), anyString())).thenReturn(this.versionedCustomPropertySetDataModel);
        when(this.ormService.newDataModel(eq(VERSIONED_CUSTOM_PROPERTY_SET_ADD_COMPONENT_ID), anyString())).thenReturn(this.versionedCustomPropertySetWithAdditionalKeyDataModel);
        when(this.nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(this.thesaurus);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(this.customPropertySet.isVersioned()).thenReturn(false);
        when(this.customPropertySet.getId()).thenReturn(CUSTOM_PROPERTY_SET_ID);
        when(this.customPropertySet.getName()).thenReturn("For testing purposes only");
        when(this.customPropertySet.getPersistenceSupport()).thenReturn(this.persistenceSupport);
        when(this.customPropertySet.getDomainClass()).thenReturn(TestDomain.class);
        when(this.customPropertySetDataModel.addTable(TABLE_NAME, DomainExtensionForTestingPurposes.class)).thenReturn(this.table);
        when(this.persistenceSupport.application()).thenReturn("Example");
        when(this.persistenceSupport.componentName()).thenReturn(CUSTOM_PROPERTY_SET_COMPONENT_ID);
        when(this.persistenceSupport.tableName()).thenReturn(TABLE_NAME);
        when(this.persistenceSupport.domainColumnName()).thenReturn(DOMAIN_COLUMN_NAME);
        when(this.persistenceSupport.domainForeignKeyName()).thenReturn(DOMAIN_FK_NAME);
        when(this.persistenceSupport.persistenceClass()).thenReturn(DomainExtensionForTestingPurposes.class);
        when(this.persistenceSupport.module()).thenReturn(Optional.empty());
        when(this.table.column(DOMAIN_COLUMN_NAME)).thenReturn(this.domainColumnBuilder);
        when(this.table.foreignKey(DOMAIN_FK_NAME)).thenReturn(this.domainForeignKeyConstraintBuilder);
        when(this.table.column(HardCodedFieldNames.CUSTOM_PROPERTY_SET.databaseName())).thenReturn(this.customPropertySetColumnBuilder);
        when(this.table.foreignKey(startsWith("FK_CPS_"))).thenReturn(this.customPropertySetForeignKeyConstraintBuilder);
        when(this.table.primaryKey(startsWith("PK_CPS_"))).thenReturn(this.primaryKeyConstraintBuilder);
        when(this.versionedCustomPropertySet.isVersioned()).thenReturn(true);
        when(this.versionedCustomPropertySet.getId()).thenReturn(VERSIONED_CUSTOM_PROPERTY_SET_ID);
        when(this.versionedCustomPropertySet.getName()).thenReturn(null);   // Will be ackward for UI but backend should not worry about that
        when(this.versionedCustomPropertySet.getDomainClass()).thenReturn(TestDomain.class);
        when(this.versionedCustomPropertySet.getPersistenceSupport()).thenReturn(this.versionedPersistenceSupport);
        when(this.versionedCustomPropertySetWithAdditionalPrimaryKey.isVersioned()).thenReturn(true);
        when(this.versionedCustomPropertySetWithAdditionalPrimaryKey.getId()).thenReturn(VERSIONED_CUSTOM_PROPERTY_SET_ID);
        when(this.versionedCustomPropertySetWithAdditionalPrimaryKey.getName()).thenReturn(null);   // Will be ackward for UI but backend should not worry about that
        when(this.versionedCustomPropertySetWithAdditionalPrimaryKey.getDomainClass()).thenReturn(TestDomain.class);
        when(this.versionedCustomPropertySetWithAdditionalPrimaryKey.getPersistenceSupport()).thenReturn(this.versionedPersistenceSupportWithAdditionalPrimaryKey);
        when(this.versionedCustomPropertySetDataModel.addTable(VERSIONED_TABLE_NAME, VersionedDomainExtensionForTestingPurposes.class)).thenReturn(this.versionedTable);
        when(this.versionedTable.column(DOMAIN_COLUMN_NAME)).thenReturn(this.domainColumnBuilder);
        when(this.versionedPersistenceSupport.application()).thenReturn("Example");
        when(this.versionedPersistenceSupport.componentName()).thenReturn(VERSIONED_CUSTOM_PROPERTY_SET_COMPONENT_ID);
        when(this.versionedPersistenceSupportWithAdditionalPrimaryKey.componentName()).thenReturn(VERSIONED_CUSTOM_PROPERTY_SET_ADD_COMPONENT_ID);
        when(this.versionedPersistenceSupport.tableName()).thenReturn(VERSIONED_TABLE_NAME);
        when(this.versionedPersistenceSupport.domainColumnName()).thenReturn(DOMAIN_COLUMN_NAME);
        when(this.versionedPersistenceSupport.domainForeignKeyName()).thenReturn(DOMAIN_FK_NAME);
        when(this.versionedPersistenceSupport.persistenceClass()).thenReturn(VersionedDomainExtensionForTestingPurposes.class);
        when(this.versionedPersistenceSupport.module()).thenReturn(Optional.empty());
        when(this.versionedCustomPropertySetWithAdditionalKeyDataModel.addTable(VERSIONED_WITH_ADDITIONAL_KEY__TABLE_NAME, VersionedDomainExtensionWithAdditionalPrimaryKeyForTestingPurposes.class)).thenReturn(this.versionedWithAdditionalPrimaryKeyTable);
        doReturn(this.versionedWithAdditionalPrimaryKeyTable).when(this.versionedCustomPropertySetWithAdditionalKeyDataModel).getTable(VERSIONED_WITH_ADDITIONAL_KEY__TABLE_NAME);
        when(this.versionedPersistenceSupportWithAdditionalPrimaryKey.application()).thenReturn("Example");
        when(this.versionedPersistenceSupportWithAdditionalPrimaryKey.componentName()).thenReturn(VERSIONED_CUSTOM_PROPERTY_SET_ADD_COMPONENT_ID);
        when(this.versionedPersistenceSupportWithAdditionalPrimaryKey.tableName()).thenReturn(VERSIONED_WITH_ADDITIONAL_KEY__TABLE_NAME);
        when(this.versionedPersistenceSupportWithAdditionalPrimaryKey.domainColumnName()).thenReturn(DOMAIN_COLUMN_NAME);
        when(this.versionedPersistenceSupportWithAdditionalPrimaryKey.domainForeignKeyName()).thenReturn(DOMAIN_FK_NAME);
        when(this.versionedPersistenceSupportWithAdditionalPrimaryKey.addCustomPropertyPrimaryKeyColumnsTo(versionedWithAdditionalPrimaryKeyTable)).thenReturn(Collections.singletonList(additionalPrimaryKeyColumn));
        when(this.versionedPersistenceSupportWithAdditionalPrimaryKey.persistenceClass()).thenReturn(VersionedDomainExtensionWithAdditionalPrimaryKeyForTestingPurposes.class);
        when(this.versionedPersistenceSupportWithAdditionalPrimaryKey.module()).thenReturn(Optional.empty());
        when(this.versionedTable.column(DOMAIN_COLUMN_NAME)).thenReturn(this.domainColumnBuilder);
        when(this.versionedTable.foreignKey(DOMAIN_FK_NAME)).thenReturn(this.domainForeignKeyConstraintBuilder);
        when(this.versionedTable.column(HardCodedFieldNames.CUSTOM_PROPERTY_SET.databaseName())).thenReturn(this.customPropertySetColumnBuilder);
        when(this.versionedTable.foreignKey(startsWith("FK_CPS_"))).thenReturn(this.customPropertySetForeignKeyConstraintBuilder);
        when(this.versionedTable.primaryKey(startsWith("PK_CPS_"))).thenReturn(this.primaryKeyConstraintBuilder);
        Column intervalStartColumn = mock(Column.class);
        Column intervalEndColumn = mock(Column.class);
        when(domainColumn.getFieldName()).thenReturn(DOMAIN_COLUMN_NAME);
        when(domainColumn.getName()).thenReturn(DOMAIN_COLUMN_NAME);
        when(additionalPrimaryKeyColumn.getFieldName()).thenReturn(ADDITIONAL_COLUMN_NAME);
        when(additionalPrimaryKeyColumn.getName()).thenReturn(ADDITIONAL_COLUMN_NAME);
        when(this.versionedTable.addIntervalColumns(anyString())).thenReturn(Arrays.asList(intervalStartColumn, intervalEndColumn));
        when(this.versionedWithAdditionalPrimaryKeyTable.column(DOMAIN_COLUMN_NAME)).thenReturn(this.domainColumnBuilder);
        when(this.versionedWithAdditionalPrimaryKeyTable.foreignKey(DOMAIN_FK_NAME)).thenReturn(this.domainForeignKeyConstraintBuilder);
        when(this.versionedWithAdditionalPrimaryKeyTable.column(HardCodedFieldNames.CUSTOM_PROPERTY_SET.databaseName())).thenReturn(this.customPropertySetColumnBuilder);
        when(this.versionedWithAdditionalPrimaryKeyTable.foreignKey(startsWith("FK_CPS_"))).thenReturn(this.customPropertySetForeignKeyConstraintBuilder);
        when(this.versionedWithAdditionalPrimaryKeyTable.primaryKey(startsWith("PK_CPS_"))).thenReturn(this.primaryKeyConstraintBuilder);
        when(this.versionedWithAdditionalPrimaryKeyTable.addIntervalColumns(anyString())).thenReturn(Arrays.asList(intervalStartColumn, intervalEndColumn));
        doReturn(Arrays.asList(domainColumn, additionalPrimaryKeyColumn)).when(this.versionedWithAdditionalPrimaryKeyTable).getPrimaryKeyColumns();
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
        when(this.serviceDataModel.getValidatorFactory()).thenReturn(this.validatorFactory);
        when(this.validatorFactory.getValidator()).thenReturn(this.validator);
        when(this.serviceDataModel.mapper(RegisteredCustomPropertySet.class)).thenReturn(this.registeredCustomPropertySetMapper);
        when(this.registeredCustomPropertySetMapper
                .getUnique(
                        eq(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName()),
                        anyString()))
                .thenReturn(Optional.empty());
    }

    @Test
    public void getKeysDoesNotReturnNull() {
        // Business method @ assert
        assertThat(this.testInstance().getKeys()).isNotNull();
    }

    @Test
    public void getComponentNameDoesNotReturnEmptyString() {
        // Business method @ assert
        assertThat(this.testInstance().getComponentName()).isNotEmpty();
    }

    @Test
    public void getLayerDoesNotReturnNull() {
        // Business method @ assert
        assertThat(this.testInstance().getLayer()).isNotNull();
    }

    @Test
    public void addCustomPropertySet_SetsAndClearsPrincipal() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.customPropertySet.getId()).thenReturn("addCustomPropertySet_SetsAndClearsPrincipal");
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));

        // Business method
        service.addCustomPropertySet(this.customPropertySet);

        // Asserts
        verify(this.threadPrincipalService).set(any(Principal.class));
        verify(this.threadPrincipalService).clear();
    }

    @Test
    public void addSystemCustomPropertySet_SetsAndClearsPrincipal() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.customPropertySet.getId()).thenReturn("addSystemCustomPropertySet_SetsAndClearsPrincipal");
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));

        // Business method
        service.addSystemCustomPropertySet(this.customPropertySet);

        // Asserts
        verify(this.threadPrincipalService).set(any(Principal.class));
        verify(this.threadPrincipalService).clear();
    }

    @Test
    public void addNonVersionedCustomPropertySetAfterInstallation() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.customPropertySet.getId()).thenReturn("addNonVersionedCustomPropertySetAfterInstallation");
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));

        // Business method
        service.addCustomPropertySet(this.customPropertySet);

        // Asserts
        verify(this.customPropertySet, atLeastOnce()).getId();
        verify(this.customPropertySet).isVersioned();
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
        verify(this.persistenceSupport).addCustomPropertyColumnsTo(eq(this.table), any(List.class));
        verify(this.table).primaryKey(startsWith("PK_CPS_"));
        verify(this.primaryKeyConstraintBuilder).add();
        verify(this.customPropertySetDataModel).register(anyVararg());
        verify(this.upgradeService).register(eq(InstallIdentifier.identifier("Example", CUSTOM_PROPERTY_SET_COMPONENT_ID)), eq(customPropertySetDataModel), any(), any());
    }

    @Test
    public void addNonVersionedCustomPropertySetBeforeInstallation() {
        when(this.serviceDataModel.isInstalled()).thenReturn(false);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.customPropertySet.getId()).thenReturn("addNonVersionedCustomPropertySetBeforeInstallation");

        // Business method
        service.addCustomPropertySet(this.customPropertySet);

        // Asserts
        verify(this.ormService, never()).newDataModel(eq(CUSTOM_PROPERTY_SET_ID), anyString());
    }

    @Test
    public void addNonVersionedCustomPropertySetBeforeActivation() {
        CustomPropertySetServiceImpl testInstance = new CustomPropertySetServiceImpl();
        testInstance.setOrmService(this.ormService, false);
        testInstance.setNlsService(this.nlsService);
        testInstance.setTransactionService(this.transactionService);
        testInstance.setSearchService(this.searchService);
        testInstance.addCustomPropertySet(this.customPropertySet);
        testInstance.setUpgradeService(upgradeService);
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, testInstance));
        when(this.customPropertySet.getId()).thenReturn("addNonVersionedCustomPropertySetBeforeActivation");

        // Busines method
        testInstance.activate();

        // Asserts
        verify(this.customPropertySet, atLeastOnce()).getId();
        verify(this.customPropertySet).isVersioned();
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
        verify(this.persistenceSupport).addCustomPropertyColumnsTo(eq(this.table), any(List.class));
        verify(this.table).primaryKey(startsWith("PK_CPS_"));
        verify(this.primaryKeyConstraintBuilder).add();
        verify(this.customPropertySetDataModel).register(anyVararg());
        verify(this.upgradeService).register(eq(InstallIdentifier.identifier("Example", CUSTOM_PROPERTY_SET_COMPONENT_ID)), eq(customPropertySetDataModel), any(), any());
    }

    @Test
    public void addVersionedCustomPropertySetAfterInstallation() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("addVersionedCustomPropertySetAfterInstallation");

        // Business method
        service.addCustomPropertySet(this.versionedCustomPropertySet);

        // Asserts
        verify(this.versionedCustomPropertySet, atLeastOnce()).getId();
        verify(this.versionedCustomPropertySet).isVersioned();
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
        verify(this.versionedPersistenceSupport).addCustomPropertyColumnsTo(eq(this.versionedTable), any(List.class));
        verify(this.versionedTable).primaryKey(startsWith("PK_CPS_"));
        verify(this.primaryKeyConstraintBuilder).add();
        verify(this.versionedTable).addIntervalColumns(HardCodedFieldNames.INTERVAL.javaName());
        verify(this.versionedCustomPropertySetDataModel).register(anyVararg());
        verify(this.upgradeService).register(eq(InstallIdentifier.identifier("Example", VERSIONED_CUSTOM_PROPERTY_SET_COMPONENT_ID)), eq(versionedCustomPropertySetDataModel), any(), any());
    }

    @Test
    public void addVersionedCustomPropertySetBeforeActivation() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        when(this.versionedCustomPropertySet.getId()).thenReturn("addVersionedCustomPropertySetBeforeActivation");
        CustomPropertySetServiceImpl testInstance = new CustomPropertySetServiceImpl();
        testInstance.setOrmService(this.ormService, false);
        testInstance.setNlsService(this.nlsService);
        testInstance.setSearchService(this.searchService);
        testInstance.setTransactionService(this.transactionService);
        testInstance.addCustomPropertySet(this.versionedCustomPropertySet);
        testInstance.setUpgradeService(upgradeService);
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, testInstance));

        // Busines method
        testInstance.activate();

        // Asserts
        verify(this.versionedCustomPropertySet, atLeastOnce()).getId();
        verify(this.versionedCustomPropertySet).isVersioned();
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
        verify(this.versionedPersistenceSupport).addCustomPropertyColumnsTo(eq(this.versionedTable), any(List.class));
        verify(this.versionedTable).primaryKey(startsWith("PK_CPS_"));
        verify(this.primaryKeyConstraintBuilder).add();
        verify(this.versionedTable).addIntervalColumns(HardCodedFieldNames.INTERVAL.javaName());
        verify(this.versionedCustomPropertySetDataModel).register(anyVararg());
        verify(this.upgradeService).register(eq(InstallIdentifier.identifier("Example", VERSIONED_CUSTOM_PROPERTY_SET_COMPONENT_ID)), eq(versionedCustomPropertySetDataModel), any(), any());
    }

    @Test
    public void addVersionedCustomPropertySetBeforeInstallation() {
        when(this.serviceDataModel.isInstalled()).thenReturn(false);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("addVersionedCustomPropertySetBeforeInstallation");

        // Business method
        service.addCustomPropertySet(this.versionedCustomPropertySet);

        // Asserts
        verify(this.ormService, never()).newDataModel(eq(VERSIONED_CUSTOM_PROPERTY_SET_ID), anyString());
    }

    @Test
    public void addVersionedCustomPropertySetWithAdditionalPrimartKeyAfterInstallation() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySetWithAdditionalPrimaryKey.getId()).thenReturn("addVersionedCustomPropertySetAfterInstallation");

        // Business method
        service.addCustomPropertySet(this.versionedCustomPropertySetWithAdditionalPrimaryKey);

        // Asserts
        verify(this.versionedCustomPropertySetWithAdditionalPrimaryKey, atLeastOnce()).getId();
        verify(this.versionedCustomPropertySetWithAdditionalPrimaryKey).isVersioned();
        verify(this.ormService).newDataModel(eq(VERSIONED_CUSTOM_PROPERTY_SET_ADD_COMPONENT_ID), anyString());
        verify(this.versionedPersistenceSupportWithAdditionalPrimaryKey, atLeastOnce()).tableName();
        verify(this.versionedCustomPropertySetWithAdditionalKeyDataModel).addTable(VERSIONED_WITH_ADDITIONAL_KEY__TABLE_NAME, VersionedDomainExtensionWithAdditionalPrimaryKeyForTestingPurposes.class);
        verify(this.versionedPersistenceSupportWithAdditionalPrimaryKey).domainColumnName();
        verify(this.versionedWithAdditionalPrimaryKeyTable).setJournalTableName(anyString());
        verify(this.versionedWithAdditionalPrimaryKeyTable).column(DOMAIN_COLUMN_NAME);
        verify(this.domainColumnBuilder).add();
        verify(this.versionedPersistenceSupportWithAdditionalPrimaryKey).domainForeignKeyName();
        verify(this.versionedWithAdditionalPrimaryKeyTable).foreignKey(DOMAIN_FK_NAME);
        verify(this.domainForeignKeyConstraintBuilder).add();
        verify(this.customPropertySetColumnBuilder).add();
        verify(this.versionedWithAdditionalPrimaryKeyTable).foreignKey(startsWith("FK_CPS_"));
        verify(this.customPropertySetForeignKeyConstraintBuilder).add();
        verify(this.versionedPersistenceSupportWithAdditionalPrimaryKey).addCustomPropertyColumnsTo(eq(this.versionedWithAdditionalPrimaryKeyTable), eq(Arrays.asList(additionalPrimaryKeyColumn)));
        verify(this.primaryKeyConstraintBuilder).add();
        verify(this.versionedWithAdditionalPrimaryKeyTable).addIntervalColumns(HardCodedFieldNames.INTERVAL.javaName());
        verify(this.versionedCustomPropertySetWithAdditionalKeyDataModel).register(anyVararg());
        verify(this.upgradeService).register(eq(InstallIdentifier.identifier("Example", VERSIONED_CUSTOM_PROPERTY_SET_ADD_COMPONENT_ID)), eq(versionedCustomPropertySetWithAdditionalKeyDataModel), any(), any());
    }

    @Test(timeout = 5000)
    public void addCustomPropertySetsWhileActivating() throws InterruptedException {
        when(this.serviceDataModel.isInstalled()).thenReturn(false);
        CustomPropertySetServiceImpl testInstance = new CustomPropertySetServiceImpl();
        testInstance.setOrmService(this.ormService, false);
        testInstance.setNlsService(this.nlsService);
        testInstance.setTransactionService(this.transactionService);
        testInstance.setUpgradeService(upgradeService);
        testInstance.setSearchService(searchService);
        testInstance.setThreadPrincipalService(threadPrincipalService);
        when(this.customPropertySet.getId()).thenReturn("addCustomPropertySetsWhileActivating-nonversioned");
        when(this.versionedCustomPropertySet.getId()).thenReturn("addCustomPropertySetsWhileActivating-versioned");
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, testInstance));

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
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.customPropertySet.getId()).thenReturn("getCustomPropertiesWhenSetIsNotRegisteredYet");

        // Business method
        service.getUniqueValuesFor(this.customPropertySet, new TestDomain(1L));

        // Asserts: see expected exception rule
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getNonVersionedCustomPropertiesForVersionedSet() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("getNonVersionedCustomPropertiesForVersionedSet");
        service.addCustomPropertySet(this.versionedCustomPropertySet);

        // Business method
        service.getUniqueValuesFor(this.versionedCustomPropertySet, new TestDomain(1L));

        // Asserts: see expected exception rule
    }

    @Test
    public void getCustomPropertiesThatDoNotExistYet() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.customPropertySet.getId()).thenReturn("getCustomPropertiesThatDoNotExistYet");
        service.addCustomPropertySet(this.customPropertySet);
        DataMapper<DomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.getOptional(anyVararg())).thenReturn(Optional.<DomainExtensionForTestingPurposes>empty());
        when(this.customPropertySetDataModel.mapper(DomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);

        // Business method
        CustomPropertySetValues properties = service.getUniqueValuesFor(this.customPropertySet, new TestDomain(1L));

        // Asserts
        assertThat(properties.isEmpty()).isTrue();
        assertThat(properties.getEffectiveRange().hasLowerBound()).isTrue();
        assertThat(properties.getEffectiveRange().lowerEndpoint()).isEqualTo(Instant.EPOCH);
        assertThat(properties.getEffectiveRange().hasUpperBound()).isFalse();
    }

    @Test
    public void getCustomProperties() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.customPropertySet.getId()).thenReturn("getCustomProperties");
        service.addCustomPropertySet(this.customPropertySet);
        DomainExtensionForTestingPurposes extension = mock(DomainExtensionForTestingPurposes.class);
        DataMapper<DomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.select(any(Condition.class))).thenReturn(Collections.singletonList(extension));
        when(this.customPropertySetDataModel.mapper(DomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);

        // Business method
        CustomPropertySetValues properties = service.getUniqueValuesFor(this.customPropertySet, new TestDomain(1L));

        // Asserts
        verify(extension).copyTo(properties);
        assertThat(properties.getEffectiveRange().hasLowerBound()).isTrue();
        assertThat(properties.getEffectiveRange().lowerEndpoint()).isEqualTo(Instant.EPOCH);
        assertThat(properties.getEffectiveRange().hasUpperBound()).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getVersionedCustomPropertiesWhenSetIsNotRegisteredYet() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("getVersionedCustomPropertiesWhenSetIsNotRegisteredYet");

        // Business method
        service.getUniqueValuesFor(this.versionedCustomPropertySet, new TestDomain(1L), Instant.now());

        // Asserts: see expected exception rule
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getVersionedCustomPropertiesForNonVersionedSet() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        service.addCustomPropertySet(this.customPropertySet);

        // Business method
        service.getUniqueValuesFor(this.customPropertySet, new TestDomain(1L), Instant.now());

        // Asserts: see expected exception rule
    }

    @Test
    public void getVersionedCustomPropertiesThatDoNotExistYet() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("getVersionedCustomPropertiesThatDoNotExistYet");
        service.addCustomPropertySet(this.versionedCustomPropertySet);
        DataMapper<VersionedDomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.getOptional(anyVararg())).thenReturn(Optional.<VersionedDomainExtensionForTestingPurposes>empty());
        when(this.versionedCustomPropertySetDataModel.mapper(VersionedDomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);

        // Business method
        CustomPropertySetValues properties = service.getUniqueValuesFor(this.versionedCustomPropertySet, new TestDomain(1L), Instant.now());

        // Asserts
        assertThat(properties.isEmpty()).isTrue();
        assertThat(properties.getEffectiveRange().hasLowerBound()).isTrue();
        assertThat(properties.getEffectiveRange().lowerEndpoint()).isEqualTo(Instant.EPOCH);
        assertThat(properties.getEffectiveRange().hasUpperBound()).isFalse();
    }

    @Test
    public void getVersionedCustomProperties() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("getVersionedCustomProperties");
        service.addCustomPropertySet(this.versionedCustomPropertySet);
        TestDomain testDomain = new TestDomain(1L);
        Interval expectedInterval = Interval.startAt(Instant.ofEpochSecond(1000L));
        VersionedDomainExtensionForTestingPurposes extension = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, expectedInterval);
        DataMapper<VersionedDomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.select(any(Condition.class))).thenReturn(Collections.singletonList(extension));
        when(this.versionedCustomPropertySetDataModel.mapper(VersionedDomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);

        // Business method
        CustomPropertySetValues properties = service.getUniqueValuesFor(this.versionedCustomPropertySet, testDomain, Instant.now());

        // Asserts
        assertThat(extension.getInterval()).isEqualTo(expectedInterval);
        assertThat(properties.propertyNames())
                .containsOnly(
                        VersionedDomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName(),
                        VersionedDomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName());
    }

    @Test
    public void hasValueForPropertySpecs() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("getVersionedCustomProperties");
        service.addCustomPropertySet(this.versionedCustomPropertySet);
        TestDomain testDomain = new TestDomain(1L);
        Interval expectedInterval = Interval.startAt(Instant.ofEpochSecond(1000L));
        VersionedDomainExtensionForTestingPurposes extension = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, expectedInterval);
        DataMapper<VersionedDomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.select(any(Condition.class))).thenReturn(Collections.singletonList(extension));
        when(this.versionedCustomPropertySetDataModel.mapper(VersionedDomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);

        // Business method
        Set<PropertySpec> specs = new HashSet<>();
        PropertySpec serviceCategorySpec = mock(PropertySpec.class);
        when(serviceCategorySpec.getName()).thenReturn(VersionedDomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName());
        PropertySpec billingCycleSpec = mock(PropertySpec.class);
        when(billingCycleSpec.getName()).thenReturn(VersionedDomainExtensionForTestingPurposes.FieldNames.BILLING_CYCLE.javaName());
        specs.add(serviceCategorySpec);
        specs.add(billingCycleSpec);

        assertTrue("For both of the given specs, a value should be present, so expecting this call returns true", service.hasValueForPropertySpecs(this.versionedCustomPropertySet, testDomain, Instant.now(), specs));
    }

    @Test
    public void hasNoValueForMissingPropertySpecs() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("getVersionedCustomProperties");
        service.addCustomPropertySet(this.versionedCustomPropertySet);
        TestDomain testDomain = new TestDomain(1L);
        Interval expectedInterval = Interval.startAt(Instant.ofEpochSecond(1000L));
        VersionedDomainExtensionForTestingPurposes extension = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, expectedInterval);
        DataMapper<VersionedDomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.select(any(Condition.class))).thenReturn(Collections.singletonList(extension));
        when(this.versionedCustomPropertySetDataModel.mapper(VersionedDomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);

        // Business method
        Set<PropertySpec> specs = new HashSet<>();
        PropertySpec serviceCategorySpec = mock(PropertySpec.class);
        when(serviceCategorySpec.getName()).thenReturn(VersionedDomainExtensionForTestingPurposes.FieldNames.SERVICE_CATEGORY.javaName());
        PropertySpec domainSpec = mock(PropertySpec.class);
        when(domainSpec.getName()).thenReturn(VersionedDomainExtensionForTestingPurposes.FieldNames.DOMAIN.javaName());
        specs.add(serviceCategorySpec);
        specs.add(domainSpec);

        assertFalse("As specs contains 'Domain' spec, for which no value exists, expecting false", service.hasValueForPropertySpecs(this.versionedCustomPropertySet, testDomain, Instant.now(), specs));
    }

    @Test
    public void findCustomPropertySetAfterAdd() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("findCustomPropertySetAfterAdd");
        service.addCustomPropertySet(this.versionedCustomPropertySet);

        // Business method
        Optional<CustomPropertySet> expectedPresent = service.findRegisteredCustomPropertySet(this.versionedCustomPropertySet.getId());

        // Asserts
        assertThat(expectedPresent).isPresent();
    }

    @Test
    public void cannotFindCustomPropertySetAfterRemoval() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("cannotFindCustomPropertySetAfterRemoval");
        service.addCustomPropertySet(this.versionedCustomPropertySet);

        // Business method
        service.removeCustomPropertySet(this.versionedCustomPropertySet);

        // Asserts
        Optional<CustomPropertySet> expectedEmpty = service.findRegisteredCustomPropertySet(this.versionedCustomPropertySet.getId());
        assertThat(expectedEmpty).isEmpty();
    }

    @Test
    public void findSystemCustomPropertySetAfterAdd() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("findSystemCustomPropertySetAfterAdd");
        service.addSystemCustomPropertySet(this.versionedCustomPropertySet);

        // Business method
        Optional<CustomPropertySet> expectedPresent = service.findRegisteredCustomPropertySet(this.versionedCustomPropertySet.getId());

        // Asserts
        assertThat(expectedPresent).isPresent();
    }

    @Test
    public void cannotFindSystemCustomPropertySetAfterRemoval() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("cannotFindSystemCustomPropertySetAfterRemoval");
        service.addSystemCustomPropertySet(this.versionedCustomPropertySet);

        // Business method
        service.removeSystemCustomPropertySet(this.versionedCustomPropertySet);

        // Asserts
        Optional<CustomPropertySet> expectedEmpty = service.findRegisteredCustomPropertySet(this.versionedCustomPropertySet.getId());
        assertThat(expectedEmpty).isEmpty();
    }

    @Test
    public void getAllVersionedValues() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("getVersionedCustomProperties");
        service.addCustomPropertySet(this.versionedCustomPropertySet);
        TestDomain testDomain = new TestDomain(1L);
        Interval firstInterval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(1L), Instant.ofEpochSecond(3L)));
        Interval secondInterval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(3L), Instant.ofEpochSecond(5L)));
        VersionedDomainExtensionForTestingPurposes extensionFirst = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, firstInterval);
        VersionedDomainExtensionForTestingPurposes extensionSecond = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, secondInterval);
        DataMapper<VersionedDomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.select(any(Condition.class), any(Order.class))).thenReturn(Arrays.asList(extensionFirst, extensionSecond));
        when(this.versionedCustomPropertySetDataModel.mapper(VersionedDomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);

        // Business method
        List<CustomPropertySetValues> values = service.getAllVersionedValuesFor(this.versionedCustomPropertySet, testDomain);

        // Asserts
        assertThat(values).isNotNull();
        assertThat(values).isNotEmpty();
        assertThat(values.get(0)).isNotNull();
        assertThat(values.get(0).getEffectiveRange()).isEqualTo(firstInterval.toClosedOpenRange());
        assertThat(values.get(0).propertyNames().contains("billingCycle")).isTrue();
        assertThat(values.get(0).propertyNames().contains("serviceCategory")).isTrue();
        assertThat(values.get(1)).isNotNull();
        assertThat(values.get(1).getEffectiveRange()).isEqualTo(secondInterval.toClosedOpenRange());
    }

    @Test
    public void getAllVersionedValuesWithAdditionalPrimaryKey() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySetWithAdditionalPrimaryKey.getId()).thenReturn("getVersionedCustomPropertiesWithAdditionalPrimaryKey");
        service.addCustomPropertySet(this.versionedCustomPropertySetWithAdditionalPrimaryKey);
        TestDomain testDomain = new TestDomain(1L);
        Interval firstInterval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(1L), Instant.ofEpochSecond(3L)));
        Interval secondInterval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(3L), Instant.ofEpochSecond(5L)));
        VersionedDomainExtensionWithAdditionalPrimaryKeyForTestingPurposes extensionFirst = new VersionedDomainExtensionWithAdditionalPrimaryKeyForTestingPurposes(testDomain, this.registeredCustomPropertySet, firstInterval);
        VersionedDomainExtensionWithAdditionalPrimaryKeyForTestingPurposes extensionSecond = new VersionedDomainExtensionWithAdditionalPrimaryKeyForTestingPurposes(testDomain, this.registeredCustomPropertySet, secondInterval);
        DataMapper<VersionedDomainExtensionWithAdditionalPrimaryKeyForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.select(any(Condition.class), any(Order.class))).thenReturn(Arrays.asList(extensionFirst, extensionSecond));
        when(this.versionedCustomPropertySetWithAdditionalKeyDataModel.mapper(VersionedDomainExtensionWithAdditionalPrimaryKeyForTestingPurposes.class)).thenReturn(dataMapper);

        // Business method
        List<CustomPropertySetValues> values = service.getAllVersionedValuesFor(this.versionedCustomPropertySetWithAdditionalPrimaryKey, testDomain, ServiceCategoryForTestingPurposes.ELECTRICITY);

        // Asserts
        assertThat(values).isNotNull();
        assertThat(values).isNotEmpty();
        assertThat(values.get(0)).isNotNull();
        assertThat(values.get(0).getEffectiveRange()).isEqualTo(firstInterval.toClosedOpenRange());
        assertThat(values.get(0).propertyNames().contains("billingCycle")).isTrue();
        assertThat(values.get(0).propertyNames().contains("serviceCategory")).isTrue();
        assertThat(values.get(1)).isNotNull();
        assertThat(values.get(1).getEffectiveRange()).isEqualTo(secondInterval.toClosedOpenRange());
        assertThat(extensionFirst.getServiceCategory()).isEqualTo(ServiceCategoryForTestingPurposes.ELECTRICITY);
        assertThat(extensionSecond.getServiceCategory()).isEqualTo(ServiceCategoryForTestingPurposes.ELECTRICITY);
    }

    @Test
    public void getVersionedCustomPropertiesConflicts() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("getVersionedCustomProperties");
        service.addCustomPropertySet(this.versionedCustomPropertySet);
        TestDomain testDomain = new TestDomain(1L);
        Interval firstInterval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(1L), Instant.ofEpochSecond(3L)));
        Interval secondInterval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(3L), Instant.ofEpochSecond(5L)));
        VersionedDomainExtensionForTestingPurposes extensionFirst = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, firstInterval);
        VersionedDomainExtensionForTestingPurposes extensionSecond = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, secondInterval);
        DataMapper<VersionedDomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.select(any(Condition.class), any(Order.class))).thenReturn(Arrays.asList(extensionFirst, extensionSecond));
        when(this.versionedCustomPropertySetDataModel.mapper(VersionedDomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);
        Range<Instant> candidateRange = Range.closedOpen(Instant.ofEpochSecond(2L), Instant.ofEpochSecond(5L));

        // Business method
        OverlapCalculatorBuilder builder = service.calculateOverlapsFor(this.versionedCustomPropertySet, testDomain);
        List<ValuesRangeConflict> conflicts = builder.whenCreating(candidateRange);
        Collections.sort(conflicts, Comparator.comparing(ValuesRangeConflict::getConflictingRange, RangeComparatorFactory.INSTANT_DEFAULT));

        // Asserts
        assertThat(conflicts).isNotNull();
        assertThat(conflicts).isNotEmpty();
        assertThat(conflicts.get(0)).isNotNull();
        assertThat(conflicts.get(0).getConflictingRange()).isEqualTo(Range.closedOpen(Instant.ofEpochSecond(2L), Instant.ofEpochSecond(3L)));
        assertThat(conflicts.get(0).getType()).isEqualTo(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END);
        assertThat(Interval.of(conflicts.get(0).getValues().getEffectiveRange())).isEqualTo(extensionFirst.getInterval());
        assertThat(conflicts.get(1)).isNotNull();
        assertThat(conflicts.get(1).getType()).isEqualTo(ValuesRangeConflictType.RANGE_INSERTED);
        assertThat(Interval.of(conflicts.get(1).getValues().getEffectiveRange())).isEqualTo(Interval.of(candidateRange));
        assertThat(conflicts.get(2)).isNotNull();
        assertThat(conflicts.get(2).getConflictingRange()).isEqualTo(Range.closedOpen(Instant.ofEpochSecond(3L), Instant.ofEpochSecond(5L)));
        assertThat(conflicts.get(2).getType()).isEqualTo(ValuesRangeConflictType.RANGE_OVERLAP_DELETE);
        assertThat(Interval.of(conflicts.get(2).getValues().getEffectiveRange())).isEqualTo(extensionSecond.getInterval());
    }

    @Test
    public void getVersionedCustomPropertiesConflictsOneInfiniteInterval() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("getVersionedCustomProperties");
        service.addCustomPropertySet(this.versionedCustomPropertySet);
        TestDomain testDomain = new TestDomain(1L);
        Interval interval = Interval.forever();
        VersionedDomainExtensionForTestingPurposes extension = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, interval);
        DataMapper<VersionedDomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.select(any(Condition.class), any(Order.class))).thenReturn(Collections.singletonList(extension));
        when(this.versionedCustomPropertySetDataModel.mapper(VersionedDomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);
        Range<Instant> candidateRange = Range.closedOpen(Instant.ofEpochSecond(2L), Instant.ofEpochSecond(5L));

        // Business method
        OverlapCalculatorBuilder builder = service.calculateOverlapsFor(this.versionedCustomPropertySet, testDomain);
        List<ValuesRangeConflict> conflicts = builder.whenCreating(candidateRange);
        Collections.sort(conflicts, Comparator.comparing(ValuesRangeConflict::getConflictingRange, RangeComparatorFactory.INSTANT_DEFAULT));

        // Asserts
        assertThat(conflicts).isNotNull();
        assertThat(conflicts).isNotEmpty();
        assertThat(conflicts.get(0)).isNotNull();
        assertThat(conflicts.get(0).getConflictingRange()).isEqualTo(Range.closedOpen(Instant.ofEpochSecond(2L), Instant.ofEpochSecond(5L)));
        assertThat(conflicts.get(0).getType()).isEqualTo(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END);
        assertThat(Interval.of(conflicts.get(0).getValues().getEffectiveRange())).isEqualTo(extension.getInterval());
        assertThat(conflicts.get(1)).isNotNull();
        assertThat(conflicts.get(1).getType()).isEqualTo(ValuesRangeConflictType.RANGE_INSERTED);
        assertThat(Interval.of(conflicts.get(1).getValues().getEffectiveRange())).isEqualTo(Interval.of(candidateRange));
    }

    @Test
    public void getVersionedCustomPropertiesConflictsTwoInfiniteIntervals() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("getVersionedCustomProperties");
        service.addCustomPropertySet(this.versionedCustomPropertySet);
        TestDomain testDomain = new TestDomain(1L);
        Interval firstInterval = Interval.of(Range.lessThan(Instant.ofEpochSecond(3L)));
        Interval secondInterval = Interval.of(Range.atLeast(Instant.ofEpochSecond(3L)));
        VersionedDomainExtensionForTestingPurposes extensionFirst = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, firstInterval);
        VersionedDomainExtensionForTestingPurposes extensionSecond = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, secondInterval);
        DataMapper<VersionedDomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.select(any(Condition.class), any(Order.class))).thenReturn(Arrays.asList(extensionFirst, extensionSecond));
        when(this.versionedCustomPropertySetDataModel.mapper(VersionedDomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);
        Range<Instant> candidateRange = Range.closedOpen(Instant.ofEpochSecond(2L), Instant.ofEpochSecond(5L));

        // Business method
        OverlapCalculatorBuilder builder = service.calculateOverlapsFor(this.versionedCustomPropertySet, testDomain);
        List<ValuesRangeConflict> conflicts = builder.whenCreating(candidateRange);
        Collections.sort(conflicts, Comparator.comparing(ValuesRangeConflict::getConflictingRange, RangeComparatorFactory.INSTANT_DEFAULT));

        // Asserts
        assertThat(conflicts).isNotNull();
        assertThat(conflicts).isNotEmpty();
        assertThat(conflicts.get(0)).isNotNull();
        assertThat(conflicts.get(0).getConflictingRange()).isEqualTo(Range.closedOpen(Instant.ofEpochSecond(2L), Instant.ofEpochSecond(3L)));
        assertThat(conflicts.get(0).getType()).isEqualTo(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END);
        assertThat(Interval.of(conflicts.get(0).getValues().getEffectiveRange())).isEqualTo(extensionFirst.getInterval());
        assertThat(conflicts.get(1)).isNotNull();
        assertThat(conflicts.get(1).getType()).isEqualTo(ValuesRangeConflictType.RANGE_INSERTED);
        assertThat(Interval.of(conflicts.get(1).getValues().getEffectiveRange())).isEqualTo(Interval.of(candidateRange));
        assertThat(conflicts.get(2)).isNotNull();
        assertThat(conflicts.get(2).getConflictingRange()).isEqualTo(Range.closedOpen(Instant.ofEpochSecond(3L), Instant.ofEpochSecond(5L)));
        assertThat(conflicts.get(2).getType()).isEqualTo(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_START);
        assertThat(Interval.of(conflicts.get(2).getValues().getEffectiveRange())).isEqualTo(extensionSecond.getInterval());
    }

    @Test
    public void getVersionedCustomPropertiesConflictsOverlapWhenEqualStartPoint() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("getVersionedCustomProperties");
        service.addCustomPropertySet(this.versionedCustomPropertySet);
        TestDomain testDomain = new TestDomain(1L);
        Interval firstInterval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(1L), Instant.ofEpochSecond(3L)));
        Interval secondInterval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(3L), Instant.ofEpochSecond(7L)));
        Interval thirdInterval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(7L), Instant.ofEpochSecond(9L)));
        VersionedDomainExtensionForTestingPurposes extensionFirst = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, firstInterval);
        VersionedDomainExtensionForTestingPurposes extensionSecond = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, secondInterval);
        VersionedDomainExtensionForTestingPurposes extensionThird = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, thirdInterval);
        DataMapper<VersionedDomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.select(any(Condition.class), any(Order.class))).thenReturn(Arrays.asList(extensionFirst, extensionSecond, extensionThird));
        when(this.versionedCustomPropertySetDataModel.mapper(VersionedDomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);
        Range<Instant> candidateRange = Range.closedOpen(Instant.ofEpochSecond(3L), Instant.ofEpochSecond(5L));

        // Business method
        OverlapCalculatorBuilder builder = service.calculateOverlapsFor(this.versionedCustomPropertySet, testDomain);
        List<ValuesRangeConflict> conflicts = builder.whenCreating(candidateRange);
        Collections.sort(conflicts, Comparator.comparing(conflict -> conflict.getValues().getEffectiveRange(), RangeComparatorFactory.INSTANT_DEFAULT));

        // Asserts
        assertThat(conflicts).isNotNull();
        assertThat(conflicts).isNotEmpty();
        assertThat(conflicts.get(0)).isNotNull();
        assertThat(conflicts.get(0).getType()).isEqualTo(ValuesRangeConflictType.RANGE_INSERTED);
        assertThat(Interval.of(conflicts.get(0).getValues().getEffectiveRange())).isEqualTo(Interval.of(candidateRange));
        assertThat(conflicts.get(1)).isNotNull();
        assertThat(conflicts.get(1).getConflictingRange()).isEqualTo(Range.closedOpen(Instant.ofEpochSecond(3L), Instant.ofEpochSecond(5L)));
        assertThat(conflicts.get(1).getType()).isEqualTo(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_START);
        assertThat(Interval.of(conflicts.get(1).getValues().getEffectiveRange())).isEqualTo(extensionSecond.getInterval());
    }

    @Test
    public void getVersionedCustomPropertiesConflictsOverlapWhenEqualStartPointAndEnclosing() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("getVersionedCustomProperties");
        service.addCustomPropertySet(this.versionedCustomPropertySet);
        TestDomain testDomain = new TestDomain(1L);
        Interval firstInterval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(1L), Instant.ofEpochSecond(3L)));
        Interval secondInterval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(3L), Instant.ofEpochSecond(7L)));
        Interval thirdInterval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(7L), Instant.ofEpochSecond(9L)));
        VersionedDomainExtensionForTestingPurposes extensionFirst = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, firstInterval);
        VersionedDomainExtensionForTestingPurposes extensionSecond = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, secondInterval);
        VersionedDomainExtensionForTestingPurposes extensionThird = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, thirdInterval);
        DataMapper<VersionedDomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.select(any(Condition.class), any(Order.class))).thenReturn(Arrays.asList(extensionFirst, extensionSecond, extensionThird));
        when(this.versionedCustomPropertySetDataModel.mapper(VersionedDomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);
        Range<Instant> candidateRange = Range.closedOpen(Instant.ofEpochSecond(3L), Instant.ofEpochSecond(8L));

        // Business method
        OverlapCalculatorBuilder builder = service.calculateOverlapsFor(this.versionedCustomPropertySet, testDomain);
        List<ValuesRangeConflict> conflicts = builder.whenCreating(candidateRange);
        Collections.sort(conflicts, Comparator.comparing(ValuesRangeConflict::getConflictingRange, RangeComparatorFactory.INSTANT_DEFAULT));

        // Asserts
        assertThat(conflicts).isNotNull();
        assertThat(conflicts).isNotEmpty();
        assertThat(conflicts.get(0)).isNotNull();
        assertThat(conflicts.get(0).getConflictingRange()).isEqualTo(Range.closedOpen(Instant.ofEpochSecond(3L), Instant.ofEpochSecond(7L)));
        assertThat(conflicts.get(0).getType()).isEqualTo(ValuesRangeConflictType.RANGE_OVERLAP_DELETE);
        assertThat(Interval.of(conflicts.get(0).getValues().getEffectiveRange())).isEqualTo(extensionSecond.getInterval());
        assertThat(conflicts.get(1)).isNotNull();
        assertThat(conflicts.get(1).getType()).isEqualTo(ValuesRangeConflictType.RANGE_INSERTED);
        assertThat(Interval.of(conflicts.get(1).getValues().getEffectiveRange())).isEqualTo(Interval.of(candidateRange));
        assertThat(conflicts.get(2)).isNotNull();
        assertThat(conflicts.get(2).getConflictingRange()).isEqualTo(Range.closedOpen(Instant.ofEpochSecond(7L), Instant.ofEpochSecond(8L)));
        assertThat(conflicts.get(2).getType()).isEqualTo(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_START);
        assertThat(Interval.of(conflicts.get(2).getValues().getEffectiveRange())).isEqualTo(extensionThird.getInterval());
    }

    @Test
    public void getVersionedCustomPropertiesConflictsGapEnclosing() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("getVersionedCustomProperties");
        service.addCustomPropertySet(this.versionedCustomPropertySet);
        TestDomain testDomain = new TestDomain(1L);
        Interval firstInterval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(1L), Instant.ofEpochSecond(3L)));
        Interval secondInterval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(3L), Instant.ofEpochSecond(7L)));
        Interval thirdInterval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(7L), Instant.ofEpochSecond(9L)));
        VersionedDomainExtensionForTestingPurposes extensionFirst = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, firstInterval);
        VersionedDomainExtensionForTestingPurposes extensionSecond = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, secondInterval);
        VersionedDomainExtensionForTestingPurposes extensionThird = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, thirdInterval);
        DataMapper<VersionedDomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.select(any(Condition.class), any(Order.class))).thenReturn(Arrays.asList(extensionFirst, extensionSecond, extensionThird));
        when(this.versionedCustomPropertySetDataModel.mapper(VersionedDomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);
        Range<Instant> candidateRange = Range.closedOpen(Instant.ofEpochSecond(4L), Instant.ofEpochSecond(5L));

        // Business method
        OverlapCalculatorBuilder builder = service.calculateOverlapsFor(this.versionedCustomPropertySet, testDomain);
        List<ValuesRangeConflict> conflicts = builder.whenCreating(candidateRange);
        Collections.sort(conflicts, Comparator.comparing(ValuesRangeConflict::getConflictingRange, RangeComparatorFactory.INSTANT_DEFAULT));

        // Asserts
        assertThat(conflicts).isNotNull();
        assertThat(conflicts).isNotEmpty();
        assertThat(conflicts.get(0)).isNotNull();
        assertThat(conflicts.get(0).getConflictingRange()).isEqualTo(Range.closedOpen(Instant.ofEpochSecond(4L), Instant.ofEpochSecond(5L)));
        assertThat(conflicts.get(0).getType()).isEqualTo(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END);
        assertThat(Interval.of(conflicts.get(0).getValues().getEffectiveRange())).isEqualTo(extensionSecond.getInterval());
        assertThat(conflicts.get(1)).isNotNull();
        assertThat(conflicts.get(1).getType()).isEqualTo(ValuesRangeConflictType.RANGE_INSERTED);
        assertThat(Interval.of(conflicts.get(1).getValues().getEffectiveRange())).isEqualTo(Interval.of(candidateRange));
        assertThat(conflicts.get(2)).isNotNull();
        assertThat(conflicts.get(2).getConflictingRange()).isEqualTo(Range.closedOpen(Instant.ofEpochSecond(5L), Instant.ofEpochSecond(7L)));
        assertThat(conflicts.get(2).getType()).isEqualTo(ValuesRangeConflictType.RANGE_GAP_BEFORE);
        assertThat(Interval.of(conflicts.get(2).getValues().getEffectiveRange())).isEqualTo(extensionThird.getInterval());
    }

    @Test
    public void getVersionedCustomPropertiesConflictsGapAfter() {
        when(this.serviceDataModel.isInstalled()).thenReturn(true);
        CustomPropertySetServiceImpl service = this.testInstance();
        when(this.serviceDataModel.getInstance(RegisteredCustomPropertySetImpl.class)).thenReturn(new RegisteredCustomPropertySetImpl(this.serviceDataModel, this.threadPrincipalService, service));
        when(this.versionedCustomPropertySet.getId()).thenReturn("getVersionedCustomProperties");
        service.addCustomPropertySet(this.versionedCustomPropertySet);
        TestDomain testDomain = new TestDomain(1L);
        Interval interval = Interval.of(Range.closedOpen(Instant.ofEpochSecond(1L), Instant.ofEpochSecond(3L)));
        VersionedDomainExtensionForTestingPurposes extension = new VersionedDomainExtensionForTestingPurposes(testDomain, this.registeredCustomPropertySet, interval);
        DataMapper<VersionedDomainExtensionForTestingPurposes> dataMapper = mock(DataMapper.class);
        when(dataMapper.select(any(Condition.class), any(Order.class))).thenReturn(Collections.singletonList(extension));
        when(this.versionedCustomPropertySetDataModel.mapper(VersionedDomainExtensionForTestingPurposes.class)).thenReturn(dataMapper);
        Range<Instant> candidateRange = Range.closedOpen(Instant.ofEpochSecond(4L), Instant.ofEpochSecond(5L));

        // Business method
        OverlapCalculatorBuilder builder = service.calculateOverlapsFor(this.versionedCustomPropertySet, testDomain);
        List<ValuesRangeConflict> conflicts = builder.whenCreating(candidateRange);
        Collections.sort(conflicts, Comparator.comparing(ValuesRangeConflict::getConflictingRange, RangeComparatorFactory.INSTANT_DEFAULT));

        // Asserts
        assertThat(conflicts).isNotNull();
        assertThat(conflicts).isNotEmpty();
        assertThat(conflicts.get(0)).isNotNull();
        assertThat(conflicts.get(0).getConflictingRange()).isEqualTo(Range.closedOpen(Instant.ofEpochSecond(3L), Instant.ofEpochSecond(4L)));
        assertThat(conflicts.get(0).getType()).isEqualTo(ValuesRangeConflictType.RANGE_GAP_AFTER);
        assertThat(Interval.of(conflicts.get(0).getValues().getEffectiveRange())).isEqualTo(extension.getInterval());
        assertThat(conflicts.get(1)).isNotNull();
        assertThat(conflicts.get(1).getType()).isEqualTo(ValuesRangeConflictType.RANGE_INSERTED);
        assertThat(Interval.of(conflicts.get(1).getValues().getEffectiveRange())).isEqualTo(Interval.of(candidateRange));
    }

    private CustomPropertySetServiceImpl testInstance() {
        CustomPropertySetServiceImpl testInstance = new CustomPropertySetServiceImpl();
        testInstance.setOrmService(this.ormService, false);
        testInstance.setNlsService(this.nlsService);
        testInstance.setUserService(this.userService);
        testInstance.setTransactionService(this.transactionService);
        testInstance.setThreadPrincipalService(this.threadPrincipalService);
        testInstance.setSearchService(searchService);
        testInstance.setUpgradeService(upgradeService);
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
