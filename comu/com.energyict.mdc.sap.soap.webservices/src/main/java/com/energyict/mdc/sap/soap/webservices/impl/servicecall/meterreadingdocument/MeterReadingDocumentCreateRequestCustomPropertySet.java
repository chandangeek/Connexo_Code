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

import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class MeterReadingDocumentCreateRequestCustomPropertySet implements CustomPropertySet<ServiceCall, MeterReadingDocumentCreateRequestDomainExtension> {
    public static final String MODEL_NAME = "MR1";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public MeterReadingDocumentCreateRequestCustomPropertySet() {
    }

    @Inject
    public MeterReadingDocumentCreateRequestCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return MeterReadingDocumentCreateRequestCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, MeterReadingDocumentCreateRequestDomainExtension> getPersistenceSupport() {
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
                        .bigDecimalSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName(), TranslationKeys.PARENT_SERVICE_CALL)
                        .describedAs(TranslationKeys.PARENT_SERVICE_CALL)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.METER_READING_DOCUMENT_ID.javaName(), TranslationKeys.METER_READING_DOCUMENT_ID)
                        .describedAs(TranslationKeys.METER_READING_DOCUMENT_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName(), TranslationKeys.DEVICE_ID)
                        .describedAs(TranslationKeys.DEVICE_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DEVICE_NAME.javaName(), TranslationKeys.DEVICE_NAME)
                        .describedAs(TranslationKeys.DEVICE_NAME)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.LRN.javaName(), TranslationKeys.LRN)
                        .describedAs(TranslationKeys.LRN)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.READING_REASON_CODE.javaName(), TranslationKeys.READING_REASON_CODE)
                        .describedAs(TranslationKeys.READING_REASON_CODE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.REQUESTED_SCHEDULED_READING_DATE.javaName(), TranslationKeys.REQUESTED_SCHEDULED_READING_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.SCHEDULED_READING_DATE.javaName(), TranslationKeys.SCHEDULED_READING_DATE)
                        .describedAs(TranslationKeys.SCHEDULED_READING_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.CHANNEL_ID.javaName(), TranslationKeys.CHANNEL_OR_REGISTER_ID)
                        .describedAs(TranslationKeys.CHANNEL_OR_REGISTER_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DATA_SOURCE.javaName(), TranslationKeys.DATA_SOURCE)
                        .describedAs(TranslationKeys.DATA_SOURCE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.EXTRA_DATA_SOURCE.javaName(), TranslationKeys.EXTRA_DATA_SOURCE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.FUTURE_CASE.javaName(), TranslationKeys.FUTURE_CASE)
                        .describedAs(TranslationKeys.FUTURE_CASE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.PROCESSING_DATE.javaName(), TranslationKeys.PROCESSING_DATE)
                        .describedAs(TranslationKeys.PROCESSING_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.CANCELLED_BY_SAP.javaName(), TranslationKeys.CANCELLED_BY_SAP)
                        .describedAs(TranslationKeys.CANCELLED_BY_SAP_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.REFERENCE_ID.javaName(), TranslationKeys.REFERENCE_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.REFERENCE_UUID.javaName(), TranslationKeys.REFERENCE_UUID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.ERROR_MESSAGE.javaName(), TranslationKeys.ERROR_MESSAGE)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MeterReadingDocumentCreateRequestDomainExtension> {
        private final String TABLE_NAME = "SAP_CPS_MR1";
        private final String FK = "FK_SAP_CPS_MR1";

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
            return MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MeterReadingDocumentCreateRequestDomainExtension> persistenceClass() {
            return MeterReadingDocumentCreateRequestDomainExtension.class;
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
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.PARENT_SERVICE_CALL.databaseName())
                    .number()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName())
                    .notNull()
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.METER_READING_DOCUMENT_ID.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.METER_READING_DOCUMENT_ID.javaName())
                    .notNull()
                    .add();
            /*table.column("deviceId")
                    .number()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName())
                    .notNull()
                    .upTo(Version.version(10, 7))
                    .add();*/
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DEVICE_ID.databaseName())
                    .varChar(80)
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName())
                    .notNull()
                    .installValue("''")
                    .since(Version.version(10, 7))
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DEVICE_NAME.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DEVICE_NAME.javaName())
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.SCHEDULED_READING_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.SCHEDULED_READING_DATE.javaName())
                    .notNull()
                    .add();

            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.PROCESSING_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.PROCESSING_DATE.javaName())
                    .add();
            /*table.column("lrn")
                    .number()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.LRN.javaName())
                    .notNull()
                    .upTo(Version.version(10, 7))
                    .add();*/
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.LRN.databaseName())
                    .varChar(80)
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.LRN.javaName())
                    .notNull()
                    .installValue("''")
                    .since(Version.version(10, 7))
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.READING_REASON_CODE.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.READING_REASON_CODE.javaName())
                    .notNull()
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DATA_SOURCE_TYPE_CODE.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DATA_SOURCE_TYPE_CODE.javaName())
                    .since(Version.version(10, 9, 15))
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.CHANNEL_ID.databaseName())
                    .number()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.CHANNEL_ID.javaName())
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DATA_SOURCE.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DATA_SOURCE.javaName())
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.EXTRA_DATA_SOURCE.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.EXTRA_DATA_SOURCE.javaName())
                    .since(Version.version(10, 7, 2))
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.FUTURE_CASE.databaseName())
                    .bool()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.FUTURE_CASE.javaName())
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.CANCELLED_BY_SAP.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.CANCELLED_BY_SAP.javaName())
                    .since(Version.version(10, 7, 1))
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.REFERENCE_ID.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.REFERENCE_ID.javaName())
                    .since(Version.version(10, 7, 2))
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.REFERENCE_UUID.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.REFERENCE_UUID.javaName())
                    .since(Version.version(10, 7, 2))
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.REQUESTED_SCHEDULED_READING_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.REQUESTED_SCHEDULED_READING_DATE.javaName())
                    .since(Version.version(10, 7, 2))
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.ERROR_MESSAGE.databaseName())
                    .varChar(DESCRIPTION_LENGTH)
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.ERROR_MESSAGE.javaName())
                    .since(Version.version(10, 7, 2))
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
