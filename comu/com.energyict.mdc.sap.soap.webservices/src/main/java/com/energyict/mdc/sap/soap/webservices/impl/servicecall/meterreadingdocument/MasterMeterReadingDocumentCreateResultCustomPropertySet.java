/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class MasterMeterReadingDocumentCreateResultCustomPropertySet implements CustomPropertySet<ServiceCall, MasterMeterReadingDocumentCreateResultDomainExtension> {
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public MasterMeterReadingDocumentCreateResultCustomPropertySet() {
    }

    @Inject
    public MasterMeterReadingDocumentCreateResultCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return MasterMeterReadingDocumentCreateResultCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<ServiceCall, MasterMeterReadingDocumentCreateResultDomainExtension> getPersistenceSupport() {
        return new CustomPropertyPersistenceSupport(thesaurus);
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public boolean isVersioned() {
        return false;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.propertySpecService
                        .stringSpec()
                        .named(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.REQUEST_UUID.javaName(), TranslationKeys.UUID)
                        .describedAs(TranslationKeys.UUID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.REFERENCE_ID.javaName(), TranslationKeys.REFERENCE_ID)
                        .describedAs(TranslationKeys.REFERENCE_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.REFERENCE_UUID.javaName(), TranslationKeys.REFERENCE_UUID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.CONFIRMATION_TIME.javaName(), TranslationKeys.CONFIRMATION_TIME)
                        .describedAs(TranslationKeys.CONFIRMATION_TIME)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.BULK.javaName(), TranslationKeys.BULK)
                        .describedAs(TranslationKeys.BULK)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MasterMeterReadingDocumentCreateResultDomainExtension> {
        private final String TABLE_NAME = "SAP_CPS_MR3";
        private final String FK = "FK_SAP_CPS_MR3";

        private Thesaurus thesaurus;

        private CustomPropertyPersistenceSupport(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public String componentName() {
            return "MR3";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MasterMeterReadingDocumentCreateResultDomainExtension> persistenceClass() {
            return MasterMeterReadingDocumentCreateResultDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.of(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Thesaurus.class).toInstance(thesaurus);
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                }
            });
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table.column(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.REQUEST_UUID.databaseName())
                    .varChar()
                    .map(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.REQUEST_UUID.javaName())
                    .notNull()
                    .add();
            Column oldReferenceId = table.column(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.REFERENCE_ID.databaseName())
                    .varChar()
                    .map(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.REFERENCE_ID.javaName())
                    .notNull()
                    .upTo(Version.version(10, 7, 1))
                    .add();
            table.column(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.REFERENCE_ID.databaseName())
                    .varChar()
                    .map(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.REFERENCE_ID.javaName())
                    .since(Version.version(10, 7, 1))
                    .previously(oldReferenceId)
                    .add();
            table.column(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.REFERENCE_UUID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.REFERENCE_UUID.javaName())
                    .since(Version.version(10, 7, 1))
                    .add();
            table.column(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.CONFIRMATION_TIME.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.CONFIRMATION_TIME.javaName())
                    .add();
            table.column(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.BULK.databaseName())
                    .bool()
                    .map(MasterMeterReadingDocumentCreateResultDomainExtension.FieldNames.BULK.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
