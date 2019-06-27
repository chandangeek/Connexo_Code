/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

@Component(name = "MeterReadingDocumentCreateRequestCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + MeterReadingDocumentCreateRequestCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class MeterReadingDocumentCreateRequestCustomPropertySet implements CustomPropertySet<ServiceCall, MeterReadingDocumentCreateRequestDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "MeterReadingDocumentCreateRequestCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public MeterReadingDocumentCreateRequestCustomPropertySet() {
    }

    @Inject
    public MeterReadingDocumentCreateRequestCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setServiceCallService(ServiceCallService serviceCallService) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        customPropertySetService.addCustomPropertySet(this);
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(WebServiceActivator.COMPONENT_NAME, Layer.SOAP);
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
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.SCHEDULED_READING_DATE.javaName(), TranslationKeys.SCHEDULED_READING_DATE)
                        .describedAs(TranslationKeys.SCHEDULED_READING_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.CHANNEL_ID.javaName(), TranslationKeys.CHANNEL_ID)
                        .describedAs(TranslationKeys.CHANNEL_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DATA_SOURCE.javaName(), TranslationKeys.DATA_SOURCE)
                        .describedAs(TranslationKeys.DATA_SOURCE)
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
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MeterReadingDocumentCreateRequestDomainExtension> {
        private final String TABLE_NAME = "SAP_CPS_MR1";
        private final String FK = "FK_SAP_CPS_MR1";

        @Override
        public String componentName() {
            return "MR1";
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
            return Optional.empty();
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
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DEVICE_ID.databaseName())
                    .number()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName())
                    .notNull()
                    .upTo(Version.version(10, 7))
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DEVICE_ID.databaseName())
                    .varChar(80)
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName())
                    .notNull()
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
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.LRN.databaseName())
                    .number()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.LRN.javaName())
                    .notNull()
                    .upTo(Version.version(10, 7))
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.LRN.databaseName())
                    .varChar(80)
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.LRN.javaName())
                    .notNull()
                    .since(Version.version(10, 7))
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.READING_REASON_CODE.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.READING_REASON_CODE.javaName())
                    .notNull()
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.CHANNEL_ID.databaseName())
                    .number()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.CHANNEL_ID.javaName())
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DATA_SOURCE.databaseName())
                    .varChar()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DATA_SOURCE.javaName())
                    .add();
            table.column(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.FUTURE_CASE.databaseName())
                    .bool()
                    .map(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.FUTURE_CASE.javaName())
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
