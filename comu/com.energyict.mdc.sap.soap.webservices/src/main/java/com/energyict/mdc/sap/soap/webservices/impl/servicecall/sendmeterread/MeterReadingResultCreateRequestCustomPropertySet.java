/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.sendmeterread;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
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

import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class MeterReadingResultCreateRequestCustomPropertySet implements CustomPropertySet<ServiceCall, MeterReadingResultCreateRequestDomainExtension> {
    public static final String MODEL_NAME = "LR5";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public MeterReadingResultCreateRequestCustomPropertySet() {
    }

    @Inject
    public MeterReadingResultCreateRequestCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return MeterReadingResultCreateRequestCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, MeterReadingResultCreateRequestDomainExtension> getPersistenceSupport() {
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
                        .named(MeterReadingResultCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName(), TranslationKeys.DEVICE_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingResultCreateRequestDomainExtension.FieldNames.METER_READING_DOCUMENT_ID.javaName(), TranslationKeys.METER_READING_DOCUMENT_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingResultCreateRequestDomainExtension.FieldNames.LRN.javaName(), TranslationKeys.LRN)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingResultCreateRequestDomainExtension.FieldNames.READING_REASON_CODE.javaName(), TranslationKeys.READING_REASON_CODE)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(MeterReadingResultCreateRequestDomainExtension.FieldNames.METER_READING_DATE_TIME.javaName(), TranslationKeys.METER_READING_DATE_TIME)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingResultCreateRequestDomainExtension.FieldNames.METER_READING_VALUE.javaName(), TranslationKeys.METER_READING_VALUE)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingResultCreateRequestDomainExtension.FieldNames.ERROR_CODE.javaName(), TranslationKeys.ERROR_CODE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingResultCreateRequestDomainExtension.FieldNames.ERROR_MESSAGE.javaName(), TranslationKeys.ERROR_MESSAGE)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MeterReadingResultCreateRequestDomainExtension> {
        private final String TABLE_NAME = "SAP_LR5_CR_SC_CPS";
        private final String FK = "FK_SAP_LR5_CR_SC_CPS";

        private Thesaurus thesaurus;

        private CustomPropertyPersistenceSupport(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public String componentName() {
            return MODEL_NAME;
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return MeterReadingResultCreateRequestDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MeterReadingResultCreateRequestDomainExtension> persistenceClass() {
            return MeterReadingResultCreateRequestDomainExtension.class;
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
            table.column(MeterReadingResultCreateRequestDomainExtension.FieldNames.DEVICE_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MeterReadingResultCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName())
                    .notNull()
                    .add();
            table.column(MeterReadingResultCreateRequestDomainExtension.FieldNames.METER_READING_DOCUMENT_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MeterReadingResultCreateRequestDomainExtension.FieldNames.METER_READING_DOCUMENT_ID.javaName())
                    .notNull()
                    .add();
            table.column(MeterReadingResultCreateRequestDomainExtension.FieldNames.LRN.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MeterReadingResultCreateRequestDomainExtension.FieldNames.LRN.javaName())
                    .notNull()
                    .add();
            table.column(MeterReadingResultCreateRequestDomainExtension.FieldNames.READING_REASON_CODE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MeterReadingResultCreateRequestDomainExtension.FieldNames.READING_REASON_CODE.javaName())
                    .notNull()
                    .add();
            table.column(MeterReadingResultCreateRequestDomainExtension.FieldNames.METER_READING_DATE_TIME.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(MeterReadingResultCreateRequestDomainExtension.FieldNames.METER_READING_DATE_TIME.javaName())
                    .notNull()
                    .add();
            table.column(MeterReadingResultCreateRequestDomainExtension.FieldNames.METER_READING_TYPE_CODE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MeterReadingResultCreateRequestDomainExtension.FieldNames.METER_READING_TYPE_CODE.javaName())
                    .notNull()
                    .add();
            table.column(MeterReadingResultCreateRequestDomainExtension.FieldNames.METER_READING_VALUE.databaseName())
                    .number()
                    .map(MeterReadingResultCreateRequestDomainExtension.FieldNames.METER_READING_VALUE.javaName())
                    .notNull()
                    .add();
            table.column(MeterReadingResultCreateRequestDomainExtension.FieldNames.ERROR_CODE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(MeterReadingResultCreateRequestDomainExtension.FieldNames.ERROR_CODE.javaName())
                    .add();
            table.column(MeterReadingResultCreateRequestDomainExtension.FieldNames.ERROR_MESSAGE.databaseName())
                    .varChar(DESCRIPTION_LENGTH)
                    .map(MeterReadingResultCreateRequestDomainExtension.FieldNames.ERROR_MESSAGE.javaName())
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
