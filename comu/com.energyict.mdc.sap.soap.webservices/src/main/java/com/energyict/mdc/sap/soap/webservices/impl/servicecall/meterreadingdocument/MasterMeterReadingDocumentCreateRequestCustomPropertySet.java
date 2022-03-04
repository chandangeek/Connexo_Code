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
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
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

public class MasterMeterReadingDocumentCreateRequestCustomPropertySet implements CustomPropertySet<ServiceCall, MasterMeterReadingDocumentCreateRequestDomainExtension> {
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    MasterMeterReadingDocumentCreateRequestCustomPropertySet() {
    }

    @Inject
    public MasterMeterReadingDocumentCreateRequestCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return MasterMeterReadingDocumentCreateRequestCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, MasterMeterReadingDocumentCreateRequestDomainExtension> getPersistenceSupport() {
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
                        .named(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.REQUEST_ID.javaName(), TranslationKeys.REQUEST_ID)
                        .describedAs(TranslationKeys.REQUEST_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.UUID.javaName(), TranslationKeys.UUID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.ATTEMPT_NUMBER.javaName(), TranslationKeys.ATTEMPT_NUMBER)
                        .describedAs(TranslationKeys.ATTEMPT_NUMBER)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.BULK.javaName(), TranslationKeys.BULK)
                        .describedAs(TranslationKeys.BULK)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MasterMeterReadingDocumentCreateRequestDomainExtension> {
        private final String TABLE_NAME = "SAP_CPS_MR2";
        private final String FK = "FK_SAP_CPS_MR2";

        private Thesaurus thesaurus;

        private CustomPropertyPersistenceSupport(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public String componentName() {
            return "MR2";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MasterMeterReadingDocumentCreateRequestDomainExtension> persistenceClass() {
            return MasterMeterReadingDocumentCreateRequestDomainExtension.class;
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
            Column oldRequestIdColumn = table.column(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.REQUEST_ID.databaseName())
                    .varChar()
                    .map(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.REQUEST_ID.javaName())
                    .notNull()
                    .upTo(Version.version(10, 7, 1))
                    .add();
            table.column(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.REQUEST_ID.databaseName())
                    .varChar()
                    .map(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.REQUEST_ID.javaName())
                    .since(Version.version(10, 7, 1))
                    .previously(oldRequestIdColumn)
                    .add();
            table.column(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.UUID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.UUID.javaName())
                    .since(Version.version(10, 7, 1))
                    .add();
            table.column(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.ATTEMPT_NUMBER.databaseName())
                    .number()
                    .map(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.ATTEMPT_NUMBER.javaName())
                    .notNull()
                    .add();
            table.column(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.BULK.databaseName())
                    .bool()
                    .map(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.BULK.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
