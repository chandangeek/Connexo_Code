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

import com.google.common.collect.Range;
import com.google.inject.Module;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class MeterReadingDocumentCreateResultCustomPropertySet implements CustomPropertySet<ServiceCall, MeterReadingDocumentCreateResultDomainExtension> {
    public static final String MODEL_NAME = "MR4";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public MeterReadingDocumentCreateResultCustomPropertySet() {
    }

    @Inject
    public MeterReadingDocumentCreateResultCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return MeterReadingDocumentCreateResultCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, MeterReadingDocumentCreateResultDomainExtension> getPersistenceSupport() {
        return new CustomPropertyPersistenceSupport();
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
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName(), TranslationKeys.PARENT_SERVICE_CALL)
                        .describedAs(TranslationKeys.PARENT_SERVICE_CALL)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.METER_READING_DOCUMENT_ID.javaName(), TranslationKeys.METER_READING_DOCUMENT_ID)
                        .describedAs(TranslationKeys.METER_READING_DOCUMENT_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.DEVICE_ID.javaName(), TranslationKeys.DEVICE_ID)
                        .describedAs(TranslationKeys.DEVICE_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.DEVICE_NAME.javaName(), TranslationKeys.DEVICE_NAME)
                        .describedAs(TranslationKeys.DEVICE_NAME)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.LRN.javaName(), TranslationKeys.LRN)
                        .describedAs(TranslationKeys.LRN)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.READING_REASON_CODE.javaName(), TranslationKeys.READING_REASON_CODE)
                        .describedAs(TranslationKeys.READING_REASON_CODE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.REQUESTED_SCHEDULED_READING_DATE.javaName(), TranslationKeys.REQUESTED_SCHEDULED_READING_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.SCHEDULED_READING_DATE.javaName(), TranslationKeys.SCHEDULED_READING_DATE)
                        .describedAs(TranslationKeys.SCHEDULED_READING_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.CHANNEL_ID.javaName(), TranslationKeys.CHANNEL_OR_REGISTER_ID)
                        .describedAs(TranslationKeys.CHANNEL_OR_REGISTER_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.DATA_SOURCE.javaName(), TranslationKeys.DATA_SOURCE)
                        .describedAs(TranslationKeys.DATA_SOURCE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.EXTRA_DATA_SOURCE.javaName(), TranslationKeys.EXTRA_DATA_SOURCE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.FUTURE_CASE.javaName(), TranslationKeys.FUTURE_CASE)
                        .describedAs(TranslationKeys.FUTURE_CASE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.PROCESSING_DATE.javaName(), TranslationKeys.PROCESSING_DATE)
                        .describedAs(TranslationKeys.PROCESSING_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.NEXT_READING_ATTEMPT_DATE.javaName(), TranslationKeys.NEXT_READING_ATTEMPT_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .longSpec()
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.READING_ATTEMPT.javaName(), TranslationKeys.READING_ATTEMPT)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.ACTUAL_READING_DATE.javaName(), TranslationKeys.ACTUAL_READING_DATE)
                        .describedAs(TranslationKeys.ACTUAL_READING_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.READING.javaName(), TranslationKeys.READING)
                        .describedAs(TranslationKeys.READING)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.CANCELLED_BY_SAP.javaName(), TranslationKeys.CANCELLED_BY_SAP)
                        .describedAs(TranslationKeys.CANCELLED_BY_SAP_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .longSpec()
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.COM_TASK_EXECUTION_ID.javaName(), TranslationKeys.COM_TASK_EXECUTION_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.REFERENCE_ID.javaName(), TranslationKeys.REFERENCE_ID)
                        .describedAs(TranslationKeys.REFERENCE_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateResultDomainExtension.FieldNames.REFERENCE_UUID.javaName(), TranslationKeys.REFERENCE_UUID)
                        .describedAs(TranslationKeys.REFERENCE_UUID)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MeterReadingDocumentCreateResultDomainExtension> {
        private final String TABLE_NAME = "SAP_CPS_MR4";
        private final String FK = "FK_SAP_CPS_MR4";

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
            return MeterReadingDocumentCreateResultDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MeterReadingDocumentCreateResultDomainExtension> persistenceClass() {
            return MeterReadingDocumentCreateResultDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.empty();
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.PARENT_SERVICE_CALL.databaseName())
                    .number()
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName())
                    .notNull()
                    .add();
            Column meterReadingDocumentId = table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.METER_READING_DOCUMENT_ID.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.METER_READING_DOCUMENT_ID.javaName())
                    .notNull()
                    .add();
            /*table.column("deviceId")
                    .number()
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.DEVICE_ID.javaName())
                    .notNull()
                    .upTo(Version.version(10, 7))
                    .add();*/
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.DEVICE_ID.databaseName())
                    .varChar(80)
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.DEVICE_ID.javaName())
                    .notNull()
                    .installValue("''")
                    .since(Version.version(10, 7))
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.DEVICE_NAME.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.DEVICE_NAME.javaName())
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.SCHEDULED_READING_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.SCHEDULED_READING_DATE.javaName())
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.PROCESSING_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.PROCESSING_DATE.javaName())
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.NEXT_READING_ATTEMPT_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.NEXT_READING_ATTEMPT_DATE.javaName())
                    .since(Version.version(10, 7, 1))
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.READING_ATTEMPT.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.READING_ATTEMPT.javaName())
                    .since(Version.version(10, 7, 1))
                    .add();
            /*table.column("lrn")
                    .number()
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.LRN.javaName())
                    .notNull()
                    .upTo(Version.version(10, 7))
                    .add();*/
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.LRN.databaseName())
                    .varChar(80)
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.LRN.javaName())
                    .notNull()
                    .installValue("''")
                    .since(Version.version(10, 7))
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.READING_REASON_CODE.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.READING_REASON_CODE.javaName())
                    .notNull()
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.CHANNEL_ID.databaseName())
                    .number()
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.CHANNEL_ID.javaName())
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.DATA_SOURCE.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.DATA_SOURCE.javaName())
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.EXTRA_DATA_SOURCE.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.EXTRA_DATA_SOURCE.javaName())
                    .since(Version.version(10, 7, 2))
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.FUTURE_CASE.databaseName())
                    .bool()
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.FUTURE_CASE.javaName())
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.ACTUAL_READING_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.ACTUAL_READING_DATE.javaName())
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.READING.databaseName())
                    .number()
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.READING.javaName())
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.CANCELLED_BY_SAP.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.CANCELLED_BY_SAP.javaName())
                    .since(Version.version(10, 7, 1))
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.COM_TASK_EXECUTION_ID.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONGWRAPPER)
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.COM_TASK_EXECUTION_ID.javaName())
                    .since(Version.version(10, 7, 1))
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.REFERENCE_ID.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.REFERENCE_ID.javaName())
                    .since(Version.version(10, 7, 2))
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.REFERENCE_UUID.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.REFERENCE_UUID.javaName())
                    .since(Version.version(10, 7, 2))
                    .add();
            table.column(MeterReadingDocumentCreateResultDomainExtension.FieldNames.REQUESTED_SCHEDULED_READING_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(MeterReadingDocumentCreateResultDomainExtension.FieldNames.REQUESTED_SCHEDULED_READING_DATE.javaName())
                    .since(Version.version(10, 7, 2))
                    .add();
            table.index("IX_" + TABLE_NAME + "_MRDID")
                    .on(meterReadingDocumentId)
                    .during(Range.closedOpen(Version.version(10, 7, 5), Version.version(10, 8)),
                            Range.atLeast(Version.version(10, 9)))
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
