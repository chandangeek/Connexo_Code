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
import com.elster.jupiter.orm.Table;
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

import static com.elster.jupiter.orm.Version.version;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

@Component(name = "MasterMeterReadingDocumentCreateRequestCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + MasterMeterReadingDocumentCreateRequestCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class MasterMeterReadingDocumentCreateRequestCustomPropertySet implements CustomPropertySet<ServiceCall, MasterMeterReadingDocumentCreateRequestDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "MasterMeterReadingDocumentCreateRequestCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public MasterMeterReadingDocumentCreateRequestCustomPropertySet() {
    }

    @Inject
    public MasterMeterReadingDocumentCreateRequestCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
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
                        .stringSpec()
                        .named(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.REQUEST_ID.javaName(), TranslationKeys.REQUEST_UUID)
                        .describedAs(TranslationKeys.REQUEST_UUID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.ATTEMPT_NUMBER.javaName(), TranslationKeys.ATTEMPT_NUMBER)
                        .describedAs(TranslationKeys.ATTEMPT_NUMBER)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.CONFIRMATION_URL.javaName(), TranslationKeys.CONFIRMATION_URL)
                        .describedAs(TranslationKeys.CONFIRMATION_URL)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.RESULT_URL.javaName(), TranslationKeys.RESULT_URL)
                        .describedAs(TranslationKeys.RESULT_URL)
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
            return Optional.empty();
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table.column(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.REQUEST_ID.databaseName())
                    .varChar()
                    .map(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.REQUEST_ID.javaName())
                    .notNull()
                    .add();
            table.column(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.ATTEMPT_NUMBER.databaseName())
                    .number()
                    .map(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.ATTEMPT_NUMBER.javaName())
                    .notNull()
                    .add();
            table.column(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.CONFIRMATION_URL.databaseName())
                    .varChar()
                    .map(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.CONFIRMATION_URL.javaName())
                    .notNull()
                    .upTo(version(10,7))
                    .add();
            table.column(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.RESULT_URL.databaseName())
                    .varChar()
                    .map(MasterMeterReadingDocumentCreateRequestDomainExtension.FieldNames.RESULT_URL.javaName())
                    .notNull()
                    .upTo(version(10,7))
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