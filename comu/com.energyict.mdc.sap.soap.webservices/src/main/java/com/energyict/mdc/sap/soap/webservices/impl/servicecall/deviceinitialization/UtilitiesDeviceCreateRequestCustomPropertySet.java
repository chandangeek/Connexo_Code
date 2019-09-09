/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

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

import com.google.inject.Module;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class UtilitiesDeviceCreateRequestCustomPropertySet implements CustomPropertySet<ServiceCall, UtilitiesDeviceCreateRequestDomainExtension> {

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public UtilitiesDeviceCreateRequestCustomPropertySet() {
    }

    @Inject
    public UtilitiesDeviceCreateRequestCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.UTILITIES_DEVICE_CREATE_REQUEST_CPS).format();
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
    public PersistenceSupport<ServiceCall, UtilitiesDeviceCreateRequestDomainExtension> getPersistenceSupport() {
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
                        .named(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName(), TranslationKeys.DEVICE_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.SERIAL_ID.javaName(), TranslationKeys.SERIAL_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.DEVICE_TYPE.javaName(), TranslationKeys.DEVICE_TYPE)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.SHIPMENT_DATE.javaName(), TranslationKeys.SHIPMENT_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.MANUFACTURER.javaName(), TranslationKeys.MANUFACTURER)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.MODEL_NUMBER.javaName(), TranslationKeys.MODEL_NUMBER)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_CODE.javaName(), TranslationKeys.ERROR_CODE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_MESSAGE.javaName(), TranslationKeys.ERROR_MESSAGE)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, UtilitiesDeviceCreateRequestDomainExtension> {
        private final String TABLE_NAME = "SAP_UD4_CR_SC_CPS";
        private final String FK = "FK_SAP_UD4_CR_SC_CPS";

        @Override
        public String componentName() {
            return "UD4";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return UtilitiesDeviceCreateRequestDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<UtilitiesDeviceCreateRequestDomainExtension> persistenceClass() {
            return UtilitiesDeviceCreateRequestDomainExtension.class;
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
            table.column(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.DEVICE_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName())
                    .notNull()
                    .add();
            table.column(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.SERIAL_ID.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.SERIAL_ID.javaName())
                    .notNull()
                    .add();
            table.column(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.DEVICE_TYPE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.DEVICE_TYPE.javaName())
                    .notNull()
                    .add();
            table.column(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.SHIPMENT_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.SHIPMENT_DATE.javaName())
                    .notNull()
                    .add();
            table.column(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.MANUFACTURER.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.MANUFACTURER.javaName())
                    .add();
            table.column(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.MODEL_NUMBER.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.MODEL_NUMBER.javaName())
                    .add();
            table.column(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_CODE.databaseName())
                    .varChar(NAME_LENGTH)
                    .map(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_CODE.javaName())
                    .add();
            table.column(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_MESSAGE.databaseName())
                    .varChar(DESCRIPTION_LENGTH)
                    .map(UtilitiesDeviceCreateRequestDomainExtension.FieldNames.ERROR_MESSAGE.javaName())
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
